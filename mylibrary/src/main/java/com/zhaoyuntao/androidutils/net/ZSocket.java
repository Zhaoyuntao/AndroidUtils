package com.zhaoyuntao.androidutils.net;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.F;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZThread;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ZSocket {

    private static final String RESPONSE_OUT = "TAKE_ME_OUT";
    public static String socketId = "DEFAULT";
    private String DETECTOR = "HEART_BYTES_";
    private String DOWNLOADFILE = "DOWNLOADFILE_";
    //心跳包频率
    private static final float frame_heart = 2;
    private static ZSocket zSocket;
    private ZThread zThread_send;
    private ZThread zThread_recv;
    private ZThread zThread_heart;
    private int port = 16880;
    private int maxSize = 200;
    private BlockingDeque<Msg> queue;
    private Map<String, Client> clients;
    private Context context;

    private final int timeout = 3;//超时时长:秒
    //当关闭线程时
    private boolean isClose;

    public static final int maxPackagSize = 63 * 1024;//UDP每个包最大不能超过64kb

    private String fileCacheDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/zsocketcache/";

    private Map<String, File> fileCache = new HashMap<>();

    private Map<String, AskResult> map_AskResult = new HashMap<>();

    private Map<String, FileDownloadResult> map_FileDownloadResult = new HashMap<>();
    private FileServer fileServer;
    private Receiver receiver;

    private ZSocket() {
        queue = new LinkedBlockingDeque<>();
        clients = new HashMap<>();
        initCache();
        recv();
        initSender();
        initHeart();
    }

    public void asServer() {
        socketId = "SERVER";
    }

    public void asClient() {
        socketId = "CLIENT";
    }

    public void initCache() {
        File file = new File(fileCacheDir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


    public void setDetector(String detector) {
        if (S.isEmpty(detector)) {
            S.e("连接密码出错:detector is null!");
            return;
        }
        this.DETECTOR = detector;
    }

    public static ZSocket getInstance() {
        return getInstance(null);
    }

    public static ZSocket getInstance(Context context) {
        if (zSocket == null) {
            synchronized (ZSocket.class) {
                if (zSocket == null) {
                    zSocket = new ZSocket();
                    zSocket.context = context;
                }
            }
        }
        return zSocket;
    }

    public void send(String msg) {
        send(msg, "");
    }

    public void send(String msg, Client client) {
        send(msg, client.ip);
    }

    public void send(String msg, String ip) {
        send(msg.getBytes(), Msg.STRING, ip);
    }

    public void send(byte[] msgData, byte type, String ip) {
        if (msgData == null) {
            return;
        }
        String id = Msg.getRandomId();
        if (msgData.length > maxPackagSize) {
            byte[][] data = F.slipByteArrays(msgData, maxPackagSize);
            for (int i = 0; i < data.length; i++) {
                byte[] tmp = data[i];
                Msg msg = new Msg(id);
                msg.ip = ip;
                msg.port = port;
                msg.type = type;
                msg.msg = tmp;
                msg.index = i;
                msg.count = data.length;
                send(msg);
            }
        } else {
            Msg msg = new Msg(id);
            msg.ip = ip;
            msg.port = port;
            msg.type = type;
            msg.msg = msgData;
            msg.count = 1;
            msg.index = 0;
            send(msg);
        }
    }

    public synchronized boolean send(Msg msg) {
        queue.add(msg);
        return queue.size() < maxSize;
    }

    public void ask(String request, AskResult askResult) {
        String id = Msg.getRandomId();
        Msg msg = new Msg(id);
        msg.port = port;
        msg.type = Msg.STRING;
        msg.msg = request.getBytes();
        send(msg);
        map_AskResult.put(id, askResult);
    }

    public void answer(String id, String response) {
        Msg msg = new Msg(id);
        msg.port = port;
        msg.type = Msg.STRING;
        msg.msg = response.getBytes();
        send(msg);
    }

    public void downloadFile(String filename, FileDownloadResult result) {
        String request = DOWNLOADFILE + filename;
        String id = Msg.getRandomId();
        Msg msg = new Msg(id);
        msg.port = port;
        msg.type = Msg.STRING;
        msg.msg = request.getBytes();
        send(msg);
        map_FileDownloadResult.put(id, result);
    }

    public void sendFile(final String id, final String ip, final File file) {
        if (file == null) {
            return;
        }
        F.slipFile(file, maxPackagSize, new F.CallBack() {

            @Override
            public void whenGotPiece(byte[] data, int index, int count, int position, long filesize, ZThread zThread) {
                if (isClose) {
                    zThread.close();
                }
                Msg msg = new Msg(id);
                msg.ip = ip;
                msg.port = port;
                msg.filename = file.getName();
                msg.type = Msg.FILE;
                msg.msg = data;
                msg.index = index;
                msg.count = count;
                msg.position = position;
                msg.zThread = zThread;
                msg.filesize = filesize;
                boolean ok = send(msg);
                if (!ok) {
                    zThread.pauseThread();
                }
            }

        });
    }

    public void sendCompressBitmap(Bitmap bitmap) {
        bitmap = B.compress(bitmap, 63);
        send(B.bitmapToBytes(bitmap), Msg.BITMAP, null);
    }

    private void initHeart() {
        stopHeart();
        zThread_heart = new ZThread(frame_heart) {
            DatagramSocket datagramSocket;

            @Override
            protected void init() {
                try {
                    datagramSocket = new DatagramSocket();
                } catch (SocketException e2) {
                    e2.printStackTrace();
                    S.e(e2);
                }
            }

            @Override
            protected void todo() {
//                S.s("正在发送心跳:" + DETECTOR);
                long time_now = S.currentTimeMillis();
                try {
                    String content = DETECTOR + socketId;
                    Msg msg = new Msg();
                    msg.type = Msg.STRING;
                    msg.msg = content.getBytes();
                    byte[] data = Msg.getPackage(msg);
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName("255.255.255.255"), port);
                    datagramSocket.send(datagramPacket);

                    for (Client client : clients.values()) {
                        long time = client.time_lastheart;
                        long during = time_now - time;
                        //大于超时时长未做出响应的client将被移除
                        if (during > timeout * 1000) {
                            removeClient(client.ip);
                        }
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                    S.e(e1);
                } catch (IOException e) {
                    e.printStackTrace();
                    S.e(e);
                }
            }

            @Override
            public void close() {
                super.close();
                try {
                    if (datagramSocket != null) {
                        datagramSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        zThread_heart.start();
    }


    private void initSender() {
        zThread_send = new ZThread() {
            DatagramSocket datagramSocket;

            @Override
            protected void init() {
                try {
                    datagramSocket = new DatagramSocket();
                } catch (SocketException e2) {
                    e2.printStackTrace();
                    S.e(e2);
                }
            }

            @Override
            protected void todo() {
                List<Client> list = new ArrayList<>();
                synchronized (ZSocket.class) {
                    if (clients.size() > 0) {
                        for (Client client : clients.values()) {
                            list.add(client);
                        }
                    } else {
                        //如果已经关闭,则等待发送完最后一个关闭指令后再关闭线程
                        if (!isClose) {
                            this.pauseThread();
                            return;
                        }
                    }
                }

                Msg msg = null;
                try {
                    msg = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //组包
                byte[] data = Msg.getPackage(msg);
                if (data.length > maxPackagSize + 1024) {
                    S.e("send err:message is too long(max:" + (maxPackagSize + 1024) + "):" + data.length + " type:" + Msg.getType(msg.type));
                    return;
                }
                try {
                    if (S.isIp(msg.ip)) {
//                        S.s("正在向[" + msg.ip + "]发送-------------------------->");
                        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(msg.ip), msg.port);
                        datagramSocket.send(datagramPacket);
//                        S.s("发送成功");
                    } else {
                        for (Client client : list) {
//                            S.s("正在向[" + client.ip + "]发送:" + data.length + "============================>");
                            DatagramPacket datagramPacket = new DatagramPacket(data, data.length, InetAddress.getByName(client.ip), msg.port);
                            datagramSocket.send(datagramPacket);
                        }
//                        S.s("发送成功");
                    }
                    S.s("正在发送文件:"+msg.index);
                    if (msg.zThread != null) {
                        msg.zThread.resumeThread();
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                    S.e(e1);
                } catch (IOException e) {
                    e.printStackTrace();
                    S.e(e);
                } finally {
                    if (RESPONSE_OUT.equals(new String(msg.msg))) {
                        S.s("关闭Socket...");
                        clearMsg();
                        clearClient();
                        stopHeart();
                        stopSend();
                        stopRecv();
                        zSocket = null;
                    }
                }
            }

            @Override
            public void close() {
                super.close();
                try {
                    if (datagramSocket != null) {
                        datagramSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        zThread_send.start();
    }


    private void recv() {
        clearMsg();
        stopRecv();
        zThread_recv = new ZThread() {

            DatagramSocket datagramSocket;

            @Override
            protected void init() {
                try {
                    datagramSocket = new DatagramSocket(port);
                } catch (SocketException e) {
                    e.printStackTrace();
                    S.e(e);
                }
            }

            @Override
            protected void todo() {
                byte[] buffer = new byte[65507];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    datagramSocket.receive(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (packet == null) {
                    return;
                }
                if (packet.getLength() < 1) {
                    return;
                }
                byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                InetAddress inetAddress = packet.getAddress();
                if (inetAddress == null) {
                    return;
                }
                final String ip = inetAddress.getHostAddress();
                String ip_self = NetworkUtil.int2ip(NetworkUtil.getIP(context));
                if (ip.equals(ip_self)) {
//                    S.e("自身的消息,略过");
                    return;
                }
                //解包
                final Msg msg = Msg.releasePackage(data);
                msg.ip = ip;
                final int port = packet.getPort();
                switch (msg.type) {

                    case Msg.STRING:
                        String content = new String(msg.msg);
                        Client client = getClient(ip);
                        if (content.startsWith(DETECTOR)) {
                            String socketIdTmp = content.substring(DETECTOR.length());
                            //如果是不同类型的socket,则加入连接
//                            if (!socketIdTmp.equals(socketId)) {
                            if (client == null) {
                                client = new Client();
                                client.ip = ip;
                                client.port = port;
                                //保存远程ip
                                addClient(client);
                            }
                            //刷新最新一次通信的时间
                            client.time_lastheart = S.currentTimeMillis();
//                            }
                        } else if (content.equals(RESPONSE_OUT)) {
                            removeClient(ip);
                        } else if (content.startsWith(DOWNLOADFILE)) {
                            String filename = content.substring(DOWNLOADFILE.length());
                            if (fileServer != null) {
                                fileServer.whenSomeOneAskFile(msg.id, msg.ip, filename);
                            }
                        } else {
                            S.s("接到来自[" + ip + ":" + port + "]" + "(type:" + Msg.getType(msg.type) + ")的消息 : " + content);
                            AskResult askResult = map_AskResult.get(msg.id);
                            map_AskResult.remove(msg.id);
                            if (askResult != null) {
                                askResult.whenGotResult(msg);
                            } else {
                                if (receiver != null) {
                                    receiver.whenGotResult(msg);
                                }
                            }
                        }
                        break;
                    case Msg.BITMAP:
                        S.s("接到来自[" + ip + ":" + port + "]" + "(type:" + Msg.getType(msg.type) + ")的图片");
                        AskResult askResult = map_AskResult.get(msg.id);
                        map_AskResult.remove(msg.id);
                        if (askResult != null) {
                            askResult.whenGotResult(msg);
                        }
                        break;
                    case Msg.FILE:
                        final FileDownloadResult fileDownloadResult = map_FileDownloadResult.get(msg.id);
                        map_FileDownloadResult.remove(msg.id);
                        //如果包含多个分包
                        File file = fileCache.get(msg.id);
                        if (file == null) {
                            file = new File(fileCacheDir, msg.filename);
                            fileCache.put(msg.id, file);
                        }
                        F.assembleFile(msg.index,file, msg.msg, msg.position, new F.Task.CallBack() {
                            @Override
                            public void whenWriteOk(String filename) {
                                fileCache.remove(msg.id);
//                                S.s("文件下载完毕:" + filename);
                                if (fileDownloadResult != null) {
                                    fileDownloadResult.whenDownloadFinished(fileCacheDir + msg.filename);
                                }
                            }

                            @Override
                            public void whenDownloading(String filename, float percent) {
//                                S.s("正在下载文件[" + filename + "]:" + percent);
                                if (fileDownloadResult != null) {
                                    fileDownloadResult.whenDownloadingFile(fileCacheDir + msg.filename, percent);
                                }
                            }

                            @Override
                            public int getCount() {
                                return msg.count;
                            }

                            @Override
                            public void whenStartDownloading(String filename) {
//                                S.s("开始下载来自[" + ip + ":" + port + "]的文件:" + filename);
                                if (fileDownloadResult != null) {
                                    fileDownloadResult.whenStartDownload(fileCacheDir + msg.filename, msg.filesize);
                                }
                            }
                        });

                        break;
                    default:
                        S.s("接到来自[" + ip + ":" + port + "]" + "(type:" + Msg.getType(msg.type) + ")的未知类型消息,长度:" + data.length);
                        break;
                }
            }

            @Override
            public void close() {
                super.close();
                try {
                    if (datagramSocket != null) {
                        datagramSocket.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        zThread_recv.start();
    }

    public ZSocket setFileServer(FileServer fileServer) {
        this.fileServer = fileServer;
        return this;
    }

    public ZSocket setReceiver(Receiver receiver) {
        this.receiver = receiver;
        return this;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public void clearMsg() {
        queue.clear();
    }

    public void clearClient() {
        synchronized (ZSocket.class) {
            clients.clear();
        }
    }

    public void addClient(Client client) {
        synchronized (ZSocket.class) {
            if (S.isIp(client.ip)) {
                S.s("[" + client.ip + "]已连接");
                clients.put(client.ip, client);
            }
        }
        resumeSender();
    }

    private void resumeSender() {
        if (zThread_send != null) {
            zThread_send.resumeThread();
        }
    }

    public Client getClient(String ip) {
        synchronized (ZSocket.class) {
            return clients.get(ip);
        }
    }

    public void removeClient(String ip) {
        synchronized (ZSocket.class) {
            S.e("[" + ip + "]已断开连接");
            clients.remove(ip);
        }
    }

    public void stopSend() {
        if (zThread_send != null) {
            zThread_send.close();
            zThread_send = null;
        }
    }

    public void stopRecv() {
        if (zThread_recv != null) {
            zThread_recv.close();
            zThread_recv = null;
        }
    }

    private void stopHeart() {
        if (zThread_heart != null) {
            zThread_heart.close();
            zThread_heart = null;
        }
    }

    public void close() {
        isClose = true;
        send(RESPONSE_OUT);
        context = null;
    }

    public interface AskResult {
        void whenGotResult(Msg msg);
    }

    public interface Receiver {
        void whenGotResult(Msg msg);
    }

    public interface FileDownloadResult {
        void whenDownloadFinished(String filename);

        void whenDownloadingFile(String filename, float percent);

        void whenStartDownload(String filename, long filesize);
    }

    public interface FileServer {
        void whenSomeOneAskFile(String id, String ip, String filename);
    }
}

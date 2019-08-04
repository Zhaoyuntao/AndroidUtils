package com.zhaoyuntao.androidutils.net;

import android.content.Context;
import android.graphics.Bitmap;

import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ZSocket {

    public static String socketId = "DEFAULT";
    //心跳包频率
    private static final float frame_heart = 2;
    private static ZSocket zSocket;
    private Sender zThread_send;
    private Sender zThread_sendFile;
    private Receiver zThread_recv;
    private Receiver zThread_recvFile;
    private ZThread zThread_heart;
    private int port = 16880;
    private int portFile = 16881;

    private Map<String, Client> clients;
    private Context context;

    private Map<String, TimeOut> timeOutQueue;

    private String path;

    private final int timeout = 3;//超时时长:秒

    public static final int maxPackagSize = 63 * 1024;//UDP每个包最大不能超过64kb


    //缓存文件下载任务
    private Map<String, FileDownloadTask> fileDownloadTaskCache = new HashMap<>();
    //缓存文件读取任务
    private Map<String, FileRandomReader> fileReaderTaskCache = new HashMap<>();
    //缓存RPC回调,当一次完整的RPC请求完成后,从缓存去除
    private Map<String, AskResult> map_AskResult = new HashMap<>();
    //RPC应答
    private Map<String, Answer> map_Answer = new HashMap<>();
    private ReceiverResult receiver;

    private ZSocket() {
        clients = new HashMap<>();
        timeOutQueue = new HashMap<>();
        recv();
        recvFile();
        initSender();
        initFileSender();
        initHeart();
    }

    public void asServer() {
        socketId = "SERVER";
    }

    public void asClient() {
        socketId = "CLIENT";
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
        send(msg.getBytes(), Msg.ASK, ip);
    }

    public void send(byte[] msgData, byte type, String ip) {
        if (msgData == null) {
            return;
        }
        Msg msg = new Msg();
        msg.ip = ip;
        msg.type = type;
        msg.msg = msgData;
        msg.count = 1;
        msg.index = 0;
        send(msg);
    }

    public synchronized void send(Msg msg) {
        zThread_send.send(msg);
    }

    public synchronized void sendFile(Msg msg) {
        zThread_sendFile.send(msg);
    }

    public void ask(String request, AskResult askResult) {
        String id = Msg.getRandomId();
        Msg msg = new Msg(id);
        msg.type = Msg.ASK;
        msg.msg = request.getBytes();
        send(msg);
        map_AskResult.put(id, askResult);
    }

    public void addAnswer(Answer answer) {
        map_Answer.put(answer.getAsk(), answer);
    }

    public void downloadFile(String filename, final FileDownloadResult fileDownloadResult) {
        if (S.isEmpty(filename)) {
            return;
        }
        String id = Msg.getRandomId();
        final FileDownloadTask fileDownloadTask = new FileDownloadTask(id, filename, new FileDownloadTask.CallBack() {
            @Override
            public void whenFileNotFind(String filename) {
                S.e("文件不存在:" + filename);
                if (fileDownloadResult != null) {
                    fileDownloadResult.whenFileNotFind(filename);
                }
            }

            @Override
            public void whenDownloadCompleted(FileDownloadTask fileDownloadTask1, String filename) {
                fileDownloadTaskCache.remove(fileDownloadTask1.getTaskId());
//                S.s("文件下载完毕:" + filename);
                if (fileDownloadResult != null) {
                    fileDownloadResult.whenDownloadCompleted(filename);
                }
            }

            @Override
            public void whenDownloading(String filename, float percent) {
//                S.s("正在下载文件[" + filename + "]:" + percent);
                if (fileDownloadResult != null) {
                    fileDownloadResult.whenDownloading(filename, percent);
                }
            }

            @Override
            public void whenStartDownloading(String filename, long filesize) {
//                S.s("开始下载文件[" + filename + "] 大小:" + filesize);
                if (fileDownloadResult != null) {
                    fileDownloadResult.whenStartDownloading(filename, filesize);
                }
            }

            @Override
            public void checkLost(int[] lost, String ip, FileDownloadTask fileDownloadTask) {
//                S.s("缺少文件块个数:" + lost.length + "=====================================================================>");
                if (lost.length <= 0) {
                    return;
                }
                Msg msg = new Msg(fileDownloadTask.getTaskId());
                msg.type = Msg.ASK_FILE_PIECE;
                msg.ip = ip;
                byte[] pieceIndexs = new byte[lost.length * 4];
                for (int i = 0; i < lost.length; i++) {
                    //计算没有接收到的下标
                    byte[] indexTmpArr = S.intToByteArr(lost[i]);
                    System.arraycopy(indexTmpArr, 0, pieceIndexs, i * 4, indexTmpArr.length);
                }
                msg.msg = pieceIndexs;
                send(msg);
            }

        });
        //缓存文件下载任务
        fileDownloadTaskCache.put(id, fileDownloadTask);
        S.s("----------------------------> 请求文件信息:" + filename);
        //向服务器请求文件信息
        Msg msg = new Msg(id);
        msg.type = Msg.ASK_FILE_INFO;
        msg.filename = filename;
        send(msg);
        TimeOut timeOut = new TimeOut() {
            @Override
            public void whenTimeOut() {
                if(fileDownloadTask!=null) {
                    fileDownloadTask.close();
                }
                fileDownloadTaskCache.remove(id);
                if (fileDownloadResult != null) {
                    fileDownloadResult.whenTimeOut();
                }
            }
        };
        timeOut.id=id;
        timeOut.time = S.currentTimeMillis();
        timeOutQueue.put(id, timeOut);
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
                    Msg msg = new Msg();
                    msg.type = Msg.HEARTBIT;
                    msg.msg = socketId.getBytes();
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
                for (TimeOut timeOut : timeOutQueue.values()) {
                    long time = timeOut.time;
                    long during = time_now - time;
                    if (during > 3000) {
                        timeOutQueue.remove(timeOut.id);
                        timeOut.whenTimeOut();
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
        zThread_heart.start();
    }


    private void initSender() {
        zThread_send = new Sender(port, clients, null);
        zThread_send.start();
    }

    private void initFileSender() {
        zThread_sendFile = new Sender(portFile, clients, new Sender.CallBack() {
            @Override
            public void whenSend() {
            }
        });
        zThread_sendFile.start();
    }


    private void recv() {
        zThread_recv = new Receiver(port, context, new Receiver.CallBack() {
            @Override
            public void whenGotMsg(final Msg msg) {
                String id = msg.id;
                timeOutQueue.remove(id);
                String ip = msg.ip;
                switch (msg.type) {
                    case Msg.HEARTBIT:
                        Client client = getClient(ip);
                        String socketIdTmp = new String(msg.msg);
                        //如果是不同类型的socket,则加入连接
                        if (!socketIdTmp.equals(socketId)) {
                            if (client == null) {
                                client = new Client();
                                client.ip = ip;
                                client.port = port;
                                //保存远程ip
                                addClient(client);
                            }
                            //刷新最新一次通信的时间
                            client.time_lastheart = S.currentTimeMillis();
                        }
                        break;
                    case Msg.LOGOUT:
                        removeClient(ip);
                        break;
                    case Msg.ASK:
                        String stringMsg = new String(msg.msg);
                        S.s("接到来自[" + ip + ":" + port + "]的请求 : " + stringMsg);
                        Answer answer = map_Answer.get(stringMsg);
                        if (answer != null) {
                            Msg response = new Msg(msg.id);
                            response.type = Msg.ASK;
                            response.msg = answer.getAnswer().getBytes();
                            send(response);
                        } else {
                            if (receiver != null) {
                                receiver.whenGotResult(stringMsg);
                            }
                        }

                        break;
                    case Msg.ANSWER:
                        String stringMsg2 = new String(msg.msg);
                        S.s("接到来自[" + ip + ":" + port + "]的回复 : " + stringMsg2);
                        AskResult askResult_rpc = map_AskResult.remove(msg.id);
                        if (askResult_rpc != null) {
                            askResult_rpc.whenGotResult(msg);
                        } else {
                            if (receiver != null) {
                                receiver.whenGotResult(stringMsg2);
                            }
                        }
                        break;
                    case Msg.ASK_FILE_INFO://文件信息请求

                        //预加载文件读取器
                        FileRandomReader fileRandomReader = new FileRandomReader(msg.id, path, msg.filename, new FileRandomReader.CallBack() {
                            @Override
                            public void whenTimeOut(FileRandomReader fileRandomReader) {
//                                S.s("文件读取超时,移除读取线程");
                                fileReaderTaskCache.remove(fileRandomReader.getTaskId());
                            }

                            @Override
                            public void whenFileNotFind(String filename, FileRandomReader fileRandomReader) {
                                S.e("FileRandomReader:文件[" + filename + "]不存在,无法获取文件信息");
                                fileReaderTaskCache.remove(fileRandomReader.getTaskId());
                                //回复文件信息
                                Msg response = new Msg(msg.id);
                                response.type = Msg.FILE_INFO;
                                response.filename = filename;
                                response.ip = msg.ip;
                                send(response);
                                S.s("已回复文件信息到[" + msg.ip + "]");
                            }

                            @Override
                            public void whenFileFind(String filename, FileRandomReader fileRandomReader) {
//                                S.s("文件[" + filename + "]存在,预加载成功,读取器加入缓存");
                                fileReaderTaskCache.put(msg.id, fileRandomReader);
                            }

                            @Override
                            public void whenGotFileInfo(int count, int piecesize, int filesize) {
                                S.s("文件信息已获取,文件块数量:" + count + " 文件块大小:" + piecesize + " 文件总大小:" + filesize);
                                //文件块大小
                                byte[] pieceSizeArr = S.intToByteArr(piecesize);
                                //回复文件信息
                                Msg response = new Msg(msg.id);
                                response.type = Msg.FILE_INFO;
                                response.msg = pieceSizeArr;
                                response.count = count;
                                response.filename = msg.filename;
                                response.filesize = filesize;
                                response.ip = msg.ip;
                                send(response);
                                S.s("已回复文件信息到[" + msg.ip + "]");
                            }
                        });
                        fileRandomReader.setPieceSize(maxPackagSize);
                        fileRandomReader.start();
                        break;
                    case Msg.FILE_INFO:
                        S.s("接到文件信息,开始下载文件...");
                        //获取文件下载任务,获取文件信息
                        FileDownloadTask fileDownloadTask = fileDownloadTaskCache.get(msg.id);
                        if (fileDownloadTask != null) {
                            //文件块大小
                            int pieceSize = S.byteArrToInt(msg.msg);
                            fileDownloadTask.setPieceSize(pieceSize);
                            fileDownloadTask.setFileSize(msg.filesize);
                            fileDownloadTask.setCount(msg.count);
                            fileDownloadTask.setIp(msg.ip);
                            fileDownloadTask.start();
                        }
                        break;
                    case Msg.ASK_FILE_PIECE:
                        //向请求者发送文件块
                        FileRandomReader reader = fileReaderTaskCache.get(msg.id);
                        if (reader != null) {
                            reader.getFilePiece(new FileRandomReader.Reader() {

                                @Override
                                public int[] getIndexs() {
                                    byte[] indexsByteArr = msg.msg;
                                    int[] indexs = new int[0];
                                    //byte数组转化成int数组
                                    if (indexsByteArr != null && indexsByteArr.length > 0 && indexsByteArr.length % 4 == 0) {
                                        for (int i = 0; i < indexsByteArr.length; i += 4) {
                                            int index = S.byteArrToInt(Arrays.copyOfRange(indexsByteArr, i, i + 4));
                                            indexs = Arrays.copyOf(indexs, indexs.length + 1);
                                            indexs[indexs.length - 1] = index;
                                        }
                                    }
                                    return indexs;
                                }

                                @Override
                                public void whenGotFilePiece(String filename, int count, int index, byte[] piece) {
                                    Msg response = new Msg(msg.id);
                                    response.filename = msg.filename;
                                    response.count = count;
                                    response.index = index;
                                    response.msg = piece;
                                    response.type = Msg.FILE_PIECE;
                                    response.ip = msg.ip;
                                    sendFile(response);
                                }

                                @Override
                                public void whenTaskEnd() {
                                    Msg response = new Msg(msg.id);
                                    response.filename = msg.filename;
                                    response.type = Msg.FILE_CHECK;
                                    response.ip = msg.ip;
                                    send(response);
                                }
                            });
                        }
                        break;
                    case Msg.FILE_CHECK:
//                        S.s("本次任务完成,检查丢包");
                        //获取文件下载任务,获取文件信息
                        FileDownloadTask fileDownloadTask_check = fileDownloadTaskCache.get(msg.id);
                        if (fileDownloadTask_check != null) {
                            fileDownloadTask_check.check();
                        }
                        break;
                    default:
                        S.s("接到来自[" + ip + ":" + port + "]" + "(type:" + msg.type + ")的未知类型消息");
                        break;
                }
            }
        });
        zThread_recv.start();
    }

    private void recvFile() {
        zThread_recvFile = new Receiver(portFile, context, new Receiver.CallBack() {
            @Override
            public void whenGotMsg(Msg msg) {
                String ip = msg.ip;
                switch (msg.type) {
                    case Msg.BITMAP:
                        S.s("接到来自[" + ip + "]的图片");
                        AskResult askResult_bitmap = map_AskResult.get(msg.id);
                        map_AskResult.remove(msg.id);
                        if (askResult_bitmap != null) {
                            askResult_bitmap.whenGotResult(msg);
                        }
                        break;
                    case Msg.FILE_PIECE:
//                        S.s("接到文件块-----<");
                        //获取文件下载任务,插入文件块
                        FileDownloadTask fileDownloadTask_assemble = fileDownloadTaskCache.get(msg.id);
                        if (fileDownloadTask_assemble != null) {
                            fileDownloadTask_assemble.addPiece(msg.msg, msg.index);
                        }
                        break;
                    default:
                        S.s("接到来自[" + ip + "]" + "(type:" + msg.type + ")的未知类型消息");
                        break;
                }
            }
        });
        zThread_recvFile.start();
    }

    public ZSocket setReceiver(ReceiverResult receiver) {
        this.receiver = receiver;
        return this;
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
        if (zThread_sendFile != null) {
            zThread_sendFile.resumeThread();
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
        if (zThread_sendFile != null) {
            zThread_sendFile.close();
            zThread_sendFile = null;
        }
    }

    public void stopRecv() {
        if (zThread_recv != null) {
            zThread_recv.close();
            zThread_recv = null;
        }
        if (zThread_recvFile != null) {
            zThread_recvFile.close();
            zThread_recvFile = null;
        }
    }

    public void clearTask() {

        for (FileDownloadTask fileDownloadTask : fileDownloadTaskCache.values()) {
            fileDownloadTask.close();
        }
        for (FileRandomReader fileRandomReader : fileReaderTaskCache.values()) {
            fileRandomReader.close();
        }
        fileDownloadTaskCache.clear();
        fileReaderTaskCache.clear();
        map_Answer.clear();
        map_AskResult.clear();
    }

    private void stopHeart() {
        if (zThread_heart != null) {
            zThread_heart.close();
            zThread_heart = null;
        }
    }

    public void close() {
        stopSend();
        stopRecv();
        stopHeart();
        clearTask();
        clearClient();
        context = null;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public abstract class TimeOut {
        public long time;
        public String id;

        public abstract void whenTimeOut() ;
    }

    public interface CallBack {
        void whenTimeOut();
    }

    public interface AskResult {
        void whenGotResult(Msg msg);
    }

    public interface Answer {
        String getAsk();

        String getAnswer();
    }

    public interface ReceiverResult extends CallBack {
        void whenGotResult(String msg);
    }

    public interface FileDownloadResult extends CallBack {
        void whenFileNotFind(String filename);

        void whenDownloadCompleted(String filename);

        void whenDownloading(String filename, float percent);

        void whenStartDownloading(String filename, long filesize);
    }

}

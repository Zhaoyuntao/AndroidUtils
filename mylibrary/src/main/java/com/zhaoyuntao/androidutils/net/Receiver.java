package com.zhaoyuntao.androidutils.net;

import android.content.Context;

import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZThread;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

public class Receiver extends ZThread {
    private DatagramSocket datagramSocket;
    private CallBack callBack;
    private Context context;
    private int port;

    public Receiver(int port, Context context, CallBack callBack) {
        this.callBack = callBack;
        this.context = context;
        this.port = port;
    }

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
        final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
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
        if(callBack!=null) {
            callBack.whenGotMsg(msg);
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
        context = null;
    }

    public interface CallBack {
        void whenGotMsg(Msg msg);
    }
}

package com.littlecat.powerbank.socket;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.littlecat.powerbank.IBackService;
import com.littlecat.powerbank.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;


public class SocketService extends Service {
    private static final String TAG = "BackService";
    //心跳包频率
    private static final long HEART_BEAT_RATE = 90 * 1000;

    public static final String HOST = "118.31.15.186";// //
    public static final int PORT = 8331;

    public static final String MESSAGE_ACTION = "com.littlecat.powerbank.socket";
    public static final String HEART_BEAT_ACTION = "com.littlecat.powerbank.socket.heart";
    public static final String SOCKET_INIT_ACTION = "com.littlecat.powerbank.socket.init";

    public static final String HEART_BEAT_STRING = "15";//心跳包内容

    private ReadThread mReadThread;

    private LocalBroadcastManager mLocalBroadcastManager;

    private WeakReference<Socket> mSocket;

    // For heart Beat
    private Handler mHandler = new Handler();
    private boolean isSuccess;
    private Runnable heartBeatRunnable = new Runnable() {

        @Override
        public void run() {
            if (System.currentTimeMillis() - sendTime >= HEART_BEAT_RATE) {
                mHandler.removeCallbacks(heartBeatRunnable);
                mReadThread.release();
                releaseLastSocket(mSocket);
                new InitSocketThread().start();
            }
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private long sendTime = System.currentTimeMillis();
    private IBackService.Stub iBackService = new IBackService.Stub() {

        @Override
        public boolean sendMessage(String message) throws RemoteException {
            sendMsg(message);
            return isSuccess;
        }
    };
    private String socketIp;
    private String socketPort;

    @Override
    public IBinder onBind(Intent intent) {
        this.socketIp = intent.getStringExtra("IP");
        this.socketPort = intent.getStringExtra("PORT");
        new InitSocketThread().start();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        return iBackService;
    }

    @Override
    public void onCreate() {
        Log.d("lixiang", "lixiang---flag");
        super.onCreate();


    }

    public void sendMsg(final String msg) {
        if (null == mSocket || null == mSocket.get()) {
//            isSuccess = false;
//            return ;
        }

        new Thread() {
            @Override
            public void run() {
                Socket soc = mSocket.get();
                try {
                    if (!soc.isClosed() && !soc.isOutputShutdown()) {
                        OutputStream os = soc.getOutputStream();
                        String message = msg;
                        int length = message.getBytes().length;
                        byte[] bytes = new byte[length + 4];
                        bytes[0] = (byte) (bytes.length & 0xff);
                        bytes[1] = (byte) (bytes.length >> 8 & 0xff);
                        bytes[2] = (byte) (bytes.length >> 16 & 0xff);
                        bytes[3] = (byte) (bytes.length >> 24 & 0xff);
//                        bytes[0] = (byte)length;
                        System.arraycopy(message.getBytes(), 0, bytes, 4, length);
                        os.write(bytes);
                        os.flush();
//                        sendTime = S
                    } else {
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void initSocket() {//初始化Socket
        try {
            Socket so = new Socket(socketIp, Integer.valueOf(socketPort));
            mSocket = new WeakReference<Socket>(so);
            mReadThread = new ReadThread(so);
            mReadThread.start();
            Intent intent = new Intent(SOCKET_INIT_ACTION);
            mLocalBroadcastManager.sendBroadcast(intent);
            mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }
    }

    private void releaseLastSocket(WeakReference<Socket> mSocket) {
        try {
            if (null != mSocket) {
                Socket sk = mSocket.get();
                if ((null != sk)&&(!sk.isClosed())) {
                    sk.close();
                }
                sk = null;
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            initSocket();
        }
    }


    // Thread to read content from Socket
    class ReadThread extends Thread {
        private WeakReference<Socket> mWeakSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            mWeakSocket = new WeakReference<Socket>(socket);
        }

        public void release() {
            isStart = false;
            releaseLastSocket(mWeakSocket);
        }

        @Override
        public void run() {
            super.run();
            Socket socket = mWeakSocket.get();
            if (null != socket) {
                try {
                    InputStream is = socket.getInputStream();
                    byte[] buffer = new byte[1024 * 4];
                    int length = 0;
                    while (!socket.isClosed() && !socket.isInputShutdown()
                            && isStart && ((length = is.read(buffer)) != -1)) {
                        if (length > 0) {
                            String message = new String(Arrays.copyOf(buffer,
                                    length)).trim();
                            Log.e(TAG, message);
                            String[] info = message.split("\\|");
                            try {
                                String cmd = info[0];
                                int res = 0;
                                switch (cmd) {
                                    case Constant.TCP_CMD_ANS_LOGIN:
                                        //sendTcpMsg(Constant.TCP_CMD_ANS_LOGIN, "0");
                                        res = Integer.parseInt(info[1]);
                                        if(res != 0) {
                                            mHandler.removeCallbacks(heartBeatRunnable);
                                            mReadThread.release();
                                            releaseLastSocket(mSocket);
                                            new InitSocketThread().start();
                                        } else {
                                            Intent intent_login = new Intent(Constant.INTENT_LOGIN_SUCCESS);
                                            mLocalBroadcastManager.sendBroadcast(intent_login);
                                        }
                                        break;
                                    case Constant.TCP_CMD_ORDER:
                                        sendTcpMsg(Constant.TCP_CMD_ANS_ORDER, info[1], "0");
                                        Intent intent = new Intent(Constant.INTENT_BORROW_CONFIRM);
                                        intent.putExtra(Constant.ORDER_ID, info[1]);
                                        mLocalBroadcastManager.sendBroadcast(intent);
                                        break;
                                    case Constant.TCP_CMD_SLOT:
                                        int slotId = Integer.parseInt(info[1]);
                                        Intent intent_slot = new Intent(Constant.INTENT_SLOT_BATTERY);
                                        intent_slot.putExtra(Constant.SLOT_ID, slotId);
                                        mLocalBroadcastManager.sendBroadcast(intent_slot);
                                        sendTcpMsg(Constant.TCP_CMD_ANS_SLOT,info[1], "0");
                                        break;

                                    case Constant.TCP_CMD_RESTART:

//todo
                                        reboot();
// reboot
                                        break;
                                    case Constant.TCP_CMD_RESETTING:
                                        Intent intent_resetting = new Intent(Constant.INTENT_RESETTING);
                                        mLocalBroadcastManager.sendBroadcast(intent_resetting);
                                        break;
                                    case Constant.TCP_CMD_HEART:
                                        sendTime = System.currentTimeMillis();
                                        break;

                                    case Constant.TCP_CMD_LOCK_SLOT:
                                        int lockSlotId = Integer.parseInt(info[1]);
                                        Intent intent_lock = new Intent(Constant.TCP_CMD_LOCK_SLOT);
                                        intent_lock.putExtra(Constant.LOCK_SLOT,lockSlotId);
                                        mLocalBroadcastManager.sendBroadcast(intent_lock);
                                        sendTcpMsg(Constant.TCP_CMD_LOCK_SLOT,info[1], "0");
                                        break;

                                    case Constant.TCP_CMD_UNLOCK_SLOT:
                                        int unlockSlotId = Integer.parseInt(info[1]);
                                        Intent intent_unlock = new Intent(Constant.TCP_CMD_UNLOCK_SLOT);
                                        intent_unlock.putExtra(Constant.LOCK_SLOT,unlockSlotId);
                                        mLocalBroadcastManager.sendBroadcast(intent_unlock);
                                        sendTcpMsg(Constant.TCP_CMD_UNLOCK_SLOT,info[1], "0");
                                        break;
                                }
                               if (cmd.equals(Constant.TCP_CMD_HEART)) {//处理心跳回复
                                    sendTime = System.currentTimeMillis();
                                    Intent intent = new Intent(HEART_BEAT_ACTION);
                                    mLocalBroadcastManager.sendBroadcast(intent);
                                } else {
                                    //其他消息回复
                                    Intent intent = new Intent(MESSAGE_ACTION);
                                    intent.putExtra("message", message);
                                    mLocalBroadcastManager.sendBroadcast(intent);
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void reboot() {
        try {
            Log.v(TAG, "root Runtime->reboot");
            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot "});
            proc.waitFor();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void sendTcpMsg(String... info) {
        String msg = Constant.getMsg(info);
        sendMsg(msg);
    }
}
package com.bluetoothchat.www.bluetoothchat.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.bluetoothchat.www.bluetoothchat.Constants;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by SS on 17-1-22.
 * 这个类负责蓝牙的建立和管理与其他设备的连接。
 * 它有一个线程监听传入连接，一个用于连接设备
 * 一个用于连接时进行数据传输的线程
 */
public class BluetoothChatService {
    //常量指示当前连接状态
    public static final int STATE_NONE = 0;//什么都不做
    public static final int STATE_LISTEN = 1;//正在监听传入的连接
    public static final int STATE_CONNECTING = 2;//正在启动一个传出的连接
    public static final int STATE_CONNECTED =3 ;//已经连接一个远程设备
    //创建一个server socket时SDP的名字
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";
    //唯一的UUID
    private static final UUID MY_UUID_SECURE =UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") ;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    //成员变量
    private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private int mState;
    private ConnectThread mConnectThread;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectedThread mConnectedThread;
    private Context context;
    /**
     * @param context 上下文
     * @param mHandler 用来传递信息给UI
     */
    public BluetoothChatService(Context context, Handler mHandler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        this.mHandler = mHandler;
        this.context = context;
    }

    /**设置当前聊天时连接的状态
     * @param state 用来定义当前连接状态的整数
     */
    private synchronized void setState(int state){
        mState = state;
        /*obtainMessage的第一个参数：指定返回的message.what字段值
        * 第二个参数：指定返回的message.arg1字段值
        * 第三个参数：指定返回的message.arg2字段值*/
        Message message = mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1);
        message.sendToTarget();//将此消息发送出去到指定的Handler的getTarget（）接收
    }

    /**
     * @return 返回当前的连接状态
     */
    public synchronized int getState(){
        return mState;
    }

    /**
     * 开始聊天服务，接受一个线程来监听聊天模式。被主线程的onResume（）调用
     */
    public synchronized void start(){
        //取消任何试图连接的线程
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        //取消当前正在运行的线程
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_LISTEN);
        //开启一个线程来监听BluetoothServerSocket
        if(mSecureAcceptThread == null){
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if(mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /**开启一个连接线程ConnectThread，来连接远端设备
     * @param device 要连接的蓝牙设备
     * @param secure Socket Security type - Secure(true),  Insecure(false)
     */
    public synchronized void connect(BluetoothDevice device,boolean secure){
        //取消任何试图连接的线程
        if(mState ==STATE_CONNECTING ){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }
        //取消任何已经连接的线程
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        //开启一个线程去连接给定的设备
        mConnectThread = new ConnectThread(device,secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    /**开始处理蓝牙连接，开启ConnectedThread线程
     * @param socket 被连接的BluetoothSocket
     * @param device 被连接的蓝牙设备
     * @param socketType
     */
    public synchronized void connected(BluetoothSocket socket,BluetoothDevice device,final String socketType){
        //取消正要连接的线程ConnectThread
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        //取消已经连接的线程ConnectedThread
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        //取消接收线程，因为我们只要连接一个设备
        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if(mInsecureAcceptThread != null){
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        //启动线程管理连接并执行传输
        mConnectedThread = new ConnectedThread(socket,socketType);
        mConnectedThread.start();
        //将连接的设备的名称返回到UI
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME,device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    /**
     * 停止所有的线程
     */
    public synchronized void stop(){
        //取消正要连接的线程ConnectThread
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        //取消已经连接的线程ConnectedThread
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        //取消接收线程
        if(mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if(mInsecureAcceptThread != null){
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**写在非同步connectedthread下的方式
     * @param out
     */
    public void write(byte[] out){
        //一个临时的对象
        ConnectedThread r ;
        synchronized (this){
            //同步ConnectedThread
            if(mState != STATE_CONNECTED)return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    /**
     * 尝试连接失败时，通知用户
     */
    private void connectionFailed(){
        //发送一条消息给UI
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST,"无法连接设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        //开启服务重新监听
        BluetoothChatService.this.start();

    }

    /**
     * 提示连接已经丢失
     */
    private void connectionLost(){
        //发送一条消息给UI
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST,"连接丢失");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        //开启服务重新监听
        BluetoothChatService.this.start();
    }

    /*
    * 此线程是监听可以连接的蓝牙，它像一个服务器的客户端，一直运行到连接被接受*/
    private class AcceptThread extends Thread{
        //The local server socket
        private final BluetoothServerSocket mServerSocket;
        private String mSocketType;

       public AcceptThread(boolean secure){
           BluetoothServerSocket tmp = null;
           mSocketType = secure?"Secure":"Insecure";
           //创建一个新的服务器套接字
           try {
               if(secure){
                   //用于监听加密的连接
                   tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,MY_UUID_SECURE);
               }else {
                   //用于监听不加密的连接
                   tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE,MY_UUID_INSECURE);
               }
           }catch (IOException e){
               e.printStackTrace();
           }
           mServerSocket = tmp;
       }

        @Override
        public void run() {
            BluetoothSocket socket = null;
            while(mState != STATE_CONNECTED){
                try {
                    /*这是一个阻塞调用。它将在连接被接受或发生异常时返回。
                    仅当远程设备发送的连接请求中所包含的 UUID 与向此侦听服务器套接字注册的 UUID 相匹配时，
                    连接才会被接受。 操作成功后，accept() 将会返回已连接的 BluetoothSocket*/
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //如果一个连接被接受
                if(socket != null){
                    synchronized (BluetoothChatService.this){
                        switch (mState){
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //正常情况下，启动连接线程
                                connected(socket,socket.getRemoteDevice(),mSocketType);
                                break;
                            //没有连接或者没有准备好，终止新的socket
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }
                }
            }
        }
        public void cancel(){
            try {
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    * 当试图与外部线程连接时此线程运行，它是通过连接成功或者失败来运行的*/
    private class ConnectThread extends Thread{
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;
        private String mSocketType;

        public ConnectThread(BluetoothDevice mDevice,boolean secure) {
            this.mDevice = mDevice;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            //得到一个与给定蓝牙设备连接的BluetoothSocket
            try{
                if(secure){
                    tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                }else {
                    tmp = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }

            }catch (IOException e){
                e.printStackTrace();
            }
            mSocket = tmp;
        }

        @Override
        public void run() {
            //取消发现，因为他会减慢连接
            mAdapter.cancelDiscovery();
            //连接BluetoothSocket
            try {
                //这是一个阻塞的调用，只会返回一个成功的连接或者异常
                mSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                connectionFailed();
                return;
            }
            //reset the ConnectThread because we're done 连接完成后，释放ConnectThread
            synchronized (BluetoothChatService.this){
               mConnectThread = null;
            }
            //开启已连接的线程
            connected(mSocket,mDevice,mSocketType);
        }
        public void cancel(){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /*
    * 此线程运行在与远程设备连接期间，它处理所有传入传出的传输*/
    private class ConnectedThread extends Thread{
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket mSocket,String socketType) {
         //   Log.d("蓝牙服务程序","连接到："+socketType);
            this.mSocket = mSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            //获取BluetoothSocket的输入输出流
            try {
                tmpIn = mSocket.getInputStream();
                tmpOut = mSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes  ;
            //连接时，持续监听输入流
            while(true){//这里被我改掉了，和API不一样
                try {
                    //从输入流读取数据，获取字节数
                    bytes = mInStream.read(buffer);
                    byte[] data = new byte[bytes];/*这里设置两个数组的原因是，接收内容的长度不确定
           ，根据获取的字节数来定义数组的长度，避免这种情况（第二次的发送内容小于第一次的发送内容）
             就不会导致读取的时候还残留上一次的数据 */
                    for (int i = 0; i <bytes ; i++) {
                        data[i] = buffer[i];
                    }
                    //发送获得的内容给UI，bytes是要传递的字节数，data是内容
                    Message message = mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, data);
                    message.sendToTarget();
                   // buffer = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    //启动服务重新监听模式
                    BluetoothChatService.this.start();
                    break;
                }
            }
        }
        public void write(byte[] buffer){
            try {
                mOutStream.write(buffer);
                Message message = mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer);
                message.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void cancel(){
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

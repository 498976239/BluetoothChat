package com.bluetoothchat.www.bluetoothchat.activity;


import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothchat.www.bluetoothchat.Constants;
import com.bluetoothchat.www.bluetoothchat.R;
import com.bluetoothchat.www.bluetoothchat.SaveDate;
import com.bluetoothchat.www.bluetoothchat.adapter.MsgAdapter;
import com.bluetoothchat.www.bluetoothchat.bean.Msg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * 这个Fragment控制蓝牙和其他设备进行通讯
 */
public class BluetoothChatFragment extends Fragment {

//intent需要的code
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothChatService mChatService;
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;
    private StringBuffer mOutStringBuffer;
    private String mConnectedDeviceName;
    private ProgressBar pb;
    private List<Msg> list =  new ArrayList();
    private MsgAdapter adapter;
    private SaveDate mSaveDate;
    /**
     * 在editor上执行操作时要调用的回调的接口定义。
     * 在EditText输入后，不点击Button进行请求，而是直接点击软键盘上的"回车"，
     */
    private TextView.OnEditorActionListener mWriter
            = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            if(i == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_UP){
                String msg = textView.getText().toString();
                sendMessage(msg);
            }
            return true;
        }
    };



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //获取本地蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //如果适配器为空，那么不支持蓝牙设备
        if(mBluetoothAdapter == null){
            FragmentActivity activity = getActivity();
            Toast.makeText(activity,"没有可以使用的蓝牙设备",Toast.LENGTH_SHORT).show();
            activity.finish();
        }
        mSaveDate = new SaveDate(getActivity(),"message.db",null,1);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mConversationView = (ListView) view.findViewById(R.id.in);
        mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
        pb = (ProgressBar) view.findViewById(R.id.waiting);
    }
    @Override
    public void onStart() {
        super.onStart();
        //如果蓝牙没有在线，那么需要被开启
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }else if(mChatService == null){
            //否则建立聊天会话
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mChatService != null){
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        /*如果在onStart()方法里蓝牙没有被启用，那么我们确保
        * 在onResume()方法里被启用*/
        if(mChatService != null){
            if(mChatService.getState() == BluetoothChatService.STATE_NONE){
                mChatService.start();
            }
        }
    }

    /**
     * 设置聊天界面和后台操作
     */
    private void setupChat() {
        adapter = new MsgAdapter(getActivity(),R.layout.message,list);
        //初始化会话线程的数组适配器，也就是初始化listview并给它设置适配器
        mConversationView.setAdapter(adapter);
        mOutEditText.setOnEditorActionListener(mWriter);//为edit设置监听器，按软键盘的回车即可发送信息
        //为按钮设置点击事件
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //使用编辑文本的内容发送消息
                View v = getView();
                if(v != null){
                    TextView textView = (TextView) v.findViewById(R.id.edit_text_out);
                    String s = textView.getText().toString();
                    sendMessage(s);
                    mConversationView.setSelection(list.size());//将listView定位到最后一行

                }
            }
        });
        //初始化BluetoothChatService进行蓝牙连接
        mChatService = new BluetoothChatService(getActivity(),mHandler);
        //初始化发送消息的缓存
        mOutStringBuffer = new StringBuffer("");
    }


    /*从BluetoothChatService获得信息
        * */
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            FragmentActivity activity = getActivity();
            switch(msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    switch(msg.arg1){
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to,mConnectedDeviceName));
                           // Log.i("main--connected---",mChatService.getState()+"");
                            adapter.clear();
                        break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                break;
               case Constants.MESSAGE_WRITE:
                   byte[] writeBuf = (byte[]) msg.obj;
                   String writeMsg = new String(writeBuf);
                   Msg s = new Msg(writeMsg,Msg.TYPE_SENT);
                   list.add(s);
                   adapter.notifyDataSetChanged();
                   break;
               case Constants.MESSAGE_READ:
                   byte[] readBuf = (byte[]) msg.obj;
                  // float f = Float.intBitsToFloat(bytesToInt(readBuf));
                   String readMsg = new String(readBuf);
                   Msg s1 = new Msg(readMsg,Msg.TYPE_RECEIVED);
                   //s1.setF(f);
                   list.add(s1);
                   adapter.notifyDataSetChanged();
                   SQLiteDatabase database = mSaveDate.getWritableDatabase();
                   ContentValues values = new ContentValues();
                   Date currentTime = new Date();
                   values.put("time",s1.getmSimpleDateFormat(s1.getmDate()));
                   values.put("content",s1.getContent());
                   values.put("current",currentTime.getTime());
                   database.insert("info", null, values);
                   values.clear();
                   database.close();
                   break;
               case Constants.MESSAGE_DEVICE_NAME:
                   mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                   if(activity != null){
                       Toast.makeText(activity,"连接到："+mConnectedDeviceName,Toast.LENGTH_SHORT).show();
                       pb.setVisibility(View.GONE);
                   }
                   break;
               case Constants.MESSAGE_TOAST:
                   if(activity != null){
                       Toast.makeText(activity,msg.getData().getString(Constants.TOAST),Toast.LENGTH_SHORT).show();
                       pb.setVisibility(View.GONE);
                   }
                   break;
            }
        }
    };
    /**发送消息
     * @param msg
     */
    private void sendMessage(String msg) {
        //操作之前检查是否已经连接
        if(mChatService.getState() != BluetoothChatService.STATE_CONNECTED){
            Toast.makeText(getActivity(),R.string.not_connected,Toast.LENGTH_SHORT).show();
            return;
        }
        //检查需要发送内容的长度
        if(msg.length() > 0){
            //获取内容并且告诉BluetoothChatService可以write了
            byte[] send = msg.getBytes();
            mChatService.write(send);
            //将缓存区设置为0，并且清空EditText
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * 发现设备
     */
    private void ensureDiscoverable(){
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }

    /**在action bar中更新状态
     * @param resId ID
     */
    private void setStatus(int resId){
        FragmentActivity activity = getActivity();
        if(activity == null){
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if(actionBar == null){
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**在action bar中更新状态
     * @param subTitle
     */
    private void setStatus(CharSequence subTitle){
        FragmentActivity activity = getActivity();
        if(activity == null){
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if(actionBar == null){
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE_SECURE:
                //当DeviceListActivity返回一个连接的加密的设备
                if(resultCode == Activity.RESULT_OK){
                    connectDevice(data,true);
                    pb.setVisibility(View.VISIBLE);
                }
                 break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                //当DeviceListActivity返回一个连接的没有加密的设备
                if(resultCode == Activity.RESULT_OK){
                    connectDevice(data,false);
                    pb.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_ENABLE_BT:
                //返回的是要启动蓝牙时
                if(resultCode == Activity.RESULT_OK){
                    setupChat();
                }else {
                    //用户没有打开蓝牙，或者报错时
                    Toast.makeText(getActivity(),R.string.bt_not_enable_leaving,Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    /**与其他设备建立连接
     * @param intent
     * @param secure
     */
    private void connectDevice(Intent intent,boolean secure) {
        //获取设备的MAC地址
        String address = intent.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        //得到蓝牙设备
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        mChatService.connect(device,secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.secure_connect_scan:{
                Intent serverIntent = new Intent(getActivity(),DeviceListActivity.class);
                startActivityForResult(serverIntent,REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan:{
                Intent intent = new Intent(getActivity(),DeviceListActivity.class);
                startActivityForResult(intent,REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable:{
                ensureDiscoverable();
                return true;
            }
            case R.id.query_item:{
                Intent intent = new Intent(getActivity(),QueryActivity.class);
                startActivity(intent);
                return true;
            }
        }
        return false;
    }

    public int bytesToInt(byte[] b) {
        if(b.length > 3){
            int i = (b[0] << 24) & 0xFF000000;
            i |= (b[1] << 16) & 0xFF0000;

            i |= (b[2] << 8) & 0xFF00;

            i |= b[3] & 0xFF;
            return i;
        }
        return 0;
    }
}

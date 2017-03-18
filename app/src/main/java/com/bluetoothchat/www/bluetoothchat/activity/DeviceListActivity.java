package com.bluetoothchat.www.bluetoothchat.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetoothchat.www.bluetoothchat.R;

import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    private BluetoothAdapter mBtAdapter;
    private Button mButton;
    private ArrayAdapter<String> mPairedDeviceArrayAdapter;//已经配对过的设备
    private ArrayAdapter<String> mNewDeviceArrayAdapter;//新设备
    private ListView mPairedDeviceListView;
    private ListView mNewDeviceListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);//如果用户退出，设置结果为canceled
        mPairedDeviceListView = (ListView) findViewById(R.id.paired_device);
        mNewDeviceListView = (ListView) findViewById(R.id.new_device);
        mButton = (Button) findViewById(R.id.button_scan);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscovery();
               // view.setVisibility(View.GONE);
                mNewDeviceArrayAdapter.clear();
            }
        });
        //获取BluetoothAdapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        /*为已经配过对的设备设置适配器,默认情况下，ArrayAdapter可以使用一个含有TextView的布局，如果要使用更为复杂的就需要自己创建了
        TextView被引用，它将充满每个对象的数组中的tostring()。
         可以添加自定义对象的列表或数组。重写你的对象的tostring()方法来确定什么样的文本将显示为列表中的项。*/
        mPairedDeviceArrayAdapter = new ArrayAdapter(this,R.layout.device_name);
        mPairedDeviceListView.setAdapter(mPairedDeviceArrayAdapter);
        mPairedDeviceListView.setOnItemClickListener(mDeviceClickListener);
        //为新的设备设置适配器
        mNewDeviceArrayAdapter = new ArrayAdapter(this,R.layout.device_name);
        mNewDeviceListView.setAdapter(mNewDeviceArrayAdapter);
        mNewDeviceListView.setOnItemClickListener(mDeviceClickListener);
        //动态注册广播，当设备被发现
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver,intentFilter);
        //注册动态广播，当搜索完成后
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver,filter);
        //如果存在已经配过对的设备，就让其显示在已有名单上
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if(pairedDevices.size() > 0){
            findViewById(R.id.title_paired_device).setVisibility(View.VISIBLE);
            //取出已有设备
            for(BluetoothDevice device : pairedDevices){
                mPairedDeviceArrayAdapter.add(device.getName()+"\n"+device.getAddress());
            }
        }else{
            String noDevice = getResources().getText(R.string.none_paired).toString();
            mPairedDeviceArrayAdapter.add( noDevice);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //确保不再搜索设备
        if(mBtAdapter != null){
            mBtAdapter.cancelDiscovery();
        }
        //解除广播的注册
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        //setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        findViewById(R.id.title_new_device).setVisibility(View.VISIBLE);
        findViewById(R.id.buffer).setVisibility(View.VISIBLE);
        //如果已经找到，停止
        if(mBtAdapter.isDiscovering()){
            mBtAdapter.cancelDiscovery();
        }
        //否则从BluetoothAdapter中发现设备
        mBtAdapter.startDiscovery();
    }
    //为两个ListView设置点击事件
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //取消查找，因为会消耗资源，即将连接
            mBtAdapter.cancelDiscovery();
            //获取设备的MAC地址，查看最后的17个字符
            String s = ((TextView) view).getText().toString();
            String substring = s.substring(s.length() - 17);
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS,substring);
            setResult(Activity.RESULT_OK,intent);
            finish();

        }
    };
    /*设置监听广播，当发现设备或者连接状态改变时
    * */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //当发现一个设备时
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                findViewById(R.id.buffer).setVisibility(View.GONE);
                //获取蓝牙设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //如果它已经配过对，跳过它，因为已经在列表上了
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    mNewDeviceArrayAdapter.add(device.getName()+"\n"+device.getAddress());
                }
                //当查找结束，改变标题
            }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                //setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if(mNewDeviceArrayAdapter.getCount() == 0){
                    findViewById(R.id.buffer).setVisibility(View.GONE);
                   // String noDevice = getResources().getText(R.string.none_found).toString();
                   // mNewDeviceArrayAdapter.add(noDevice);
                    Toast.makeText(DeviceListActivity.this,R.string.none_found,Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
}

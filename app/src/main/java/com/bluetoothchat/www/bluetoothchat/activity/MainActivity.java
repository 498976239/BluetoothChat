package com.bluetoothchat.www.bluetoothchat.activity;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bluetoothchat.www.bluetoothchat.R;
import com.bluetoothchat.www.bluetoothchat.activity.BluetoothChatFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            FragmentManager manager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment,fragment);
            transaction.commit();
        }
    }
}

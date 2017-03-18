package com.bluetoothchat.www.bluetoothchat;

/**
 * Created by SS on 17-1-22.
 */
public interface Constants {
    //由BluetoothChatService的Handler发送的消息类型,这里本来是要写 public static final 的，但是接口已经默认了
     int MESSAGE_STATE_CHANGE = 1;
     int MESSAGE_READ = 2;
     int MESSAGE_WRITE = 3;
     int MESSAGE_DEVICE_NAME = 4;
     int MESSAGE_TOAST = 5;
    //由BluetoothChatService的Handler收到的key names
     String DEVICE_NAME = "device_name";
     String TOAST = "toast";
}

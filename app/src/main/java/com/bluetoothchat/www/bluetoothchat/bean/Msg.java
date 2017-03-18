package com.bluetoothchat.www.bluetoothchat.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by SS on 17-1-31.
 */
public class Msg {
    public static final int TYPE_RECEIVED = 0;
    public static final int TYPE_SENT = 1;
    private String content;
    private int type;
    private Date mDate;
    private SimpleDateFormat mSimpleDateFormat;
    private float f;

    public Msg(String content, int type) {
        this.content = content;
        this.type = type;
        mDate = new Date();
    }

    public String getContent() {
        return content;
    }

    public int getType() {
        return type;
    }

    public Date getmDate() {
        return mDate;
    }

    public String getmSimpleDateFormat(Date mDate){
        /* SimpleDateFormat是定制时间日期的模式 */
        mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String t = mSimpleDateFormat.format(mDate);
        return t;

    }
    public String getLocalTime(Date times){
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        String detailTime = format.format(times);
        return detailTime;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }
}

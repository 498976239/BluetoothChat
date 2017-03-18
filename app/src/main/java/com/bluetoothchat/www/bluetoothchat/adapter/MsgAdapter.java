package com.bluetoothchat.www.bluetoothchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluetoothchat.www.bluetoothchat.bean.Msg;
import com.bluetoothchat.www.bluetoothchat.R;

import java.util.List;

/**
 * Created by SS on 17-1-31.
 */
public class MsgAdapter extends ArrayAdapter {
    private int resouceId;
    public MsgAdapter(Context context, int resource, List<Msg> objects) {
        super(context, resource,objects);
        resouceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Msg s = (Msg) getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resouceId,null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            viewHolder.leftMsg = (TextView) view.findViewById(R.id.left_msg);
            viewHolder.rightMsg = (TextView) view.findViewById(R.id.right_msg);
            viewHolder.timeLeft = (TextView) view.findViewById(R.id.time_left);
            viewHolder.timeRight = (TextView) view.findViewById(R.id.time_right);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if(s.getType() == Msg.TYPE_RECEIVED){
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            //viewHolder.leftMsg.setText(s.getContent());
            viewHolder.leftMsg.setText(s.getF()+"");
            viewHolder.timeLeft.setText(s.getmSimpleDateFormat(s.getmDate()));
            viewHolder.timeRight.setText("");
        }else if(s.getType() == Msg.TYPE_SENT){
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightMsg.setText(s.getContent());
            viewHolder.timeRight.setText(s.getmSimpleDateFormat(s.getmDate()));
            viewHolder.timeLeft.setText("");
        }

        return view;
    }
    class ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsg;
        TextView rightMsg;
        TextView timeRight;
        TextView timeLeft;

    }
}

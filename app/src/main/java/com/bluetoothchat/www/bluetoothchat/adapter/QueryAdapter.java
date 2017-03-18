package com.bluetoothchat.www.bluetoothchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.bluetoothchat.www.bluetoothchat.bean.Information;
import com.bluetoothchat.www.bluetoothchat.R;
import java.util.List;

/**
 * Created by SS on 17-2-13.
 */
public class QueryAdapter extends ArrayAdapter{
    private int mLayout;
    public QueryAdapter(Context context, int resource, List<Information> objects) {
        super(context, resource, objects);
        mLayout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       Information information = (Information) getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(mLayout,null);
            viewHolder = new ViewHolder();
            viewHolder.mId = (TextView) view.findViewById(R.id.id_text);
            viewHolder.mData = (TextView) view.findViewById(R.id.content_text);
            viewHolder.mTime = (TextView) view.findViewById(R.id.time_text);
            view.setTag(viewHolder);
        }else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.mId.setText(information.getId()+"");//将int型的参数写入，转换成string
        viewHolder.mData.setText(information.getContent());
        viewHolder.mTime.setText(information.getTime());
        return view;
    }
     class ViewHolder{
        TextView mId;
        TextView mData;
        TextView mTime;
    }
}

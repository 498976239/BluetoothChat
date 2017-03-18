package com.bluetoothchat.www.bluetoothchat.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by SS on 17-2-12.
 */
public class DatePickerActivity extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private QueryActivity mQueryActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueryActivity = (QueryActivity) getActivity();
    }

    //创建一个dialog，使用者会调用它的show方法，如果已经有了dialog就不会创建，否则就创建一个
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(getActivity(),this,year,month,day);
        return dialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
            mQueryActivity.setCalendar(i,i1,i2);
    }
}

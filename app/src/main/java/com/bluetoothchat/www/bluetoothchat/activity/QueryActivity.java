package com.bluetoothchat.www.bluetoothchat.activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bluetoothchat.www.bluetoothchat.R;
import com.bluetoothchat.www.bluetoothchat.SaveDate;
import com.bluetoothchat.www.bluetoothchat.activity.DatePickerActivity;
import com.bluetoothchat.www.bluetoothchat.adapter.QueryAdapter;
import com.bluetoothchat.www.bluetoothchat.bean.Information;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class QueryActivity extends AppCompatActivity {
    private Button mQuery_button;
    private Button mExport_button;
    private Button mAll_Query_button;
    private Button mDateChoose_button;
    private SQLiteDatabase readableDatabase;
    private ListView mQuery;
    private List<Information> mList = new ArrayList();
    private  Calendar mCalendar;
    private  QueryAdapter myAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        mQuery_button = (Button) findViewById(R.id.query_button);
        mExport_button = (Button) findViewById(R.id.export_button);
        mAll_Query_button = (Button) findViewById(R.id.all_query_btn);
        mDateChoose_button = (Button) findViewById(R.id.date_picker);
        mQuery = (ListView) findViewById(R.id.query_list);
        SaveDate db = new SaveDate(this,"message.db",null,1);
        readableDatabase = db.getReadableDatabase();
        myAdapter = new QueryAdapter(this,R.layout.date_cell,mList);
        mQuery.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
        //全部查询
        mAll_Query_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor allQuery = readableDatabase.query("info", null, null, null, null, null, null);
                function_Query(allQuery);
            }
        });
        //选择查询的日期
        mDateChoose_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerActivity dpa = new DatePickerActivity();
                dpa.show(getSupportFragmentManager(),"日期选择");
            }
        });
        //部分查询
        mQuery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取当前时间
                //Date now = new Date();
                Date now = mCalendar.getTime();
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(now);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
              //毫秒可根据系统需要清除或不清除
                cal.set(Calendar.MILLISECOND, 0);
                long startTime = cal.getTimeInMillis();
                long endTime = startTime + 24 * 3600 * 1000;
                Cursor partQuery = readableDatabase.rawQuery("SELECT * FROM info WHERE"+" current>=? and current<?", new String[]{String.valueOf(startTime),String.valueOf(endTime)});
                function_Query(partQuery);
            }
        });

        mExport_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //将查询出来的数据取出来
                ExportToExcel();
            }
        });
    }
    public void ExportToExcel(){
        Cursor cursor = readableDatabase.query("info", null, null, null, null, null, null);
        function_Query(cursor);
        File external = Environment.getExternalStorageDirectory();
        Workbook wb = new HSSFWorkbook();//创建一个工作簿
        Sheet sheet = wb.createSheet("采集数据");//创建一个sheet页
        Row row = sheet.createRow(0);//创建第一行
        row.createCell(0).setCellValue("序号");//设置第一列并命名
        row.createCell(1).setCellValue("数据");
        row.createCell(2).setCellValue("日期");
        if(cursor.getCount()>0){
            for (int i = 0; i < mList.size(); i++) {
                Row row1 = sheet.createRow(i + 1);
                row1.createCell(0).setCellValue(mList.get(i).getId());//将序号写入单元格
                row1.createCell(1).setCellValue(mList.get(i).getContent());//将数据写入单元格
                row1.createCell(2).setCellValue(mList.get(i).getTime());
            }
            File saveFile  = new File(external,"我的导出数据.xls");
            try {
                FileOutputStream fileOut = new FileOutputStream(saveFile);
                try {
                    wb.write(fileOut);
                    fileOut.close();
                    mList.clear();
                    Toast.makeText(this,"导出成功",Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                if(cursor != null){
                    cursor.close();
                }
            }
        }
    }
    public void setCalendar(int year,int month,int day){
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR,year);
        mCalendar.set(Calendar.MONTH,month);
        mCalendar.set(Calendar.DAY_OF_MONTH,day);

        mDateChoose_button.setText(year+"-"+(month+1)+"-"+day);
    }
    public void function_Query(Cursor cursor){
        mList.clear();
        if(cursor.moveToFirst()){
            do{
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                int d = cursor.getInt(cursor.getColumnIndex("_id"));
                Information dataModel = new Information();
                dataModel.setId(d);
                dataModel.setContent(content);
                dataModel.setTime(time);
                mList.add(dataModel);
            }while (cursor.moveToNext());
        }
        myAdapter.notifyDataSetChanged();
        cursor.close();
    }


}

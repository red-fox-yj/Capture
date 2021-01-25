package com.example.dataget;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataLookActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look);

        StringBuffer sb = new StringBuffer();
        File file = new File("/sdcard/DataGet/log.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        while(true){
            try {
                if (!((line = br.readLine())!=null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(line);
            sb.append("\n");
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditText i=(EditText)findViewById(R.id.editText1);
        i.setText(sb.toString());

    }

    /**
     * 清空捕获记录
     */
    public void Delete(View v) throws IOException {
        EditText i=(EditText)findViewById(R.id.editText1);
        i.setText("无内容");
        Toast.makeText(DataLookActivity.this, "已删除", Toast.LENGTH_SHORT).show();
        File file = new File("/sdcard/DataGet/log.txt");
        file.delete();
        File newfile = new File("/sdcard/DataGet/log.txt");
        newfile.createNewFile();
    }

    /**
     * 刷新捕获记录
     */
    public void Fresh(View v) throws IOException {
        StringBuffer sb = new StringBuffer();
        File file = new File("/sdcard/DataGet/log.txt");
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = "";
        while(true){
            try {
                if (!((line = br.readLine())!=null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            sb.append(line);
            sb.append("\n");
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        EditText i=(EditText)findViewById(R.id.editText1);
        i.setText(sb.toString());
        Toast.makeText(DataLookActivity.this, "已刷新", Toast.LENGTH_SHORT).show();
    }

    public void upload(View v){
        Toast.makeText(DataLookActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
    }
}
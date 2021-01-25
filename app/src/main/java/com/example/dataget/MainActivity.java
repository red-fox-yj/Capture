package com.example.dataget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionRequest();
        createSDCardDir();
    }
    /**
     * 获取用户权限
     */
    private void permissionRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE,Manifest.permission.INTERNET};

            List<String> mPermissionList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }

            if (mPermissionList.isEmpty()) {// 全部允许
                System.out.println("Caputre获取到所有权限");
            } else {//存在未允许的权限
                String[] mPermissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(MainActivity.this, mPermissions, 1001);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {//回调检查权限
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1001:
                for (int i = 0; i < grantResults.length; i++) {
//                   如果拒绝获取权限
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean flag= ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permissions[i]);
                        if (flag) {
                            permissionRequest();
                            return;//用户权限是一个一个的请求的，只要有拒绝，剩下的请求就可以停止，再次请求打开权限了
                        } else { // 勾选不再询问，并拒绝
                            //ToastUtils.showToast(SplashActivity.this, "Please go to Settings to get user permissions"));
                            return;
                        }
                    }
                }
                //toMain(); //执行下一步操作
                break;
            default:
                break;
        }
    }
    private void initData() {
        String filePath = "/sdcard/DataGet/";
        String fileName = "log.txt";
        writeTxtToFile("", filePath, fileName);
    }

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath+fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            file.createNewFile();
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }
    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //在SD卡上创建一个文件夹
    public void createSDCardDir(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            // 创建一个文件夹对象，赋值为外部存储器的目录
            File sdcardDir =Environment.getExternalStorageDirectory();
            //得到一个路径，内容是sdcard的文件夹路径和名字
            String path=sdcardDir.getPath()+"/DataGet";
            File path1 = new File(path);
            if (!path1.exists()) {
                //若不存在，创建目录，可以在应用启动的时候创建
                path1.mkdirs();
                initData();
            }
        }
    }

    public void function01(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(MainActivity.this, DataLookActivity.class);
        startActivity(newIntent);//启动Intent对象
    }
    public void function02(View v){//返回桌面
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
        Toast.makeText(MainActivity.this, "开启捕捉", Toast.LENGTH_SHORT).show();
    }
    public void function03(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(MainActivity.this, instructionActivity.class);
        startActivity(newIntent);//启动Intent对象
    }
    public void function04(View v){
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        //intent.setData(Uri.fromParts("package", getContext().getPackageName(), null));
        startActivity(intent);
    }
    public void function05(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(MainActivity.this, UserEnterActivity.class);
        startActivity(newIntent);//启动Intent对象
    }
}

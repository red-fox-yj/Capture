package com.example.dataget;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class UserRegisterActivity extends AppCompatActivity {
    URI uri = URI.create("ws://81.69.7.50:8001/wss");
    private Handler button_Handler;//将mHandler指定轮询的Looper
    private Handler mHandler_m;
    private int num=60;
    private Button send_email;
    private EditText email;
    private EditText username;
    private EditText validation;
    private EditText password;
    private TextView status;
    JWebSocketClient client = new JWebSocketClient(uri) {
        @Override
        public void onMessage(String message) {
            //message就是接收到的消息
            System.out.println("收到的消息为："+message);
            Gson gson = new Gson();
            Map<String, String> map = new HashMap<String, String>();
            map = gson.fromJson(message, map.getClass());
            status.setText(map.get("response"));
            if (map.get("response").equals("注册成功")) {
                final Map<String, String> finalMap = map;
                Thread mythread=new Thread(){
                    @Override
                    public void run() {
                        try {
                            username.setEnabled(false);
                            email.setEnabled(false);
                            //延时两秒之后进行页面跳转
                            Thread.sleep(2000);
                            Intent newIntent = new Intent();//新建一个Intent对象
                            //选择当前Activity和下一个要运行的Activity
                            newIntent.setClass(UserRegisterActivity.this, UserPageActivity.class);
                            newIntent.putExtra("username", "" + username.getText().toString());
                            newIntent.putExtra("email", "" + email.getText().toString());
                            startActivity(newIntent);//启动Intent对象
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                mythread.start();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);
        //开启子线程建立服务器连接
        final Thread connect_thread=new Thread(){
            @Override
            public void run() {
                //建立服务器连接
                try {
                    if(!client.connectBlocking()){
                        status.setText("服务器连接失败");
                    }else {
                        status.setText("服务器连接成功");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        //开启另一个子线程监听服务器超时
        Thread time_thread=new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(client==null||!client.isOpen()){
                    status.setText("服务器连接超时");
                    //杀死连接线程
                    connect_thread.interrupt();
                }
            }
        };
        time_thread.start();
        connect_thread.start();
        //绑定控件
        send_email=(Button) findViewById(R.id.validation);
        email=(EditText)findViewById(R.id.EditText_email);
        username=(EditText)findViewById(R.id.EditText_user_name);
        validation=(EditText)findViewById(R.id.EditText_validation);
        password=(EditText)findViewById(R.id.EditText_password);
        status=(TextView)findViewById(R.id.status);

        mHandler_m = new Handler(){
            public void handleMessage(android.os.Message msg) {
                switch (msg.what){
                    case 1:{
                        status.setText("用户名重复");
                        break;
                    }
                    case 2:{
                        status.setText("邮箱重复");
                        break;
                    }
                    case 3:{
                        status.setText("用户注册中...");
                        //进一步更新ui
                        send_register_request();
                        break;
                    }
                    case 4:{
                        status.setText("验证码发送中...");
                        //进一步更新ui
                        send_validation_request();
                        break;
                    }
                    case 5:{
                        status.setText("数据库查询失败");
                        break;
                    }
                    case 6:{
                        status.setText("数据库连接失败");
                        break;
                    }
                }
            }
        };
        button_Handler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                if(msg.what==2){
                    send_email.setText("重新发送"+num+"s");
                }
                else if (msg.what==1){
                    send_email.setText("发送验证码");
                    send_email.setEnabled(true);
                }
                else if(msg.what==3){
                    //使按钮无效
                    send_email.setEnabled(false);
                }

            };
        };
    }

    /**
     * 发送邮箱验证码响应函数
     */
    public void send_email(View v){
        if (client != null && client.isOpen()) {//服务器连接成功
            if(isEmpty(email)){
                status.setText("邮箱不能为空");
            }else{
                DBisrepeat("email",email.getText().toString());
        }
        }else {
            status.setText("服务器连接失败");
        }
    }

    /**
     * 注册按钮响应函数
     */
    public void register(View v){
        if (client != null && client.isOpen()) {//服务器连接成功
            if (isEmpty(username)) {
                status.setText("用户名不能为空");
            }else if (isEmpty(email)) {
                status.setText("邮箱不能为空");
            }else if(isEmpty(validation)){
                validation.setText("邮箱不能为空");
            }else if(isEmpty(password)){
                password.setText("密码不能为空");
            }else{
                DBisrepeat("username",username.getText().toString());
            }
        }else {
            status.setText("服务器连接失败");
        }
    }

    /**
     * 发送用户注册请求
     */
    public void send_register_request(){
        System.out.println("新用户注册");
        Map<String, Object> map = new HashMap<>();
        map.put("type", "register");
        map.put("email", email.getText().toString());
        map.put("username", username.getText().toString());
        map.put("validation", validation.getText().toString());
        map.put("password", password.getText().toString());
        //map转string
        Gson gson = new Gson();
        String map_string = gson.toJson(map);
        client.send(map_string);
    }

    /**
     * 发送邮箱验证请求
     */
    public void send_validation_request(){
        System.out.println("邮箱验证");
        Map<String,Object> map=new HashMap<>();
        map.put("type","validation");
        //使按钮无效
        button_Handler.sendEmptyMessage(3);
        //开启子线程倒计时
        Thread mythread=new Thread(){
            @Override
            public void run() {
                while (num>0){
                    try {
                        Thread.sleep(1000);

                        map.put("email",email.getText().toString());
        //map转string
        Gson gson = new Gson();
        String map_string = gson.toJson(map);
        client.send(map_string);     } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                                    num=num-1;
                    //按钮文本倒计时
                    button_Handler.sendEmptyMessage(2);
                }
                //使按钮有效
                button_Handler.sendEmptyMessage(1);
                num=60;
            }
        };
        mythread.start();
    }

    /**
     * 判断输入框是否为空
     */
    public boolean isEmpty(EditText EditText_input){
        return EditText_input.getText().toString().equals("");
    }

    /**
     * 连接数据库判断注册信息是否重复
     */
    public void DBisrepeat(final String list_name, final String value) {
        Thread mythread=new Thread(){
            @Override
            public void run() {
                //连接数据库
                try {
                    Class.forName("com.mysql.jdbc.Driver");//动态加载类
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Connection connection=null;
                try {
                    connection = (Connection) DriverManager.getConnection("jdbc:mysql://cdb-g76irqy0.cd.tencentcdb.com:10186/mysql", "root", "red-fox-yj2020");
                } catch (SQLException e) {
                    System.out.println("数据库连接失败");
                    System.out.println(e.toString());
                    e.printStackTrace();
                }
                Message msg = Message.obtain();
                msg.obj = list_name;
                if(connection!=null){
                    System.out.println("数据库连接成功");
                    Statement stmt = null; //根据返回的Connection对象创建 Statement对象
                    try {
                        stmt = connection.createStatement();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    String username_sql = "SELECT * FROM Caputure_user WHERE " + list_name + "=" + "'"+value+"'"; //要执行的sql语句
                    try {
                        ResultSet rs = stmt.executeQuery(username_sql); //使用executeQury方法执行sql语句 返回ResultSet对象 即查询的结果
                        if(rs.next()){
                            //用户名或者邮箱已存在
                            if(list_name.equals("username")) {
                                msg.what = 1;
                            }
                            else if(list_name.equals("email")){
                                msg.what = 2;
                            }
                            mHandler_m.sendMessage(msg);
                        }
                        else {
                            //用户名或者邮箱未重复
                            if(list_name.equals("username")) {
                                //用户注册操作
                                msg.what = 3;
                            }
                            else if(list_name.equals("email")){
                                //发送验证码操作
                                msg.what = 4;
                            }
                            mHandler_m.sendMessage(msg);
                        }
                        rs.close();
                    } catch (SQLException e) {
                        System.out.println("数据库查询失败：" + e.toString());
                        msg.what = 5;
                        mHandler_m.sendMessage(msg);
                        e.printStackTrace();
                    }
                    try {//释放链接
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else{
                    msg.what = 6;//数据库连接失败
                    mHandler_m.sendMessage(msg);
                }
            }
        };
        mythread.start();
    }
}


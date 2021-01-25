package com.example.dataget;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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

public class UserEnterActivity extends AppCompatActivity {
    public EditText EditText_username;
    public EditText EditText_password;
    public TextView status;
    public Handler mHandler_m;
    URI uri = URI.create("ws://10.132.5.174:8000/wss");
    JWebSocketClient client = new JWebSocketClient(uri) {
        @Override
        public void onMessage(String message) {
            //message就是接收到的消息
            System.out.println("收到的消息为："+message);
            Gson gson = new Gson();
            Map<String, String> map = new HashMap<String, String>();
            map = gson.fromJson(message, map.getClass());
//            Toast.makeText(UserEnterActivity.this, map.get("response"), Toast.LENGTH_SHORT).show();
            if (map.get("response").equals("enter successed!")){
                //跳转到用户界面
                Intent newIntent = new Intent();//新建一个Intent对象
                //选择当前Activity和下一个要运行的Activity
                newIntent.setClass(UserEnterActivity.this, UserPageActivity.class);
                newIntent.putExtra("username", ""+map.get("username"));
                newIntent.putExtra("email", ""+map.get("email"));
                startActivity(newIntent);//启动Intent对象
            }
            System.out.println(map.get("response"));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_enter);
        //绑定控件
        EditText_username=(EditText)findViewById(R.id.EditText_user_name);
        EditText_password=(EditText)findViewById(R.id.EditText_password);
        status=(TextView) findViewById(R.id.status);

        mHandler_m = new Handler(){
            public void handleMessage(android.os.Message msg) {
                switch (msg.what){
                    case 1:{
                        //密码正确，跳转到用户界面
                        Intent newIntent = new Intent();//新建一个Intent对象
                        //选择当前Activity和下一个要运行的Activity
                        newIntent.setClass(UserEnterActivity.this, UserPageActivity.class);
                        newIntent.putExtra("username", EditText_username.getText().toString());
                        newIntent.putExtra("email", ""+msg.obj);
                        startActivity(newIntent);//启动Intent对象
                        break;
                    }
                    case 2:{
                        //用户名不存在
                        status.setText("用户名不存在");
                        break;
                    }
                    case 3:{
                        //数据库查询失败
                        status.setText("数据库查询失败");
                        break;
                    }
                    case 4:{
                        //数据库连接失败
                        status.setText("数据库连接失败");
                        break;
                    }
                    case 5:{
                        //密码错误
                        status.setText("密码错误");
                        break;
                    }
                }

            };
        };
    }

    /**
     * 判断输入框是否为空
     */
    public boolean isEmpty(EditText EditText_input){
        return EditText_input.getText().toString().equals("");
    }

    /**
     * 判断输入的用户名是否存在
     */
    public void isUserNameExist(){
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
                if(connection!=null){
                    System.out.println("数据库连接成功");
                    Statement stmt = null; //根据返回的Connection对象创建 Statement对象
                    try {
                        stmt = connection.createStatement();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    String username_sql = "SELECT password,email FROM Caputure_user WHERE username=" + "'"+EditText_username.getText().toString()+"'"; //要执行的sql语句
                    try {
                        ResultSet rs = stmt.executeQuery(username_sql); //使用executeQury方法执行sql语句 返回ResultSet对象 即查询的结果

                        if(rs.next()){
                            //用户名存在，检查密码是否正确
                            if(rs.getString("password").equals(EditText_password.getText().toString())){
                                //密码正确，进行跳转
                                msg.what = 1;
                                msg.obj=rs.getString("email");
                                mHandler_m.sendMessage(msg);
                            }
                            else {
                                //密码错误，更新ui
                                msg.what = 5;
                                mHandler_m.sendMessage(msg);
                            }
                        }
                        else {
                            //用户名不存在，更新ui
                            msg.what = 2;
                            mHandler_m.sendMessage(msg);
                        }
                        rs.close();
                    } catch (SQLException e) {
                        //数据库查询失败，更新ui
                        msg.what = 3;
                        System.out.println(e.toString());
                        mHandler_m.sendMessage(msg);
                        e.printStackTrace();
                    }
                    try {
                        //释放链接
                        connection.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }else{
                    //数据库连接失败，更新ui
                    msg.what = 4;
                    mHandler_m.sendMessage(msg);
                }
            }
        };
        mythread.start();
    }

    /**
     * 注册按钮响应函数
     */
    public void register(View v){
        Intent newIntent = new Intent();//新建一个Intent对象
        //选择当前Activity和下一个要运行的Activity
        newIntent.setClass(UserEnterActivity.this, UserRegisterActivity.class);
        startActivity(newIntent);//启动Intent对象
    }

    /**
     * 登录按钮响应函数
     */
    public void enter(View v){
        if(isEmpty(EditText_username)){
            status.setText("用户名不能为空");
        }else if(isEmpty(EditText_password)){
            status.setText("密码不能为空");
        }else {
            isUserNameExist();
        }
    }
}


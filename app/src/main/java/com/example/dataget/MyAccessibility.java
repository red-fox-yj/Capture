package com.example.dataget;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class MyAccessibility extends AccessibilityService {
    private static final String TAG = "MyAccessibility";
    boolean matching = false;//保证物品名称和价格配套出现
    String data = "";//打印输出
    String price_now = "";//保存现价
    public List<String> event_list = new ArrayList<String>();

    @Override
    protected void onServiceConnected() {
    }

    @SuppressLint("NewApi")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // TODO Auto-generated method stub
        String packageName = event.getPackageName().toString();
        String className = event.getClassName().toString();
        AccessibilityNodeInfo rowNode = getRootInActiveWindow();
        if (rowNode == null) {
            System.out.println("noteInfo is　null");
            return;
        } else {
            if(packageName.equals("com.taobao.taobao")){
                System.out.println("》》**AccessibilityEvent REPORT** 当前页面class为："+className+",当前页面孩子个数为："+rowNode.getChildCount()+",根节点类型:"+rowNode.getClassName());
                if(className.equals("com.taobao.android.detail.wrapper.activity.DetailActivity")||!event_list.isEmpty()) {
                    System.out.println("找到目标页面");
                    event_list.add(className);
                    System.out.println("《《事件队列："+event_list);
                    if(event_list.size()>4){
                        recycle(rowNode, rowNode, "");
                        event_list.clear();
                    }
                }
            }
            else{
                System.out.println("等待进入淘宝");
            }
        }
    }

    public void recycle(AccessibilityNodeInfo info, AccessibilityNodeInfo Rootinfo, String msg) {
        if (info.getChildCount() == 0) {//遍历到叶子节点
            if (info.getText() != null) {
                if (info.getText().toString().equals("￥")) {//找到价格
                    if (!matching) {//第一次索引到价格
                        matching = true;//保证价格名称配套出现
                        System.out.println("访问编号1.0:" + msg);
                        char[] stringArr = msg.toCharArray();
                        stringArr[stringArr.length - 1] = (char) ((Integer.parseInt(String.valueOf(stringArr[stringArr.length - 1])) + 1) + '0');//向后移一位
                        System.out.println("访问编号1.1:" + String.valueOf(stringArr));
                        price_now = getDate(Rootinfo, String.valueOf(stringArr));
                        data = "\n商品价格：￥" + price_now + ";";
                    }
                }
                if (info.isLongClickable() && matching) {//找到物品名称
                    System.out.println("访问编号2.0:" + msg);
                    //生成json对象
                    JSONArray array = new JSONArray();
                    JSONObject object = new JSONObject();
                    JSONObject obj = new JSONObject();
                    try {
                        object.put("price_nex", price_now);//现价

                        data = data + "\n商品名称：" + info.getText().toString() + ";";
                        object.put("name", info.getText().toString());

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日HH时mm分ss秒");// HH:mm:ss
                        Date date = new Date(System.currentTimeMillis());
                        data = data + "\n捕获时间:" + simpleDateFormat.format(date) + ";";
                        object.put("time", simpleDateFormat.format(date));

                        String m_szAndroidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        data = data + "\n设备名：" + m_szAndroidID;
                        object.put("equipment", m_szAndroidID);

                        array.put(object);
                        obj.put("Capture", array);//需要上传的json对象
                    } catch (Exception e) {
                        System.out.println("出错了" + e.getMessage());
                    }
                    System.out.println(obj.toString());
                    writeTxtToFile(data, "/sdcard/DataGet/", "log.txt");
                    matching = false;
                    price_now = "";
                }
            } else {
                System.out.println("无意义控件");
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    String a = msg + i;
                    recycle(info.getChild(i), Rootinfo, a);
                }
            }
        }
    }

    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
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


    public String getDate(AccessibilityNodeInfo info, String msg) {//根据字符串进行索引
        for (int i = 0; i < msg.length(); i++) {
            info = info.getChild(Integer.parseInt(String.valueOf(msg.charAt(i))));
        }
//        System.out.println("Text：" + info.getText());
//        System.out.println("windowId:" + info.getWindowId());
//        System.out.println("isLongClickable:" + info.isLongClickable());
//        System.out.println("getClassName:" + info.getClassName());
//        System.out.println("getClassName:" + info.getClassName());
//        System.out.println("getViewIdResourceName:" + info.getViewIdResourceName());
//        System.out.println("getContentDescription:" + info.getContentDescription());
        return info.getText().toString();
    }


    @Override
    public void onInterrupt() {
        // TODO Auto-generated method stub

    }
}
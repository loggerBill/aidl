package com.zhongmin.aidl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * MainActivity - Java实现
 * 演示跨进程通信（仿AIDL方式）
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private IMyAidlInterface mService;
    private boolean mBound = false;
    
    private TextView tvStatus;
    private TextView tvLog;
    private Button btnBind;
    private Button btnUnbind;
    private Button btnGetPid;
    private Button btnAdd;
    private Button btnGetServiceName;
    private Button btnBasicTypes;
    
    private StringBuilder logBuilder = new StringBuilder();
    
    /**
     * ServiceConnection回调
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: " + name);
            mService = MyAidlStub.asInterface(service);
            mBound = true;
            updateStatus("服务已连接");
            appendLog("✓ 服务连接成功");
            enableButtons(true);
            Toast.makeText(MainActivity.this, "服务绑定成功", Toast.LENGTH_SHORT).show();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: " + name);
            mService = null;
            mBound = false;
            updateStatus("服务已断开");
            appendLog("✗ 服务连接断开");
            enableButtons(false);
            Toast.makeText(MainActivity.this, "服务连接断开", Toast.LENGTH_SHORT).show();
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 创建UI
        createUI();
        
        appendLog("MainActivity 创建完成");
        appendLog("当前进程 PID: " + Process.myPid());
        updateStatus("未连接");
    }
    
    /**
     * 创建UI界面
     */
    private void createUI() {
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(32, 32, 32, 32);
        
        // 标题
        TextView tvTitle = new TextView(this);
        tvTitle.setText("AIDL 跨进程通信示例");
        tvTitle.setTextSize(24);
        tvTitle.setPadding(0, 0, 0, 32);
        mainLayout.addView(tvTitle);
        
        // 状态显示
        tvStatus = new TextView(this);
        tvStatus.setText("状态: 未连接");
        tvStatus.setTextSize(16);
        tvStatus.setPadding(16, 16, 16, 16);
        tvStatus.setBackgroundColor(0xFFEEEEEE);
        mainLayout.addView(tvStatus);
        
        // 添加间距
        addSpace(mainLayout, 16);
        
        // 绑定服务按钮
        btnBind = new Button(this);
        btnBind.setText("绑定服务");
        btnBind.setOnClickListener(v -> bindService());
        mainLayout.addView(btnBind);
        
        // 解绑服务按钮
        btnUnbind = new Button(this);
        btnUnbind.setText("解绑服务");
        btnUnbind.setEnabled(false);
        btnUnbind.setOnClickListener(v -> unbindService());
        mainLayout.addView(btnUnbind);
        
        addSpace(mainLayout, 16);
        
        // 获取PID按钮
        btnGetPid = new Button(this);
        btnGetPid.setText("获取服务进程PID");
        btnGetPid.setEnabled(false);
        btnGetPid.setOnClickListener(v -> getPid());
        mainLayout.addView(btnGetPid);
        
        // 加法测试按钮
        btnAdd = new Button(this);
        btnAdd.setText("测试加法 (5 + 3)");
        btnAdd.setEnabled(false);
        btnAdd.setOnClickListener(v -> testAdd());
        mainLayout.addView(btnAdd);
        
        // 获取服务名称按钮
        btnGetServiceName = new Button(this);
        btnGetServiceName.setText("获取服务名称");
        btnGetServiceName.setEnabled(false);
        btnGetServiceName.setOnClickListener(v -> getServiceName());
        mainLayout.addView(btnGetServiceName);
        
        // 基本类型测试按钮
        btnBasicTypes = new Button(this);
        btnBasicTypes.setText("测试基本类型传递");
        btnBasicTypes.setEnabled(false);
        btnBasicTypes.setOnClickListener(v -> testBasicTypes());
        mainLayout.addView(btnBasicTypes);
        
        addSpace(mainLayout, 16);
        
        // 日志标题
        TextView tvLogTitle = new TextView(this);
        tvLogTitle.setText("通信日志:");
        tvLogTitle.setTextSize(16);
        mainLayout.addView(tvLogTitle);
        
        // 日志显示区域
        tvLog = new TextView(this);
        tvLog.setTextSize(12);
        tvLog.setPadding(16, 16, 16, 16);
        tvLog.setBackgroundColor(0xFF000000);
        tvLog.setTextColor(0xFF00FF00);
        
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(tvLog);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                0, 
                1.0f
        );
        scrollView.setLayoutParams(scrollParams);
        mainLayout.addView(scrollView);
        
        setContentView(mainLayout);
    }
    
    /**
     * 添加间距
     */
    private void addSpace(LinearLayout layout, int dp) {
        View space = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                (int) (dp * getResources().getDisplayMetrics().density)
        );
        space.setLayoutParams(params);
        layout.addView(space);
    }
    
    /**
     * 绑定服务
     */
    private void bindService() {
        if (!mBound) {
            Intent intent = new Intent(this, RemoteService.class);
            boolean success = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            if (success) {
                appendLog("→ 正在绑定服务...");
                btnBind.setEnabled(false);
                btnUnbind.setEnabled(true);
            } else {
                appendLog("✗ 绑定服务失败");
                Toast.makeText(this, "绑定服务失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * 解绑服务
     */
    private void unbindService() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
            mService = null;
            updateStatus("未连接");
            appendLog("← 服务已解绑");
            btnBind.setEnabled(true);
            btnUnbind.setEnabled(false);
            enableButtons(false);
            Toast.makeText(this, "服务已解绑", Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * 获取服务进程PID
     */
    private void getPid() {
        if (mService != null) {
            try {
                int pid = mService.getPid();
                int myPid = Process.myPid();
                appendLog("⚡ getPid() 调用成功");
                appendLog("  客户端 PID: " + myPid);
                appendLog("  服务端 PID: " + pid);
                if (pid != myPid) {
                    appendLog("  ✓ 跨进程通信成功！");
                } else {
                    appendLog("  ⚠ 同进程通信");
                }
                Toast.makeText(this, "服务PID: " + pid, Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                appendLog("✗ getPid() 调用失败: " + e.getMessage());
                Log.e(TAG, "Error calling getPid", e);
            }
        }
    }
    
    /**
     * 测试加法
     */
    private void testAdd() {
        if (mService != null) {
            try {
                int a = 5, b = 3;
                int result = mService.add(a, b);
                appendLog("⚡ add(" + a + ", " + b + ") = " + result);
                Toast.makeText(this, "结果: " + result, Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                appendLog("✗ add() 调用失败: " + e.getMessage());
                Log.e(TAG, "Error calling add", e);
            }
        }
    }
    
    /**
     * 获取服务名称
     */
    private void getServiceName() {
        if (mService != null) {
            try {
                String name = mService.getServiceName();
                appendLog("⚡ getServiceName() = " + name);
                Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                appendLog("✗ getServiceName() 调用失败: " + e.getMessage());
                Log.e(TAG, "Error calling getServiceName", e);
            }
        }
    }
    
    /**
     * 测试基本类型传递
     */
    private void testBasicTypes() {
        if (mService != null) {
            try {
                mService.basicTypes(100, 200L, true, 3.14f, 2.71828, "Hello AIDL");
                appendLog("⚡ basicTypes() 调用成功");
                appendLog("  参数: int=100, long=200, boolean=true");
                appendLog("  float=3.14, double=2.71828, String=\"Hello AIDL\"");
                Toast.makeText(this, "基本类型传递成功", Toast.LENGTH_SHORT).show();
            } catch (RemoteException e) {
                appendLog("✗ basicTypes() 调用失败: " + e.getMessage());
                Log.e(TAG, "Error calling basicTypes", e);
            }
        }
    }
    
    /**
     * 更新状态显示
     */
    private void updateStatus(String status) {
        tvStatus.setText("状态: " + status);
    }
    
    /**
     * 添加日志
     */
    private void appendLog(String message) {
        logBuilder.append(message).append("\n");
        tvLog.setText(logBuilder.toString());
        Log.d(TAG, message);
    }
    
    /**
     * 启用/禁用功能按钮
     */
    private void enableButtons(boolean enabled) {
        btnGetPid.setEnabled(enabled);
        btnAdd.setEnabled(enabled);
        btnGetServiceName.setEnabled(enabled);
        btnBasicTypes.setEnabled(enabled);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }
}

package com.zhongmin.aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

/**
 * 后台服务进程
 */
public class RemoteService extends Service {
    private static final String TAG = "RemoteService";
    
    /**
     * Binder实现
     */
    private final MyAidlStub mBinder = new MyAidlStub() {
        @Override
        public int getPid() throws RemoteException {
            Log.d(TAG, "getPid called, returning: " + Process.myPid());
            return Process.myPid();
        }
        
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                               double aDouble, String aString) throws RemoteException {
            Log.d(TAG, "basicTypes called with params: " +
                    "int=" + anInt +
                    ", long=" + aLong +
                    ", boolean=" + aBoolean +
                    ", float=" + aFloat +
                    ", double=" + aDouble +
                    ", String=" + aString);
        }
        
        @Override
        public int add(int a, int b) throws RemoteException {
            int result = a + b;
            Log.d(TAG, "add called: " + a + " + " + b + " = " + result);
            return result;
        }
        
        @Override
        public String getServiceName() throws RemoteException {
            String name = "RemoteService (PID: " + Process.myPid() + ")";
            Log.d(TAG, "getServiceName called, returning: " + name);
            return name;
        }
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "RemoteService onCreate, PID: " + Process.myPid());
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "RemoteService onBind");
        return mBinder;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "RemoteService onUnbind");
        return super.onUnbind(intent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "RemoteService onDestroy");
    }
}

package com.zhongmin.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

/**
 * 仿AIDL接口定义
 * 定义跨进程通信的接口契约
 */
public interface IMyAidlInterface extends IInterface {
    
    /**
     * 获取进程ID
     */
    int getPid() throws RemoteException;
    
    /**
     * 基本类型方法
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                    double aDouble, String aString) throws RemoteException;
    
    /**
     * 加法运算
     */
    int add(int a, int b) throws RemoteException;
    
    /**
     * 获取服务名称
     */
    String getServiceName() throws RemoteException;
    
    /**
     * Binder描述符 - 用于验证接口一致性
     */
    String DESCRIPTOR = "com.zhongmin.aidl.IMyAidlInterface";
    
    /**
     * 事务码 - 每个方法对应一个唯一的事务码
     */
    int TRANSACTION_getPid = IBinder.FIRST_CALL_TRANSACTION + 0;
    int TRANSACTION_basicTypes = IBinder.FIRST_CALL_TRANSACTION + 1;
    int TRANSACTION_add = IBinder.FIRST_CALL_TRANSACTION + 2;
    int TRANSACTION_getServiceName = IBinder.FIRST_CALL_TRANSACTION + 3;
}

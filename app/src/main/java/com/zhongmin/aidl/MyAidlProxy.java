package com.zhongmin.aidl;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Proxy类 - 客户端代理
 * 客户端通过此代理调用远程服务
 */
public class MyAidlProxy implements IMyAidlInterface {
    
    private IBinder mRemote;
    
    /**
     * 构造函数
     * 
     * @param remote 远程Binder对象
     */
    public MyAidlProxy(IBinder remote) {
        mRemote = remote;
    }
    
    @Override
    public IBinder asBinder() {
        return mRemote;
    }
    
    /**
     * 获取接口描述符
     */
    public String getInterfaceDescriptor() {
        return DESCRIPTOR;
    }
    
    /**
     * 获取进程ID
     */
    @Override
    public int getPid() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result;
        
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            // 发起跨进程调用
            mRemote.transact(TRANSACTION_getPid, data, reply, 0);
            reply.readException();
            result = reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
        
        return result;
    }
    
    /**
     * 基本类型传递测试
     */
    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                           double aDouble, String aString) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            // 序列化参数
            data.writeInt(anInt);
            data.writeLong(aLong);
            data.writeInt(aBoolean ? 1 : 0);
            data.writeFloat(aFloat);
            data.writeDouble(aDouble);
            data.writeString(aString);
            // 发起跨进程调用
            mRemote.transact(TRANSACTION_basicTypes, data, reply, 0);
            reply.readException();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
    
    /**
     * 加法运算
     */
    @Override
    public int add(int a, int b) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        int result;
        
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            // 序列化参数
            data.writeInt(a);
            data.writeInt(b);
            // 发起跨进程调用
            mRemote.transact(TRANSACTION_add, data, reply, 0);
            reply.readException();
            // 反序列化返回值
            result = reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
        
        return result;
    }
    
    /**
     * 获取服务名称
     */
    @Override
    public String getServiceName() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        String result;
        
        try {
            data.writeInterfaceToken(DESCRIPTOR);
            // 发起跨进程调用
            mRemote.transact(TRANSACTION_getServiceName, data, reply, 0);
            reply.readException();
            // 反序列化返回值
            result = reply.readString();
        } finally {
            reply.recycle();
            data.recycle();
        }
        
        return result;
    }
}

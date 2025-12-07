package com.zhongmin.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * Stub类 - 服务端基类
 * 服务端需要继承此类并实现接口方法
 */
public abstract class MyAidlStub extends Binder implements IMyAidlInterface {
    
    /**
     * 构造函数 - 注册接口描述符
     */
    public MyAidlStub() {
        this.attachInterface(this, DESCRIPTOR);
    }
    
    /**
     * 将IBinder转换为IMyAidlInterface接口
     * 如果是同进程调用，直接返回本地对象
     * 如果是跨进程调用，返回代理对象
     * 
     * @param obj IBinder对象
     * @return IMyAidlInterface接口实例
     */
    public static IMyAidlInterface asInterface(IBinder obj) {
        if (obj == null) {
            return null;
        }
        
        // 查询本地接口
        IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if (iin != null && iin instanceof IMyAidlInterface) {
            // 同进程，直接返回
            return (IMyAidlInterface) iin;
        }
        
        // 跨进程，返回代理对象
        return new MyAidlProxy(obj);
    }
    
    @Override
    public IBinder asBinder() {
        return this;
    }
    
    /**
     * 处理客户端请求
     * 运行在Binder线程池中
     * 
     * @param code 事务码，标识要调用的方法
     * @param data 客户端传来的参数
     * @param reply 返回给客户端的数据
     * @param flags 调用标志
     * @return 是否处理成功
     */
    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION:
                // 返回接口描述符
                reply.writeString(DESCRIPTOR);
                return true;
                
            case TRANSACTION_getPid:
                // 处理getPid()方法调用
                data.enforceInterface(DESCRIPTOR);
                int pidResult = this.getPid();
                reply.writeNoException();
                reply.writeInt(pidResult);
                return true;
                
            case TRANSACTION_basicTypes:
                // 处理basicTypes()方法调用
                data.enforceInterface(DESCRIPTOR);
                int anInt = data.readInt();
                long aLong = data.readLong();
                boolean aBoolean = data.readInt() != 0;
                float aFloat = data.readFloat();
                double aDouble = data.readDouble();
                String aString = data.readString();
                this.basicTypes(anInt, aLong, aBoolean, aFloat, aDouble, aString);
                reply.writeNoException();
                return true;
                
            case TRANSACTION_add:
                // 处理add()方法调用
                data.enforceInterface(DESCRIPTOR);
                int arg0 = data.readInt();
                int arg1 = data.readInt();
                int addResult = this.add(arg0, arg1);
                reply.writeNoException();
                reply.writeInt(addResult);
                return true;
                
            case TRANSACTION_getServiceName:
                // 处理getServiceName()方法调用
                data.enforceInterface(DESCRIPTOR);
                String serviceName = this.getServiceName();
                reply.writeNoException();
                reply.writeString(serviceName);
                return true;
        }
        
        return super.onTransact(code, data, reply, flags);
    }
}

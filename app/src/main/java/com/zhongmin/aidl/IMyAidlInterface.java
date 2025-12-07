package com.zhongmin.aidl;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/**
 * 仿AIDL接口定义
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
     * Binder描述符
     */
    String DESCRIPTOR = "com.zhongmin.aidl.IMyAidlInterface";
    
    /**
     * 事务码
     */
    int TRANSACTION_getPid = IBinder.FIRST_CALL_TRANSACTION + 0;
    int TRANSACTION_basicTypes = IBinder.FIRST_CALL_TRANSACTION + 1;
    int TRANSACTION_add = IBinder.FIRST_CALL_TRANSACTION + 2;
    int TRANSACTION_getServiceName = IBinder.FIRST_CALL_TRANSACTION + 3;
    
    /**
     * 抽象的Stub类（服务端实现）
     */
    abstract class Stub extends android.os.Binder implements IMyAidlInterface {
        
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }
        
        /**
         * 将IBinder转换为IMyAidlInterface
         */
        public static IMyAidlInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin != null && iin instanceof IMyAidlInterface) {
                return (IMyAidlInterface) iin;
            }
            return new Stub.Proxy(obj);
        }
        
        @Override
        public IBinder asBinder() {
            return this;
        }
        
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION:
                    reply.writeString(DESCRIPTOR);
                    return true;
                    
                case TRANSACTION_getPid:
                    data.enforceInterface(DESCRIPTOR);
                    int result = this.getPid();
                    reply.writeNoException();
                    reply.writeInt(result);
                    return true;
                    
                case TRANSACTION_basicTypes:
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
                    data.enforceInterface(DESCRIPTOR);
                    int arg0 = data.readInt();
                    int arg1 = data.readInt();
                    int addResult = this.add(arg0, arg1);
                    reply.writeNoException();
                    reply.writeInt(addResult);
                    return true;
                    
                case TRANSACTION_getServiceName:
                    data.enforceInterface(DESCRIPTOR);
                    String serviceName = this.getServiceName();
                    reply.writeNoException();
                    reply.writeString(serviceName);
                    return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
        
        /**
         * 代理类（客户端使用）
         */
        private static class Proxy implements IMyAidlInterface {
            private IBinder mRemote;
            
            Proxy(IBinder remote) {
                mRemote = remote;
            }
            
            @Override
            public IBinder asBinder() {
                return mRemote;
            }
            
            public String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }
            
            @Override
            public int getPid() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                int result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(TRANSACTION_getPid, data, reply, 0);
                    reply.readException();
                    result = reply.readInt();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }
            
            @Override
            public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
                                   double aDouble, String aString) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeInt(anInt);
                    data.writeLong(aLong);
                    data.writeInt(aBoolean ? 1 : 0);
                    data.writeFloat(aFloat);
                    data.writeDouble(aDouble);
                    data.writeString(aString);
                    mRemote.transact(TRANSACTION_basicTypes, data, reply, 0);
                    reply.readException();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
            
            @Override
            public int add(int a, int b) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                int result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeInt(a);
                    data.writeInt(b);
                    mRemote.transact(TRANSACTION_add, data, reply, 0);
                    reply.readException();
                    result = reply.readInt();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }
            
            @Override
            public String getServiceName() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                String result;
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(TRANSACTION_getServiceName, data, reply, 0);
                    reply.readException();
                    result = reply.readString();
                } finally {
                    reply.recycle();
                    data.recycle();
                }
                return result;
            }
        }
    }
}

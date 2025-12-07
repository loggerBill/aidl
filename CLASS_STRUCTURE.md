# 类结构说明

## 文件列表

本项目包含以下核心Java类文件：

```
app/src/main/java/com/zhongmin/aidl/
├── IMyAidlInterface.java    (接口定义)
├── MyAidlStub.java          (Binder服务端基类)
├── MyAidlProxy.java         (Binder客户端代理)
├── RemoteService.java       (后台服务实现)
└── MainActivity.java        (客户端主界面)
```

## 类关系图

```
┌─────────────────────────────────────────────────────────────┐
│                     IInterface (Android)                     │
│                     (系统接口)                                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     │ extends
                     ▼
        ┌────────────────────────┐
        │  IMyAidlInterface      │
        │  (业务接口定义)         │
        │                        │
        │  + getPid()            │
        │  + add(int, int)       │
        │  + getServiceName()    │
        │  + basicTypes(...)     │
        └────┬────────────────┬──┘
             │                │
    ┌────────▼─────┐    ┌────▼──────────┐
    │ implements   │    │  implements   │
    │              │    │               │
┌───┴─────────┐  ┌─┴────────────────┐  │
│   Binder    │  │  MyAidlProxy     │◄─┘
│  (Android)  │  │  (客户端代理)     │
└───┬─────────┘  └──────────────────┘
    │ extends
    ▼
┌────────────────┐
│  MyAidlStub    │
│  (服务端基类)   │
└────┬───────────┘
     │ extends
     ▼
┌─────────────────┐
│ RemoteService   │
│ (服务端实现)     │
└─────────────────┘
```

## 详细说明

### 1. IMyAidlInterface.java

**类型**: 接口（Interface）

**作用**: 定义跨进程通信的契约

**继承关系**:
```java
public interface IMyAidlInterface extends IInterface
```

**主要内容**:
- 业务方法声明（getPid、add、getServiceName、basicTypes）
- 接口描述符（DESCRIPTOR）
- 事务码常量（TRANSACTION_*）

**代码行数**: 约45行

**依赖**:
- `android.os.IInterface`
- `android.os.IBinder`
- `android.os.RemoteException`

---

### 2. MyAidlStub.java

**类型**: 抽象类（Abstract Class）

**作用**: Binder服务端基类，处理客户端请求

**继承关系**:
```java
public abstract class MyAidlStub extends Binder implements IMyAidlInterface
```

**主要方法**:

#### asInterface(IBinder obj)
- **作用**: 将IBinder对象转换为IMyAidlInterface接口
- **返回**: 同进程返回本地对象，跨进程返回MyAidlProxy代理
- **关键代码**:
```java
IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (iin != null && iin instanceof IMyAidlInterface) {
    return (IMyAidlInterface) iin;  // 同进程
}
return new MyAidlProxy(obj);  // 跨进程
```

#### onTransact(int code, Parcel data, Parcel reply, int flags)
- **作用**: 接收并处理客户端的远程调用
- **运行线程**: Binder线程池
- **流程**:
  1. 根据事务码（code）识别方法
  2. 从data中读取参数
  3. 调用对应的抽象方法
  4. 将结果写入reply

**代码行数**: 约112行

**依赖**:
- `android.os.Binder`
- `android.os.Parcel`
- `IMyAidlInterface`
- `MyAidlProxy`

---

### 3. MyAidlProxy.java

**类型**: 普通类（Class）

**作用**: Binder客户端代理，封装远程调用

**继承关系**:
```java
public class MyAidlProxy implements IMyAidlInterface
```

**成员变量**:
```java
private IBinder mRemote;  // 远程Binder对象
```

**方法实现模式**:

每个接口方法都遵循相同的模式：

```java
@Override
public int add(int a, int b) throws RemoteException {
    Parcel data = Parcel.obtain();    // 1. 获取数据包
    Parcel reply = Parcel.obtain();   // 2. 获取回复包
    int result;
    
    try {
        data.writeInterfaceToken(DESCRIPTOR);  // 3. 写入接口标识
        data.writeInt(a);                      // 4. 序列化参数
        data.writeInt(b);
        
        // 5. 发起跨进程调用（阻塞）
        mRemote.transact(TRANSACTION_add, data, reply, 0);
        
        reply.readException();     // 6. 检查异常
        result = reply.readInt();  // 7. 反序列化返回值
    } finally {
        reply.recycle();  // 8. 回收资源
        data.recycle();
    }
    
    return result;
}
```

**代码行数**: 约137行

**依赖**:
- `android.os.IBinder`
- `android.os.Parcel`
- `IMyAidlInterface`

---

### 4. RemoteService.java

**类型**: 服务类（Service）

**作用**: 后台服务，实现具体业务逻辑

**继承关系**:
```java
public class RemoteService extends Service
```

**核心成员**:
```java
private final MyAidlStub mBinder = new MyAidlStub() {
    @Override
    public int getPid() { ... }
    
    @Override
    public int add(int a, int b) { ... }
    
    @Override
    public String getServiceName() { ... }
    
    @Override
    public void basicTypes(...) { ... }
};
```

**生命周期**:
- `onCreate()`: 服务创建
- `onBind(Intent)`: 返回mBinder对象
- `onUnbind(Intent)`: 客户端解绑
- `onDestroy()`: 服务销毁

**运行进程**: `:remote`（独立进程）

**代码行数**: 约77行

**依赖**:
- `android.app.Service`
- `android.os.Process`
- `MyAidlStub`

---

### 5. MainActivity.java

**类型**: 活动类（Activity）

**作用**: 客户端界面，绑定服务并调用远程方法

**继承关系**:
```java
public class MainActivity extends AppCompatActivity
```

**核心成员**:
```java
private IMyAidlInterface mService;  // 远程服务接口
private boolean mBound = false;     // 绑定状态
```

**ServiceConnection实现**:
```java
private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = MyAidlStub.asInterface(service);  // 关键！
        mBound = true;
    }
    
    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
        mBound = false;
    }
};
```

**主要功能**:
- 绑定/解绑远程服务
- 调用远程方法（getPid、add、getServiceName、basicTypes）
- 显示通信日志
- UI交互

**代码行数**: 约336行

**依赖**:
- `androidx.appcompat.app.AppCompatActivity`
- `android.content.ServiceConnection`
- `IMyAidlInterface`
- `MyAidlStub`
- `RemoteService`

---

## 类之间的调用关系

### 服务绑定流程

```
MainActivity                    RemoteService
    │                               │
    │ 1. bindService()              │
    ├──────────────────────────────>│
    │                               │ onCreate()
    │                               │ onBind()
    │                               │   return mBinder
    │<──────────────────────────────┤
    │ 2. onServiceConnected()       │
    │    IBinder service            │
    │                               │
    │ 3. MyAidlStub.asInterface(service)
    │         │                     │
    │         └─> new MyAidlProxy(service)
    │                               │
    │ mService = MyAidlProxy        │
    └───────────────────────────────┘
```

### 远程方法调用流程

```
MainActivity          MyAidlProxy       Binder驱动      MyAidlStub        RemoteService
    │                     │                 │               │                  │
    │ mService.add(5,3)   │                 │               │                  │
    ├────────────────────>│                 │               │                  │
    │                     │ transact()      │               │                  │
    │                     ├────────────────>│               │                  │
    │                     │                 │ onTransact()  │                  │
    │                     │                 ├──────────────>│                  │
    │                     │                 │               │ add(5,3)         │
    │                     │                 │               ├─────────────────>│
    │                     │                 │               │      return 8    │
    │                     │                 │               │<─────────────────┤
    │                     │                 │  return 8     │                  │
    │                     │                 │<──────────────┤                  │
    │                     │   return 8      │               │                  │
    │                     │<────────────────┤               │                  │
    │      return 8       │                 │               │                  │
    │<────────────────────┤                 │               │                  │
    │                     │                 │               │                  │
```

## 数据流向

### 客户端 → 服务端（请求）

```
MainActivity
    ↓ (调用方法)
MyAidlProxy
    ↓ (序列化参数到Parcel)
Binder驱动
    ↓ (跨进程传输)
MyAidlStub.onTransact()
    ↓ (反序列化参数)
RemoteService.实现方法
```

### 服务端 → 客户端（响应）

```
RemoteService.实现方法
    ↓ (返回结果)
MyAidlStub.onTransact()
    ↓ (序列化返回值到Parcel)
Binder驱动
    ↓ (跨进程传输)
MyAidlProxy
    ↓ (反序列化返回值)
MainActivity
    ↓ (使用结果)
```

## 关键设计模式

### 1. 代理模式（Proxy Pattern）

**MyAidlProxy** 是远程服务的本地代理：
- 客户端不直接访问远程对象
- 通过代理封装复杂的Binder调用
- 透明地处理序列化/反序列化

### 2. 模板方法模式（Template Method Pattern）

**MyAidlStub** 定义了处理流程：
- `onTransact()` 定义了固定的处理流程
- 子类实现具体的业务方法
- 框架控制整体流程

### 3. 工厂模式（Factory Pattern）

**MyAidlStub.asInterface()** 是工厂方法：
- 根据进程情况创建不同对象
- 同进程返回本地对象
- 跨进程返回代理对象

## 类职责总结

| 类名 | 职责 | 运行位置 | 行数 |
|------|------|---------|------|
| IMyAidlInterface | 接口定义 | N/A | 45 |
| MyAidlStub | 服务端基类，处理请求 | 服务端进程 | 112 |
| MyAidlProxy | 客户端代理，发起调用 | 客户端进程 | 137 |
| RemoteService | 业务逻辑实现 | 服务端进程 | 77 |
| MainActivity | UI界面，服务调用 | 客户端进程 | 336 |

## 进程分布

### 主进程（com.zhongmin.aidl）
- MainActivity
- MyAidlProxy（运行时创建）

### 远程进程（com.zhongmin.aidl:remote）
- RemoteService
- MyAidlStub（匿名子类实例）

---

**总结**: 通过将Stub和Proxy拆分为独立的类文件，代码结构更加清晰，每个类职责单一，便于理解和维护。

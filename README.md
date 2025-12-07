# AIDL 跨进程通信示例

## 项目说明

本项目演示了如何使用**纯Java手写实现AIDL机制**，实现Android跨进程通信（IPC）。

## 核心文件

### 1. IMyAidlInterface.java
仿AIDL接口定义：
- **IMyAidlInterface**: 接口定义，定义跨进程通信的方法
- 定义接口描述符（DESCRIPTOR）
- 定义事务码（TRANSACTION_*）

### 2. MyAidlStub.java
Binder服务端基类：
- 继承`Binder`并实现`IMyAidlInterface`
- **asInterface()**: 将IBinder转换为接口
- **onTransact()**: 处理客户端请求
  - 反序列化参数
  - 调用实际方法
  - 序列化返回值

### 3. MyAidlProxy.java
Binder客户端代理类：
- 实现`IMyAidlInterface`接口
- 负责序列化参数
- 通过`transact()`发起Binder事务
- 反序列化返回值

### 4. RemoteService.java
后台服务，运行在独立进程`:remote`中：
- 继承`MyAidlStub`实现业务逻辑
- 实现方法：
  - `getPid()`: 返回进程ID
  - `add(int, int)`: 简单加法运算
  - `getServiceName()`: 返回服务名称
  - `basicTypes()`: 测试基本数据类型传递

### 5. MainActivity.java
客户端主界面：
- 纯Java实现，不使用Compose
- 绑定/解绑远程服务
- 调用远程方法进行跨进程通信
- 显示通信日志

## 工作原理

### Binder通信流程

```
客户端进程                           服务端进程
┌─────────────┐                    ┌─────────────┐
│ MainActivity│                    │RemoteService│
│             │                    │             │
│ 1. bindService()                 │             │
│    ↓                             │             │
│ 2. onServiceConnected()          │             │
│    ├─ IBinder service            │             │
│    ├─ asInterface(service)       │             │
│    └─ 获得Proxy对象              │             │
│             │                    │             │
│ 3. 调用方法(如add(5,3))          │             │
│    ├─ Proxy.add()                │             │
│    ├─ 写入Parcel                 │             │
│    ├─ transact()  ────Binder──>  │             │
│    │                             │ 4. Stub.onTransact()
│    │                             │    ├─ 读取Parcel
│    │                             │    ├─ 调用实现方法
│    │                             │    └─ 写入返回值
│    ├─ <────Binder────────────── │             │
│    ├─ 读取返回值                │             │
│    └─ 返回结果: 8                │             │
└─────────────┘                    └─────────────┘
```

## 关键技术点

### 1. 进程隔离
在`AndroidManifest.xml`中配置：
```xml
<service
    android:name=".RemoteService"
    android:process=":remote" />
```
`:remote`表示该服务运行在独立进程中，进程名为`包名:remote`

### 2. Binder机制
- **Parcel**: 数据序列化/反序列化
- **IBinder**: Binder对象的抽象
- **transact()**: 发起跨进程调用
- **onTransact()**: 接收并处理跨进程调用

### 3. 事务码（Transaction Code）
每个方法对应一个唯一的事务码：
```java
int TRANSACTION_getPid = IBinder.FIRST_CALL_TRANSACTION + 0;
int TRANSACTION_add = IBinder.FIRST_CALL_TRANSACTION + 2;
```

### 4. 接口描述符（Descriptor）
用于验证接口一致性：
```java
String DESCRIPTOR = "com.zhongmin.aidl.IMyAidlInterface";
```

## 使用步骤

1. **启动应用**: 打开MainActivity
2. **绑定服务**: 点击"绑定服务"按钮
3. **调用方法**: 
   - 获取服务进程PID（验证跨进程）
   - 测试加法运算
   - 获取服务名称
   - 测试基本类型传递
4. **查看日志**: 观察通信日志输出
5. **解绑服务**: 点击"解绑服务"按钮

## 与标准AIDL的对比

| 特性 | 标准AIDL | 本项目手写实现 |
|------|---------|---------------|
| 接口定义 | .aidl文件 | Java接口 |
| 代码生成 | 自动生成 | 手动编写 |
| Stub类 | 自动生成 | 手动实现 |
| Proxy类 | 自动生成 | 手动实现 |
| Binder机制 | 底层支持 | 相同 |
| 学习价值 | 黑盒使用 | 深入理解原理 |

## 优势

1. **深入理解**: 完全掌握AIDL底层实现原理
2. **灵活控制**: 可自定义序列化逻辑
3. **教学价值**: 适合学习Binder机制
4. **无依赖**: 不依赖AIDL编译工具

## 测试验证

运行应用后，可以通过以下方式验证跨进程通信：

1. **查看日志**:
```
MainActivity PID: 12345
RemoteService PID: 12346  // 不同的进程ID
```

2. **adb命令验证**:
```bash
adb shell ps | grep com.zhongmin.aidl
```
应该看到两个进程：
- `com.zhongmin.aidl` (主进程)
- `com.zhongmin.aidl:remote` (服务进程)

## 技术栈

- **语言**: Java
- **最低SDK**: 36
- **目标SDK**: 36
- **架构**: Client-Server (Binder IPC)

## 扩展建议

1. 支持自定义Parcelable对象传递
2. 添加回调接口（Callback）
3. 实现多进程事件监听
4. 添加权限验证机制
5. 实现连接池管理

## 注意事项

1. 所有跨进程调用都是**阻塞的**，建议在子线程中调用
2. 注意处理`RemoteException`
3. 及时解绑服务，避免内存泄漏
4. Parcel写入和读出顺序必须一致

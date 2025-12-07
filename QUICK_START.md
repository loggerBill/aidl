# 快速开始指南

## 一、项目概览

这是一个**手写AIDL实现**的Android跨进程通信示例项目，使用纯Java编写，不依赖AIDL工具。

### 项目特点
✅ 纯Java实现，无Kotlin依赖  
✅ 手写Binder机制，深入理解原理  
✅ 完整的跨进程通信示例  
✅ 详细的代码注释和文档  

## 二、环境要求

- **Android Studio**: Arctic Fox 或更高版本
- **JDK**: 11 或更高版本
- **Android SDK**: API 36 (Android 16)
- **Gradle**: 8.0+

## 三、快速运行

### 方法1：Android Studio
1. 打开Android Studio
2. File → Open → 选择项目目录
3. 等待Gradle同步完成
4. 点击运行按钮（绿色三角形）

### 方法2：命令行
```bash
# Windows PowerShell
cd c:\Users\Administrator\AndroidStudioProjects\aidl

# 清理项目
.\gradlew.bat clean

# 编译并安装
.\gradlew.bat installDebug

# 启动应用
adb shell am start -n com.zhongmin.aidl/.MainActivity
```

## 四、核心文件说明

### 📄 IMyAidlInterface.java
仿AIDL接口定义文件
- 定义了跨进程通信的接口
- 包含接口描述符和事务码

### 📄 MyAidlStub.java
Binder服务端基类
- 服务端继承此类实现业务逻辑
- 处理客户端的远程调用
- 包含asInterface方法

### 📄 MyAidlProxy.java
Binder客户端代理类
- 客户端通过代理调用远程方法
- 负责数据序列化和反序列化
- 发起Binder事务

### 📄 RemoteService.java
后台服务（运行在`:remote`进程）
- 实现具体的业务逻辑
- 处理客户端的远程调用
- 返回处理结果

### 📄 MainActivity.java
客户端主界面
- 绑定/解绑远程服务
- 调用远程方法
- 显示通信日志

### 📄 AndroidManifest.xml
清单文件配置
- 注册Service组件
- 配置`:remote`独立进程

## 五、使用步骤

### 1️⃣ 启动应用
在设备或模拟器上启动应用，会看到主界面。

### 2️⃣ 绑定服务
点击"绑定服务"按钮，连接后台服务。

### 3️⃣ 测试通信
依次点击功能按钮，测试跨进程通信：
- **获取服务进程PID**: 验证跨进程
- **测试加法**: 简单计算
- **获取服务名称**: 字符串传递
- **测试基本类型**: 多类型参数

### 4️⃣ 查看日志
观察界面下方的日志区域，了解通信详情。

### 5️⃣ 解绑服务
使用完毕后，点击"解绑服务"按钮。

## 六、验证跨进程

### 方式1：查看日志
日志会显示：
```
客户端 PID: 12345
服务端 PID: 12346
✓ 跨进程通信成功！
```

### 方式2：adb命令
```bash
adb shell ps | grep com.zhongmin.aidl
```

应该看到两个进程：
```
com.zhongmin.aidl           # 主进程
com.zhongmin.aidl:remote    # 服务进程
```

## 七、常见问题

### Q1: 编译失败
**A**: 检查Android SDK是否安装API 36

### Q2: 无法绑定服务
**A**: 检查AndroidManifest.xml中Service是否正确注册

### Q3: 同进程通信（PID相同）
**A**: 检查Service的`android:process=":remote"`配置

### Q4: RemoteException
**A**: 检查服务是否正常运行，可通过logcat查看日志

### Q5: AppCompat依赖缺失
**A**: Sync Gradle，等待依赖下载完成

## 八、学习路径

### 初级：运行示例
1. 运行应用
2. 测试各项功能
3. 观察日志输出

### 中级：阅读代码
1. 阅读`IMyAidlInterface.java`，理解接口定义
2. 阅读`Stub.onTransact()`，理解服务端处理
3. 阅读`Proxy`实现，理解客户端调用

### 高级：深入原理
1. 学习Binder机制
2. 研究Parcel序列化
3. 理解进程间通信

### 扩展：自定义功能
1. 添加新的接口方法
2. 实现Parcelable对象传递
3. 添加回调机制

## 九、调试技巧

### 查看实时日志
```bash
adb logcat -s MainActivity:D RemoteService:D
```

### 清除应用数据
```bash
adb shell pm clear com.zhongmin.aidl
```

### 强制停止应用
```bash
adb shell am force-stop com.zhongmin.aidl
```

### 查看应用信息
```bash
adb shell dumpsys package com.zhongmin.aidl
```

## 十、扩展阅读

- 📖 [README.md](README.md) - 项目详细说明
- 📖 [BINDER_MECHANISM.md](BINDER_MECHANISM.md) - Binder机制详解
- 📖 [TEST_CHECKLIST.md](TEST_CHECKLIST.md) - 测试清单

## 十一、下一步

### 功能扩展建议
1. ✨ 添加自定义Parcelable对象
2. ✨ 实现进度回调（Callback）
3. ✨ 添加权限验证
4. ✨ 实现异步调用
5. ✨ 添加连接池管理

### 学习建议
1. 📚 对比标准AIDL实现
2. 📚 研究Messenger机制
3. 📚 学习ContentProvider
4. 📚 了解MMKV等跨进程方案

## 十二、获取帮助

### 查看文档
- 代码中有详细的注释
- 每个类都有功能说明
- 关键方法都有注释

### 问题排查
1. 查看logcat日志
2. 检查AndroidManifest配置
3. 验证进程是否启动
4. 阅读异常堆栈信息

## 十三、性能提示

⚡ **跨进程调用是阻塞的**，避免在主线程执行耗时操作  
⚡ **减少调用次数**，批量传输数据  
⚡ **及时解绑服务**，避免内存泄漏  
⚡ **处理异常**，所有远程调用都要try-catch  

## 十四、安全提示

🔒 **不要暴露敏感接口**给其他应用  
🔒 **验证调用者身份**，检查UID  
🔒 **限制数据大小**，避免TransactionTooLargeException  
🔒 **处理恶意输入**，验证参数有效性  

---

**祝你学习愉快！** 🎉

如有问题，请查看详细文档或提交Issue。

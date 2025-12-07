# Parcel 详解 - 小船上的货箱

## 🎯 核心理解

**Parcel 是跨进程传递数据的容器（货箱）**

```
完整比喻：

MyAidlProxy（河这边）           MyAidlStub（河那边）
     ↓                               ↑
准备货箱 Parcel                      收到货箱 Parcel
     ↓                               ↑
装货（序列化）                       拆箱（反序列化）
     ↓                               ↑
放到小船                             小船送达
     ↓                               ↑
     └──── Binder 驱动（小船🚤）─────┘
```

---

## 📦 什么是 Parcel？

### 定义

```java
// Android 源码
public final class Parcel {
    // Parcel 是一个容器，用于打包数据以便跨进程传输
}
```

**类比**：
- 📦 **Parcel** = 快递箱
- **数据** = 箱子里的货物
- **Binder 驱动** = 快递车
- **进程A → 进程B** = 寄件人 → 收件人

---

## 🔄 完整的 Parcel 生命周期

### 步骤1：获取 Parcel（准备空箱子）

```java
// MyAidlProxy.java
Parcel data = Parcel.obtain();   // 📦 空箱子（发货用）
Parcel reply = Parcel.obtain();  // 📦 空箱子（收货用）
```

**Parcel.obtain()** 从对象池中获取，避免频繁创建对象。

---

### 步骤2：写入数据（装箱 - 序列化）

```java
// MyAidlProxy.java（河这边装箱）

// 📝 往 data 箱子里装东西
data.writeInterfaceToken(DESCRIPTOR);  
// 装第1件：接口标识（验证用）

data.writeInt(5);   
// 装第2件：参数1 = 5

data.writeInt(3);   
// 装第3件：参数2 = 3

// 现在箱子里有3件货物了
```

**内部过程**（简化）：
```
Parcel 内部有一个字节数组：
┌─────────────────────────────┐
│ [描述符] [5] [3] [空] [空]  │
│    ↑      ↑   ↑             │
│   第1件  第2 第3            │
└─────────────────────────────┘
```

---

### 步骤3：传输（小船运货）

```java
// 把箱子交给 Binder 驱动
mRemote.transact(TRANSACTION_add, data, reply, 0);
//                                 ↑      ↑
//                            发货箱  收货箱
```

**Binder 驱动的工作**：

```
1. 从客户端进程拿到 Parcel data
   ┌──────────────────┐
   │ [描述符] [5] [3] │ ← 客户端内存
   └──────────────────┘
         ↓ 复制
   ┌──────────────────┐
   │ [描述符] [5] [3] │ ← Binder 驱动
   └──────────────────┘
         ↓ 传递
   ┌──────────────────┐
   │ [描述符] [5] [3] │ ← 服务端内存
   └──────────────────┘

2. 唤醒服务端的 onTransact()
```

---

### 步骤4：读取数据（拆箱 - 反序列化）

```java
// MyAidlStub.java（河那边拆箱）

// 📖 打开 data 箱子，按顺序取出货物
data.enforceInterface(DESCRIPTOR);  
// 取第1件：验证接口标识

int arg0 = data.readInt();  
// 取第2件：读到 5

int arg1 = data.readInt();  
// 取第3件：读到 3

// 所有货物都取出来了
```

**关键规则**：读取顺序必须和写入顺序一致！

```
✅ 正确：
写入：writeInt(5) → writeInt(3)
读取：readInt()   → readInt()

❌ 错误：
写入：writeInt(5)    → writeInt(3)
读取：readString()   → readInt()  💥 崩溃！
```

---

### 步骤5：处理业务

```java
// 用读取的数据干活
int result = this.add(arg0, arg1);  // 5 + 3 = 8
```

---

### 步骤6：写入返回值（装回复箱）

```java
// 📝 往 reply 箱子里装返回值
reply.writeNoException();  
// 装第1件：标记没有异常

reply.writeInt(result);     
// 装第2件：结果 = 8

// reply 箱子准备好了
```

---

### 步骤7：返回（小船返程）

```java
// onTransact() 返回 true
return true;

// Binder 驱动把 reply 箱子送回客户端
```

**Binder 驱动的返回工作**：

```
服务端内存
   ┌───────────────┐
   │ [无异常] [8]  │ ← reply
   └───────────────┘
         ↓ 复制
   ┌───────────────┐
   │ [无异常] [8]  │ ← Binder 驱动
   └───────────────┘
         ↓ 传递
   ┌───────────────┐
   │ [无异常] [8]  │ ← 客户端内存
   └───────────────┘

唤醒客户端等待的线程
transact() 返回
```

---

### 步骤8：读取返回值（拆回复箱）

```java
// MyAidlProxy.java（河这边拆回复箱）

// transact() 返回后继续执行
reply.readException();     
// 取第1件：检查有没有异常

int result = reply.readInt();  
// 取第2件：读到 8

return result;  // 返回给 MainActivity
```

---

### 步骤9：回收 Parcel（回收箱子）

```java
finally {
    // ♻️ 用完的箱子要回收，避免内存泄漏
    reply.recycle();
    data.recycle();
}
```

**Parcel.recycle()** 把对象放回对象池，供下次复用。

---

## 📊 Parcel 的数据结构

### 内部结构（简化）

```java
public final class Parcel {
    
    private long mNativePtr;  // 指向 native 内存的指针
    
    private int mDataSize;    // 数据大小
    private int mDataPos;     // 当前读写位置
    
    // 写入方法
    public final void writeInt(int val) {
        nativeWriteInt(mNativePtr, val);
    }
    
    // 读取方法
    public final int readInt() {
        return nativeReadInt(mNativePtr);
    }
}
```

### 内存布局示例

```
写入：data.writeInt(5); data.writeInt(3);

Parcel 内部内存：
┌────┬────┬────┬────┬────┬────┬────┬────┐
│ 05 │ 00 │ 00 │ 00 │ 03 │ 00 │ 00 │ 00 │
└────┴────┴────┴────┴────┴────┴────┴────┘
  ↑                    ↑
  5 (int = 4 bytes)    3 (int = 4 bytes)
  
读取：data.readInt() → 5; data.readInt() → 3
```

---

## 🎯 Parcel 支持的数据类型

### 1. 基本类型

```java
// 写入
data.writeByte((byte) 1);
data.writeInt(100);
data.writeLong(200L);
data.writeFloat(3.14f);
data.writeDouble(2.718);
data.writeBoolean(true);
data.writeChar('A');
data.writeString("Hello");

// 读取（必须相同顺序）
byte b = data.readByte();
int i = data.readInt();
long l = data.readLong();
float f = data.readFloat();
double d = data.readDouble();
boolean bool = data.readInt() != 0;  // 注意：boolean 读取方式
char c = (char) data.readInt();
String s = data.readString();
```

### 2. 数组

```java
// 写入
int[] intArray = {1, 2, 3, 4, 5};
data.writeIntArray(intArray);

String[] strArray = {"a", "b", "c"};
data.writeStringArray(strArray);

// 读取
int[] intArray = data.createIntArray();
String[] strArray = data.createStringArray();
```

### 3. List 和 Map

```java
// 写入
List<String> list = Arrays.asList("a", "b", "c");
data.writeStringList(list);

// 读取
List<String> list = data.createStringArrayList();
```

### 4. Parcelable 对象

```java
// 自定义类需要实现 Parcelable
public class User implements Parcelable {
    String name;
    int age;
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(age);
    }
    
    // ... 其他实现
}

// 写入
User user = new User("Alice", 25);
data.writeParcelable(user, 0);

// 读取
User user = data.readParcelable(User.class.getClassLoader());
```

---

## ⚠️ Parcel 使用注意事项

### 1. 读写顺序必须一致

```java
// ❌ 错误示例
// 写入
data.writeInt(5);
data.writeString("hello");
data.writeLong(100L);

// 读取（顺序错了）
String s = data.readString();  // 💥 读到了 5，类型错误！
int i = data.readInt();
long l = data.readLong();

// ✅ 正确示例
// 写入
data.writeInt(5);
data.writeString("hello");
data.writeLong(100L);

// 读取（相同顺序）
int i = data.readInt();        // 5
String s = data.readString();  // "hello"
long l = data.readLong();      // 100L
```

### 2. 必须回收

```java
Parcel data = Parcel.obtain();
try {
    // 使用 data
} finally {
    // ⚠️ 重要：必须回收
    data.recycle();
}
```

### 3. 数据大小限制

Binder 传输有大小限制（通常是 1MB）：

```java
// ❌ 可能失败
byte[] bigData = new byte[2 * 1024 * 1024];  // 2MB
data.writeByteArray(bigData);  // 💥 TransactionTooLargeException

// ✅ 应该分批传输或使用其他方式（如文件）
```

### 4. 不要跨进程持有 Parcel

```java
// ❌ 错误：不要这样做
private Parcel mData;  // 不要作为成员变量长期持有

// ✅ 正确：用完立即回收
public void method() {
    Parcel data = Parcel.obtain();
    try {
        // 使用
    } finally {
        data.recycle();
    }
}
```

---

## 🎨 完整示例

### 传递复杂数据

```java
// 客户端（装箱）
Parcel data = Parcel.obtain();
Parcel reply = Parcel.obtain();

try {
    // 传递多种类型的数据
    data.writeInterfaceToken(DESCRIPTOR);
    data.writeString("Alice");      // 姓名
    data.writeInt(25);              // 年龄
    data.writeBoolean(true);        // 是否激活
    data.writeStringArray(          // 爱好
        new String[]{"读书", "游泳", "编程"}
    );
    
    mRemote.transact(TRANSACTION_updateUser, data, reply, 0);
    
    reply.readException();
    boolean success = reply.readInt() != 0;
    
} finally {
    reply.recycle();
    data.recycle();
}
```

```java
// 服务端（拆箱）
@Override
protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
    
    switch (code) {
        case TRANSACTION_updateUser:
            data.enforceInterface(DESCRIPTOR);
            
            // 按相同顺序读取
            String name = data.readString();      // "Alice"
            int age = data.readInt();             // 25
            boolean active = data.readInt() != 0; // true
            String[] hobbies = data.createStringArray();  
            // ["读书", "游泳", "编程"]
            
            // 处理业务
            boolean success = updateUser(name, age, active, hobbies);
            
            // 返回结果
            reply.writeNoException();
            reply.writeInt(success ? 1 : 0);
            
            return true;
    }
}
```

---

## 🔍 Parcel vs Serializable

| 特性 | Parcel | Serializable |
|------|--------|--------------|
| 用途 | 跨进程通信（IPC） | 持久化存储 |
| 性能 | 快（为 IPC 优化） | 慢（反射） |
| 使用场景 | Binder、Intent | 文件、网络 |
| 实现难度 | 需手动写代码 | 简单（自动） |

---

## 📝 总结

### Parcel 是什么？

**Parcel 是跨进程传输数据的容器（货箱）**

### 关键特点

1. ✅ **高效**：为 Binder 优化，速度快
2. ✅ **灵活**：支持多种数据类型
3. ⚠️ **顺序**：读写必须一致
4. ♻️ **回收**：用完必须 recycle()
5. 📏 **限制**：有大小限制（~1MB）

### 完美比喻

```
Parcel = 📦 快递箱
    ↓
装货（序列化）
    ↓
🚚 Binder 驱动（快递车）
    ↓
拆箱（反序列化）
    ↓
使用数据
    ↓
♻️ 回收箱子
```

---

你的理解完全正确！**Parcel 就是小船上装货的容器（箱子）**！🎉

# JUC并发编程

## 1. 进程与线程

### 1.1 进程与线程

#### 进程

- 程序由指令和数据组成，但这些指令要运行，数据要读写，就必须将指令加载至 CPU，数据加载至内存。在 指令运行过程中还需要用到磁盘、网络等设备。进程就是用来加载指令、管理内存、管理 IO 的
- 当一个程序被运行，从磁盘加载这个程序的代码至内存，这时就开启了一个进程。
- 进程就可以视为程序的一个实例。大部分程序可以同时运行多个实例进程（例如记事本、画图、浏览器 等），也有的程序只能启动一个实例进程（例如网易云音乐、360 安全卫士等）

#### 线程

- 一个进程之内可以分为一到多个线程。
- 一个线程就是一个指令流，将指令流中的一条条指令以一定的顺序交给 CPU 执行
- Java 中，线程作为最小调度单位，进程作为资源分配的最小单位。 在 windows 中进程是不活动的，只是作 为线程的容器

#### 二者对比

- 进程基本上相互独立的，而线程存在于进程内，是进程的一个子集
- 进程拥有共享的资源，如内存空间等，供其内部的线程共享
- 进程间通信较为复杂
  - 同一台计算机的进程通信称为 IPC（Inter-process communication）
  - 不同计算机之间的进程通信，需要通过网络，并遵守共同的协议，例如 HTTP
- 线程通信相对简单，因为它们共享进程内的内存，一个例子是多个线程可以访问同一个共享变量
- 线程更轻量，线程上下文切换成本一般上要比进程上下文切换低

### 1.2 并行与并发

单核 cpu 下，线程实际还是 `串行执行` 的。操作系统中有一个组件叫做任务调度器，将 cpu 的时间片（windows 下时间片最小约为 15 毫秒）分给不同的程序使用，只是由于 cpu 在线程间（时间片很短）的切换非常快，人类感 觉是 `同时运行`的 。总结为一句话就是：`微观串行，宏观并行`， 

一般会将这种 线程轮流使用 CPU 的做法称为并发， concurrent

多核 cpu下，每个  `核（core）` 都可以调度运行线程，这时候线程可以是`并行`的。

引用 Rob Pike 的一段描述：

- 并发（concurrent）是同一时间应对（dealing with）多件事情的能力
- 并行（parallel）是同一时间动手做（doing）多件事情的能力

## 2.Java 线程

### 2.1 创建和运行线程

#### 方法一，直接使用 Thread

```java
// 创建线程对象
Thread t = new Thread() {
    public void run() {
        // 要执行的任务
    }
};
// 启动线程
t.start();
```

例如：

```java
// 构造方法的参数是给线程指定名字，推荐
Thread t1 = new Thread("t1") {
    @Override
    // run 方法内实现了要执行的任务
    public void run() {
        log.debug("hello");
    }
};
 t1.start();
```

输出：

```
19:19:00 [t1] c.ThreadStarter - hello
```

#### 方法二，使用 Runnable 配合 Thread  

把【线程】和【任务】（要执行的代码）分开

- Thread 代表线程
- Runnable 可运行的任务（线程要执行的代码）

```java
Runnable runnable = new Runnable() {
    public void run(){
        // 要执行的任务
    }
};
// 创建线程对象
Thread t = new Thread( runnable );
// 启动线程
t.start(); 
```

例如：

```java
// 创建任务对象
Runnable task2 = new Runnable() {
    @Override
    public void run() {
        log.debug("hello");
    }
};
// 参数1 是任务对象; 参数2 是线程名字，推荐
Thread t2 = new Thread(task2, "t2");
 t2.start();
```

输出：

```shell
19:19:00 [t2] c.ThreadStarter - hello
```

 Java 8 以后可以使用 lambda 精简代码

```java
// 创建任务对象
Runnable task2 = () -> log.debug("hello");
 // 参数1 是任务对象; 参数2 是线程名字，推荐
Thread t2 = new Thread(task2, "t2");
 t2.start();
```

##### \* 原理之 Thread 与 Runnable 的关系

分析 Thread 的源码，理清它与 Runnable 的关系

##### 小结

- 方法1 是把线程和任务合并在了一起，方法2 是把线程和任务分开了
- 用 Runnable 更容易与线程池等高级 API 配合
- 用 Runnable 让任务类脱离了 Thread 继承体系，更灵活

#### 方法三，FutureTask 配合 Thread

FutureTask 能够接收 Callable 类型的参数，用来处理有返回结果的情况

```java
// 创建任务对象
FutureTask<Integer> task3 = new FutureTask<>(() -> {
    log.debug("hello");
    return 100;
});
// 参数1 是任务对象; 参数2 是线程名字，推荐
new Thread(task3, "t3").start();
// 主线程阻塞，同步等待 task 执行完毕的结果
Integer result = task3.get();
 log.debug("结果是:{}", result);
```

输出：

```shell
19:22:27 [t3] c.ThreadStarter - hello
19:22:27 [main] c.ThreadStarter - 结果是:100
```

### 2.2 查看进程线程的方法

#### windows

- 任务管理器可以查看进程和线程数，也可以用来杀死进程
- `tasklist` 查看进程
- `taskkill` 杀死进程

#### Linux

- `ps -fe` 查看所有进程
- `ps -fT -p`  查看某个进程（PID）的所有线程
- `kill`  杀死进程’
- `top` 按大写 H 切换是否显示线程
- `top -H -p`  查看某个进程（PID）的所有线程

#### Java

- `jps` 命令查看所有 Java 进程
- `jstack`  查看某个 Java 进程（PID）的所有线程状态 
- `jconsole` 来查看某个 Java 进程中线程的运行情况（图形界面）

jconsole 远程监控配置

- 需要以如下方式运行你的 java 类

  ```shell
  java -Djava.rmi.server.hostname=`ip地址` -Dcom.sun.management.jmxremote 
  Dcom.sun.management.jmxremote.port=`连接端口` -Dcom.sun.management.jmxremote.ssl=是否安全连接 
  Dcom.sun.management.jmxremote.authenticate=是否认证 java类
  ```

- 修改 /etc/hosts 文件将 127.0.0.1 映射至主机名

如果要认证访问，还需要做如下步骤

- 复制 jmxremote.password 文件
- 修改 jmxremote.password 和 jmxremote.access 文件的权限为 600 即文件所有者可读写
- 连接时填入 controlRole（用户名），R&D（密码）

### 2.3 * 原理之线程运行

#### 栈与栈帧

Java Virtual Machine Stacks （Java 虚拟机栈） 

我们都知道 JVM 中由堆、栈、方法区所组成，其中栈内存是给谁用的呢？其实就是线程，每个线程启动后，虚拟 机就会为其分配一块栈内存。

- 每个栈由多个栈帧（Frame）组成，对应着每次方法调用时所占用的内存
- 每个线程只能有一个活动栈帧，对应着当前正在执行的那个方法

#### 线程上下文切换（Thread Context Switch）

因为以下一些原因导致 cpu 不再执行当前的线程，转而执行另一个线程的代码

- 线程的 cpu 时间片用完
- 垃圾回收
- 有更高优先级的线程需要运行
- 线程自己调用了 sleep、yield、wait、join、park、synchronized、lock 等方法

当 Context Switch 发生时，需要由操作系统保存当前线程的状态，并恢复另一个线程的状态，Java 中对应的概念 就是程序计数器（Program Counter Register），它的作用是记住下一条 jvm 指令的执行地址，是线程私有的

- 状态包括程序计数器、虚拟机栈中每个栈帧的信息，如局部变量、操作数栈、返回地址等
- Context Switch 频繁发生会影响性能

### 2.4 常见方法

| 方法名           | Static | 功能说明                                                     | 注意                                                         |
| ---------------- | ------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| start()          |        | 启动一个新线 程，在新的线程 运行 run 方法 中的代码           | start 方法只是让线程进入就绪，里面代码不一定立刻 运行（CPU 的时间片还没分给它）。每个线程对象的 start方法只能调用一次，如果调用了多次会出现  IllegalThreadStateException |
| run()            |        | 新线程启动后会 调用的方法                                    | 如果在构造 Thread 对象时传递了 Runnable 参数，则 线程启动后会调用 Runnable 中的 run 方法，否则默 认不执行任何操作。但可以创建 Thread 的子类对象， 来覆盖默认行为 |
| join()           |        | 等待线程运行结 束                                            |                                                              |
| join(long n)     |        | 等待线程运行结 束,最多等待 n  毫秒                           |                                                              |
| getId()          |        | 获取线程长整型 的 id                                         | id 唯一                                                      |
| getName()        |        | 获取线程名                                                   |                                                              |
| setName(String)  |        | 修改线程名                                                   |                                                              |
| getPriority()    |        | 获取线程优先级                                               |                                                              |
| setPriority(int) |        | 修改线程优先级                                               | java中规定线程优先级是1~10 的整数，较大的优先级 能提高该线程被 CPU 调度的机率 |
| getState()       |        | 获取线程状态                                                 | Java 中线程状态是用 6 个 enum 表示，分别为： NEW, RUNNABLE, BLOCKED, WAITING,  TIMED_WAITING, TERMINATED |
| isInterrupted()  |        | 判断是否被打断                                               | 不会清除 `打断标记`                                          |
| isAlive()        |        | 线程是否存活 （还没有运行完 毕）                             |                                                              |
| interrupt()      |        | 打断线程                                                     | 如果被打断线程正在 sleep，wait，join 会导致被打断 的线程抛出 InterruptedException，并清除打断标记 ；如果打断的正在运行的线程，则会设置打断标 记 ；park 的线程被打断，也会设置打断标记 |
| interrupted()    | static | 判断当前线程是 否被打断                                      | 会清除打断标记                                               |
| currentThread()  | static | 获取当前正在执 行的线程                                      |                                                              |
| sleep(long n)    | static | 让当前执行的线 程休眠n毫秒， 休眠时让出 cpu  的时间片给其它 线程 |                                                              |
| yield()          | static | 提示线程调度器 让出当前线程对 CPU的使用                      | 主要是为了测试和调试                                         |



### 2.5 start 与 run

#### 调用 run

```java
public static void main(String[] args) {
    Thread t1 = new Thread("t1") {
        @Override
        public void run() {
            log.debug(Thread.currentThread().getName());
            FileReader.read(Constants.MP4_FULL_PATH);
        }
    };
}
 t1.run();
 log.debug("do other things ...");
```

输出：

```shell
19:39:14 [main] c.TestStart - main
 19:39:14 [main] c.FileReader - read [1.mp4] start ...
 19:39:18 [main] c.FileReader - read [1.mp4] end ... cost: 4227 ms
 19:39:18 [main] c.TestStart - do other things ...
```

程序仍在 main 线程运行， FileReader.read() 方法调用还是同步的

#### 调用 start

将上述代码的  t1.run() 改为

```java
t1.start();
```

输出：

```shell
19:41:30 [main] c.TestStart - do other things ...
 19:41:30 [t1] c.TestStart - t1
 19:41:30 [t1] c.FileReader - read [1.mp4] start ...
 19:41:35 [t1] c.FileReader - read [1.mp4] end ... cost: 4542 ms
```

程序在 t1 线程运行， `FileReader.read()` 方法调用是异步的

#### 小结

- 直接调用 run 是在主线程中执行了 run，没有启动新的线程
- 使用 start 是启动新的线程，通过新的线程间接执行 run 中的代码

### 2.6 sleep 与 yield

#### sleep

1. 调用 sleep 会让当前线程从 Running  进入 Timed Waiting 状态（阻塞） 
2. 其它线程可以使用  interrupt 方法打断正在睡眠的线程，这时 sleep 方法会抛出`InterruptedException` 
3. 睡眠结束后的线程未必会立刻得到执行 
4. 建议用 TimeUnit 的 sleep 代替 Thread 的 sleep 来获得更好的可读性

#### yield

1. 调用 yield 会让当前线程从 Running 进入 Runnable  就绪状态，然后调度执行其它线程 
2. 具体的实现依赖于操作系统的任务调度器

#### 线程优先级

- 线程优先级会提示（hint）调度器优先调度该线程，但它仅仅是一个提示，调度器可以忽略它 
- 如果 cpu 比较忙，那么优先级高的线程会获得更多的时间片，但 cpu 闲时，优先级几乎没作用

```java
Runnable task1 = () -> {
    int count = 0;
    for (;;) {
        System.out.println("---->1 " + count++);
    }
};
Runnable task2 = () -> {
    int count = 0;
    for (;;) {
        // Thread.yield();
        System.out.println("              ---->2 " + count++);
    }
};
Thread t1 = new Thread(task1, "t1");
Thread t2 = new Thread(task2, "t2");
// t1.setPriority(Thread.MIN_PRIORITY);
// t2.setPriority(Thread.MAX_PRIORITY);
 t1.start();
 t2.start();
```



### 2.7 join 方法详解

#### 为什么需要 join

下面的代码执行，打印 r 是什么？

```java
static int r = 0;
public static void main(String[] args) throws InterruptedException {
    test1();
}
private static void test1() throws InterruptedException {
    log.debug("开始");
    Thread t1 = new Thread(() -> {
        log.debug("开始");
        sleep(1);
        log.debug("结束");
        r = 10;
    });
    t1.start();
    log.debug("结果为:{}", r);
    log.debug("结束");
}
```

分析

- 因为主线程和线程 t1 是并行执行的，t1 线程需要 1 秒之后才能算出 `r=10`
- 而主线程一开始就要打印 r 的结果，所以只能打印出 `r=0`

解决方法

- 用 sleep 行不行？为什么？ 
- 用 join，加在  `t1.start()` 之后即可

以调用方角度来讲，如果等待**多个结果**

问，下面代码 cost 大约多少秒？

```java
static int r1 = 0;
static int r2 = 0;
public static void main(String[] args) throws InterruptedException {
    test2();
}
private static void test2() throws InterruptedException {
    Thread t1 = new Thread(() -> {
        sleep(1);
        r1 = 10;
    });
    Thread t2 = new Thread(() -> {
        sleep(2);
        r2 = 20
    });
    long start = System.currentTimeMillis();
    t1.start();
    t2.start();
    t1.join();
    t2.join();
    long end = System.currentTimeMillis();
    log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
}
```

分析如下

- 第一个 join：等待 t1 时, t2 并没有停止, 而在运行 
- 第二个 join：1s 后, 执行到此, t2 也运行了 1s, 因此也只需再等待 1s

如果颠倒两个 join 呢？ 

最终都是输出

```shell
20:45:43.239 [main] c.TestJoin - r1: 10 r2: 20 cost: 2005
```

#### 有时效的 join

等够时间

```java
static int r1 = 0;
static int r2 = 0;
public static void main(String[] args) throws InterruptedException {
    test3();
}
public static void test3() throws InterruptedException {
    Thread t1 = new Thread(() -> {
        sleep(1);
        r1 = 10;
    });
    long start = System.currentTimeMillis();
    t1.start();
    // 线程执行结束会导致 join 结束
    t1.join(1500);
    long end = System.currentTimeMillis();
    log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
}
```

输出：

```shell
20:48:01.320 [main] c.TestJoin - r1: 10 r2: 0 cost: 1010
```

没等够时间

```java
static int r1 = 0;
static int r2 = 0;
public static void main(String[] args) throws InterruptedException {
    test3();
}
public static void test3() throws InterruptedException {
    Thread t1 = new Thread(() -> {
        sleep(2);
        r1 = 10;
    });
    long start = System.currentTimeMillis();
    t1.start();
}
// 线程执行结束会导致 join 结束
 t1.join(1500);
long end = System.currentTimeMillis();
 log.debug("r1: {} r2: {} cost: {}", r1, r2, end - start);
}
```

输出

```
20:52:15.623 [main] c.TestJoin - r1: 0 r2: 0 cost: 1502
```



###  2.8 interrupt 方法详解

#### 打断 sleep，wait，join 的线程

这几个方法都会让线程进入阻塞状态 

打断 sleep 的线程, 会清空打断状态，以 sleep 为例

```java
private static void test1() throws InterruptedException {
    Thread t1 = new Thread(()->{
        sleep(1);
    }, "t1");
    t1.start();

    sleep(0.5);
    t1.interrupt();
    log.debug(" 打断状态: {}", t1.isInterrupted())；
}
```

输出

```sh
java.lang.InterruptedException: sleep interrupted
 at java.lang.Thread.sleep(Native Method)
 at java.lang.Thread.sleep(Thread.java:340)
 at java.util.concurrent.TimeUnit.sleep(TimeUnit.java:386)
 at cn.itcast.n2.util.Sleeper.sleep(Sleeper.java:8)
 at cn.itcast.n4.TestInterrupt.lambda$test1$3(TestInterrupt.java:59)
 at java.lang.Thread.run(Thread.java:745)
 21:18:10.374 [main] c.TestInterrupt -  打断状态: false
```

#### 打断正常运行的线程(\* 模式之两阶段终止)

打断正常运行的线程, 不会清空打断状态

```java
private static void test2() throws InterruptedException {
  Thread t2 = new Thread(()->{
 	while(true) {
 		Thread current = Thread.currentThread();
 		boolean interrupted = current.isInterrupted();
 		if(interrupted) {
 			log.debug(" 打断状态: {}", interrupted);
 			break;
            }
        }
   }, "t2");
 t2.start();
 sleep(0.5);
 t2.interrupt();
}
```

输出

```
20:57:37.964 [t2] c.TestInterrupt -  打断状态: true 
```

#### 打断 park 线程

打断 park 线程, 不会清空打断状态

```java
private static void test3() throws InterruptedException {
 Thread t1 = new Thread(() -> {
 	log.debug("park...");
 	LockSupport.park();
	log.debug("unpark...");
 	log.debug("打断状态：{}", Thread.currentThread().isInterrupted());
  }, "t1");
 t1.start();
 sleep(0.5);
 t1.interrupt();
}
```

输出

```sh
21:11:52.795 [t1] c.TestInterrupt - park... 
21:11:53.295 [t1] c.TestInterrupt - unpark... 
21:11:53.295 [t1] c.TestInterrupt - 打断状态：true 	
```

如果打断标记已经是 true, 则 park 会失效

```java
private static void test4() {
   Thread t1 = new Thread(() -> {
 		for (int i = 0; i < 5; i++) {
 			log.debug("park...");
 			LockSupport.park();
 			log.debug("打断状态：{}", Thread.currentThread().isInterrupted());
        }
    });
 t1.start();
 sleep(1);
 t1.interrupt();
}
```

输出

```shell
21:13:48.783 [Thread-0] c.TestInterrupt - park... 
21:13:49.809 [Thread-0] c.TestInterrupt - 打断状态：true 
21:13:49.812 [Thread-0] c.TestInterrupt - park... 
21:13:49.813 [Thread-0] c.TestInterrupt - 打断状态：true 
21:13:49.813 [Thread-0] c.TestInterrupt - park... 
21:13:49.813 [Thread-0] c.TestInterrupt - 打断状态：true 
21:13:49.813 [Thread-0] c.TestInterrupt - park... 
21:13:49.813 [Thread-0] c.TestInterrupt - 打断状态：true 
21:13:49.813 [Thread-0] c.TestInterrupt - park... 
21:13:49.813 [Thread-0] c.TestInterrupt - 打断状态：true 
```

>提示 
>
>可以使用  `Thread.interrupted()` 清除打断状态

### 2.9 不推荐的方法

还有一些不推荐使用的方法，这些方法已过时，容易破坏同步代码块，造成线程死锁

| 方法名    | static | 功能说明             |
| --------- | ------ | -------------------- |
| stop()    |        | 停止线程运行         |
| suspend() |        | 挂起（暂停）线程运行 |
| resume()  |        | 恢复线程运行         |



### 2.10 主线程与守护线程

默认情况下，Java 进程需要等待所有线程都运行结束，才会结束。有一种特殊的线程叫做守护线程，只要其它非守 护线程运行结束了，即使守护线程的代码没有执行完，也会强制结束。

例：

```java
log.debug("开始运行...");
 Thread t1 = new Thread(() -> {
 	log.debug("开始运行...");
 	sleep(2);
 	log.debug("运行结束...");
 }, "daemon");
 // 设置该线程为守护线程
 t1.setDaemon(true);
 t1.start();
 sleep(1);
 log.debug("运行结束...");
```

输出

```
08:26:38.123 [main] c.TestDaemon - 开始运行... 
08:26:38.213 [daemon] c.TestDaemon - 开始运行... 
08:26:39.215 [main] c.TestDaemon - 运行结束... 
```

> **注意** 
>
> - 垃圾回收器线程就是一种守护线程 
> - Tomcat 中的 Acceptor 和 Poller 线程都是守护线程，所以 Tomcat 接收到 shutdown 命令后，不会等 待它们处理完当前请求

### 2.11 五种状态

这是从 操作系统 层面来描述的

- 【初始状态】仅是在语言层面创建了线程对象，还未与操作系统线程关联 
- 【可运行状态】（就绪状态）指该线程已经被创建（与操作系统线程关联），可以由 CPU 调度执行 
- 【运行状态】指获取了 CPU 时间片运行中的状态
  - 当 CPU 时间片用完，会从【运行状态】转换至【可运行状态】，会导致线程的上下文切换
- 【阻塞状态】
  - 如果调用了阻塞 API，如 BIO 读写文件，这时该线程实际不会用到 CPU，会导致线程上下文切换，进入 【阻塞状态】 
  - 等 BIO 操作完毕，会由操作系统唤醒阻塞的线程，转换至【可运行状态】 
  - 与【可运行状态】的区别是，对【阻塞状态】的线程来说只要它们一直不唤醒，调度器就一直不会考虑 调度它们
- 【终止状态】表示线程已经执行完毕，生命周期已经结束，不会再转换为其它状态

### 2.12 六种状态

这是从 Java API 层面来描述的 

根据 Thread.State 枚举，分为六种状态

- `NEW`  线程刚被创建，但是还没有调用  start() 方法 
- `RUNNABLE` 当调用了  start() 方法之后，注意，**Java AP**I 层面的  RUNNABLE 状态涵盖了 操作系统 层面的 【可运行状态】、【运行状态】和【阻塞状态】（由于 BIO 导致的线程阻塞，在 Java 里无法区分，仍然认为 是可运行）
- `BLOCKED` ， `WAITING` ， `TIMED_WAITING` 都是 **Java API** 层面对【阻塞状态】的细分，后面会在状态转换一节 
- `TERMINATED` 当线程代码运行结束

### 本章小结

本章的重点在于掌握

- 线程创建 
- 线程重要 api，如 start，run，sleep，join，interrupt 等 
- 线程状态 
- 应用方面
  - 异步调用：主线程执行期间，其它线程异步执行耗时操作 
  - 提高效率：并行计算，缩短运算时间 
  - 同步等待：join 
  - 统筹规划：合理使用线程，得到最优效果
- 原理方面
  - 线程运行流程：栈、栈帧、上下文切换、程序计数器 
  - Thread 两种创建方式 的源码
- 模式方面
  - 终止模式之两阶段终止

## 3. 共享模型之管程

### 3.1 共享带来的问题

#### 临界区 Critical Section

- 一个程序运行多个线程本身是没有问题的 
- 问题出在多个线程访问**共享资源**
  - 多个线程读**共享资源**其实也没有问题 
  - 在多个线程对**共享资源**读写操作时发生指令交错，就会出现问题
- 一段代码块内如果存在对**共享资源**的多线程读写操作，称这段代码块为**临界区**

例如，下面代码中的临界区

```java
static int counter = 0;
 static void increment() 
// 临界区
{    
	counter++;
}

static void decrement() 
// 临界区
{    
	counter--;
}
```

#### 竞态条件 Race Condition

多个线程在临界区内执行，由于代码的**执行序列不同**而导致结果无法预测，称之为发生了**竞态条件**

### 3.2 synchronized 解决方案

#### *应用之互斥

为了避免临界区的竞态条件发生，有多种手段可以达到目的

- 阻塞式的解决方案：synchronized，Lock
- 非阻塞式的解决方案：原子变量

本次使用阻塞式的解决方案：synchronized，来解决上述问题，即俗称的【对象锁】，它采用互斥的方式让同一 时刻至多只有一个线程能持有【对象锁】，其它线程再想获取这个【对象锁】时就会阻塞住。这样就能保证拥有锁 的线程可以安全的执行临界区内的代码，不用担心线程上下文切换

>**注意** 
>
>虽然 java 中互斥和同步都可以采用 synchronized 关键字来完成，但它们还是有区别的：
>
>- 互斥是保证临界区的竞态条件发生，同一时刻只能有一个线程执行临界区代码 
>- 同步是由于线程执行的先后、顺序不同、需要一个线程等待其它线程运行到某个点

#### synchronized

语法

```java
synchronized(对象) // 线程1， 线程2(blocked)
{
	临界区
}
```

解决

```java
static int counter = 0;
static final Object room = new Object();
public static void main(String[] args) throws InterruptedException {
    Thread t1 = new Thread(() -> {
        for (int i = 0; i < 5000; i++) {
            synchronized (room) {
                counter++;
            }
        }
    }, "t1");
    Thread t2 = new Thread(() -> {
        for (int i = 0; i < 5000; i++) {
            synchronized (room) {
                counter--;
            }
        }
    }, "t2");

    t1.start();
    t2.start();
    t1.join();
    t2.join();
    log.debug("{}", counter);
}
```

你可以做这样的类比：

- `synchronized(对象) `中的对象，可以想象为一个房间（room），有唯一入口（门）房间只能一次进入一人 进行计算，线程 t1，t2 想象成两个人
- 当线程 t1 执行到  `synchronized(room)` 时就好比 t1 进入了这个房间，并锁住了门拿走了钥匙，在门内执行  `count++` 代码
- 这时候如果 t2 也运行到了  `synchronized(room) `时，它发现门被锁住了，只能在门外等待，发生了上下文切 换，阻塞住了
- 这中间即使 t1 的 cpu 时间片不幸用完，被踢出了门外（不要错误理解为锁住了对象就能一直执行下去哦）， 这时门还是锁住的，t1 仍拿着钥匙，t2 线程还在阻塞状态进不来，只有下次轮到 t1 自己再次获得时间片时才 能开门进入
- 当 t1 执行完 ` synchronized{} `块内的代码，这时候才会从 obj 房间出来并解开门上的锁，唤醒 t2 线程把钥 匙给他。t2 线程这时才可以进入 obj 房间，锁住了门拿上钥匙，执行它的  `count--` 代码

#### 面向对象改进

把需要保护的共享变量放入一个类

```java
class Room {
    int value = 0;
    public void increment() {
        synchronized (this) {
            value++;
        }
    }
    public void decrement() {
        synchronized (this) {
            value--;
        }
    }
    public int get() {
        synchronized (this) {
            return value;
        }
    }
}
@Slf4j
public class Test1 {
    public static void main(String[] args) throws InterruptedException {
        Room room = new Room();
        Thread t1 = new Thread(() -> {
            for (int j = 0; j < 5000; j++) {
                room.increment();
            }
        },
                "t1");
        Thread t2 = new Thread(() -> {
            for (int j = 0; j < 5000; j++) {
                room.decrement();
            }
        },
                "t2");
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        log.debug("count: {}" , room.get());
    }
}
```

### 3.3 方法上的 synchronized

```java
class Test{
    public synchronized void test() {
    }
}
等价于
class Test{
    public void test() {
        synchronized(this) {
        }
    }
}
```

```java
class Test{
    public synchronized static void test() {
    }
}
等价于
class Test {
    public static void test() {
        synchronized (Test.class) {
        }
    }
}
```

#### 不加 synchronized 的方法

不加 synchronzied 的方法就好比不遵守规则的人，不去老实排队（好比翻窗户进去的）

#### 所谓的“线程八锁”

其实就是考察 synchronized 锁住的是哪个对象 

情况1:  12 或 21

```java
@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a() {
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n1.b(); }).start();
}
```

情况2： 1s后12，或 2 1s后 

```java
@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n1.b(); }).start();
}
```

情况3： 3 1s 12 或  23 1s 1 或 32 1s 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
    public void c() {
        log.debug("3");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n1.b(); }).start();
    new Thread(()->{ n1.c(); }).start();
}
```

情况4： 2 1s 后 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    Number n2 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n2.b(); }).start();
}
```

情况5： 2 1s 后 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n1.b(); }).start();
}
```

情况6： 1s 后12， 或 2 1s后 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public static synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n1.b(); }).start();
}
```

情况7： 2 1s 后 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    Number n2 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n2.b(); }).start();
}
```

情况8： 1s 后12， 或 2 1s后 1

```java
@Slf4j(topic = "c.Number")
class Number{
    public static synchronized void a() {
        sleep(1);
        log.debug("1");
    }
    public static synchronized void b() {
        log.debug("2");
    }
}
public static void main(String[] args) {
    Number n1 = new Number();
    Number n2 = new Number();
    new Thread(()->{ n1.a(); }).start();
    new Thread(()->{ n2.b(); }).start();
}
```

### 3.4 变量的线程安全分析

#### 成员变量和静态变量是否线程安全？

- 如果它们没有共享，则线程安全 
- 如果它们被共享了，根据它们的状态是否能够改变，又分两种情况
  - 如果只有读操作，则线程安全 
  - 如果有读写操作，则这段代码是临界区，需要考虑线程安全

#### 局部变量是否线程安全？

- 局部变量是线程安全的 
- 但局部变量引用的对象则未必
  - 如果该对象没有逃离方法的作用访问，它是线程安全的 
  - 如果该对象逃离方法的作用范围，需要考虑线程安全

#### 常见线程安全类

- String 
- Integer 
- StringBuffer 
- Random 
- Vector 
- Hashtable 
- java.util.concurrent 包下的类

这里说它们是线程安全的是指，多个线程调用它们同一个实例的某个方法时，是线程安全的。也可以理解为

```java
Hashtable table = new Hashtable();
 new Thread(()->{
        table.put("key", "value1");
 }).start();
 new Thread(()->{
        table.put("key", "value2");
 }).start();
```

- 它们的每个方法是原子的 
- 但**注意**它们多个方法的组合不是原子的，见后面分析

#### 线程安全类方法的组合

分析下面代码是否线程安全？

```java
Hashtable table = new Hashtable();
// 线程1，线程2
 if( table.get("key") == null) {
        table.put("key", value);
 }
```

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E7%BA%BF%E7%A8%8B%E6%88%AA%E5%9B%BE2025-07-02%20000628.png)

#### 不可变类线程安全性

String、Integer 等都是不可变类，因为其内部的状态不可以改变，因此它们的方法都是线程安全的

有同学或许有疑问，String 有 replace，substring 等方法【可以】改变值啊，那么这些方法又是如何保证线程安全的呢？

它是创建一个新的对象来返回，并不是对原本的对象进行修改，是一种保护性复制

### 3.5 Monitor 概念

#### Java 对象头

以 32 位虚拟机为例

普通对象

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E5%AF%B9%E8%B1%A1%E5%A4%B4%202025-07-02%20001356.png)

数组对象

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E5%AF%B9%E8%B1%A1%E5%A4%B4%202025-07-02%20001403.png)

其中 Mark Word 结构为

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E5%AF%B9%E8%B1%A1%E5%A4%B4%202025-07-02%20001410.png)

64 位虚拟机 Mark Word

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E5%AF%B9%E8%B1%A1%E5%A4%B4%20%202025-07-02%20001421.png)

使用`synchronozed`会将对象头的`Mark Word` 字段替换成指向`Monitor`对象地址

### 3.6 wait notify

#### API 介绍

- `obj.wait() `让进入 object 监视器的线程到 waitSet 等待 
- `obj.notify() `在 object 上正在 waitSet 等待的线程中挑一个唤醒  
- `obj.notifyAll() `让 object 上正在 waitSet 等待的线程全部唤醒

`wait()` 方法会释放对象的锁，进入 WaitSet 等待区，从而让其他线程就机会获取对象的锁。无限制等待，直到 notify 为止

`wait(long n) `有时限的等待, 到 n 毫秒后结束等待，或是被 notify

### 3.7 wait& notify 正确姿势

开始之前先看看

#### `sleep(long n) `和 ` wait(long n)` 的区别

1) sleep 是 Thread 方法，而 wait 是 Object 的方法 

2) sleep 不需要强制和 synchronized 配合使用，但 wait 需要 和 synchronized 一起用 
3) sleep 在睡眠的同时，不会释放对象锁的，但 wait 在等待的时候会释放对象锁 
4)  它们 状态 TIMED_WAITING

- notify 只能随机唤醒一个 WaitSet 中的线程，这时如果有其它线程也在等待，那么就可能唤醒不了正确的线 程，称之为【虚假唤醒】
- 解决方法，改为 notifyAll

 \* 模式之保护性暂停  

* 模式之生产者消费者

### 3.8 Park & Unpark

#### 基本使用

它们是 LockSupport 类中的方法

```java
// 暂停当前线程
LockSupport.park();
// 恢复某个线程的运行
LockSupport.unpark(暂停线程对象)
```

#### 特点

与 Object 的 wait & notify 相比

- wait，notify 和 notifyAll 必须配合 Object Monitor 一起使用，而 park，unpark 不必
- park & unpark 是以线程为单位来【阻塞】和【唤醒】线程，而 notify 只能随机唤醒一个等待线程，notifyAll  是唤醒所有等待线程，就不那么【精确】
- park & unpark 可以先 unpark，而 wait & notify 不能先 notify

### 3.9 重新理解线程状态转换

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E7%BA%BF%E7%A8%8B%E7%8A%B6%E6%80%81%E8%BD%AC%E6%8D%A2%202025-07-02%20003341.png)

假设有线程  `Thread t`

#### 情况 1`  NEW --> RUNNABLE`

- 当调用 ` t.start(`) 方法时，由  `NEW --> RUNNABLE`

#### 情况 2 ` RUNNABLE <--> WAITING`

t 线程用  `synchronized(obj)` 获取了对象锁后

- 调用 `obj.wait()` 方法时，t 线程从  `RUNNABLE --> WAITING`
- 调用 `obj.notify() `， `obj.notifyAll()` ， `t.interrupt() `时
  - 竞争锁成功，t 线程从   `WAITING --> RUNNABLE `
  - 竞争锁失败，t 线程从   `WAITING --> BLOCKED`

#### 情况 3  `RUNNABLE <--> WAITING`

- 当前线程调用  `t.join()` 方法时，当前线程从  `RUNNABLE --> WAITING`
  - 注意是当前线程在t 线程对象的监视器上等待
- t 线程运行结束，或调用了当前线程的  `interrupt() `时，当前线程从` WAITING --> RUNNABLE`

#### 情况 4 ` RUNNABLE <--> WAITING`

- 当前线程调用`LockSupport.park() `方法会让当前线程从 ` RUNNABLE --> WAITING`
- 调用  `LockSupport.unpark(目标线程)` 或调用了线程 的  `interrupt()` ，会让目标线程从`WAITING  -->RUNNABLE`

#### 情况 5  `RUNNABLE <--> TIMED_WAITING`

t 线程用  `synchronized(obj)` 获取了对象锁后

- 调用  `obj.wait(long n) `方法时，t 线程从  `RUNNABLE --> TIMED_WAITING`
-  t 线程等待时间超过了 n 毫秒，或调用  `obj.notify() `，` obj.notifyAll()` , `t.interrupt()` 时
  - 竞争锁成功，t 线程从  ` TIMED_WAITING --> RUNNABLE`
  - 竞争锁失败，t 线程从  ` TIMED_WAITING --> BLOCKED `

#### 情况 6  `RUNNABLE <--> TIMED_WAITING`

- 当前线程调用  `t.join(long n) `方法时，当前线程从  `RUNNABLE --> TIMED_WAITING`
  - 注意是当前线程在t 线程对象的监视器上等待
- 当前线程等待时间超过了 n 毫秒，或t 线程运行结束，或调用了当前线程的  `interrupt() `时，当前线程从` TIMED_WAITING --> RUNNABLE`

#### 情况 7  `RUNNABLE <--> TIMED_WAITING`

- 当前线程调用  `Thread.sleep(long n)` ，当前线程从 ` RUNNABLE --> TIMED_WAITING`
- 当前线程等待时间超过了 n 毫秒，当前线程从 `  TIMED_WAITING --> RUNNABLE `

#### 情况 8  `RUNNABLE <--> TIMED_WAITING`

- 当前线程调用  `LockSupport.parkNanos(long nanos) `或` LockSupport.parkUntil(long millis)` 时，当前线程从  `RUNNABLE --> TIMED_WAITING`
- 调用 `LockSupport.unpark(目标线程`) 或调用了线程 的  `interrupt() `，或是等待超时，会让目标线程从  `TIMED_WAITING--> RUNNABLE`

#### 情况 9  `RUNNABLE <--> BLOCKED`

- t 线程用  ` synchronized(obj)` 获取了对象锁时如果竞争失败，从   `RUNNABLE --> BLOCKED `
- 持 obj 锁线程的同步代码块执行完毕，会唤醒该对象上所有  `BLOCKED  `的线程重新竞争，如果其中 t 线程竞争 成功，从  `BLOCKED --> RUNNABLE` ，其它失败的线程仍然   `BLOCKED `

#### 情况 10  `RUNNABLE <--> TERMINATED`

当前线程所有代码运行完毕，进入 `TERMINATED`

### 3.10 多把锁

将锁的粒度细分

- 好处，是可以增强并发度 
- 坏处，如果一个线程需要同时获得多把锁，就容易发生死锁

### 3.11 活跃性

#### 死锁

有这样的情况：一个线程需要同时获取多把锁，这时就容易发生死锁

##### 核心特征：

- **相互等待**：每个线程都持有对方需要的资源，同时等待对方释放资源。
- **不可解除**：若无外部干预，死锁状态会永久持续，线程无法自行恢复。

##### 产生死锁的四个必要条件：

- **互斥条件**：资源只能被一个线程独占（如一把锁同一时间只能被一个线程持有）。
- **持有并等待**：线程持有至少一个资源，同时等待获取其他线程持有的资源。
- **不可剥夺条件**：线程已持有的资源不能被强制剥夺，只能由线程主动释放。
- **循环等待条件**：多个线程形成环形等待链（如线程 A 等待线程 B 的资源，线程 B 等待线程 C 的资源，线程 C 等待线程 A 的资源）。

经典问题：哲学家就餐问题

#### 定位死锁	

- 检测死锁可以使用 jconsole工具，或者使用 jps 定位进程 id，再用 jstack 定位死锁：
- 避免死锁要注意加锁顺序
- 另外如果由于某个线程进入了死循环，导致其它线程一直等待，对于这种情况 linux 下可以通过` top` 先定位到  CPU 占用高的 Java 进程，再利用  `top -Hp `进程id 来定位是哪个线程，最后再用` jstack` 排查

#### 活锁

活锁是并发编程中一种特殊的阻塞状态，指两个或多个线程（或进程）为了响应对方的行为而不断调整自身状态，最终导致所有线程都无法继续执行，陷入 “忙碌却无效” 的循环。

##### 核心特征：

- **线程未阻塞**：与死锁中线程完全阻塞不同，活锁中的线程处于运行状态，不断执行操作（如释放资源、重试）。
- **行为冲突**：线程间的行为相互干扰，例如为了礼让对方而反复释放资源，导致谁都无法获取完整资源继续执行。
- **无进展**：尽管线程在 “工作”，但整体任务没有任何推进，最终陷入无效循环。

##### **活锁的产生原因与解决思路**

- **产生原因**：线程过度 “礼让” 或重试逻辑设计不合理，导致行为冲突。
- 解决思路：
  - **引入随机性**：在重试前加入随机延迟（如随机睡眠时长），避免线程同步重试，降低冲突概率。
  - **固定优先级**：为线程设置获取资源的固定顺序（如都先获取 A 再获取 B），避免相互避让。
  - **限制重试次数**：设置最大重试阈值，超过后放弃或进入阻塞，避免无限循环。

#### 饥饿

在并发编程中，**饥饿（Starvation）** 指的是一个或多个线程（或进程）长期无法获得所需的资源（如 CPU 时间、锁、内存等），导致任务永远无法执行或长时间延迟执行的状态。

##### 核心特征：

- **资源分配不公**：部分线程因优先级低、竞争能力弱等原因，始终无法获得必要资源。
- **持续等待**：线程处于可运行状态（非阻塞），但因资源被其他线程长期占用，始终无法执行。
- **任务停滞**：饥饿线程的任务可能无限期延迟，甚至永远无法完成。

##### **解决思路**

1. **使用公平机制**：采用公平锁（如`ReentrantLock(true)`），确保线程按等待顺序获取资源，避免 “插队”。
2. **平衡优先级**：避免线程优先级差异过大，或动态调整优先级，防止低优先级线程被完全忽略。
3. **限制资源占用时间**：避免线程长期持有资源（如缩短锁持有时间、拆分长任务），减少对其他线程的阻塞。
4. **引入超时机制**：为资源等待设置超时（如`tryLock(timeout)`），避免线程无限期等待。

### 3.12 ReentrantLock

相对于 synchronized 它具备如下特点

- 可中断 
- 可以设置超时时间 
- 可以设置为公平锁 
- 支持多个条件变量

与 synchronized 一样，都支持可重入 

基本语法

```java
// 获取锁
reentrantLock.lock();
 try {
         // 临界区
 } finally {
         // 释放锁
         reentrantLock.unlock();
 }
```

#### 可重入（synchronized也可重入）

可重入是指同一个线程如果首次获得了这把锁，那么因为它是这把锁的拥有者，因此有权利再次获取这把锁 如果是不可重入锁，那么第二次获得锁时，自己也会被锁挡住

#### 可打断

ReentrantLock在等待获取锁时可以被打断，避免长时间的阻塞

#### 锁超时

ReentrantLock提供了tryLock方法指定获取锁的时间，若在指定时间内未获取到锁，返回false

立刻失败

```java
ReentrantLock lock = new ReentrantLock();
Thread t1 = new Thread(() -> {
    log.debug("启动...");
    if (!lock.tryLock()) {
        log.debug("获取立刻失败，返回");
        return;
    }
    try {
        log.debug("获得了锁");
    }
    finally {
        lock.unlock();
    }
}, "t1");
lock.lock();
log.debug("获得了锁");
t1.start();
try {
     sleep(2);
} finally {
     lock.unlock();
}
```

超时失败：

```java
ReentrantLock lock = new ReentrantLock();
Thread t1 = new Thread(() -> {
    log.debug("启动...");
    try {
        if (!lock.tryLock(1, TimeUnit.SECONDS)) {
            log.debug("获取等待 1s 后失败，返回");
            return;
        }
    }
    catch (InterruptedException e) {
        e.printStackTrace();
    }
    try {
        log.debug("获得了锁");
    }
    finally {
        lock.unlock();
    }
}, "t1");
 lock.lock();
 log.debug("获得了锁");
 t1.start();
 try {
sleep(2);
 } finally {
         lock.unlock();
 }
```

#### 公平锁

在并发编程中，**公平锁（Fair Lock）** 是一种遵循 “先到先得” 原则的同步机制，即线程获取锁的顺序与它们请求锁的顺序一致，等待时间最长的线程会优先获得锁，避免线程因 “插队” 而长期饥饿。

公平锁一般没有必要，会降低并发度

#### 条件变量

synchronized 中也有条件变量，就是我们讲原理时那个 waitSet 休息室，当条件不满足时进入 waitSet 等待 ReentrantLock 的条件变量比 synchronized 强大之处在于，它是支持多个条件变量的，这就好比

- synchronized 是那些不满足条件的线程都在一间休息室等消息 
- 而 ReentrantLock 支持多间休息室，有专门等烟的休息室、专门等早餐的休息室、唤醒时也是按休息室来唤 醒

使用要点：

- await 前需要获得锁 
- await 执行后，会释放锁，进入 conditionObject 等待 
- await 的线程被唤醒（或打断、或超时）取重新竞争 lock 锁 
- 竞争 lock 锁成功后，从 await 后继续执行

### 本章小结

本章我们需要重点掌握的是

- 分析多线程访问共享资源时，哪些代码片段属于临界区 
- 使用 synchronized 互斥解决临界区的线程安全问题
  - 掌握 synchronized 锁对象语法 
  - 掌握 synchronzied 加载成员方法和静态方法语法 
  - 掌握 wait/notify 同步方法
- 使用 lock 互斥解决临界区的线程安全问题
  - 掌握 lock 的使用细节：可打断、锁超时、公平锁、条件变量
- 学会分析变量的线程安全性、掌握常见线程安全类的使用
- 了解线程活跃性问题：死锁、活锁、饥饿 
- 应用方面
  - 互斥：使用 synchronized 或 Lock 达到共享资源互斥效果 
  - 同步：使用 wait/notify 或 Lock 的条件变量来达到线程间通信效果
- 原理方面
  - monitor、synchronized 、wait/notify 原理 
  - synchronized 进阶原理 
  - park & unpark 原理
- 模式方面
  - 同步模式之保护性暂停 
  - 异步模式之生产者消费者 
  - 同步模式之顺序控制

## 4.共享模型之内存

### 4.1 Java 内存模型

JMM 即 Java Memory Model，它定义了主存、工作内存抽象概念，底层对应着 CPU 寄存器、缓存、硬件内存、 CPU 指令优化等。 

JMM 体现在以下几个方面

- 原子性 - 保证指令不会受到线程上下文切换的影响 
- 可见性 - 保证指令不会受 cpu 缓存的影响 
- 有序性 - 保证指令不会受 cpu 指令并行优化的影响

### 4.2 可见性

#### 退不出的循环

先来看一个现象，main 线程对 run 变量的修改对于 t 线程不可见，导致了 t 线程无法停止：

```java
static boolean run = true;
public static void main(String[] args) throws InterruptedException {
    Thread t = new Thread(() -> {
        while (run) {
            // ....
        }
    });
    t.start();

    sleep(1);
    run = false; // 线程t不会如预想的停下来
}
```

为什么呢？分析一下：

1. 初始状态， t 线程刚开始从主内存读取了 run 的值到工作内存。
2. 因为 t 线程要频繁从主内存中读取 run 的值，JIT 编译器会将 run 的值缓存至自己工作内存中的高速缓存中， 减少对主存中 run 的访问，提高效率
3.  1 秒之后，main 线程修改了 run 的值，并同步至主存，而 t 是从自己工作内存中的高速缓存中读取这个变量 的值，结果永远是旧值

#### 解决方法

volatile（易变关键字） 

它可以用来修饰成员变量和静态成员变量，他可以避免线程从自己的工作缓存中查找变量的值，必须到主存中获取 它的值，线程操作 volatile 变量都是直接操作主存

#### 可见性 vs 原子性

前面例子体现的实际就是可见性，它保证的是在多个线程之间，一个线程对 volatile 变量的修改对另一个线程可 见， 不能保证原子性，仅用在一个写线程，多个读线程的情况： 上例从字节码理解是这样的：

```java
getstatic     run   // 线程 t 获取 run true 
getstatic     run   // 线程 t 获取 run true 
getstatic     run   // 线程 t 获取 run true 
getstatic     run   // 线程 t 获取 run true 
putstatic     run  //  线程 main 修改 run 为 false， 仅此一次 
getstatic     run   // 线程 t 获取 run false
```

>**注意** synchronized 语句块既可以保证代码块的原子性，也同时保证代码块内变量的可见性。但缺点是 synchronized 是属于重量级操作，性能相对更低 
>
>如果在前面示例的死循环中加入 System.out.println() 会发现即使不加 volatile 修饰符，线程 t 也能正确看到 对 run 变量的修改了，想一想为什么？

### 4.3 有序性

#### 指令重排

JVM 会在不影响正确性的前提下，可以调整语句的执行顺序，思考下面一段代码

```java
static int i;
static int j;
// 在某个线程内执行如下赋值操作
i = ...;
j = ...;
```

可以看到，至于是先执行 i 还是 先执行 j ，对最终的结果不会产生影响。所以，上面代码真正执行时，既可以是

```java
i = ...; 
j = ...;
```

也可以是

```java
j = ...;
i = ...; 
```

这种特性称之为『指令重排』，多线程下『指令重排』会影响正确性。

- 现代 CPU 采用流水线（Pipeline）、多核等架构，指令执行需经过取指、译码、执行、访存、写回等多个阶段。
- 通过指令重排，CPU 可调整无依赖关系的指令顺序（如将不相关的指令插入等待间隙），充分利用硬件资源，减少流水线空置时间，提高吞吐量。

#### 解决方法

volatile 修饰的变量，可以禁用指令重排

- volatile通过读屏障和写屏障来防止指令重排（写屏障之前的代码不能被重排序到写屏障之后）
- 和可见性（写屏障会将写屏障之前的赋值操作刷新到主存里，读屏障会让读屏障之后的读取操作都从主存中读取，而不是直接读自己的工作内存）

#### happens-before

happens-before 规定了对共享变量的写操作对其它线程的读操作可见，它是可见性与有序性的一套规则总结，抛 开以下 happens-before 规则，JMM 并不能保证一个线程对共享变量的写，对于其它线程对该共享变量的读可见

- 线程解锁 m 之前对变量的写，对于接下来对 m 加锁的其它线程对该变量的读可见
- 线程对 volatile 变量的写，对接下来其它线程对该变量的读可见
- 线程 start 前对变量的写，对该线程开始后对该变量的读可见
- 线程结束前对变量的写，对其它线程得知它结束后的读可见（比如其它线程调用 t1.isAlive() 或 t1.join()等待 它结束）
- 线程 t1 打断 t2（interrupt）前对变量的写，对于其他线程得知 t2 被打断后对变量的读可见（通过 t2.interrupted 或 t2.isInterrupted）
- 对变量默认值（0，false，null）的写，对其它线程对该变量的读可见
- 具有传递性，如果  x hb-> y 并且  y hb-> z 那么有  x hb-> z ，配合 volatile 的防指令重排，有下面的例子

```java
volatile static int x;
static int y;
 new Thread(()->{
y = 10;
x = 20;
        },"t1").start();
 new Thread(()->{
        // x=20 对 t2 可见, 同时 y=10 也对 t2 可见
        System.out.println(x); 
},"t2").start();
```

>变量都是指成员变量或静态成员变量

### balking 模式

`balking` 模式（犹豫模式）是一种并发设计模式，核心思想是：**当线程发现某个操作不适合在当前状态下执行时/或其它线程正在执行相同的操作时，直接放弃该操作而不等待**。它适用于 “只需要一个线程完成目标操作” 或 “操作仅在特定状态下有效” 的场景，避免无效的等待和资源浪费。

### 本章小结

本章重点讲解了 JMM 中的

- 可见性 - 由 JVM 缓存优化引起 
- 有序性 - 由 JVM 指令重排序优化引起 
- happens-before 规则 
- 原理方面
  - CPU 指令并行 
  - volatile
- 模式方面
  - 两阶段终止模式的 volatile 改进 
  - 同步模式之 balking

##  5.共享模型之无锁

- CAS 与 volatile 
- 原子整数 
- 原子引用 
- 原子累加器 
- Unsafe

### 5.1 CAS 与 volatile

#### CAS 的特点

原子类型具有 compareAndSet方法，它的简称就是 CAS （也有 Compare And Swap 的说法），它是原子操作

结合 CAS 和 volatile 可以实现无锁并发，适用于线程数少、多核 CPU 的场景下

- CAS 是基于乐观锁的思想：最乐观的估计，不怕别的线程来修改共享变量，就算改了也没关系，我吃亏点再 重试呗。
- synchronized 是基于悲观锁的思想：最悲观的估计，得防着其它线程来修改共享变量，我上了锁你们都别想 改，我改完了解开锁，你们才有机会。
- CAS 体现的是无锁并发、无阻塞并发，请仔细体会这两句话的意思
  - 因为没有使用 synchronized，所以线程不会陷入阻塞，这是效率提升的因素之一 
  - 但如果竞争激烈，可以想到重试必然频繁发生，反而效率会受影响

>CAS 的底层是   `lock cmpxchg` 指令（X86 架构），在单核 CPU 和多核 CPU 下都能够保证【比较-交换】的原子性。
>
>在多核状态下，某个核执行到带 lock 的指令时，CPU 会让总线锁住，当这个核把此指令执行完毕，再 开启总线。这个过程中不会被线程的调度机制所打断，保证了多个线程对内存操作的准确性，是原子的.



#### volatile

获取共享变量时，为了保证该变量的可见性，需要使用 volatile 修饰。

它可以用来修饰成员变量和静态成员变量，他可以避免线程从自己的工作缓存中查找变量的值，必须到主存中获取 它的值，线程操作 volatile 变量都是直接操作主存。即一个线程对 volatile 变量的修改，对另一个线程可见。

> **注意** 
>
> volatile 仅仅保证了共享变量的可见性，让其它线程能够看到最新值，但不能解决指令交错问题（不能保证原子性)

CAS 必须借助 volatile 才能读取到共享变量的最新值来实现【比较并交换】的效果

#### 为什么无锁效率高

- 无锁情况下，即使重试失败，线程始终在高速运行，没有停歇，而 synchronized 会让线程在没有获得锁的时 候，发生上下文切换，进入阻塞。打个比喻
- 线程就好像高速跑道上的赛车，高速运行时，速度超快，一旦发生上下文切换，就好比赛车要减速、熄火， 等被唤醒又得重新打火、启动、加速... 恢复到高速运行，代价比较大
- 但无锁情况下，因为线程要保持运行，需要额外 CPU 的支持，CPU 在这里就好比高速跑道，没有额外的跑道，线程想高速运行也无从谈起，虽然不会进入阻塞，但由于没有分到时间片，仍然会进入可运行状态，还是会导致上下文切换。

### 5.2 原子整数

J.U.C 并发包提供了：

- AtomicBoolean 
- AtomicInteger 
- AtomicLong

以 AtomicInteger 为例

```java
AtomicInteger i = new AtomicInteger(0);
// 获取并自增（i = 0, 结果 i = 1, 返回 0），类似于 i++
System.out.println(i.getAndIncrement());
// 自增并获取（i = 1, 结果 i = 2, 返回 2），类似于 ++i
System.out.println(i.incrementAndGet());
// 自减并获取（i = 2, 结果 i = 1, 返回 1），类似于 --i
System.out.println(i.decrementAndGet());
// 获取并自减（i = 1, 结果 i = 0, 返回 1），类似于 i-
System.out.println(i.getAndDecrement());
// 获取并加值（i = 0, 结果 i = 5, 返回 0）
System.out.println(i.getAndAdd(5));
// 加值并获取（i = 5, 结果 i = 0, 返回 0）
System.out.println(i.addAndGet(-5));
// 获取并更新（i = 0, p 为 i 的当前值, 结果 i = -2, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
System.out.println(i.getAndUpdate(p -> p - 2));
// 更新并获取（i = -2, p 为 i 的当前值, 结果 i = 0, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
System.out.println(i.updateAndGet(p -> p + 2));
// 获取并计算（i = 0, p 为 i 的当前值, x 为参数1, 结果 i = 10, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
// getAndUpdate 如果在 lambda 中引用了外部的局部变量，要保证该局部变量是 final 的
// getAndAccumulate 可以通过 参数1 来引用外部的局部变量，但因为其不在 lambda 中因此不必是 final
System.out.println(i.getAndAccumulate(10, (p, x) -> p + x));
// 计算并获取（i = 10, p 为 i 的当前值, x 为参数1, 结果 i = 0, 返回 0）
// 其中函数中的操作能保证原子，但函数需要无副作用
System.out.println(i.accumulateAndGet(-10, (p, x) -> p + x));
```

### 5.3 原子引用

为什么需要原子引用类型？

- AtomicReference 
- AtomicMarkableReference 
- AtomicStampedReference

#### 保证对象引用的原子更新

通过`compareAndSet`（CAS）等方法，原子引用类型可实现 “比较当前引用是否为预期值，若是则更新为新引用” 的原子操作，避免并发修改导致的引用不一致。
示例（Java `AtomicReference`）：

```java
AtomicReference<User> userRef = new AtomicReference<>(new User("初始用户"));
User oldUser = userRef.get(); // 读取当前引用
User newUser = new User("新用户");
// 原子更新：若当前引用仍为oldUser，则更新为newUser
boolean success = userRef.compareAndSet(oldUser, newUser);
```

该操作是原子的，不会被其他线程的修改打断。

#### 解决基本原子类的局限

- 支持任意对象类型，而非仅基本类型。
- 可结合对象的属性进行复杂逻辑的原子操作（如基于旧对象的属性计算新对象）。

#### ABA 问题及解决  

##### AtomicStampedReference

普通原子引用存在 ABA 问题（对象引用被修改为 B 后又改回 A，CAS 会误判为未修改）。而`AtomicStampedReference`通过 “引用 + 版本号” 的组合，在 CAS 时同时检查引用和版本号，避免 ABA 问题。
示例：

```java
// 初始化：引用为"初始值"，版本号为0
AtomicStampedReference<String> ref = new AtomicStampedReference<>("初始值", 0);
int oldStamp = 0;
String oldRef = ref.getReference();
String newRef = "新值";
int newStamp = 1;
// 同时检查引用和版本号，均匹配才更新
boolean success = ref.compareAndSet(oldRef, newRef, oldStamp, newStamp);
```

##### AtomicMarkableReference

而`AtomicMarkableReference`通过 “引用 + 标记位” 的组合，在 CAS 时同时检查引用和标记位，可以判断该引用是否被修改过

### 5.4 原子数组

- AtomicIntegerArray 
- AtomicLongArray 
- AtomicReferenceArray

有如下方法

```java
/**
 * 参数1，提供数组、可以是线程不安全数组或线程安全数组
 * 参数2，获取数组长度的方法
 * 参数3，自增方法，回传 array, index
 * 参数4，打印数组的方法
 */
// supplier 提供者 无中生有  ()->结果
// function 函数   一个参数一个结果   (参数)->结果  ,  BiFunction (参数1,参数2)->结果
// consumer 消费者 一个参数没结果  (参数)->void,      BiConsumer (参数1,参数2)->
private static <T> void demo(
        Supplier<T> arraySupplier,
        Function<T, Integer> lengthFun,
        BiConsumer<T, Integer> putConsumer,
        Consumer<T> printConsumer) {
    List<Thread> ts = new ArrayList<>();
    T array = arraySupplier.get();
    int length = lengthFun.apply(array);
    for (int i = 0; i < length; i++) {
        // 每个线程对数组作 10000 次操作
        ts.add(new Thread(() -> {
            for (int j = 0; j < 10000; j++) {
                putConsumer.accept(array, j % length);
            }
        }));
    }
    ts.forEach(t -> t.start()); // 启动所有线程
    ts.forEach(t -> {
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });   // 等所有线程结束
    printConsumer.accept(array);
}
```

不安全的数组

```java
demo(
	()->new int[10],
	(array)->array.length,
	(array, index) -> array[index]++,
	array-> System.out.println(Arrays.toString(array))
);
```

结果

```
[9870, 9862, 9774, 9697, 9683, 9678, 9679, 9668, 9680, 9698] 
```

安全的数组

```java
demo(
    ()-> new AtomicIntegerArray(10),
    (array) -> array.length(),
    (array, index) -> array.getAndIncrement(index),
 	array -> System.out.println(array)
 );
```

结果

```
[10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000]
```

### 5.5 字段更新器

- AtomicReferenceFieldUpdater // 域  字段 
- AtomicIntegerFieldUpdater 
- AtomicLongFieldUpdater

利用字段更新器，可以针对对象的某个域（Field）进行原子操作，只能配合 volatile 修饰的字段使用，否则会出现 异常：

```shell
Exception in thread "main" java.lang.IllegalArgumentException: Must be volatile type
```



```java
public class Test5 {
    private volatile int field;
    public static void main(String[] args) {
        AtomicIntegerFieldUpdater fieldUpdater = AtomicIntegerFieldUpdater.newUpdater(Test5.class, "field");
        Test5 test5 = new Test5();
        fieldUpdater.compareAndSet(test5, 0, 10);
        // 修改成功 field = 10
        System.out.println(test5.field);
        // 修改成功 field = 20
        fieldUpdater.compareAndSet(test5, 10, 20);
        System.out.println(test5.field);
        // 修改失败 field = 20
        fieldUpdater.compareAndSet(test5, 10, 30);
        System.out.println(test5.field);
    }
}
```

输出

```
10
20
20
```

### 5.6 原子累加器

#### 累加器性能比较

```java
private static <T> void demo(Supplier<T> adderSupplier, Consumer<T> action) {
    T adder = adderSupplier.get();
    long start = System.nanoTime();
    List<Thread> ts = new ArrayList<>();
    // 4 个线程，每人累加 50 万
    for (int i = 0; i < 40; i++) {
        ts.add(new Thread(() -> {
            for (int j = 0; j < 500000; j++) {
                action.accept(adder);
            }
        }));
    }
    ts.forEach(t -> t.start());
    ts.forEach(t -> {
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    });
    long end = System.nanoTime();
    System.out.println(adder + " cost:" + (end - start) / 1000_000);
}
```

比较 AtomicLong 与 LongAdder

```java
 for (int i = 0; i < 5; i++) {
 	demo(() -> new LongAdder(), adder -> adder.increment());
 }
 for (int i = 0; i < 5; i++) {
 	demo(() -> new AtomicLong(), adder -> adder.getAndIncrement());
 }
```

输出

```
1000000 cost:43 
1000000 cost:9 
1000000 cost:7 
1000000 cost:7 
1000000 cost:7 

1000000 cost:31 
1000000 cost:27 
1000000 cost:28 
1000000 cost:24 
1000000 cost:22
```

性能提升的原因很简单，就是在有竞争时，设置多个累加单元，Therad-0 累加 Cell[0]，而 Thread-1 累加 Cell[1]... 最后将结果汇总。这样它们在累加时操作的不同的 Cell 变量，因此减少了 CAS 重试失败，从而提高性能

\* 源码之 LongAdder

LongAdder 是并发大师 @author Doug Lea （大哥李）的作品，设计的非常精巧

LongAdder 类有几个关键域

```java
// 累加单元数组, 懒惰初始化
transient volatile Cell[] cells;
// 基础值, 如果没有竞争, 则用 cas 累加这个域
transient volatile long base;
// 在 cells 创建或扩容时, 置为 1, 表示加锁
transient volatile int cellsBusy;
```

\* 原理之伪共享

其中 Cell 即为累加单元

```java
// 防止缓存行伪共享
@sun.misc.Contended
static final class Cell {
    volatile long value;

    Cell(long x) {
        value = x;
    }

    // 最重要的方法, 用来 cas 方式进行累加, prev 表示旧值, next 表示新值
    final boolean cas(long prev, long next) {
        return UNSAFE.compareAndSwapLong(this, valueOffset, prev, next);
    }
// 省略不重要代码
}
```

> @sun.misc.Contended注解可以使一个缓存行只含有一个变量，多余的空间补空
>
> 当一个缓存行只含有一个变量时，就不存在伪共享了

#### 伪共享的产生逻辑

当多个线程同时操作**不同的共享变量**，但这些变量恰好位于**同一缓存行**时：

1. 线程 A 修改变量 X → 该缓存行被标记为 “失效”（缓存一致性协议如 MESI 的要求）。
2. 线程 B 虽操作的是同缓存行中的变量 Y（与 X 无关），但因缓存行失效，必须重新从主内存加载数据 → 产生额外的内存开销和延迟。

这种 “**因无关变量共享缓存行，导致无辜线程被迫承受缓存失效**” 的现象，就是伪共享。

### 5.7 Unsafe

#### 概述

Unsafe 对象提供了非常底层的，操作内存、线程的方法，Unsafe 对象不能直接调用，只能通过反射获得

```java
public class UnsafeAccessor {
    static Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    static Unsafe getUnsafe() {
        return unsafe;
    }
}
```

#### Unsafe CAS 操作

```java
@Data
class Student {
    volatile int id;
    volatile String name;
}
```

```java
Unsafe unsafe = UnsafeAccessor.getUnsafe();
Field id = Student.class.getDeclaredField("id");
Field name = Student.class.getDeclaredField("name");
// 获得成员变量的偏移量
long idOffset = UnsafeAccessor.unsafe.objectFieldOffset(id);
long nameOffset = UnsafeAccessor.unsafe.objectFieldOffset(name);

Student student = new Student();
// 使用 cas 方法替换成员变量的值
UnsafeAccessor.unsafe.compareAndSwapInt(student, idOffset, 0, 20);  // 返回 true
 UnsafeAccessor.unsafe.compareAndSwapObject(student, nameOffset, null, "张三"); // 返回 true

System.out.println(student);
```

输出

```java
Student(id=20, name=张三)
```



使用自定义的 AtomicData 实现之前线程安全的原子整数 Account 实现

```java
class AtomicData {
    private volatile int data;
    static final Unsafe unsafe;
    static final long DATA_OFFSET;

    static {
        unsafe = UnsafeAccessor.getUnsafe();
        try {
            // data 属性在 DataContainer 对象中的偏移量，用于 Unsafe 直接访问该属性
            DATA_OFFSET = unsafe.objectFieldOffset(AtomicData.class.getDeclaredField("data"));
        } catch (NoSuchFieldException e) {
            throw new Error(e);
        }
    }

    public AtomicData(int data) {
        this.data = data;
    }

    public void decrease(int amount) {
        int oldValue;
        while (true) {
            // 获取共享变量旧值，可以在这一行加入断点，修改 data 调试来加深理解
            oldValue = data;
            // cas 尝试修改 data 为 旧值 + amount，如果期间旧值被别的线程改了，返回 false
            if (unsafe.compareAndSwapInt(this, DATA_OFFSET, oldValue, oldValue - amount)) {
                return;
            }
        }
    }

    public int getData() {
        return data;
    }
}
```

 Account 实现

```java
Account.demo(new Account() {
    AtomicData atomicData = new AtomicData(10000);
    @Override
    public Integer getBalance() {
        return atomicData.getData();
    }
    @Override
    public void withdraw(Integer amount) {
        atomicData.decrease(amount);
    }
});
```

## 6.共享模型之不可变

- 不可变类的使用 
- 不可变类设计 
- 无状态类设计

### 6.1 日期转换的问题

下面的代码在运行时，由于 SimpleDateFormat 不是线程安全的

```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 for (int i = 0; i < 10; i++) {
        new Thread(() -> {
        try {
            log.debug("{}", sdf.parse("1951-04-21"));
        }catch (Exception e) {
            log.error("{}", e);
        }}).start();
 }
```

有很大几率出现 java.lang.NumberFormatException 或者出现不正确的日期解析结果，例如：

```shell
19:10:40.859 [Thread-2] c.TestDateParse - {} 
java.lang.NumberFormatException: For input string: "" 
	at java.lang.NumberFormatException.forInputString(NumberFormatException.java:65) 
	at java.lang.Long.parseLong(Long.java:601) 
	at java.lang.Long.parseLong(Long.java:631) 
	at java.text.DigitList.getLong(DigitList.java:195) 
	at java.text.DecimalFormat.parse(DecimalFormat.java:2084) 
	at java.text.SimpleDateFormat.subParse(SimpleDateFormat.java:2162) 
	at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1514) 
	at java.text.DateFormat.parse(DateFormat.java:364) 
	at cn.itcast.n7.TestDateParse.lambda$test1$0(TestDateParse.java:18) 
	at java.lang.Thread.run(Thread.java:748) 
19:10:40.859 [Thread-1] c.TestDateParse - {} 
java.lang.NumberFormatException: empty String 
	at sun.misc.FloatingDecimal.readJavaFormatString(FloatingDecimal.java:1842) 
	at sun.misc.FloatingDecimal.parseDouble(FloatingDecimal.java:110) 
	at java.lang.Double.parseDouble(Double.java:538) 
	at java.text.DigitList.getDouble(DigitList.java:169) 
	at java.text.DecimalFormat.parse(DecimalFormat.java:2089) 
	at java.text.SimpleDateFormat.subParse(SimpleDateFormat.java:2162) 
	at java.text.SimpleDateFormat.parse(SimpleDateFormat.java:1514) 
	at java.text.DateFormat.parse(DateFormat.java:364) 
	at cn.itcast.n7.TestDateParse.lambda$test1$0(TestDateParse.java:18) 
    at java.lang.Thread.run(Thread.java:748) 
19:10:40.857 [Thread-8] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-9] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-6] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-4] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-5] c.TestDateParse - Mon Apr 21 00:00:00 CST 178960645 
19:10:40.857 [Thread-0] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-7] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951 
19:10:40.857 [Thread-3] c.TestDateParse - Sat Apr 21 00:00:00 CST 1951
```

#### 思路 - 同步锁

这样虽能解决问题，但带来的是性能上的损失，并不算很好：

```java
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 for (int i = 0; i < 50; i++) {
        new Thread(() -> {
            synchronized (sdf) {
                try {
                    log.debug("{}", sdf.parse("1951-04-21"));
                }catch (Exception e) {
                    log.error("{}", e);
                }
            }
        }).start();
 }
```

#### 思路 - 不可变

如果一个对象在不能够修改其内部状态（属性），那么它就是线程安全的，因为不存在并发修改啊！这样的对象在 Java 中有很多，例如在 Java 8 后，提供了一个新的日期格式化类：

```java
DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
 for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            LocalDate date = dtf.parse("2018-10-01", LocalDate::from);
            log.debug("{}", date);
        }).start();
 }
```

可以看 DateTimeFormatter 的文档：

```java
@implSpec
This class is immutable and thread-safe.
```

不可变对象，实际是另一种避免竞争的方式。

### 6.2  不可变设计

另一个大家更为熟悉的 String 类也是不可变的，以它为例，说明一下不可变设计的要素

```java
public final class String
        implements java.io.Serializable, Comparable<String>, CharSequence {
    /** The value is used for character storage. */
    private final char value[];
    /** Cache the hash code for the string */
    private int hash; // Default to 0
    // ...
}
```

#### f inal 的使用

发现该类、类中所有属性都是 final 的

- 属性用 final 修饰保证了该属性是只读的，不能修改 
- 类用 final 修饰保证了该类中的方法不能被覆盖，防止子类无意间破坏不可变性

#### 保护性拷贝

但有同学会说，使用字符串时，也有一些跟修改相关的方法啊，比如 substring 等，那么下面就看一看这些方法是 如何实现的，就以 substring 为例：

```java
public String substring(int beginIndex) {
    if (beginIndex < 0) {
        throw new StringIndexOutOfBoundsException(beginIndex);
    }
    int subLen = value.length - beginIndex;
    if (subLen < 0) {
        throw new StringIndexOutOfBoundsException(subLen);
    }
    return (beginIndex == 0) ? this : new String(value, beginIndex, subLen);
}
```

发现其内部是调用 String 的构造方法创建了一个新字符串，再进入这个构造看看，是否对 final char[] value 做出 了修改：

```java
public String(char value[], int offset, int count) {
    if (offset < 0) {
        throw new StringIndexOutOfBoundsException(offset);
    }
    if (count <= 0) {
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        if (offset <= value.length) {
            this.value = "".value;
            return;
        }
    }
    if (offset > value.length - count) {
        throw new StringIndexOutOfBoundsException(offset + count);
    }
    this.value = Arrays.copyOfRange(value, offset, offset+count);
}
```

结果发现也没有，构造新字符串对象时，会生成新的 char[] value，对内容进行复制 。这种通过创建副本对象来避 免共享的手段称之为【保护性拷贝（defensive copy）】

#### 享元模式

享元模式（Flyweight Pattern）是一种**结构型设计模式**，核心思想是**通过复用已创建的对象（享元）来减少内存占用和对象创建开销**，尤其适用于需要大量相似对象的场景。

##### 核心目标

解决 “**大量细粒度对象重复创建导致的内存浪费**” 问题，通过共享对象实例，降低系统资源消耗。

##### 关键概念

1. **享元（Flyweight）**：
   可被多个场景共享的对象，其内部状态（Intrinsic State）是不变的（如字符的字形、颜色），外部状态（Extrinsic State）是可变的（如字符的位置、大小），由使用方传入。
2. **享元工厂（Flyweight Factory）**：
   负责创建和管理享元对象，当请求新对象时，优先返回已存在的共享实例，避免重复创建。

##### 实现逻辑

1. **分离状态**：将对象的属性拆分为**内部状态**（可共享，如 “字母 'A' 的字形”）和**外部状态**（不可共享，如 “字母 'A' 在屏幕上的坐标”）。
2. **共享内部状态**：通过工厂类缓存具有相同内部状态的对象，确保相同内部状态的对象仅存在一个实例。
3. **外部状态传入**：使用享元对象时，由调用方传入外部状态，避免内部状态被修改。

### 6.3  无状态

在 web 阶段学习时，设计 Servlet 时为了保证其线程安全，都会有这样的建议，不要为 Servlet 设置成员变量，这 种没有任何成员变量的类是线程安全的

> 因为成员变量保存的数据也可以称为状态信息，因此没有成员变量就称之为【无状态】

## 7.共享模型之工具

### 7.1  线程池

#### 1  自定义线程池

步骤1：自定义拒绝策略接口

```java
@FunctionalInterface // 拒绝策略
interface RejectPolicy<T> {
    void reject(BlockingQueue<T> queue, T task);
}
```

步骤2：自定义任务队列

```java
class BlockingQueue<T> {
    // 1. 任务队列
    private Deque<T> queue = new ArrayDeque<>();
    // 2. 锁
    private ReentrantLock lock = new ReentrantLock();
    // 3. 生产者条件变量
    private Condition fullWaitSet = lock.newCondition();
    // 4. 消费者条件变量
    private Condition emptyWaitSet = lock.newCondition();
    // 5. 容量
    private int capcity;

    public BlockingQueue(int capcity) {
        this.capcity = capcity;
    }

    // 带超时阻塞获取
    public T poll(long timeout, TimeUnit unit) {
        lock.lock();
        try {
            // 将 timeout 统一转换为 纳秒
            long nanos = unit.toNanos(timeout);
            while (queue.isEmpty()) {
                try {
                    // 返回值是剩余时间
                    if (nanos <= 0) {
                        return null;
                    }
                    nanos = emptyWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞获取
    public T take() {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                try {
                    emptyWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            T t = queue.removeFirst();
            fullWaitSet.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    // 阻塞添加
    public void put(T task) {
        lock.lock();
        try {
            while (queue.size() == capcity) {
                try {
                    log.debug("等待加入任务队列 {} ...", task);
                    fullWaitSet.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
        } finally {
            lock.unlock();
        }
    }

    // 带超时时间阻塞添加
    public boolean offer(T task, long timeout, TimeUnit timeUnit) {
        lock.lock();
        try {
            long nanos = timeUnit.toNanos(timeout);
            while (queue.size() == capcity) {
                try {
                    if (nanos <= 0) {
                        return false;
                    }
                    log.debug("等待加入任务队列 {} ...", task);
                    nanos = fullWaitSet.awaitNanos(nanos);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.debug("加入任务队列 {}", task);
            queue.addLast(task);
            emptyWaitSet.signal();
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    public void tryPut(RejectPolicy<T> rejectPolicy, T task) {
        lock.lock();
        try {
            // 判断队列是否满
            if (queue.size() == capcity) {
                rejectPolicy.reject(this, task);
            } else {  // 有空闲
                log.debug("加入任务队列 {}", task);
                queue.addLast(task);
                emptyWaitSet.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}

```

步骤3：自定义线程池

```java
class ThreadPool {
    // 任务队列
    private BlockingQueue<Runnable> taskQueue;
    // 线程集合
    private HashSet<Worker> workers = new HashSet<>();
    // 核心线程数
    private int coreSize;
    // 获取任务时的超时时间
    private long timeout;
    private TimeUnit timeUnit;
    private RejectPolicy<Runnable> rejectPolicy;

    // 执行任务
    public void execute(Runnable task) {
        // 当任务数没有超过 coreSize 时，直接交给 worker 对象执行
		// 如果任务数超过 coreSize 时，加入任务队列暂存
        synchronized (workers) {
            if (workers.size() < coreSize) {
                Worker worker = new Worker(task);
                log.debug("新增 worker{}, {}", worker, task);
                workers.add(worker);
                worker.start();
            } else {
//                taskQueue.put(task);
                // 1) 死等
				// 2) 带超时等待
				// 3) 让调用者放弃任务执行
				// 4) 让调用者抛出异常
				// 5) 让调用者自己执行任务
                taskQueue.tryPut(rejectPolicy, task);
            }
        }
    }

    public ThreadPool(int coreSize, long timeout, TimeUnit timeUnit, int queueCapcity,
                      RejectPolicy<Runnable> rejectPolicy) {
        this.coreSize = coreSize;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.taskQueue = new BlockingQueue<>(queueCapcity);
        this.rejectPolicy = rejectPolicy;
    }

    class Worker extends Thread {
        private Runnable task;

        public Worker(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            // 执行任务
			// 1) 当 task 不为空，执行任务
			// 2) 当 task 执行完毕，再接着从任务队列获取任务并执行
//            while(task != null || (task = taskQueue.take()) != null) {
            while (task != null || (task = taskQueue.poll(timeout, timeUnit)) != null) {
                try {
                    log.debug("正在执行...{}", task);
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    task = null;
                }
            }
            synchronized (workers) {
                log.debug("worker 被移除{}", this);
                workers.remove(this);
            }
        }
    }
}
```

步骤4：测试

```java
public static void main(String[] args) {
    ThreadPool threadPool = new ThreadPool(1,
            1000, TimeUnit.MILLISECONDS, 1, (queue, task) -> {
        // 1. 死等
//            queue.put(task);
        // 2) 带超时等待
//            queue.offer(task, 1500, TimeUnit.MILLISECONDS);
        // 3) 让调用者放弃任务执行
//            log.debug("放弃{}", task);
        // 4) 让调用者抛出异常
//            throw new RuntimeException("任务执行失败 " + task);
        // 5) 让调用者自己执行任务
        task.run();
    });
    for (int i = 0; i < 4; i++) {
        int j = i;
        threadPool.execute(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("{}", j);
        });
    }
}
```

#### 2   ThreadPoolExecutor

##### 1. 线程池状态

ThreadPoolExecutor 使用 int 的高 3 位来表示线程池状态，低 29 位表示线程数量

| 状态名     | 高3位 | 接收新任务 | 处理阻塞队列任务 | 说明                                       |
| ---------- | ----- | ---------- | ---------------- | ------------------------------------------ |
| RUNNING    | 111   | Y          | Y                |                                            |
| SHUTDOWN   | 000   | N          | Y                | 不会接收新任务，但会处理阻塞队列剩余 任务  |
| STOP       | 001   | N          | N                | 会中断正在执行的任务，并抛弃阻塞队列 任务  |
| TIDYING    | 010   | -          | -                | 任务全执行完毕，活动线程为 0 即将进入 终结 |
| TERMINATED | 011   | -          | -                | 终结状态                                   |

从数字上比较，TERMINATED > TIDYING > STOP > SHUTDOWN > RUNNING 这些信息存储在一个原子变量 ctl 中，目的是将线程池状态与线程个数合二为一，这样就可以用一次 cas 原子操作 进行赋值

```java
// c 为旧值， ctlOf 返回结果为新值
ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c))));
 // rs 为高 3 位代表线程池状态， wc 为低 29 位代表线程个数，ctl 是合并它们
private static int ctlOf(int rs, int wc) { return rs | wc; }
```

##### 2. 构造方法

```java
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler)
```

-  corePoolSize 核心线程数目 (最多保留的线程数) 
- maximumPoolSize 最大线程数目 
- keepAliveTime 生存时间 - 针对救急线程 unit 时间单位 - 针对救急线程 
- workQueue 阻塞队列 
- threadFactory 线程工厂 - 可以为线程创建时起个好名字 
- handler 拒绝策略

工作方式：

- 线程池中刚开始没有线程，当一个任务提交给线程池后，线程池会创建一个新线程来执行任务。 
- 当线程数达到 corePoolSize 并没有线程空闲，这时再加入任务，新加的任务会被加入workQueue 队列排 队，直到有空闲的线程。 
- 如果队列选择了有界队列，那么任务超过了队列大小时，会创建 maximumPoolSize - corePoolSize 数目的线程来救急。 
- 如果线程到达 maximumPoolSize 仍然有新任务这时会执行拒绝策略。拒绝策略 jdk 提供了 4 种实现，其它著名框架也提供了实现
  - AbortPolicy 让调用者抛出 RejectedExecutionException 异常，这是默认策略
  - CallerRunsPolicy 让调用者运行任务 
  - DiscardPolicy 放弃本次任务 
  - DiscardOldestPolicy 放弃队列中最早的任务，本任务取而代之 
  - Dubbo 的实现，在抛出 RejectedExecutionException 异常之前会记录日志，并 dump 线程栈信息，方 便定位问题 
  - Netty 的实现，是创建一个新线程来执行任务 
  - ActiveMQ 的实现，带超时等待（60s）尝试放入队列，类似我们之前自定义的拒绝策略 
  - PinPoint 的实现，它使用了一个拒绝策略链，会逐一尝试策略链中每种拒绝策略
- 当高峰过去后，超过corePoolSize 的救急线程如果一段时间没有任务做，需要结束节省资源，这个时间由 keepAliveTime 和 unit 来控制。

![线程池拒绝策略](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E7%BA%BF%E7%A8%8B%E6%B1%A0%E6%8B%92%E7%BB%9D%E7%AD%96%E7%95%A5.png)

根据这个构造方法，JDK Executors 类中提供了众多工厂方法来创建各种用途的线程池

##### 3.  newFixedThreadPool

```java
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
}
```

特点

- 核心线程数 == 最大线程数（没有救急线程被创建），因此也无需超时时间
- 阻塞队列是无界的，可以放任意数量的任务

> **评价** 适用于任务量已知，相对耗时的任务

##### 4.  newCachedThreadPool

```java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
}
```

特点

- 核心线程数是 0， 最大线程数是 Integer.MAX_VALUE，救急线程的空闲生存时间是 60s，意味着
  - 全部都是救急线程（60s 后可以回收）
  - 救急线程可以无限创建
- 队列采用了 SynchronousQueue 实现特点是，它没有容量，没有线程来取是放不进去的（一手交钱、一手交 货）

> **评价** 整个线程池表现为线程数会根据任务量不断增长，没有上限，当任务执行完毕，空闲 1分钟后释放线 程。 适合任务数比较密集，但每个任务执行时间较短的情况

##### 5. newSingleThreadExecutor

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
            (new ThreadPoolExecutor(1, 1,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>()));
}
```

使用场景：

希望多个任务排队执行。线程数固定为 1，任务数多于 1 时，会放入无界队列排队。任务执行完毕，这唯一的线程 也不会被释放。

区别：

- 自己创建一个单线程串行执行任务，如果任务执行失败而终止那么没有任何补救措施，而线程池还会新建一 个线程，保证池的正常工作
- Executors.newSingleThreadExecutor() 线程个数始终为1，不能修改
  - FinalizableDelegatedExecutorService 应用的是装饰器模式，只对外暴露了 ExecutorService 接口，因 此不能调用 ThreadPoolExecutor 中特有的方法
- Executors.newFixedThreadPool(1) 初始时为1，以后还可以修改
  - 对外暴露的是 ThreadPoolExecutor 对象，可以强转后调用 setCorePoolSize 等方法进行修改

##### 6.  提交任务

```java
// 执行任务
void execute(Runnable command);

// 提交任务 task，用返回值 Future 获得任务执行结果
<T> Future<T> submit(Callable<T> task);

// 提交 tasks 中所有任务
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

// 提交 tasks 中所有任务，带超时时间
<T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                              long timeout, TimeUnit unit)
        throws InterruptedException;

// 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消
<T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

// 提交 tasks 中所有任务，哪个任务先成功执行完毕，返回此任务执行结果，其它任务取消，带超时时间
<T> T invokeAny(Collection<? extends Callable<T>> tasks,
                long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
```

##### 7.  关闭线程池

###### shutdown

```java
/*
线程池状态变为 SHUTDOWN- 不会接收新任务- 但已提交任务会执行完- 此方法不会阻塞调用线程的执行
*/
void shutdown();
```

```java
public void shutdown() {
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(SHUTDOWN);
        // 仅会打断空闲线程
        interruptIdleWorkers();
        onShutdown(); // 扩展点 ScheduledThreadPoolExecutor
    } finally {
        mainLock.unlock();
    }
    // 尝试终结(没有运行的线程可以立刻终结，如果还有运行的线程也不会等)
    tryTerminate();
}
```

######  shutdownNow

```java
 /*
线程池状态变为 STOP- 不会接收新任务- 会将队列中的任务返回- 并用 interrupt 的方式中断正在执行的任务
*/
 List<Runnable> shutdownNow();
```

```java
public List<Runnable> shutdownNow() {
    List<Runnable> tasks;
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        checkShutdownAccess();
        // 修改线程池状态
        advanceRunState(STOP);
        // 打断所有线程
        interruptWorkers();
        // 获取队列中剩余任务
        tasks = drainQueue();
    } finally {
        mainLock.unlock();
    }
    // 尝试终结
    tryTerminate();
    return tasks;
}

```

###### 其它方法

```java
// 不在 RUNNING 状态的线程池，此方法就返回 true
boolean isShutdown();

// 线程池状态是否是 TERMINATED
boolean isTerminated();
	
// 调用 shutdown 后，由于调用线程并不会等待所有任务运行结束，因此如果它想在线程池 TERMINATED 后做些事
情，可以利用此方法等待
boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;
```

##### 8.  任务调度池

在『任务调度线程池』功能加入之前，可以使用 java.util.Timer 来实现定时功能，Timer 的优点在于简单易用，但 由于所有任务都是由同一个线程来调度，因此所有任务都是串行执行的，同一时间只能有一个任务在执行，前一个 任务的延迟或异常都将会影响到之后的任务。

###### Timer写法

```java
public static void main(String[] args) {
    Timer timer = new Timer();
    TimerTask task1 = new TimerTask() {
        @Override
        public void run() {
            log.debug("task 1");
            sleep(2);
        }
    };
    TimerTask task2 = new TimerTask() {
        @Override
        public void run() {
            log.debug("task 2");
        }
    };
    // 使用 timer 添加两个任务，希望它们都在 1s 后执行
// 但由于 timer 内只有一个线程来顺序执行队列中的任务，因此『任务1』的延时，影响了『任务2』的执行
    timer.schedule(task1, 1000);
    timer.schedule(task2, 1000);
}
```

###### ScheduledThreadPool写法

```java
ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
// 添加两个任务，希望它们都在 1s 后执行
executor.schedule(() -> {
        System.out.println("任务1，执行时间：" + new Date());
        try { Thread.sleep(2000); } catch (InterruptedException e) { }
        }, 1000, TimeUnit.MILLISECONDS);
        executor.schedule(() -> {
        System.out.println("任务2，执行时间：" + new Date());
        }, 1000, TimeUnit.MILLISECONDS);
```

scheduleAtFixedRate 例子（每隔一段时间执行一次任务`从上一次任务开始计时`，若任务执行时间超过了间隔时间，间隔被『撑』到任务执行时间）：

```java
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
 log.debug("start...");
 pool.scheduleAtFixedRate(() -> {
 	log.debug("running...");
 	sleep(2);
 }, 1, 1, TimeUnit.SECONDS);

-------
   	间隔时间2s
```

scheduleWithFixedDelay 例子（每隔一段时间执行一次任务`从上一次任务结束计时`）：

```java
ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
 log.debug("start...");
 pool.scheduleWithFixedDelay(()-> {
	log.debug("running...");
 	sleep(2);
 }, 1, 1, TimeUnit.SECONDS);

-------
    间隔时间3s
```

##### 9.  正确处理执行任务异常

方法1：主动捉异常

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
 pool.submit(() -> {
        try {
            log.debug("task1");
            int i = 1 / 0;
    }catch (Exception e) {
        log.error("error:", e);
    }
 });
```

方法2：使用 Future

```java
ExecutorService pool = Executors.newFixedThreadPool(1);
Future<Boolean> f = pool.submit(() -> {
    log.debug("task1");
    int i = 1 / 0;
    return true;
});
log.debug("result:{}", f.get());
```

##### 10.  Tomcat 线程池

Tomcat 在哪里用到了线程池呢

- LimitLatch 用来限流，可以控制最大连接个数，类似 J.U.C 中的  Semaphore
- Acceptor 只负责【接收新的 socket 连接】
- Poller 只负责监听 socket channel 是否有【可读的 I/O 事件】
- 一旦可读，封装一个任务对象（socketProcessor），提交给 Executor 线程池处理
- Executor 线程池中的工作线程最终负责【处理请求】

Tomcat 线程池扩展了 ThreadPoolExecutor，行为稍有不同

- 如果总线程数达到 maximumPoolSize
  - 这时不会立刻抛 RejectedExecutionException 异常
  - 而是再次尝试将任务放入队列，如果还失败，才抛出 RejectedExecutionException 异常

 Connector 配置

| 配置项              | 默认值 | 说明                                   |
| ------------------- | ------ | -------------------------------------- |
| acceptorThreadCount | 1      | acceptor 线程数量                      |
| pollerThreadCount   | 1      | poller 线程数量                        |
| minSpareThreads     | 10     | 核心线程数，即 corePoolSize            |
| maxThreads          | 200    | 最大线程数，即 maximumPoolSize         |
| executor            | -      | Executor 名称，用来引用下面的 Executor |

 Executor 线程配置

| 配置项                  | 默认值            | 说明                                      |
| ----------------------- | ----------------- | ----------------------------------------- |
| threadPriority          | 5                 | 线程优先级                                |
| daemon                  | true              | 是否守护线程                              |
| minSpareThreads         | 25                | 核心线程数，即 corePoolSize               |
| maxThreads              | 200               | 最大线程数，即 maximumPoolSize            |
| maxIdleTime             | 60000             | 线程生存时间，单位是毫秒，默认值即 1 分钟 |
| maxQueueSize            | Integer.MAX_VALUE | 队列长度                                  |
| prestartminSpareThreads | false             | 核心线程是否在服务器启动时启动            |

#### 3.   Fork/Join

**概念**

Fork/Join 是 JDK 1.7 加入的新的线程池实现，它体现的是一种分治思想，适用于能够进行任务拆分的 cpu 密集型 运算

所谓的任务拆分，是将一个大任务拆分为算法上相同的小任务，直至不能拆分可以直接求解。跟递归相关的一些计 算，如归并排序、斐波那契数列、都可以用分治思想进行求解

Fork/Join 在分治的基础上加入了多线程，可以把每个任务的分解和合并交给不同的线程来完成，进一步提升了运算效率

Fork/Join 默认会创建与 cpu 核心数大小相同的线程池

**使用**

提交给 Fork/Join 线程池的任务需要继承 RecursiveTask（有返回值）或 RecursiveAction（没有返回值）

### 7.2  J.U.C

#### 1.  \* AQS 原理

#### 2.   \* ReentrantLock 原理

#### 3.  读写锁

##### 3.1  ReentrantReadWriteLock

当读操作远远高于写操作时，这时候使用 `读写锁`让`读-读`可以并发，提高性能。类似于数据库中的`select ...from ... lock in share mode`

提供一个 `数据容器类`内部分别使用读锁保护数据的`read`方法，写锁保护数据的  `write() `方法

```java
 private ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
 private ReentrantReadWriteLock.ReadLock r = rw.readLock();
 private ReentrantReadWriteLock.WriteLock w = rw.writeLock()
 
 r.lock()
 r.unlock()
 
 w.lock()
 w.unlock()
 
 --------
 读-读可以并发
 读-写不可并发
 写-写不可并发
```

**注意事项**

- 读锁不支持条件变量
- 重入时升级不支持：即持有读锁的情况下去获取写锁，会导致获取写锁永久等待
- 重入时降级支持：即持有写锁的情况下去获取读锁

##### 3.2   StampedLock

类自 JDK 8 加入，是为了进一步优化读性能，它的特点是在使用读锁、写锁时都必须配合【戳】使用

加解读锁

```java
long stamp = lock.readLock();
lock.unlockRead(stamp);
```

加解写锁

```java
long stamp = lock.writeLock();
lock.unlockWrite(stamp);
```

乐观读，StampedLock 支持  tryOptimisticRead() 方法（乐观读），读取完毕后需要做一次  戳校验 如果校验通 过，表示这期间确实没有写操作，数据可以安全使用，如果校验没通过，需要重新获取读锁，保证数据安全。

```java
long stamp = lock.tryOptimisticRead();
// 验戳
if(!lock.validate(stamp)){
        // 锁升级
}
```

> **注意**
>
> - StampedLock 不支持条件变量
> - StampedLock 不支持可重入

#### 4.   Semaphore

**基本使用**

[ˈsɛməˌfɔr] 信号量，用来限制能同时访问共享资源的线程上限。

```java
public static void main(String[] args) {
    // 1. 创建 semaphore 对象
    Semaphore semaphore = new Semaphore(3);
    // 2. 10个线程同时运行
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            // 3. 获取许可
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                log.debug("running...");
                sleep(1);
                log.debug("end...");
            } finally {
                // 4. 释放许可
                semaphore.release();
            }
        }).start();
    }
}
```

输出

```shell
07:35:15.485 c.TestSemaphore [Thread-2] - running... 
07:35:15.485 c.TestSemaphore [Thread-1] - running... 
07:35:15.485 c.TestSemaphore [Thread-0] - running... 
07:35:16.490 c.TestSemaphore [Thread-2] - end... 
07:35:16.490 c.TestSemaphore [Thread-0] - end... 
07:35:16.490 c.TestSemaphore [Thread-1] - end... 
07:35:16.490 c.TestSemaphore [Thread-3] - running... 
07:35:16.490 c.TestSemaphore [Thread-5] - running... 
07:35:16.490 c.TestSemaphore [Thread-4] - running... 
07:35:17.490 c.TestSemaphore [Thread-5] - end... 
07:35:17.490 c.TestSemaphore [Thread-4] - end... 
07:35:17.490 c.TestSemaphore [Thread-3] - end... 
07:35:17.490 c.TestSemaphore [Thread-6] - running... 
07:35:17.490 c.TestSemaphore [Thread-7] - running... 
07:35:17.490 c.TestSemaphore [Thread-9] - running... 
07:35:18.491 c.TestSemaphore [Thread-6] - end... 
07:35:18.491 c.TestSemaphore [Thread-7] - end... 
07:35:18.491 c.TestSemaphore [Thread-9] - end... 
07:35:18.491 c.TestSemaphore [Thread-8] - running... 
07:35:19.492 c.TestSemaphore [Thread-8] - end... 
```

#### 5.  CountdownLatch

用来进行线程同步协作，等待所有线程完成倒计时。

其中构造参数用来初始化等待计数值，await() 用来等待计数归零，countDown() 用来让计数减一

```java
public static void main(String[] args) throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(3);
    new Thread(() -> {
        log.debug("begin...");
        sleep(1);
        latch.countDown();
        log.debug("end...{}", latch.getCount());
    }).start();
    new Thread(() -> {
        log.debug("begin...");
        sleep(2);
        latch.countDown();
        log.debug("end...{}", latch.getCount());
    }).start();
    new Thread(() -> {
        log.debug("begin...");
        sleep(1.5);
        latch.countDown();
        log.debug("end...{}", latch.getCount());
    }).start();
    log.debug("waiting...");
    latch.await();
    log.debug("wait end...");
}
```

输出

```shell
18:44:00.778 c.TestCountDownLatch [main] - waiting... 
18:44:00.778 c.TestCountDownLatch [Thread-2] - begin... 
18:44:00.778 c.TestCountDownLatch [Thread-0] - begin... 
18:44:00.778 c.TestCountDownLatch [Thread-1] - begin... 
18:44:01.782 c.TestCountDownLatch [Thread-0] - end...2 
18:44:02.283 c.TestCountDownLatch [Thread-2] - end...1 
18:44:02.782 c.TestCountDownLatch [Thread-1] - end...0 
18:44:02.782 c.TestCountDownLatch [main] - wait end...
```

#### 6.  CyclicBarrier

[ˈsaɪklɪk ˈbæriɚ] 循环栅栏，用来进行线程协作，等待线程满足某个计数。构造时设置『计数个数』，每个线程执 行到某个需要“同步”的时刻调用 await() 方法进行等待，当等待的线程数满足『计数个数』时，继续执行

```java
CyclicBarrier cb = new CyclicBarrier(2); // 个数为2时才会继续执行

new Thread(()->{
    System.out.println("线程1开始.."+new Date());
    try {
        cb.await(); // 当个数不足时，等待
    }catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
    }
    System.out.println("线程1继续向下运行..."+new Date());
}).start();
new Thread(()->{
    System.out.println("线程2开始.."+new Date());
    try { Thread.sleep(2000); } catch (InterruptedException e) { }
    try {
        cb.await(); // 2 秒后，线程个数够2，继续运行
    }catch (InterruptedException | BrokenBarrierException e) {
        e.printStackTrace();
    }
    System.out.println("线程2继续向下运行..."+new Date());
}).start();
```

> **注意 **CyclicBarrier 与 CountDownLatch 的主要区别在于 CyclicBarrier 是可以重用的 CyclicBarrier 可以被比 喻为『人满发车』

#### 7.  线程安全集合类概述

![](https://file-testyt.oss-cn-beijing.aliyuncs.com/blog/%E7%BA%BF%E7%A8%8B%E5%AE%89%E5%85%A8%E9%9B%86%E5%90%88%E7%B1%BB%202025-07-06%20223924.png)

线程安全集合类可以分为三大类：

- 遗留的线程安全集合如 ` Hashtable` ，` Vector`
- 使用 Collections 装饰的线程安全集合，如：
  - Collections.synchronizedCollection
  - Collections.synchronizedList
  -  Collections.synchronizedMap
  -  Collections.synchronizedSet
  - Collections.synchronizedNavigableMap
  - Collections.synchronizedNavigableSet 
  - Collections.synchronizedSortedMap
  - Collections.synchronizedSortedSet
- java.util.concurrent.*

重点介绍 Concurrent类 `java.util.concurrent.*` 下的线程安全集合类，可以发现它们有规律，里面包含三类关键词： Blocking、CopyOnWrite、Concurrent

- Blocking 大部分实现基于锁，并提供用来阻塞的方法
- CopyOnWrite 之类容器修改开销相对较重
- Concurrent 类型的容器
  - 内部很多操作使用 cas 优化，一般可以提供较高吞吐量
  - 弱一致性
    - 遍历时弱一致性，例如，当利用迭代器遍历时，如果容器发生修改，迭代器仍然可以继续进行遍 历，这时内容是旧的
    - 求大小弱一致性，size 操作未必是 100% 准确
    - 读取弱一致性

> 遍历时如果发生了修改，对于非安全容器来讲，使用` fail-fast `机制也就是让遍历立刻失败，抛出 ConcurrentModificationException，不再继续遍历

#### 8.  ConcurrentHashMap

`ConcurrentHashMap` 是 Java 集合框架中**线程安全的哈希表实现**，位于 `java.util.concurrent` 包下，主要用于**多线程环境下的高效并发读写操作**，解决了传统 `Hashtable` 效率低和 `HashMap` 线程不安全的问题。

**核心特性与设计**

1. **线程安全**
   - 内部通过**分段锁（JDK 7）** 或**CAS + synchronized（JDK 8+）** 实现线程安全，避免全表锁带来的性能损耗。
   - 允许多个线程同时读写，不同分段 / 桶的操作互不阻塞，并发性能优于 `Hashtable`。
2. **数据结构（JDK 8+）**
   - 底层采用**数组** + **链表** + **红黑树**结构（与`HashMap`类似）：
     - 数组（`Node[] table`）：存储哈希桶，每个桶对应一个链表或红黑树。
     - 链表：用于哈希冲突较少的情况，节点为 `Node`（存储键值对）。
     - 红黑树：当链表长度超过阈值（默认 8）时转为红黑树，优化查询效率（从 O (n) 提升至 O (log n)）。
3. **并发优化**
   - **JDK 7**：采用**分段锁（Segment）**，将哈希表分为多个独立分段，每个分段独立加锁，减少锁竞争。
   - **JDK 8+**：移除分段锁，改为对**单个桶（链表 / 红黑树的头节点）** 加锁（`synchronized`），配合 CAS 操作（无锁化），进一步提升并发效率。
4. **功能特点**
   - 支持**原子性操作**：如 `putIfAbsent(key, value)`（键不存在时才插入）、`remove(key, value)`（仅当键值匹配时删除）等，避免多线程下的竞态条件。
   - 弱一致性迭代器：迭代时不抛出 `ConcurrentModificationException`，但可能无法实时反映最新修改（牺牲强一致性换取性能）。
   - 不允许 `null` 键或值（与 `HashMap` 不同，避免多线程下的歧义）。

**典型应用场景**

- 多线程环境下的**缓存存储**（如共享配置、计数器）。
- 需要**高并发读写**的哈希表场景（如分布式系统中的本地缓存）。
- 替代 `Hashtable` 或手动加锁的 `HashMap`，简化线程安全代码。

#### 9.  BlockingQueue

`BlockingQueue` 是 Java 并发包（`java.util.concurrent`）中的**阻塞队列接口**，专为多线程协作设计，核心特点是在队列空或满时提供**阻塞等待**的能力，简化生产者 - 消费者模式的实现。

**核心特性**

1. **阻塞操作**
   - **当队列空时**：获取元素的线程（消费者）会被阻塞，直到队列中有元素可用。
   - **当队列满时**：添加元素的线程（生产者）会被阻塞，直到队列有空闲空间。
2. **线程安全**
   所有方法均通过内置锁或 CAS 操作保证线程安全，无需额外同步处理。
3. **支持边界与非边界**
   - 有界队列：如 `ArrayBlockingQueue`（固定容量）、`LinkedBlockingQueue`（可指定容量）。
   - 无界队列：如 `LinkedBlockingQueue`（默认容量为 `Integer.MAX_VALUE`）、`PriorityBlockingQueue`，理论上可无限添加元素（受内存限制）。

**核心方法分类**

按功能可分为三类（以添加 / 移除元素为例）：



| 操作类型     | 队列满时（添加）        | 队列空时（移除）       | 典型方法                                         |
| ------------ | ----------------------- | ---------------------- | ------------------------------------------------ |
| **阻塞式**   | 阻塞等待空间            | 阻塞等待元素           | `put(e)`、`take()`                               |
| **非阻塞式** | 立即返回特殊值（false） | 立即返回特殊值（null） | `offer(e)`、`poll()`                             |
| **超时式**   | 超时后返回 false        | 超时后返回 null        | `offer(e, timeout, unit)`、`poll(timeout, unit)` |

**常见实现类**

- **`ArrayBlockingQueue`**：基于数组的有界队列，初始化时需指定容量，内部使用单锁（`ReentrantLock`）控制并发。
- **`LinkedBlockingQueue`**：基于链表的队列，默认无界（容量极大），也可指定容量；内部使用两把锁（分别控制入队和出队），并发性能优于 `ArrayBlockingQueue`。
- **`SynchronousQueue`**：无缓冲队列，生产者添加元素后必须等待消费者取走（一对一直接传递），适用于线程间直接通信。
- **`PriorityBlockingQueue`**：支持优先级的无界队列（元素需实现 `Comparable`），取出元素时按优先级排序，内部通过堆结构实现。
- **`DelayQueue`**：延迟队列，元素需实现 `Delayed` 接口，仅当延迟时间到期后才可被取出，适用于定时任务（如缓存过期清理）。

**典型应用场景**

- **生产者 - 消费者模型**：生产者线程向队列添加数据，消费者线程从队列取数据，队列自动平衡两者速度差异。
- **线程池任务调度**：线程池（如 `ThreadPoolExecutor`）的任务队列本质是 `BlockingQueue`，当任务数超过核心线程数时，任务会暂存到队列中等待执行。
- **异步通信**：多线程间通过队列传递数据，避免直接同步导致的性能损耗。

#### 10.  ConcurrentLinkedQueue

ConcurrentLinkedQueue 的设计与 LinkedBlockingQueue 非常像，也是

- 两把【锁】，同一时刻，可以允许两个线程同时（一个生产者与一个消费者）执行
- dummy 节点的引入让两把【锁】将来锁住的是不同对象，避免竞争
- 只是这【锁】使用了 cas 来实现

事实上，ConcurrentLinkedQueue 应用还是非常广泛的 例如之前讲的 Tomcat 的 Connector 结构时，Acceptor 作为生产者向 Poller 消费者传递事件信息时，正是采用了 ConcurrentLinkedQueue 将 SocketChannel 给 Poller 使用

#### 11.   CopyOnWriteArrayList

`CopyOnWriteArraySet `是它的马甲 底层实现采用了  写入时拷贝 的思想，增删改操作会将底层数组拷贝一份，更 改操作在新数组上执行，这时不影响其它线程的并发读，读写分离。 以新增为例：

```java
public boolean add(E e) {
    synchronized (lock) {
        // 获取旧的数组
        Object[] es = getArray();
        int len = es.length;
        // 拷贝新的数组（这里是比较耗时的操作，但不影响其它读线程）
        es = Arrays.copyOf(es, len + 1);
        // 添加新元素
        es[len] = e;
        // 替换旧的数组
        setArray(es);
        return true;
    }
}
```

> 这里的源码版本是 Java 11，在 Java 1.8 中使用的是可重入锁而不是 synchronized

其它读操作并未加锁，例如：

```java
public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    for (Object x : getArray()) {
        @SuppressWarnings("unchecked") E e = (E) x;
        action.accept(e);
    }
}
```

适合『读多写少』的应用场景

##### get 弱一致性

| 时间点 | 操作                         |
| ------ | ---------------------------- |
| 1      | Thread-0 getArray()          |
| 2      | Thread-1 getArray()          |
| 3      | Thread-1 setArray(arrayCopy) |
| 4      | Thread-0 array[index]        |

在thread-1线程更改之后,thread-0线程并没有拿到最新值

> 不容易测试，但问题确实存在

##### 迭代器弱一致性

```java
CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();
list.add(1);
list.add(2);
list.add(3);
Iterator<Integer> iter = list.iterator();
new Thread(() -> {
    list.remove(0);
    System.out.println(list);
}).start();

sleep1s();
while (iter.hasNext()){
        System.out.println(iter.next());
}
```

> 不要觉得弱一致性就不好
>
> - 数据库的 MVCC 都是弱一致性的表现
> - 并发高和一致性是矛盾的，需要权衡

### 7.3  CompletableFuture

[什么是 Java 的 CompletableFuture？ - 面试鸭 - 程序员求职面试刷题神器](https://www.mianshiya.com/question/1780933294951526402)
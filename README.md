# 悟空IM Android sdk 源码
![](https://img.shields.io/badge/platform-android-blue.svg)  ![](https://img.shields.io/badge/compileSdkVersion-33-blue.svg) ![](https://img.shields.io/badge/minSdkVersion-19-blue.svg) ![](https://img.shields.io/hexpm/l/plug.svg)

该项目是一个完全自定义协议的即时通讯sdk。

## [详细文档](https://githubim.com/sdk/android.html "文档")

## 官方Demo
悟空团队官方基于本sdk编写了一个Android项目，该项目覆盖了sdk所有方法的使用及演示流程。[跳转到Demo](https://github.com/TangSengDaoDao/TangSengDaoDaoAndroid)

## 快速入门

**Gradle**

[![](https://jitpack.io/v/WuKongIM/WuKongIMAndroidSDK.svg)](https://jitpack.io/#WuKongIM/WuKongIMAndroidSDK)

```
implementation 'com.github.WuKongIM:WuKongIMAndroidSDK:version' // 版本号请看上面
```

jitpack还需在主程序的`build.gradle`文件中添加：

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

由于sdk内使用了sqlcipher加密数据库和curve25519加密算法，需将库添加到项目中
```
implementation "net.zetetic:android-database-sqlcipher:4.5.3"
implementation "androidx.sqlite:sqlite-ktx:2.3.1"
implementation 'org.whispersystems:curve25519-android:0.5.0'
implementation 'org.whispersystems:signal-protocol-android:2.8.1'
```

**混淆**
```
-dontwarn com.xinbida.wukongim.**
-keep class com.xinbida.wukongim.**{*;}

#数据库加密
-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

#--------- 混淆dh curve25519-------
-keep class org.whispersystems.curve25519.**{*;}
-keep class org.whispersystems.** { *; }
-keep class org.thoughtcrime.securesms.** { *; }
```

**初始化sdk**
```
WKIM.getInstance().init(context, uid, token);
```
**初始化IP**
```
 WKIM.getInstance().getConnectionManager().addOnGetIpAndPortListener(new IGetIpAndPort() {
            @Override
            public void getIP(IGetSocketIpAndPortListener iGetSocketIpAndPortListener) {
                // 可请求接口后返回到sdk
                iGetSocketIpAndPortListener.onGetSocketIpAndPort("xxx.xxx.xxx",6666);
            }
        });
```
**连接服务端**
```
WKIM.getInstance().getConnectionManager().connection();
```

**发消息**
```
WKIM.getInstance().getConnectionManager().sendMessage(new WKTextContent("我是文本消息"), channelID, channelType);
```

## 监听
**连接状态监听**
```
WKIM.getInstance().getConnectionManager().addOnConnectionStatusListener("listener_key",new IConnectionStatus() {
            @Override
            public void onStatus(int status) {
                // 0 失败 【WKConnectStatus.fail】
                // 1 成功 【WKConnectStatus.success】
                // 2 被踢 【WKConnectStatus.kicked】
                // 3 同步消息中【WKConnectStatus.syncMsg】
                // 4 连接中 【WKConnectStatus.connecting】
                // 5 无网络连接 【WKConnectStatus.noNetwork】
            }
        });
```
**发送消息结果监听**
```
WKIM.getInstance().getMsgManager().addSendMsgAckListener("listener_key", new ISendACK() {
            @Override
            public void msgACK(long clientSeq, String messageID, long messageSeq, byte reasonCode) {
                // clientSeq 客户端序列号
                // messageID 服务器消息ID
                // messageSeq 服务器序列号
                // reasonCode 消息状态码【0:发送中1:成功2:发送失败3:不是好友或不在群内4:黑名单】
            }
        })
 ```
**监听新消息**
```
 WKIM.getInstance().getMsgManager().addOnNewMsgListener("listener_key", new INewMsgListener() {
            @Override
            public void newMsg(List<WKMsg> list) {
                // todo 
            }
        });
```
**命令消息(cmd)监听**
```
WKIM.getInstance().getCMDManager().addCmdListener("listener_key", new ICMDListener() {
            @Override
            public void onMsg(WKCMD cmd) {
                // todo
            }
        });
```
### 许可证
悟空IM 使用 Apache 2.0 许可证。有关详情，请参阅 LICENSE 文件。
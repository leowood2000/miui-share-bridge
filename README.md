# 小米互传桥接

一个很小的 Android 中转应用：让“**小米互传桥接**”作为普通图标出现在原生 Android 分享面板中，然后把收到的文件转交给系统内置的 Xiaomi sender activity。

## 原理

手机上的小米互传设备行由 MIUI 私有框架注入，不能通过普通 `ACTION_SEND` 注册直接复现。本应用注册标准 `ACTION_SEND`/`ACTION_SEND_MULTIPLE`，收到文件后显式转发到：

```text
com.miui.newmidrive/.ui.SendFileIntermediaryActivity
```

该组件在当前 HyperOS 系统上已经存在，并且注册了图片、视频、PDF 等标准分享入口。

## 使用

1. 构建并安装 Debug APK。
2. 在文件管理器选择文件，点击分享。
3. 选择“**小米互传桥接**”。
4. 应用会跳转到小米互联发送界面。

首次测试建议保持 MIUI 优化开启，并确认小米互传、蓝牙和附近设备权限正常。

## 限制

- 这是对系统私有组件的兼容性桥接，不是官方 API。
- HyperOS 更新后如果小米修改 `com.miui.newmidrive` 的 Activity 或参数，可能需要更新。
- 应用本身不上传文件，也不申请网络权限。

## 构建

```text
gradle assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

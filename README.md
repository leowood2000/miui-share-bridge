# 小米互传桥接

这是一个实验性的 Android 分享接收器：它会出现在原生 `ACTION_SEND` / `ACTION_SEND_MULTIPLE` 分享面板中，接收文件后通过 MIUI 的 `IMiShareService` 搜索设备并发送。

## 重要限制

HyperOS 的 `com.miui.mishare.connectivity/.MiShareService` 要求：

```text
com.miui.mishare.PERMISSION.ALL  (signature|privileged)
```

所以普通“点击安装”的 APK 无法调用该服务。普通安装仍可出现在原生分享面板，但点进去会显示权限提示；它不会跳转到小米云盘，也不会假装发送成功。

要实际发送，需要把应用作为系统特权应用安装，并让系统授予上述权限，或使用与系统 MIShare 相同的签名。不同 HyperOS 版本的特权权限白名单和 SELinux 规则可能不同，不能仅靠 APK 自己绕过。

## 安装（KernelSU）

普通 APK 安装不能获得 MIShare 的特权权限。推荐使用 Release 中的 KernelSU 模块：

1. 在 KernelSU 中先安装 `meta-overlayfs` metamodule，并按提示重启。
2. 安装 `miui-share-bridge-privileged-ksu-system-*.zip`，再重启手机。
3. 在文件管理器的原生分享面板选择“**小米互传桥接**”。
4. 选择附近设备即可发送。

如果重启后分享面板没有刷新，可以执行：

```text
adb shell pm enable com.leowood.miuisharebridge
```

APK 仅适合调试或手动安装验证；不具备系统特权时无法实际调用 MIShare 服务。

## 已验证环境

- 设备：Xiaomi 24122RKC7C
- 系统：HyperOS `OS2.0.15.0.VOMCNXM` / Android 15
- MiShare：`com.miui.mishare.connectivity` `3.3.1-103101`

## 已验证功能

- 原生分享面板入口
- MIShare 设备发现
- 通过电脑端 MIShare 发送文件
- Android 15 刘海屏的状态栏、导航栏 Insets 适配

## 构建

```text
gradle assembleDebug
```

生成 `app/build/outputs/apk/debug/app-debug.apk`。

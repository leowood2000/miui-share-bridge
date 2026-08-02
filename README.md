# 小米互传桥接

这是一个实验性的 Android 分享接收器：它会出现在原生 `ACTION_SEND` / `ACTION_SEND_MULTIPLE` 分享面板中，接收文件后通过 MIUI 的 `IMiShareService` 搜索设备并发送。

## 重要限制

HyperOS 的 `com.miui.mishare.connectivity/.MiShareService` 要求：

```text
com.miui.mishare.PERMISSION.ALL  (signature|privileged)
```

所以普通“点击安装”的 APK 无法调用该服务。普通安装仍可出现在原生分享面板，但点进去会显示权限提示；它不会跳转到小米云盘，也不会假装发送成功。

要实际发送，需要把应用作为系统特权应用安装，并让系统授予上述权限，或使用与系统 MIShare 相同的签名。不同 HyperOS 版本的特权权限白名单和 SELinux 规则可能不同，不能仅靠 APK 自己绕过。

## 使用

1. 安装 APK。
2. 在文件管理器的原生分享面板选择“**小米互传桥接**”。
3. 特权环境下应用会列出附近设备，点击设备即可发送。

## 已验证环境

- 设备：Xiaomi 24122RKC7C
- 系统：HyperOS `OS2.0.15.0.VOMCNXM` / Android 15
- MiShare：`com.miui.mishare.connectivity` `3.3.1-103101`

## 构建

```text
gradle assembleDebug
```

生成 `app/build/outputs/apk/debug/app-debug.apk`。

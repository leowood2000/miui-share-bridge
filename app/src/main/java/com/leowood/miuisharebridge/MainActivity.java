package com.leowood.miuisharebridge;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;

public final class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        TextView text = new TextView(this);
        text.setText("小米互传桥接\n\n从原生分享面板选择“ 小米互传桥接 ”，应用会搜索设备并发送文件。\n\n注意：HyperOS 的 MiShareService 只允许系统特权应用调用。普通安装会显示权限提示；需要 root/系统签名环境才能真正发送。\n\n本应用不上传文件。\n\n版本 1.2.0");
        text.setTextColor(Color.DKGRAY);
        text.setTextSize(18);
        text.setGravity(Gravity.CENTER);
        text.setPadding(48, 48, 48, 48);
        setContentView(text);
    }
}

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
        text.setText("小米互传桥接\n\n从原生分享面板选择“ 小米互传桥接 ”，\n应用会把文件转交给手机内置的小米互联发送入口。\n\n如果转交失败，请开启 MIUI 优化并确认小米互传可用。\n\n本应用不读取或上传文件。\n\n版本 1.0.0");
        text.setTextColor(Color.DKGRAY);
        text.setTextSize(18);
        text.setGravity(Gravity.CENTER);
        text.setPadding(48, 48, 48, 48);
        setContentView(text);
    }
}

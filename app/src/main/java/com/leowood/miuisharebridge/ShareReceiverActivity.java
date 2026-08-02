package com.leowood.miuisharebridge;

import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

/** Receives a normal Android share and probes Xiaomi's private MiShare service. */
public final class ShareReceiverActivity extends Activity {
    private static final String TAG = "MiShareBridge";
    private static final String TARGET_PACKAGE = "com.miui.mishare.connectivity";
    private boolean bound;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
            bound = true;
            Log.i(TAG, "MiShare service connected: " + name);
            fail("已连接小米互传服务，正在继续开发发送流程");
        }

        @Override public void onServiceDisconnected(ComponentName name) {
            bound = false;
            Log.w(TAG, "MiShare service disconnected: " + name);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        probe(getIntent());
    }

    private void probe(Intent source) {
        String action = source.getAction();
        if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            fail("不是文件分享 Intent");
            return;
        }

        ClipData clipData = source.getClipData();
        if (clipData != null) {
            grantClipData(clipData);
        }
        Uri single = source.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        if (single != null) {
            grant(single);
        }
        ArrayList<Uri> multiple = source.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri.class);
        if (multiple != null) {
            for (Uri uri : multiple) {
                grant(uri);
            }
        }

        Intent service = new Intent();
        service.setComponent(new ComponentName(TARGET_PACKAGE,
                "com.miui.mishare.connectivity.MiShareService"));
        try {
            if (!bindService(service, connection, BIND_AUTO_CREATE)) {
                fail("无法连接小米互传服务：bindService 返回 false");
            }
        } catch (SecurityException error) {
            Log.e(TAG, "MiShare bind rejected", error);
            fail("小米互传服务拒绝普通应用：需要系统签名/特权权限");
        }
    }

    private void grantClipData(ClipData data) {
        for (int i = 0; i < data.getItemCount(); i++) {
            grant(data.getItemAt(i).getUri());
        }
    }

    private void grant(Uri uri) {
        if (uri == null) return;
        try {
            grantUriPermission(TARGET_PACKAGE, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // The target may already have access through the original ClipData grant.
        }
    }

    private void fail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
}

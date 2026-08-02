package com.leowood.miuisharebridge;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

/** Receives a normal Android share and forwards it to Xiaomi's existing sender activity. */
public final class ShareReceiverActivity extends Activity {
    private static final String TARGET_PACKAGE = "com.miui.newmidrive";
    private static final String TARGET_ACTIVITY = "com.miui.newmidrive.ui.SendFileIntermediaryActivity";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        forward(getIntent());
    }

    private void forward(Intent source) {
        String action = source.getAction();
        if (!Intent.ACTION_SEND.equals(action) && !Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            fail("不是文件分享 Intent");
            return;
        }

        Intent target = new Intent(source);
        target.setComponent(new ComponentName(TARGET_PACKAGE, TARGET_ACTIVITY));
        target.setPackage(TARGET_PACKAGE);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        ClipData clipData = source.getClipData();
        if (clipData != null) {
            target.setClipData(clipData);
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

        try {
            startActivity(target);
            finish();
        } catch (ActivityNotFoundException | SecurityException error) {
            fail("小米互联发送入口不可用：" + error.getClass().getSimpleName());
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

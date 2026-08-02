package com.leowood.miuisharebridge;

import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.miui.mishare.MiShareTask;
import com.miui.mishare.RemoteDevice;
import com.leowood.miuisharebridge.aidl.IMiShareDiscoverCallback;
import com.leowood.miuisharebridge.aidl.IMiShareService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Receives ACTION_SEND and uses Xiaomi's private MiShare AIDL when privileged. */
public final class ShareReceiverActivity extends Activity {
    private static final String TARGET_PACKAGE = "com.miui.mishare.connectivity";
    private final Map<String, RemoteDevice> devices = new LinkedHashMap<>();
    private final ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView status;
    private Intent source;
    private IMiShareService service;
    private boolean bound;

    private final IMiShareDiscoverCallback callback = new IMiShareDiscoverCallback.Stub() {
        @Override public void onDeviceUpdated(RemoteDevice device) {
            if (device == null || device.getDeviceId() == null) return;
            runOnUiThread(() -> addDevice(device));
        }

        @Override public void onDeviceLost(String deviceId) {
            runOnUiThread(() -> removeDevice(deviceId));
        }
    };

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            bound = true;
            service = IMiShareService.Stub.asInterface(binder);
            status.setText("正在搜索附近的小米互传设备…");
            try {
                service.enable();
                service.discover(callback);
            } catch (RemoteException error) {
                showError("MiShare 服务调用失败：" + error.getMessage());
            }
        }

        @Override public void onServiceDisconnected(ComponentName name) {
            bound = false;
            service = null;
            showError("小米互传服务已断开");
        }
    };

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        source = getIntent();
        buildUi();
        if (!Intent.ACTION_SEND.equals(source.getAction())
                && !Intent.ACTION_SEND_MULTIPLE.equals(source.getAction())) {
            showError("这不是文件分享请求");
            return;
        }
        grantSourceUris();
        Intent serviceIntent = new Intent().setComponent(new ComponentName(
                TARGET_PACKAGE, TARGET_PACKAGE + ".MiShareService"));
        try {
            if (!bindService(serviceIntent, connection, BIND_AUTO_CREATE)) {
                showError("无法连接 MiShare 服务");
            }
        } catch (SecurityException error) {
            showError("普通 APK 无法调用 MiShareService。\n\n"
                    + "系统返回：Not allowed to bind to service\n\n"
                    + "需要把本应用安装为系统特权应用，或使用与 MIShare 相同的系统签名。");
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 40, 40, 20);
        status = new TextView(this);
        status.setTextSize(17);
        status.setText("正在连接小米互传…");
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));
        ListView list = new ListView(this);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            if (position < labels.size()) sendTo(devices.get(new ArrayList<>(devices.keySet()).get(position)));
        });
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addDevice(RemoteDevice device) {
        String id = device.getDeviceId();
        String label = id;
        Bundle extras = device.getExtras();
        if (extras != null) {
            String nickname = extras.getString(RemoteDevice.KEY_NICKNAME);
            String model = extras.getString(RemoteDevice.KEY_DEVICE_MODEL);
            if (nickname != null && !nickname.isEmpty()) label = nickname;
            else if (model != null && !model.isEmpty()) label = model;
        }
        if (!devices.containsKey(id)) labels.add(label);
        devices.put(id, device);
        adapter.notifyDataSetChanged();
        status.setText("选择要发送到的设备：");
    }

    private void removeDevice(String id) {
        if (id == null || !devices.containsKey(id)) return;
        int index = new ArrayList<>(devices.keySet()).indexOf(id);
        devices.remove(id);
        if (index >= 0 && index < labels.size()) labels.remove(index);
        adapter.notifyDataSetChanged();
    }

    private void sendTo(RemoteDevice device) {
        if (device == null || service == null) return;
        ClipData clipData = source.getClipData();
        if (clipData == null) {
            Uri uri = source.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
            if (uri != null) clipData = ClipData.newRawUri("shared", uri);
        }
        if (clipData == null || clipData.getItemCount() == 0) {
            showError("分享请求没有文件 URI");
            return;
        }
        MiShareTask task = new MiShareTask();
        task.send = true;
        task.taskId = UUID.randomUUID().toString();
        task.count = clipData.getItemCount();
        task.device = device;
        task.clipData = clipData;
        task.mimeType = source.getType();
        try {
            service.send(task);
            status.setText("已提交发送任务：" + task.count + " 个文件");
        } catch (RemoteException error) {
            showError("发送失败：" + error.getMessage());
        }
    }

    private void grantSourceUris() {
        ClipData data = source.getClipData();
        if (data != null) for (int i = 0; i < data.getItemCount(); i++) grant(data.getItemAt(i).getUri());
        Uri uri = source.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        if (uri != null) grant(uri);
    }

    private void grant(Uri uri) {
        if (uri == null) return;
        try { grantUriPermission(TARGET_PACKAGE, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
        catch (SecurityException ignored) { }
    }

    private void showError(String message) {
        if (status != null) status.setText(message);
    }

    @Override protected void onDestroy() {
        if (bound) {
            try { if (service != null) service.stopDiscover(callback); } catch (RemoteException ignored) { }
            unbindService(connection);
        }
        super.onDestroy();
    }
}

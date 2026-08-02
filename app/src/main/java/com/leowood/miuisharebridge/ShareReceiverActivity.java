package com.leowood.miuisharebridge;

import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Receives ACTION_SEND and calls the MIUI framework AIDL proxy on HyperOS. */
public final class ShareReceiverActivity extends Activity {
    private static final String TARGET_PACKAGE = "com.miui.mishare.connectivity";
    private static final String SERVICE_CLASS = TARGET_PACKAGE + ".MiShareService";
    private static final String SERVICE_API = "com.miui.mishare.IMiShareService";
    private static final String CALLBACK_API = "com.miui.mishare.IMiShareDiscoverCallback";
    private static final String DEVICE_CLASS = "com.miui.mishare.RemoteDevice";
    private static final String TASK_CLASS = "com.miui.mishare.MiShareTask";
    private static final String CALLBACK_DESCRIPTOR = CALLBACK_API;
    private static final int CALLBACK_DEVICE_UPDATED = 1;
    private static final int CALLBACK_DEVICE_LOST = 2;

    private final Map<String, Object> devices = new LinkedHashMap<>();
    private final ArrayList<String> labels = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView status;
    private Intent source;
    private Object service;
    private Class<?> serviceApi;
    private Class<?> callbackType;
    private Object callbackProxy;
    private CallbackBinder callbackBinder;
    private boolean bound;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder binder) {
            bound = true;
            try {
                serviceApi = Class.forName(SERVICE_API);
                Class<?> stub = Class.forName(SERVICE_API + "$Stub");
                service = stub.getMethod("asInterface", IBinder.class).invoke(null, binder);
                callbackType = Class.forName(CALLBACK_API);
                callbackBinder = new CallbackBinder(Class.forName(DEVICE_CLASS));
                callbackProxy = Proxy.newProxyInstance(callbackType.getClassLoader(),
                        new Class<?>[]{callbackType}, new CallbackHandler(callbackBinder));
                status.setText("正在搜索附近的小米互传设备…");
                try { invokeService("enable", new Class<?>[0]); }
                catch (Exception ignored) { /* Some HyperOS builds enable internally. */ }
                invokeService("discover", new Class<?>[]{callbackType}, callbackProxy);
            } catch (Exception error) {
                showError("MiShare AIDL 初始化失败：" + rootMessage(error));
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
                TARGET_PACKAGE, SERVICE_CLASS));
        try {
            if (!bindService(serviceIntent, connection, BIND_AUTO_CREATE)) {
                showError("无法连接 MiShare 服务");
            }
        } catch (SecurityException error) {
            showError("普通 APK 无法调用 MiShareService。\n\n"
                    + "请将本应用安装为系统特权应用，并授予 MiShare 权限。");
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
            if (position < labels.size()) {
                String deviceId = new ArrayList<>(devices.keySet()).get(position);
                sendTo(devices.get(deviceId));
            }
        });
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void addDevice(Object device) {
        try {
            Method getId = device.getClass().getMethod("getDeviceId");
            String id = (String) getId.invoke(device);
            if (id == null) return;
            String label = id;
            Bundle extras = (Bundle) device.getClass().getMethod("getExtras").invoke(device);
            if (extras != null) {
                String nickname = extras.getString("nickname");
                String model = extras.getString("device_model");
                if (nickname != null && !nickname.isEmpty()) label = nickname;
                else if (model != null && !model.isEmpty()) label = model;
            }
            if (!devices.containsKey(id)) labels.add(label);
            devices.put(id, device);
            adapter.notifyDataSetChanged();
            status.setText("选择要发送到的设备：");
        } catch (Exception error) {
            showError("读取设备信息失败：" + rootMessage(error));
        }
    }

    private void removeDevice(String id) {
        if (id == null || !devices.containsKey(id)) return;
        int index = new ArrayList<>(devices.keySet()).indexOf(id);
        devices.remove(id);
        if (index >= 0 && index < labels.size()) labels.remove(index);
        adapter.notifyDataSetChanged();
    }

    private void sendTo(Object device) {
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
        try {
            Class<?> taskType = Class.forName(TASK_CLASS);
            Constructor<?> ctor = taskType.getDeclaredConstructor();
            ctor.setAccessible(true);
            Object task = ctor.newInstance();
            setField(taskType, task, "send", true);
            setField(taskType, task, "taskId", UUID.randomUUID().toString());
            setField(taskType, task, "count", clipData.getItemCount());
            setField(taskType, task, "device", device);
            setField(taskType, task, "clipData", clipData);
            setField(taskType, task, "mimeType", source.getType());
            invokeService("send", new Class<?>[]{taskType}, task);
            status.setText("已提交发送任务：" + clipData.getItemCount() + " 个文件");
        } catch (Exception error) {
            showError("发送失败：" + rootMessage(error));
        }
    }

    private Object invokeService(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        Method method = serviceApi.getMethod(methodName, parameterTypes);
        try {
            return method.invoke(service, args);
        } catch (InvocationTargetException error) {
            Throwable cause = error.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw error;
        }
    }

    private static void setField(Class<?> type, Object object, String name, Object value)
            throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.set(object, value);
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
        if (status != null) runOnUiThread(() -> status.setText(message));
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) current = current.getCause();
        return current.getClass().getSimpleName() + ": " + current.getMessage();
    }

    @Override protected void onDestroy() {
        if (bound) {
            try {
                if (service != null && callbackProxy != null) {
                    invokeService("stopDiscover", new Class<?>[]{callbackType}, callbackProxy);
                }
            } catch (Exception ignored) { }
            unbindService(connection);
        }
        super.onDestroy();
    }

    private final class CallbackHandler implements InvocationHandler {
        private final IBinder binder;
        CallbackHandler(IBinder binder) { this.binder = binder; }
        @Override public Object invoke(Object proxy, Method method, Object[] args) {
            if ("asBinder".equals(method.getName())) return binder;
            if ("toString".equals(method.getName())) return "MiShareBridgeCallback";
            return null;
        }
    }

    private final class CallbackBinder extends android.os.Binder {
        private final Class<?> deviceType;
        CallbackBinder(Class<?> deviceType) {
            this.deviceType = deviceType;
            attachInterface(null, CALLBACK_DESCRIPTOR);
        }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(CALLBACK_DESCRIPTOR);
                return true;
            }
            data.enforceInterface(CALLBACK_DESCRIPTOR);
            if (code == CALLBACK_DEVICE_UPDATED) {
                try {
                    Object device = data.readParcelable(deviceType.getClassLoader());
                    runOnUiThread(() -> addDevice(device));
                } catch (RuntimeException ignored) { }
                return true;
            }
            if (code == CALLBACK_DEVICE_LOST) {
                String id = data.readString();
                runOnUiThread(() -> removeDevice(id));
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}

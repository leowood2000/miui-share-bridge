package com.leowood.miuisharebridge.aidl;

import android.content.ClipData;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

/** Minimal wire-compatible proxy for the MIShare AIDL interface. */
public interface IMiShareService {
    void enable() throws RemoteException;
    void discover(IMiShareDiscoverCallback callback) throws RemoteException;
    void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException;
    void send(ShareTask task) throws RemoteException;

    final class ShareTask {
        public final String taskId;
        public final int count;
        public final String deviceId;
        public final Bundle deviceExtras;
        public final ClipData clipData;
        public final String mimeType;

        public ShareTask(String taskId, int count, String deviceId, Bundle deviceExtras,
                         ClipData clipData, String mimeType) {
            this.taskId = taskId;
            this.count = count;
            this.deviceId = deviceId;
            this.deviceExtras = deviceExtras;
            this.clipData = clipData;
            this.mimeType = mimeType;
        }
    }

    final class Proxy implements IMiShareService {
        private static final String DESCRIPTOR = "com.miui.mishare.IMiShareService";
        private final IBinder remote;
        public Proxy(IBinder remote) { this.remote = remote; }

        private void callVoid(int code, Parcel data) throws RemoteException {
            Parcel reply = Parcel.obtain();
            try {
                remote.transact(code, data, reply, 0);
                reply.readException();
            } finally { reply.recycle(); data.recycle(); }
        }

        @Override public void enable() throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            callVoid(4, data);
        }
        @Override public void discover(IMiShareDiscoverCallback callback) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback == null ? null : callback.asBinder());
            callVoid(6, data);
        }
        @Override public void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback == null ? null : callback.asBinder());
            callVoid(8, data);
        }
        @Override public void send(ShareTask task) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            if (task == null) {
                data.writeInt(0);
            } else {
                data.writeInt(1);
                data.writeByte((byte) 1);
                data.writeString(task.taskId);
                data.writeInt(task.count);
                writeRemoteDevice(data, task.deviceId, task.deviceExtras);
                data.writeInt(0);
                data.writeInt(0);
                data.writeParcelable(task.clipData, 0);
                data.writeString(task.mimeType);
                data.writeInt(0);
                data.writeInt(0);
            }
            callVoid(9, data);
        }

        private static void writeRemoteDevice(Parcel data, String deviceId, Bundle extras) {
            data.writeString("com.miui.mishare.RemoteDevice");
            data.writeString(deviceId);
            data.writeBundle(extras);
        }
    }

    final class Stub {
        private Stub() {}
        public static IMiShareService asInterface(IBinder binder) {
            return binder == null ? null : new Proxy(binder);
        }
    }
}

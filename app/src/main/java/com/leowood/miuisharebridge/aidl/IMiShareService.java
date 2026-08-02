package com.leowood.miuisharebridge.aidl;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.miui.mishare.MiShareTask;

/** Minimal proxy for the MIShare AIDL interface used by the bridge. */
public interface IMiShareService {
    void enable() throws RemoteException;
    void discover(IMiShareDiscoverCallback callback) throws RemoteException;
    void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException;
    void send(MiShareTask task) throws RemoteException;

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
            callVoid(6, data);
        }
        @Override public void discover(IMiShareDiscoverCallback callback) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback == null ? null : callback.asBinder());
            callVoid(4, data);
        }
        @Override public void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            data.writeStrongBinder(callback == null ? null : callback.asBinder());
            callVoid(16, data);
        }
        @Override public void send(MiShareTask task) throws RemoteException {
            Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
            data.writeParcelable(task, 0);
            callVoid(15, data);
        }
    }

    final class Stub {
        private Stub() {}
        public static IMiShareService asInterface(IBinder binder) {
            return binder == null ? null : new Proxy(binder);
        }
    }
}

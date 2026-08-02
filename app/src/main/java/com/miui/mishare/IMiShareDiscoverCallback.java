package com.miui.mishare;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMiShareDiscoverCallback extends IInterface {
    void onDeviceUpdated(RemoteDevice device) throws RemoteException;
    void onDeviceLost(String deviceId) throws RemoteException;

    abstract class Stub extends Binder implements IMiShareDiscoverCallback {
        private static final String DESCRIPTOR = "com.miui.mishare.IMiShareDiscoverCallback";
        private static final int TRANSACTION_onDeviceUpdated = 1;
        private static final int TRANSACTION_onDeviceLost = 2;

        public Stub() { attachInterface(this, DESCRIPTOR); }

        public static IMiShareDiscoverCallback asInterface(IBinder binder) {
            if (binder == null) return null;
            IInterface local = binder.queryLocalInterface(DESCRIPTOR);
            return local instanceof IMiShareDiscoverCallback
                    ? (IMiShareDiscoverCallback) local : new Proxy(binder);
        }

        @Override public IBinder asBinder() { return this; }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            if (code == TRANSACTION_onDeviceUpdated) {
                data.enforceInterface(DESCRIPTOR);
                onDeviceUpdated(data.readParcelable(RemoteDevice.class.getClassLoader()));
                return true;
            }
            if (code == TRANSACTION_onDeviceLost) {
                data.enforceInterface(DESCRIPTOR);
                onDeviceLost(data.readString());
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static final class Proxy implements IMiShareDiscoverCallback {
            private final IBinder remote;
            Proxy(IBinder remote) { this.remote = remote; }
            @Override public IBinder asBinder() { return remote; }

            @Override public void onDeviceUpdated(RemoteDevice device) throws RemoteException {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeParcelable(device, 0);
                    remote.transact(TRANSACTION_onDeviceUpdated, data, null, IBinder.FLAG_ONEWAY);
                } finally { data.recycle(); }
            }

            @Override public void onDeviceLost(String deviceId) throws RemoteException {
                Parcel data = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    data.writeString(deviceId);
                    remote.transact(TRANSACTION_onDeviceLost, data, null, IBinder.FLAG_ONEWAY);
                } finally { data.recycle(); }
            }
        }
    }
}

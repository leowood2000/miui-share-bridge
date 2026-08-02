package com.leowood.miuisharebridge.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.miui.mishare.RemoteDevice;

/** Local Binder callback using MIShare's wire descriptor. */
public interface IMiShareDiscoverCallback extends IInterface {
    void onDeviceUpdated(RemoteDevice device) throws RemoteException;
    void onDeviceLost(String deviceId) throws RemoteException;

    abstract class Stub extends Binder implements IMiShareDiscoverCallback {
        private static final String DESCRIPTOR = "com.miui.mishare.IMiShareDiscoverCallback";
        private static final int UPDATED = 1;
        private static final int LOST = 2;

        public Stub() { attachInterface(this, DESCRIPTOR); }
        @Override public IBinder asBinder() { return this; }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            data.enforceInterface(DESCRIPTOR);
            if (code == UPDATED) {
                onDeviceUpdated(data.readParcelable(RemoteDevice.class.getClassLoader()));
                return true;
            }
            if (code == LOST) {
                onDeviceLost(data.readString());
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }
}

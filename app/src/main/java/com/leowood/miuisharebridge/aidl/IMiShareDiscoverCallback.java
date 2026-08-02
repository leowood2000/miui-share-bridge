package com.leowood.miuisharebridge.aidl;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/** Local Binder callback using MIShare's wire format without loading MIUI classes. */
public interface IMiShareDiscoverCallback extends IInterface {
    void onDeviceUpdated(String deviceId, Bundle extras) throws RemoteException;
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
                String deviceId = null;
                Bundle extras = null;
                if (data.readInt() != 0) {
                    deviceId = data.readString();
                    extras = data.readBundle();
                }
                onDeviceUpdated(deviceId, extras);
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

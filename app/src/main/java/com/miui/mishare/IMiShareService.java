package com.miui.mishare;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMiShareService extends IInterface {
    void enable() throws RemoteException;
    int getState() throws RemoteException;
    void discover(IMiShareDiscoverCallback callback) throws RemoteException;
    void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException;
    void send(MiShareTask task) throws RemoteException;

    abstract class Stub extends Binder implements IMiShareService {
        private static final String DESCRIPTOR = "com.miui.mishare.IMiShareService";
        private static final int TRANSACTION_discover = 4;
        private static final int TRANSACTION_enable = 6;
        private static final int TRANSACTION_getState = 7;
        private static final int TRANSACTION_send = 15;
        private static final int TRANSACTION_stopDiscover = 16;

        public Stub() { attachInterface(this, DESCRIPTOR); }

        public static IMiShareService asInterface(IBinder binder) {
            if (binder == null) return null;
            IInterface local = binder.queryLocalInterface(DESCRIPTOR);
            return local instanceof IMiShareService ? (IMiShareService) local : new Proxy(binder);
        }

        @Override public IBinder asBinder() { return this; }

        @Override public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            if (code == INTERFACE_TRANSACTION) {
                reply.writeString(DESCRIPTOR);
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static final class Proxy implements IMiShareService {
            private final IBinder remote;
            Proxy(IBinder remote) { this.remote = remote; }
            @Override public IBinder asBinder() { return remote; }

            private void callVoid(int code, Parcel data) throws RemoteException {
                Parcel reply = Parcel.obtain();
                try {
                    remote.transact(code, data, reply, 0);
                    reply.readException();
                } finally { reply.recycle(); data.recycle(); }
            }

            @Override public void enable() throws RemoteException {
                Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
                callVoid(TRANSACTION_enable, data);
            }

            @Override public int getState() throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(DESCRIPTOR);
                    remote.transact(TRANSACTION_getState, data, reply, 0);
                    reply.readException();
                    return reply.readInt();
                } finally { reply.recycle(); data.recycle(); }
            }

            @Override public void discover(IMiShareDiscoverCallback callback) throws RemoteException {
                Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder(callback == null ? null : callback.asBinder());
                callVoid(TRANSACTION_discover, data);
            }

            @Override public void stopDiscover(IMiShareDiscoverCallback callback) throws RemoteException {
                Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
                data.writeStrongBinder(callback == null ? null : callback.asBinder());
                callVoid(TRANSACTION_stopDiscover, data);
            }

            @Override public void send(MiShareTask task) throws RemoteException {
                Parcel data = Parcel.obtain(); data.writeInterfaceToken(DESCRIPTOR);
                data.writeParcelable(task, 0);
                callVoid(TRANSACTION_send, data);
            }
        }
    }
}

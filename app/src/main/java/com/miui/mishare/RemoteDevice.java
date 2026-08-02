package com.miui.mishare;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/** Wire-compatible copy of the MIShare remote-device parcelable. */
public final class RemoteDevice implements Parcelable {
    public static final String KEY_IS_PC = "is_pc";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_DEVICE_MODEL = "device_model";

    private final String deviceId;
    private final Bundle extras;

    public RemoteDevice(String deviceId, Bundle extras) {
        this.deviceId = deviceId;
        this.extras = extras;
    }

    private RemoteDevice(Parcel in) {
        deviceId = in.readString();
        extras = in.readBundle(RemoteDevice.class.getClassLoader());
    }

    public String getDeviceId() { return deviceId; }
    public Bundle getExtras() { return extras; }

    @Override public void writeToParcel(Parcel out, int flags) {
        out.writeString(deviceId);
        out.writeBundle(extras);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<RemoteDevice> CREATOR = new Creator<RemoteDevice>() {
        @Override public RemoteDevice createFromParcel(Parcel in) { return new RemoteDevice(in); }
        @Override public RemoteDevice[] newArray(int size) { return new RemoteDevice[size]; }
    };
}

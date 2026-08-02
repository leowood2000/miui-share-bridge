package com.miui.mishare;

import android.content.ClipData;
import android.os.Parcel;
import android.os.Parcelable;

/** Wire-compatible copy of the MIShare task parcelable. */
public final class MiShareTask implements Parcelable {
    public ClipData clipData;
    public int count;
    public RemoteDevice device;
    public int deviceX;
    public int deviceY;
    public String mimeType;
    public boolean send;
    public String taskId;
    public int tbHeight;
    public int tbWidth;

    public MiShareTask() {}

    private MiShareTask(Parcel in) {
        send = in.readByte() != 0;
        taskId = in.readString();
        count = in.readInt();
        device = in.readParcelable(RemoteDevice.class.getClassLoader());
        deviceX = in.readInt();
        deviceY = in.readInt();
        clipData = in.readParcelable(ClipData.class.getClassLoader());
        mimeType = in.readString();
        tbWidth = in.readInt();
        tbHeight = in.readInt();
    }

    @Override public void writeToParcel(Parcel out, int flags) {
        out.writeByte((byte) (send ? 1 : 0));
        out.writeString(taskId);
        out.writeInt(count);
        out.writeParcelable(device, flags);
        out.writeInt(deviceX);
        out.writeInt(deviceY);
        out.writeParcelable(clipData, flags);
        out.writeString(mimeType);
        out.writeInt(tbWidth);
        out.writeInt(tbHeight);
    }

    @Override public int describeContents() { return 0; }

    public static final Creator<MiShareTask> CREATOR = new Creator<MiShareTask>() {
        @Override public MiShareTask createFromParcel(Parcel in) { return new MiShareTask(in); }
        @Override public MiShareTask[] newArray(int size) { return new MiShareTask[size]; }
    };
}

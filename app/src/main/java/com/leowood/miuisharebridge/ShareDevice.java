package com.leowood.miuisharebridge;

import android.os.Bundle;

/** Local representation of MIShare's device parcelable. */
final class ShareDevice {
    final String deviceId;
    final Bundle extras;

    ShareDevice(String deviceId, Bundle extras) {
        this.deviceId = deviceId;
        this.extras = extras;
    }
}

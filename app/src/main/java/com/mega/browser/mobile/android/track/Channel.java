package com.mega.browser.mobile.android.track;

import android.content.Context;
import android.text.TextUtils;

public class Channel {

    private static String CHANNEL = "";
    public static String CHANNEL_TAG = "Channel";

    public static void init(Context context) {
        try {
            CHANNEL = readChannelFromString(context);
        } catch (Throwable ignore) {
//            Timber.e(ignore);
        }
        if (TextUtils.isEmpty(CHANNEL)) {
            CHANNEL = "A0";
        }
    }

    private static String readChannelFromString(Context context) {
        return context.getString(context.getResources().getIdentifier("MEGA_BROWSER_CHANNEL", "string", context.getPackageName()));
    }




    public static String channel() {
        return CHANNEL;
    }
}

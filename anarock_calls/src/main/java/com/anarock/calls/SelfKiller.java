package com.anarock.calls;

import android.app.ActivityManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

public class SelfKiller {
    private final static String TAG = "SerialKiller";
    public static void killSelf(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(context.getPackageName());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // this is all we can do before ICS. luckily Xiaomi phones have newer system versions :)
            System.exit(1);
            return;
        }

        // set up a callback so System.exit() is called as soon as all
        // the activities are finished
        context.registerComponentCallbacks(new ComponentCallbacks2() {
            @Override
            public void onTrimMemory(int i) {
                if (i == TRIM_MEMORY_UI_HIDDEN) {
                    Log.v(TAG, "UI Hidden");
                    System.exit(1);
                }
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {

            }

            @Override
            public void onLowMemory() {

            }
        });

        // see below
        ActivityTracker.getInstance().finishAllActivities();
    }
}

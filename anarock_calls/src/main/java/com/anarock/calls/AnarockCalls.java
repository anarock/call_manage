package com.anarock.calls;

import android.app.Application;

import com.bugsnag.android.Bugsnag;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class AnarockCalls {

    static public void onCreate(Application app) {
        initCrashlytics(app);
        Bugsnag.init(app);
        ResurrectionService.start(app);
        ActivityTracker.init(app);
    }

    static public void onTerminate(Application app) {
        ActivityTracker.release(app);
    }

    static private void initCrashlytics(Application app) {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(app, crashlyticsKit);
    }
}

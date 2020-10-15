package com.anarock.calls;

import android.app.Application;

import com.bugsnag.android.Bugsnag;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initCrashlytics();
        Bugsnag.init(this);
        ResurrectionService.start(this);
        ActivityTracker.init(this);
    }

    public void onTerminate() {
        super.onTerminate();
        ActivityTracker.release(this);
    }

    private void initCrashlytics() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        Fabric.with(this, crashlyticsKit);
    }
}

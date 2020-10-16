package com.chooloo.www.callmanager;

import android.app.Application;

import com.anarock.calls.AnarockCalls;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import timber.log.Timber;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.Tree() {
                @Override
                protected void log(int priority, @Nullable String tag, @NotNull String message, @Nullable Throwable t) {
                    //Do nothing
                }
            });
        }

        AnarockCalls.onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AnarockCalls.onTerminate(this);
    }
}

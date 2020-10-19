package com.anarock.calls;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bugsnag.android.Bugsnag;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONException;
import org.json.JSONObject;

public class FirebaseHelper {
    static public String FLAG_UPDATE_REQUIRED = "FIREBASE_UPDATE_REQUIRED";
    static public String DATA_LATEST_APP_URL = "FIREBASE_LATEST_APP_URL";

    final private Activity context;

    private FirebaseRemoteConfig firebaseRemoteConfig;

    public FirebaseHelper(Activity activity) {
        this.context = activity;
    }

    public void initRemoteConfig() {
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public void fetchRemoteConfig() {
        int cacheExpiration = 15 * 60; // 15 mins

        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        firebaseRemoteConfig.fetch(cacheExpiration).addOnCompleteListener(context, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                firebaseRemoteConfig.activateFetched();

                if (!isUpdateRequired()) {
                    return;
                }

                Bundle extras = new Bundle();
                extras.putBoolean(FLAG_UPDATE_REQUIRED, true);
                extras.putString(DATA_LATEST_APP_URL, getLatestAppUrl());

                Intent intent = new Intent(context, PermissionsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtras(extras);

                context.startActivity(intent);
            }
        });
    }

    private long getAppVersionCode() {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Bugsnag.notify(e);
        }
        return 0L;
    }

    private JSONObject getRemoteConfig() {
        try {
            return new JSONObject(firebaseRemoteConfig.getValue(BuildConfig.REMOTE_CONFIG_KEY).asString());
        } catch (JSONException e) {
            Bugsnag.notify(e);
        }
        return new JSONObject();
    }

    @Nullable
    private String getLatestAppUrl() {
        try {
            return getRemoteConfig().getString("latest_app_url");
        } catch (JSONException e) {
            Bugsnag.notify(e);
        }
        return null;
    }

    private boolean isUpdateRequired() {
        try {
            return getRemoteConfig().getLong("latest_app_version_code") > getAppVersionCode();
        } catch (JSONException e) {
            Bugsnag.notify(e);
        }
        return false;
    }
}
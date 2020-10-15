package com.anarock.calls;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.util.ArraySet;
import android.util.Log;

import java.util.Collections;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public final class ActivityTracker implements Application.ActivityLifecycleCallbacks {
    private final static String TAG = "ActivityTracker";

    private final Set<Activity> mCreatedActivities = new ArraySet<>();

    public static ActivityTracker getInstance() {
        return Holder.INSTANCE;
    }

    public static void init(Application application) {
        application.registerActivityLifecycleCallbacks(getInstance());
    }

    public static void release(Application application) {
        ActivityTracker activityTracker = getInstance();
        application.unregisterActivityLifecycleCallbacks(activityTracker);
        activityTracker.mCreatedActivities.clear();
    }

    public void finishAllActivities() {
        // iterate over active activities and finish them all
        for (Activity activity : mCreatedActivities) {
            Log.v(TAG, "Finishing " + activity);
            activity.finish();
        }
    }

    public Set<Activity> getCreatedActivities() {
        return Collections.unmodifiableSet(mCreatedActivities);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mCreatedActivities.add(activity);
    }

    @Override
    public  void onActivityStarted(Activity activity) {
    }

    @Override
    public  void onActivityResumed(Activity activity) {
    }

    @Override
    public  void onActivityPaused(Activity activity) {
    }

    @Override
    public  void onActivityStopped(Activity activity) {
    }

    @Override
    public  void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mCreatedActivities.remove(activity);
    }

    private static final class Holder {
        private static final ActivityTracker INSTANCE = new ActivityTracker();
    }
}

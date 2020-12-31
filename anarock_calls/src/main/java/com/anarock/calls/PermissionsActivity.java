package com.anarock.calls;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bugsnag.android.Bugsnag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final List<String> permissions = new ArrayList<String>() {{
        add(Manifest.permission.READ_CALL_LOG);
        add(Manifest.permission.READ_PHONE_STATE);
        add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        add(Manifest.permission.CALL_PHONE);
    }};

    public static final List<Intent> POWERMANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")).setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(Uri.parse("mobilemanager://function/entry/AutoStart")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"))
                    .setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart")),
            new Intent().setComponent(new ComponentName("com.meizu.safe", "com.meizu.safe.security.SHOW_APPSEC")).addCategory(Intent.CATEGORY_DEFAULT).putExtra("packageName", BuildConfig.APPLICATION_ID),
            new Intent().setComponent(new ComponentName("com.dewav.dwappmanager", "com.dewav.dwappmanager.memory.SmartClearupWhiteList"))
    );

    private List<String> pendingPermissions;

    ImageView illustration;
    TextView title;
    TextView description;
    Button actionButton;
    Button actionButton2;
    Button skipButton;

    private final int PERMISSION_REQUEST_CODE = 200;

    private boolean hasAutoStartTestFailed;

    private boolean alreadyEnabled;
    private boolean skipTest;
    private boolean isUpdateRequired;
    private String latestAppUrl;

    private Resources res;

    public static void init(Context context) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(context, PermissionsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            skipTest = extras.containsKey(AutostartDetector.AUTO_START_FAILURE);
            alreadyEnabled = skipTest;
            hasAutoStartTestFailed = extras.getBoolean(AutostartDetector.AUTO_START_FAILURE);
            isUpdateRequired = extras.containsKey(FirebaseHelper.FLAG_UPDATE_REQUIRED);
            latestAppUrl = isUpdateRequired
                    ? extras.getString(FirebaseHelper.DATA_LATEST_APP_URL, null)
                    : null;
        }
        setContentView(R.layout.activity_permissions);
        illustration = findViewById(R.id.illustration);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        actionButton = findViewById(R.id.action_btn);
        skipButton = findViewById(R.id.skip_btn);
        actionButton.setOnClickListener(this);
        actionButton2 = findViewById(R.id.action_btn2);
        actionButton2.setOnClickListener(this);
        res = getResources();
        handleView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.handleView();
    }

    private boolean isIgnoringBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            return pm.isIgnoringBatteryOptimizations(getPackageName());
        }
        return true;
    }

    private void startPowerSaverIntent() {
        for (final Intent intent : POWERMANAGER_INTENTS) {
            if (isCallable(this, intent)) {
                new AlertDialog.Builder(this)
                        .setTitle(Build.MANUFACTURER + " Protected Apps")
                        .setMessage(String.format("%s requires to be enabled in 'Protected Apps' to log all your lead calls.%n", getString(R.string.app_name)))
                        .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                handleView();
                            }
                        })
                        .show();
                break;
            }
        }
    }

    public static boolean canOpenPowerManager(Context context) {
        for (final Intent intent : POWERMANAGER_INTENTS) {
            if (isCallable(context, intent)) return true;
        }
        return false;
    }

    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void handleView() {
        setPendingPermissions();
        String appName = res.getString(R.string.app_name);
        String agentsAppName = res.getString(R.string.agents_app_name);
        actionButton2.setVisibility(View.GONE);
        if(isUpdateRequired) {
            illustration.setImageResource(R.mipmap.settings);
            title.setText(R.string.update_title);
            description.setText(R.string.update_description);
            skipButton.setVisibility(View.GONE);
            actionButton.setText(R.string.update_btn_txt);
            actionButton.setTag(R.string.update_btn_txt);
        } else if ( !isIgnoringBatteryOptimizations() || pendingPermissions.size() > 0) {
            illustration.setImageResource(R.mipmap.settings);
            title.setText(R.string.permissions_title);
            description.setText(res.getString(R.string.permissions_description, appName, agentsAppName));
            skipButton.setVisibility(View.GONE);
            actionButton.setText(R.string.permissions_btn_text);
            actionButton.setTag(R.string.permissions_btn_text);
        } else if (canOpenPowerManager(this) && !alreadyEnabled) {
            illustration.setImageResource(R.mipmap.settings);
            title.setText(R.string.auto_start_title);
            description.setText(res.getString(R.string.auto_start_description, appName));
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alreadyEnabled = true;
                    handleView();
                }
            });
            skipButton.setText(R.string.already_enabled);
            skipButton.setVisibility(View.VISIBLE);
            actionButton.setText(R.string.auto_start_btn_text);
            actionButton.setTag(R.string.auto_start_btn_text);
        } else if (!hasAutoStartTestFailed && !skipTest) {
            illustration.setImageResource(R.mipmap.settings);
            title.setText(R.string.auto_start_test_title);
            description.setText(R.string.auto_start_test_description);
            actionButton.setText(R.string.auto_start_test_btn_text);
            actionButton.setTag(R.string.auto_start_test_btn_text);
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    skipTest = true;
                    handleView();
                }
            });
            skipButton.setText(R.string.auto_start_test_skip);
            skipButton.setVisibility(View.VISIBLE);
        } else {
            String successButtonText = res.getString(R.string.success_btn_text, agentsAppName);
            illustration.setImageResource(R.mipmap.success);
            if (hasAutoStartTestFailed) {
                title.setText(R.string.failure_title);
                description.setText(R.string.failure_description);
            } else {
                title.setText(R.string.success_title);
                description.setText(res.getString(R.string.success_description, agentsAppName));
            }
            skipButton.setVisibility(View.GONE);
            actionButton.setText(successButtonText);
            actionButton.setTag(R.string.success_btn_text);
//            TODO: uncomment when CP app live on prod
//            actionButton2.setVisibility(View.VISIBLE);
            actionButton2.setText(
                res.getString(R.string.success_btn_text, res.getString(R.string.cp_app_name))
            );
            actionButton2.setTag(R.id.action_btn2);
        }
    }

    private void handleAction() {
        int actionTag = (int) actionButton.getTag();
        if (actionTag == R.string.permissions_btn_text) {
            if (pendingPermissions.size() > 0) {
                requestPermissions();
            } else {
                requestIgnoreBatteryOptimization();
            }
        } else if (actionTag == R.string.update_btn_txt) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(latestAppUrl));
            startActivity(i);
        } else if (actionTag == R.string.auto_start_btn_text) {
            startPowerSaverIntent();
        } else if (actionTag == R.string.auto_start_test_btn_text) {
            AutostartDetector.testAutoStart(this);
        } else if (actionTag == R.string.success_btn_text) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(BuildConfig.AGENTS_APP_ID + BuildConfig.AGENTS_APP_ID_SUFFIX);
            if (intent == null) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.AGENTS_APP_ID));
            }
            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.action_btn) {
            this.handleAction();
        } else if (id == R.id.action_btn2) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(BuildConfig.CP_APP_ID + BuildConfig.CP_APP_ID_SUFFIX);
            if (intent == null) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.CP_APP_ID));
            }
            startActivity(intent);
        }
    }

    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /**
             * WARNING: This violates google play content policy by directly requesting user
             * to exempt this app from battery optimized apps.
             * https://developer.android.com/reference/android/provider/Settings#ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
             */
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            try {
                startActivity(intent);
            } catch (Exception e1) {
                /**
                 * Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS fails on Samsung devices running android v6.
                 * Redirect user to settings screen for battery optimization permissions.
                 * https://stackoverflow.com/a/50840603/1343488
                 */
                Bugsnag.notify(e1);
                try {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } catch (Exception e2) {
                    Bugsnag.notify(e2);
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                }
            }
        }
    }

    private void setPendingPermissions() {
        List<String> pending = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                pending.add(permission);
            }
        }
        pendingPermissions = pending;
    }

    private void requestPermissions() {
        if (pendingPermissions.size() > 0) {
            ActivityCompat.requestPermissions(this, pendingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                this.handleView();
                break;
        }
    }
}

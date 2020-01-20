package com.anarock.callmanager.ui.activity;

import androidx.annotation.CallSuper;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.anarock.callmanager.R;
import com.anarock.callmanager.util.PreferenceUtils;
import com.anarock.callmanager.util.ThemeUtils;

import static com.anarock.callmanager.util.ThemeUtils.ThemeType;

public abstract class AbsThemeActivity extends AppCompatActivity {

    private @StyleRes int mThemeStyle = -1;
    private @ThemeType int mThemeType;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        updateTheme();
    }

    protected void setThemeType(@ThemeType int type) {
        mThemeType = type;
        updateTheme();
    }

    protected void updateTheme() {
        String theme = PreferenceUtils.getInstance(this).getString(R.string.pref_app_theme_key);
        int newThemeStyle = ThemeUtils.themeFromId(theme, mThemeType);

        boolean isInOnCreate = mThemeStyle == -1;

        if (mThemeStyle != newThemeStyle) {
            mThemeStyle = newThemeStyle;
            setTheme(mThemeStyle);

            if (!isInOnCreate) {
                finish();
                startActivity(getIntent());
            }
        }
    }
}

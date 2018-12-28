package jmapps.questions200;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

public class MainApplication extends Application {

    private static final String nightMode = "night_mode";
    private boolean isNightModeEnabled = false;
    private static MainApplication singleton;

    private SharedPreferences.Editor mEditor;

    public static MainApplication getInstance() {
        if (singleton == null) {
            singleton = new MainApplication();
        }
        return singleton;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();

        singleton = this;

        this.isNightModeEnabled = mPreferences.getBoolean(nightMode, false);

        if (isNightModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;
        mEditor.putBoolean(nightMode, isNightModeEnabled).apply();
    }
}
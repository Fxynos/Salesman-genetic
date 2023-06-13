package com.vl.salesman;

import android.app.Application;
import android.app.UiModeManager;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
            getSystemService(UiModeManager.class).setNightMode(UiModeManager.MODE_NIGHT_NO);
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}

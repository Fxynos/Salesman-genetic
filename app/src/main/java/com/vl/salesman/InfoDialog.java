package com.vl.salesman;

import android.content.Context;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class InfoDialog {
    public static void show(Context context, String title, String message, boolean cancelable) {
        new MaterialAlertDialogBuilder(context)
                .setCancelable(cancelable)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ок", null)
                .show();
    }

    public static void show(Context context, String title, String message) {
        show(context, title, message, true);
    }
}

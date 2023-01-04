package com.example.myapplication.utils;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Process;
import android.os.UserHandle;
import android.widget.Toast;

public class MyDeviceAdmin extends DeviceAdminReceiver {

    static SharedPreferences getSamplePreferences(Context context) {
        return context.getSharedPreferences(
                DeviceAdminReceiver.class.getName(), 0);
    }

    static String PREF_PASSWORD_QUALITY = "password_quality";
    static String PREF_PASSWORD_LENGTH = "password_length";
    static String PREF_MAX_FAILED_PW = "max_failed_pw";
    static UserHandle userHandle = Process.myUserHandle();

    void showToast(Context context, CharSequence msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "Sample Device Admin: enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "This is an optional message to warn the user about disabling.";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "Sample Device Admin: disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, "Sample Device Admin: pw changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        showToast(context, "Sample Device Admin: pw failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        showToast(context, "Sample Device Admin: pw succeeded");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent, UserHandle userHandle) {
        showToast(context, "Sample Device Admin: pw changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent, UserHandle userHandle) {
        showToast(context, "Sample Device Admin: pw failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent, UserHandle userHandle) {
        showToast(context, "Sample Device Admin: pw succeeded");
    }
}

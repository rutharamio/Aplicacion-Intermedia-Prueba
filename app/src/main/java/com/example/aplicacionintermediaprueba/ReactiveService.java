package com.example.aplicacionintermediaprueba;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ReactivateService extends BroadcastReceiver {
    private static final String TAG = "ReactivateService";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Receiver Started");
        Intent serviceIntent = new Intent(context, SensorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}

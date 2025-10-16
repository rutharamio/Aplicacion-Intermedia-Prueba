package com.example.aplicacionintermediaprueba;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;

import java.util.List;

public class SensorService extends Service {
    private static final String TAG = "SensorService";
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startMyOwnForeground();
        else startForeground(1, new Notification());

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mShakeDetector = new ShakeDetector();
            mShakeDetector.setOnShakeListener(count -> {
                if (count == 3) {
                    vibrate();
                    fetchLocationAndSendSms();
                }
            });
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.e(TAG, "SensorManager null");
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLocationAndSendSms() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, new CancellationToken() {
                    @Override public boolean isCancellationRequested() { return false; }
                    @NonNull
                    @Override public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) { return null; }
                })
                .addOnSuccessListener(location -> {
                    sendSmsToAll(location);
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "getCurrentLocation onFailure: " + e);
                    sendSmsToAll(null);
                });
    }

    private void sendSmsToAll(Location location) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            DbHelper db = new DbHelper(this);
            List<ContactModel> list = db.getAllContacts();

            if (list == null || list.isEmpty()) {
                Log.i(TAG, "No contacts to send SMS to.");
                return;
            }

            for (ContactModel c : list) {
                String message;
                if (location != null) {
                    message = "Hey " + c.getName() + ", I'm in danger. My location: http://maps.google.com/maps?q="
                            + location.getLatitude() + "," + location.getLongitude();
                } else {
                    message = "I am in DANGER, I need help. GPS unavailable.";
                }
                smsManager.sendTextMessage(c.getPhoneNo(), null, message, null, null);
            }
            Log.i(TAG, "SMS sent to contacts");
        } catch (Exception e) {
            Log.e(TAG, "Error sending SMS: " + e);
        }
    }

    public void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);
            vibrator.cancel();
            vibrator.vibrate(effect);
        } else {
            vibrator.vibrate(500);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String channelId = "sos_background_channel";
        String channelName = "SOS Background Service";
        NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) manager.createNotificationChannel(chan);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("SOS service active")
                .setContentText("Listening for shake to send emergency messages")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN);

        startForeground(2, builder.build());
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        if (mSensorManager != null && mShakeDetector != null) {
            mSensorManager.unregisterListener(mShakeDetector);
        }
        // restart via broadcast
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ReactivateService.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }
}

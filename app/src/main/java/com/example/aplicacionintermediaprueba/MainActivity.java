package com.example.aplicacionintermediaprueba;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // permisos requeridos (ajusta según targetSdk)
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private void ensurePermissionsAndStartService() {
        List<String> perms = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.SEND_SMS);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.READ_CONTACTS);
        // For Android Q+ request ACCESS_BACKGROUND_LOCATION if you want location while app in background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startSensorServiceIfNotRunning();
        }
    }

    private void startSensorServiceIfNotRunning() {
        Intent intent = new Intent(this, SensorService.class);
        if (!isMyServiceRunning(SensorService.class)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
        }
    }

    @OverrideI
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            // Podrías chequear cada permiso y mostrar diálogo si fue denegado; simplificamos:
            startSensorServiceIfNotRunning();
        }
    }
}I
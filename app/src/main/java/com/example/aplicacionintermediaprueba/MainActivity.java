package com.example.aplicacionintermediaprueba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private DbHelper db;
    private CustomAdapter customAdapter;

    // Código de solicitud de permisos
    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la lista de contactos
        listView = findViewById(R.id.contact_list);
        db = new DbHelper(this);

        List<ContactModel> contacts = db.getAllContacts();
        customAdapter = new CustomAdapter(this, contacts);
        listView.setAdapter(customAdapter);

        // Botón para agregar contacto (por ahora sin funcionalidad)
        findViewById(R.id.add_contact_button).setOnClickListener(v -> {
            // Ejemplo: mostrar mensaje temporal
            // Toast.makeText(this, "Agregar contacto", Toast.LENGTH_SHORT).show();
        });

        // Verifica permisos y activa el servicio del sensor
        ensurePermissionsAndStartService();
    }

    // Método para verificar permisos y comenzar el servicio de sensores
    private void ensurePermissionsAndStartService() {
        List<String> perms = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.SEND_SMS);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.READ_CONTACTS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.POST_NOTIFICATIONS);

        // Android 10+ requiere permiso adicional para ubicación en segundo plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        // Solicita permisos si alguno falta
        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startSensorServiceIfNotRunning();
        }
    }

    // Inicia el servicio del sensor si no está corriendo
    private void startSensorServiceIfNotRunning() {
        Intent intent = new Intent(this, SensorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
    }

    // Resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            startSensorServiceIfNotRunning();
        }
    }
}

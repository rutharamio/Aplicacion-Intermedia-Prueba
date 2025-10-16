package com.example.aplicacionintermediaprueba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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

        // ✅ Botón para agregar contacto con formulario
        findViewById(R.id.add_contact_button).setOnClickListener(v -> showAddContactDialog());

        // Verifica permisos y activa el servicio del sensor
        ensurePermissionsAndStartService();
    }

    // 🟢 Muestra un cuadro de diálogo para agregar un nuevo contacto
    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Agregar Contacto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (!name.isEmpty() && !phone.isEmpty()) {
                        ContactModel contact = new ContactModel(0, name, phone);
                        db.addcontact(contact);
                        Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();

                        // Refresca la lista
                        List<ContactModel> updatedContacts = db.getAllContacts();
                        customAdapter.refresh(updatedContacts);
                    } else {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🟣 Verifica permisos y comienza el servicio del sensor
    private void ensurePermissionsAndStartService() {
        List<String> perms = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.SEND_SMS);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.READ_CONTACTS);

        // Android 10+ requiere permiso para ubicación en segundo plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        // Si faltan permisos, se solicitan
        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startSensorServiceIfNotRunning();
        }
    }

    // 🔵 Inicia el servicio del sensor si no está corriendo
    private void startSensorServiceIfNotRunning() {
        Intent intent = new Intent(this, SensorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
    }

    // 🟢 Resultado de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            startSensorServiceIfNotRunning();
        }
    }
}

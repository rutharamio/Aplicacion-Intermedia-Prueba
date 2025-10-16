package com.example.aplicacionintermediaprueba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvEmpty;
    private DbHelper db;
    private CustomAdapter adapter;

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    // 🔹 Lanzadores modernos para permisos y selección de contacto
    private final ActivityResultLauncher<String> contactsPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openContactPicker();
                else Toast.makeText(this, "Se necesita permiso para leer contactos.", Toast.LENGTH_SHORT).show();
            });

    private final ActivityResultLauncher<Intent> pickContactLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handlePickedContact(result.getData().getData());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.contact_list);
        tvEmpty = findViewById(R.id.tvEmpty);
        db = new DbHelper(this);
        adapter = new CustomAdapter(this, db.getAllContacts());
        listView.setAdapter(adapter);
        updateEmptyState();

        // 🔹 Botón principal para agregar contacto
        findViewById(R.id.add_contact_button).setOnClickListener(v -> showAddContactOptions());

        // 🔹 Permisos y servicio SOS
        ensurePermissionsAndStartService();
    }

    // 🟢 Mostrar opciones de agregar contacto
    private void showAddContactOptions() {
        String[] options = {"Agregar manualmente", "Seleccionar desde contactos"};
        new AlertDialog.Builder(this)
                .setTitle("Agregar contacto")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) showAddContactDialog();  // Manual
                    else requestOrPickContact();             // Desde la agenda
                })
                .show();
    }

    // 🟣 Agregar contacto manualmente
    private void showAddContactDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_contact, null);
        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etPhone = dialogView.findViewById(R.id.etPhone);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo contacto")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String phone = etPhone.getText().toString().trim();

                    if (name.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (db.count() >= 5) {
                        Toast.makeText(this, "Máximo 5 contactos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    db.addcontact(new ContactModel(0, name, phone));
                    adapter.refresh(db.getAllContacts());
                    updateEmptyState();
                    Toast.makeText(this, "Contacto agregado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // 🟣 Pedir permiso o abrir selector de contacto
    private void requestOrPickContact() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            openContactPicker();
        } else {
            contactsPermission.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactLauncher.launch(intent);
    }

    // 🟢 Manejar contacto seleccionado del teléfono
    private void handlePickedContact(Uri contactUri) {
        if (contactUri == null) return;
        Cursor c = getContentResolver().query(contactUri, null, null, null, null);
        if (c == null) return;

        try {
            if (c.moveToFirst()) {
                String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                String hasPhone = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                String phone = null;

                if ("1".equals(hasPhone)) {
                    Cursor phones = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                            new String[]{id},
                            null
                    );
                    if (phones != null) {
                        if (phones.moveToFirst()) phone = phones.getString(0);
                        phones.close();
                    }
                }

                if (phone == null || phone.trim().isEmpty()) {
                    Toast.makeText(this, "El contacto no tiene número", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (db.count() >= 5) {
                    Toast.makeText(this, "Máximo 5 contactos", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.addcontact(new ContactModel(0, name, phone));
                adapter.refresh(db.getAllContacts());
                updateEmptyState();
                Toast.makeText(this, "Contacto agregado desde agenda", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            c.close();
        }
    }

    // 🔵 Mostrar u ocultar texto de lista vacía
    private void updateEmptyState() {
        if (tvEmpty == null) return;
        boolean empty = adapter.getCount() == 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        if (!perms.isEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toArray(new String[0]), PERMISSIONS_REQUEST_CODE);
        } else {
            startSensorServiceIfNotRunning();
        }
    }

    // 🟢 Inicia el servicio de detección SOS
    private void startSensorServiceIfNotRunning() {
        Intent intent = new Intent(this, SensorService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            startSensorServiceIfNotRunning();
        }
    }
}

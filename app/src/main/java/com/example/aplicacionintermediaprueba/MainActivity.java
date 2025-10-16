package com.example.aplicacionintermediaprueba;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private TextView tvEmpty;
    private DbHelper db;
    private CustomAdapter adapter;

    // Solicitud de permiso READ_CONTACTS
    private final ActivityResultLauncher<String> contactsPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openContactPicker();
                else Toast.makeText(this, "Se necesita permiso para leer contactos.", Toast.LENGTH_SHORT).show();
            });

    // Picker de contactos del teléfono
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
        tvEmpty  = findViewById(R.id.tvEmpty);
        Button addBtn = findViewById(R.id.add_contact_button);

        db = new DbHelper(this);
        adapter = new CustomAdapter(this, db.getAllContacts());
        listView.setAdapter(adapter);
        updateEmptyState();

        addBtn.setOnClickListener(v -> {
            if (db.count() >= 5) {
                Toast.makeText(this, "Máximo 5 contactos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED) {
                openContactPicker();
            } else {
                contactsPermission.launch(Manifest.permission.READ_CONTACTS);
            }
        });
    }

    private void openContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        pickContactLauncher.launch(intent);
    }

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

                db.addcontact(new ContactModel(0, name, phone));
                adapter.refresh(db.getAllContacts());
                updateEmptyState();
                Toast.makeText(this, "Contacto agregado", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            c.close();
        }
    }

    private void updateEmptyState() {
        if (tvEmpty == null) return;
        boolean empty = adapter.getCount() == 0;
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }
}

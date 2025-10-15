package com.example.aplicacionintermediaprueba;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private DbHelper db;
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.contact_list);
        db = new DbHelper(this);

        List<ContactModel> contacts = db.getAllContacts();
        customAdapter = new CustomAdapter(this, contacts);
        listView.setAdapter(customAdapter);

        findViewById(R.id.add_contact_button).setOnClickListener(v -> {
            // Implement functionality to add contacts
            // For example, open contact picker or input fields
        });
    }
}

package com.example.aplicacionintermediaprueba;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<ContactModel> {

    private final Context context;
    private final List<ContactModel> contacts;

    public CustomAdapter(Context context, List<ContactModel> contacts) {
        super(context, 0, contacts);
        this.context = context;
        this.contacts = contacts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflar solo si es necesario
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        }

        // Obtener el contacto actual
        ContactModel contact = getItem(position);

        // Referencias a los elementos del layout
        LinearLayout layout = convertView.findViewById(R.id.linear);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvPhone = convertView.findViewById(R.id.tvPhone);

        // Cargar datos
        tvName.setText(contact.getName());
        tvPhone.setText(contact.getPhoneNo());

        // 🟣 Acción al mantener presionado (eliminar contacto)
        layout.setOnLongClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar contacto")
                    .setMessage("¿Deseas eliminar a " + contact.getName() + "?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        DbHelper db = new DbHelper(context);
                        db.deleteContact(contact);
                        contacts.remove(contact);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        return convertView;
    }

    // Refresca la lista cuando hay cambios
    public void refresh(List<ContactModel> updatedContacts) {
        contacts.clear();
        contacts.addAll(updatedContacts);
        notifyDataSetChanged();
    }
}

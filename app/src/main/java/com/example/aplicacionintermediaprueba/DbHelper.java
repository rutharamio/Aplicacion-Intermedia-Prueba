package com.example.aplicacionintermediaprueba;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {

    // Versión y nombre de base de datos
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "contactdata";

    // Nombre de la tabla y columnas
    private static final String TABLE_NAME = "contacts";
    private static final String KEY_ID = "id";
    private static final String NAME = "Name";
    private static final String PHONENO = "PhoneNo";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de contactos
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NAME + " TEXT,"
                + PHONENO + " TEXT"
                + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar la tabla anterior si existe y recrearla
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // 🟢 Agregar nuevo contacto
    public void addcontact(ContactModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues c = new ContentValues();
        c.put(NAME, contact.getName());
        c.put(PHONENO, contact.getPhoneNo());
        db.insert(TABLE_NAME, null, c);
        db.close();
    }

    // 🟢 Obtener todos los contactos
    public List<ContactModel> getAllContacts() {
        List<ContactModel> list = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);

        if (c.moveToFirst()) {
            do {
                list.add(new ContactModel(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2)
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    // 🟢 Contar cantidad de contactos
    public int count() {
        int count = 0;
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        db.close();
        return count;
    }

    // 🟢 Eliminar contacto específico
    public void deleteContact(ContactModel contact) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
    }
}

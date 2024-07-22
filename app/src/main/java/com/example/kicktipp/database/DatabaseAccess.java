package com.example.kicktipp.database;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAccess {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;
    private static DatabaseAccess instance;

    // Private Konstruktor, um die Singleton-Instanz zu gewährleisten
    private DatabaseAccess(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    // Methode, um eine Singleton-Instanz von DatabaseAccess zu erhalten
    public static synchronized DatabaseAccess getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseAccess(context.getApplicationContext());
        }
        return instance;
    }

    // Öffne die Datenbankverbindung
    public void open() {
        this.database = dbHelper.getWritableDatabase();
    }

    // Schließe die Datenbankverbindung
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }


}

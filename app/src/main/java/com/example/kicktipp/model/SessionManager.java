package com.example.kicktipp.model;

import android.content.Context;

import com.example.kicktipp.database.DatabaseHelper;

public class SessionManager {
    private static String loggedInUsername;
    private static String userLeague;
    private static DatabaseHelper databaseHelper;


    public static void initDatabaseHelper(Context context) {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context);
        }
    }


    public SessionManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static void setLoggedInUsername(String username) {
        loggedInUsername = username;
    }

    public static String getLoggedInUsername() {
        return loggedInUsername;
    }

    public static void setUserLeague(String league) {
        if (isLoggedIn()) {
            userLeague = league;
        }
    }

    public static String getUserLeague() {
        return userLeague;
    }

    public static boolean isLoggedIn() {
        String loggedInUsername = getLoggedInUsername();
        return loggedInUsername != null && !loggedInUsername.isEmpty();
    }

    public static boolean isCurrentUserFounder() {
        String loggedInUsername = getLoggedInUsername();
        if (isLoggedIn()) {
            // Hier verwenden Sie den Zugriff auf Ihre Datenbank, um den "founder"-Wert für den aktuellen Benutzer abzurufen
            // Nehmen Sie an, dass Ihre Datenbank eine Methode "getUserFounder" hat, die den Gründerstatus für einen Benutzer überprüft
            Integer founder = databaseHelper.getUserFounder(loggedInUsername);
            return founder != null && founder == 1;
        } else {
            return false; // Wenn kein Benutzer angemeldet ist, ist er kein Gründer
        }
    }

    public static int getUserId() {
        if (isLoggedIn()) {
            // Hier verwenden Sie den Zugriff auf Ihre Datenbank, um die Benutzer-ID für den aktuellen Benutzer abzurufen
            // Nehmen Sie an, dass Ihre Datenbank eine Methode "getUserId" hat, die die Benutzer-ID für einen Benutzernamen abruft
            return databaseHelper.getUserId(loggedInUsername);
        } else {
            return -1; // Rückgabe eines ungültigen Werts, wenn kein Benutzer angemeldet ist
        }
    }

}

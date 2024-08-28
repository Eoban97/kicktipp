package com.example.kicktipp.model;

import android.content.Context;

import com.example.kicktipp.database.DatabaseHelper;

public class SessionManager {
    private static String loggedInUsername;
    private static String userLeague;
    private static int userLeagueId; // Variable für die Liga-ID
    private static DatabaseHelper databaseHelper;

    // Initialisierung des DatabaseHelper
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
        if (loggedInUsername != null) {
            userLeague = databaseHelper.getUserLeague(username);

            userLeagueId = databaseHelper.getLeagueIdByUsername(username);
        } else {
            userLeague = null;
            userLeagueId = -1;
        }
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

    public static void setUserLeagueId(int leagueId) {
        userLeagueId = leagueId;
    }

    public static int getUserLeagueId() {
        return userLeagueId;
    }

    public static boolean isLoggedIn() {
        return loggedInUsername != null && !loggedInUsername.isEmpty();
    }

    public static boolean isCurrentUserFounder() {
        if (isLoggedIn()) {
            Integer founder = databaseHelper.getUserFounder(loggedInUsername);
            return founder != null && founder == 1;
        } else {
            return false;
        }
    }

    public static int getUserId() {
        if (isLoggedIn()) {
            return databaseHelper.getUserId(loggedInUsername);
        } else {
            return -1;
        }
    }
}

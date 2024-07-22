package com.example.kicktipp.database;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.kicktipp.model.Game;
import com.example.kicktipp.model.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Name und Version der Datenbank
    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 9;
    static final String DATABASE_TABLE="user_expected_results";

    // Konstruktor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Diese Methode wird aufgerufen, wenn die Datenbank zum ersten Mal erstellt wird
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL-Anweisung zum Erstellen einer Tabelle
        String createTableUsers = "CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT, founder INTEGER DEFAULT 0, league TEXT)";

        db.execSQL(createTableUsers);

        String createTableGames ="CREATE TABLE games (id Integer PRIMARY KEY AUTOINCREMENT, gameday Integer, team1 TEXT, team2 TEXT, goalsTeam1 INTEGER, goalsTeam2 Integer, result TEXT)";
        db.execSQL(createTableGames);


        String createTablePoints = "CREATE TABLE points ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "userId INTEGER,"
                + "username TEXT,"
                + "score INTEGER,"
                + "league TEXT,"
                + "FOREIGN KEY (userId) REFERENCES users(id)"
                + ")";

        db.execSQL(createTablePoints);




        String createUserExpectedResults = "CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + "( id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, gameId INTEGER, expectedGoalsTeam1 INTEGER, expectedGoalsTeam2 INTEGER, FOREIGN KEY (userId) REFERENCES users(id), FOREIGN KEY (gameId) REFERENCES games(id))";

        db.execSQL(createUserExpectedResults);
        db.execSQL("ALTER TABLE user_expected_results ADD COLUMN expectedResult TEXT;");



    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gib eine Meldung aus, um anzuzeigen, dass die Datenbankversion aktualisiert wird
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Lösche die vorhandenen Tabellen und rufe onCreate auf, um die Tabellen gemäß dem aktuellen Schema zu erstellen
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS games");
        db.execSQL("DROP TABLE IF EXISTS points");
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
        onCreate(db);
    }


    public Boolean insertGames(Integer gameday, String team1, String team2) {
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("gameday", gameday);
        contentValues.put("team1", team1);
        contentValues.put("team2", team2);

        long result = MyDB.insert("games", null, contentValues);
        return result != -1; //result ungleich -1 bedeuted erfolgreiche Einfügung
    }



    // Diese Methode wird aufgerufen, wenn die Datenbank aktualisiert werden muss



    public boolean validateCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "username = ? AND password = ?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query("users", null, selection, selectionArgs, null, null, null);
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    public boolean registerUser(String username, String password, String founder, String league) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("founder", founder);
        values.put("league", league);
        long result = db.insert("users", null, values);
        return result != -1;
    }

    public void addLeagueToUser(String username, String league) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("league", league);
        String selection = "username = ?";
        String[] selectionArgs = {username};
        db.update("users", values, selection, selectionArgs);
    }

    public List<String> getAllLeagues() {
        List<String> leagues = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(true, "users", new String[]{"league"}, null, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int leagueIndex = cursor.getColumnIndex("league");
                if (leagueIndex != -1) {
                    String league = cursor.getString(leagueIndex);
                    leagues.add(league);
                }
            }
            cursor.close();
        }
        return leagues;
    }

    public String getUserLeague(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "username = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query("users", new String[]{"league"}, selection, selectionArgs, null, null, null);
        String userLeague = "";
        if (cursor != null && cursor.moveToFirst()) {
            int leagueIndex = cursor.getColumnIndex("league");
            if (leagueIndex != -1) {
                userLeague = cursor.getString(leagueIndex);
            }
            cursor.close();
        }
        return userLeague;
    }


    public String getUserUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "username = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query("users", new String[]{"username"}, selection, selectionArgs, null, null, null);
        String userUsername = "";
        if (cursor != null && cursor.moveToFirst()) {
            int usernameIndex = cursor.getColumnIndex("username");
            if (usernameIndex != -1) {
                userUsername = cursor.getString(usernameIndex);
            }
            cursor.close();
        }
        return userUsername;
    }

    public String getCurrentMatchday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentMatchday = "";

        // Erstellen Sie eine Abfrage, um den ersten Spieltag mit Nullwerten für Tore und Ergebnisse zu erhalten
        String query = "SELECT gameday FROM games WHERE goalsTeam1 IS NULL AND goalsTeam2 IS NULL AND result IS NULL LIMIT 1";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            int matchdayIndex = cursor.getColumnIndex("gameday");
            if (matchdayIndex != -1) {
                currentMatchday = cursor.getString(matchdayIndex);
            }
            cursor.close();
        }
        return currentMatchday;
    }

    public String getNewestUserUsername() {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = "";

        // Hier wird nach dem Benutzer mit der höchsten ID gesucht, was dem neuesten Benutzer entspricht
        Cursor cursor = db.query("users", new String[]{"username"}, null, null, null, null, "id DESC", "1");
        if (cursor != null && cursor.moveToFirst()) {
            int usernameIndex = cursor.getColumnIndex("username");
            if (usernameIndex != -1) {
                username = cursor.getString(usernameIndex);
            }
            cursor.close();
        }
        return username;
    }

    public  Integer getUserFounder(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "username = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query("users", new String[]{"founder"}, selection, selectionArgs, null, null, null);
        Integer userFounder=0;
        if (cursor != null && cursor.moveToFirst()) {
            int founderIndex = cursor.getColumnIndex("founder");
            if (founderIndex != -1) {
                userFounder = cursor.getInt(founderIndex);
            }
            cursor.close();
        }
        return userFounder;
    }

    public void setUserFounder(String username, int founderValue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("founder", founderValue);
        String selection = "username = ?";
        String[] selectionArgs = {username};
        db.update("users", values, selection, selectionArgs);
    }

    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("users", null, null); // Löscht alle Daten in der Tabelle "users"
        db.delete("games", null, null); // Löscht alle Daten in der Tabelle "games"
        db.delete("points", null, null); // Löscht alle Daten in der Tabelle "points"
        // Fügen Sie weitere Tabellen hinzu, falls erforderlich

        // Optional: Setzen Sie den Autoincrement-Wert auf 1 zurück, um mit der ID von 1 zu beginnen
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'users'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'games'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'points'");
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'user_expected_results'");
        // Fügen Sie weitere Tabellen hinzu, falls erforderlich

        db.close();
    }

    public void insertInitialGames() {
        // Öffne die Datenbank im Schreibmodus
        SQLiteDatabase db = this.getWritableDatabase();

        // Erstelle ein Array von Spielen mit den angegebenen Datensätzen
        String[][] games = {
                {"1", "Schalke 04", "1. FC Nürnberg"},
                {"1", "Bayern München", "Borussia Dortmund"},
                {"2", "Borussia Dortmund", "Schalke 04"},
                {"2", "1.FC Nürnberg", "Bayern München"}
        };

        // Durchlaufe das Array und füge jedes Spiel einzeln ein
        for (String[] game : games) {
            ContentValues values = new ContentValues();
            values.put("gameday", Integer.parseInt(game[0])); // Konvertiere den Spieltag in Integer
            values.put("team1", game[1]);
            values.put("team2", game[2]);
            db.insert("games", null, values);
        }

        // Schließe die Datenbankverbindung
        db.close();
    }

    public String getCurrentGameday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String currentGameday = "";

        // Erstellen Sie eine Abfrage, um die ersten beiden Datensätze zu erhalten,
        // bei denen die Spalten goalsTeam1, goalsTeam2 und result den Wert null haben
        String query = "SELECT DISTINCT gameday FROM games " +
                "WHERE goalsTeam1 IS NULL AND goalsTeam2 IS NULL AND result IS NULL " +
                "LIMIT 2";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null && cursor.moveToFirst()) {
            int gamedayIndex = cursor.getColumnIndex("gameday");
            if (gamedayIndex != -1) {
                currentGameday = cursor.getString(gamedayIndex);
            }
            cursor.close();
        }
        return currentGameday;
    }

    public List<Game> getCurrentMatchdayGames() {
        List<Game> games = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Erstellen Sie eine Abfrage, um den aktuellen Spieltag und seine Spiele abzurufen
        String query = "SELECT id, gameday, team1, team2 FROM games WHERE goalsTeam1 IS NULL AND goalsTeam2 IS NULL AND result IS NULL LIMIT 2";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("id");
                int gamedayIndex = cursor.getColumnIndex("gameday");
                int team1Index = cursor.getColumnIndex("team1");
                int team2Index = cursor.getColumnIndex("team2");
                if (idIndex != -1 && gamedayIndex != -1 && team1Index != -1 && team2Index != -1) {
                    int id = cursor.getInt(idIndex);
                    int gameday = cursor.getInt(gamedayIndex);
                    String team1 = cursor.getString(team1Index);
                    String team2 = cursor.getString(team2Index);
                    games.add(new Game(id, gameday, team1, team2));
                }
            }
            cursor.close();
        }
        return games;
    }


    public void updateGameResults(int gameId, int goalsTeam1, int goalsTeam2) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("goalsTeam1", goalsTeam1);
        values.put("goalsTeam2", goalsTeam2);
        db.update("games", values, "id = ?", new String[]{String.valueOf(gameId)});
        db.close();


    }

    public void chooseResult(SQLiteDatabase db, int gameId) {


        // Abfrage, um die Tore von Team 1 und Team 2 zu erhalten
        String query = "SELECT goalsTeam1, goalsTeam2 FROM games WHERE id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gameId)});
        if (cursor != null && cursor.moveToFirst()) {
            int goalsTeam1Index = cursor.getColumnIndex("goalsTeam1");
            int goalsTeam2Index = cursor.getColumnIndex("goalsTeam2");
            if (goalsTeam1Index != -1 && goalsTeam2Index != -1) {
                int goalsTeam1 = cursor.getInt(goalsTeam1Index);
                int goalsTeam2 = cursor.getInt(goalsTeam2Index);

                String result;
                if (goalsTeam1 > goalsTeam2) {
                    result = "Team1";
                } else if (goalsTeam2 > goalsTeam1) {
                    result = "Team2";
                } else {
                    result = "draw";
                }

                // Update der result Spalte in der Datenbank
                ContentValues values = new ContentValues();
                values.put("result", result);
                db.update("games", values, "id = ?", new String[]{String.valueOf(gameId)});
            }
            cursor.close();
        }

    }

    public void chooseExpectedResult(SQLiteDatabase db, int gameId, int userId) {
        // Abfrage, um das erwartete Ergebnis des spezifischen Benutzers für das angegebene Spiel abzurufen
        String query = "SELECT expectedGoalsTeam1, expectedGoalsTeam2 FROM user_expected_results WHERE gameId = ? AND userId = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gameId), String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            int expectedGoalsTeam1Index = cursor.getColumnIndex("expectedGoalsTeam1");
            int expectedGoalsTeam2Index = cursor.getColumnIndex("expectedGoalsTeam2");
            if (expectedGoalsTeam1Index != -1 && expectedGoalsTeam2Index != -1) {
                int expectedGoalsTeam1 = cursor.getInt(expectedGoalsTeam1Index);
                int expectedGoalsTeam2 = cursor.getInt(expectedGoalsTeam2Index);

                // Bestimme das erwartete Ergebnis basierend auf den Toren von Team 1 und Team 2
                String expectedResult;
                if (expectedGoalsTeam1 > expectedGoalsTeam2) {
                    expectedResult = "Team1";
                } else if (expectedGoalsTeam2 > expectedGoalsTeam1) {
                    expectedResult = "Team2";
                } else {
                    expectedResult = "draw";
                }

                // Update des erwarteten Ergebnisses in der Tabelle user_expected_results für das angegebene Spiel und den angegebenen Benutzer
                ContentValues values = new ContentValues();
                values.put("expectedResult", expectedResult);
                db.update("user_expected_results", values, "gameId = ? AND userId = ?", new String[]{String.valueOf(gameId), String.valueOf(userId)});
            }
            cursor.close();
        }
    }



    @SuppressLint("Range")
    public void updatePoints(SQLiteDatabase db, int gameId, String username) {


        // Abfrage, um das Ergebnis des Spiels abzurufen
        String gameQuery = "SELECT goalsTeam1, goalsTeam2 FROM games WHERE id = ?";
        Cursor gameCursor = db.rawQuery(gameQuery, new String[]{String.valueOf(gameId)});
        if (gameCursor != null && gameCursor.moveToFirst()) {
            int goalsTeam1 = gameCursor.getInt(gameCursor.getColumnIndex("goalsTeam1"));
            int goalsTeam2 = gameCursor.getInt(gameCursor.getColumnIndex("goalsTeam2"));

            // Überprüfen, ob das Ergebnis des Spiels vorhanden ist
            if (goalsTeam1 >= 0 && goalsTeam2 >= 0) {
                // Abfrage, um alle Benutzer in derselben Liga zu erhalten
                String usersQuery = "SELECT username FROM points WHERE league = ?";
                Cursor usersCursor = db.rawQuery(usersQuery, new String[]{SessionManager.getUserLeague()});
                if (usersCursor != null && usersCursor.moveToFirst()) {
                    do {
                        username = usersCursor.getString(usersCursor.getColumnIndex("username"));

                        // Abfrage, um das erwartete Ergebnis des Benutzers abzurufen
                        String query = "SELECT expectedGoalsTeam1, expectedGoalsTeam2 FROM user_expected_results WHERE gameId = ? AND userId = ?";
                        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(gameId), String.valueOf(getUserId(username))});
                        if (cursor != null && cursor.moveToFirst()) {
                            int expectedGoalsTeam1Index = cursor.getColumnIndex("expectedGoalsTeam1");
                            int expectedGoalsTeam2Index = cursor.getColumnIndex("expectedGoalsTeam2");
                            if (expectedGoalsTeam1Index != -1 && expectedGoalsTeam2Index != -1) {
                                int expectedGoalsTeam1 = cursor.getInt(expectedGoalsTeam1Index);
                                int expectedGoalsTeam2 = cursor.getInt(expectedGoalsTeam2Index);

                                int scoreToAdd = 0; // Punkte, die dem Benutzer hinzugefügt werden

                                // Überprüfen, ob das Ergebnis des Benutzers und das tatsächliche Ergebnis übereinstimmen
                                if ((goalsTeam1 > goalsTeam2 && expectedGoalsTeam1 > expectedGoalsTeam2) ||
                                        (goalsTeam1 < goalsTeam2 && expectedGoalsTeam1 < expectedGoalsTeam2) ||
                                        (goalsTeam1 == goalsTeam2 && expectedGoalsTeam1 == expectedGoalsTeam2)) {
                                    // Das Ergebnis stimmt mit dem erwarteten Ergebnis überein
                                    scoreToAdd += 3; // Füge 3 Punkte für das korrekte Ergebnis hinzu
                                } else if (goalsTeam1 == goalsTeam2 && expectedGoalsTeam1 == expectedGoalsTeam2) {
                                    // Das Spiel endete unentschieden
                                    scoreToAdd += 2; // Füge 2 Punkte für das korrekte Unentschieden hinzu
                                }

                                // Punkte in der Tabelle "points" für den entsprechenden Benutzer aktualisieren
                                ContentValues values = new ContentValues();
                                values.put("userId", getUserId(username));
                                values.put("username", username);
                                values.put("league", getUserLeague(username));
                                values.put("score", getPointsForUser(username) + scoreToAdd);
                                db.insertWithOnConflict("points", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                            }
                            cursor.close();
                        }
                    } while (usersCursor.moveToNext());
                    usersCursor.close();
                }
            }
        }
        if (gameCursor != null) {
            gameCursor.close();
        }

    }

    public int getPointsForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Abfrage, um die Punkte des Benutzers abzurufen
        String query = "SELECT score FROM points WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int points = 0;

        if (cursor != null && cursor.moveToFirst()) {
            int scoreIndex = cursor.getColumnIndex("score");
            if (scoreIndex != -1) {
                points = cursor.getInt(scoreIndex);
            }
            cursor.close();
        }
        db.close();

        return points;
    }



    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = "username = ?";
        String[] selectionArgs = {username};
        Cursor cursor = db.query("users", new String[]{"id"}, selection, selectionArgs, null, null, null);
        int userId = -1; // Defaultwert für den Fall, dass der Benutzer nicht gefunden wird
        if (cursor != null && cursor.moveToFirst()) {
            int userIdIndex = cursor.getColumnIndex("id");
            if (userIdIndex != -1) {
                userId = cursor.getInt(userIdIndex);
            }
            cursor.close();
        }
        return userId;
    }

    // In der DatabaseHelper-Klasse

    public void initializePointsTableForUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("userId", getUserId(username)); // Die Benutzer-ID aus der Benutzertabelle abrufen
        values.put("username", username);
        values.put("score", 0); // Startwert von 0 für den Benutzer
        values.put("league", SessionManager.getUserLeague());
        db.insert("points", null, values);
        db.close();
    }



}

package com.example.kicktipp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.kicktipp.model.Game;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 22;
    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.enableWriteAheadLogging();  // Aktiviert den WAL-Modus
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableUsers = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "password TEXT, " +
                "founder INTEGER DEFAULT 0, " +
                "leagueId INTEGER, " +
                "FOREIGN KEY (leagueId) REFERENCES leagues(id))";
        db.execSQL(createTableUsers);

        String createTableGames = "CREATE TABLE games (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "leagueId INTEGER, " +
                "gameday INTEGER, " +
                "team1 TEXT, " +
                "team2 TEXT, " +
                "goalsTeam1 INTEGER, " +
                "goalsTeam2 INTEGER, " +
                "result TEXT, " +
                "FOREIGN KEY (leagueId) REFERENCES leagues(id))";
        db.execSQL(createTableGames);

        String createTablePoints = "CREATE TABLE points (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "username TEXT, " +
                "score INTEGER, " +
                "leagueId INTEGER, " +
                "FOREIGN KEY (userId) REFERENCES users(id), " +
                "FOREIGN KEY (leagueId) REFERENCES leagues(id))";
        db.execSQL(createTablePoints);

        String createTableLeagues = "CREATE TABLE IF NOT EXISTS leagues (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "leagueName TEXT)";
        db.execSQL(createTableLeagues);

        String createUserExpectedResults = "CREATE TABLE IF NOT EXISTS user_expected_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "userId INTEGER, " +
                "gameId INTEGER, " +
                "expectedGoalsTeam1 INTEGER, " +
                "expectedGoalsTeam2 INTEGER, " +
                "expectedResult TEXT, " +
                "FOREIGN KEY (userId) REFERENCES users(id), " +
                "FOREIGN KEY (gameId) REFERENCES games(id))";
        db.execSQL(createUserExpectedResults);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS games");
        db.execSQL("DROP TABLE IF EXISTS points");
        db.execSQL("DROP TABLE IF EXISTS leagues");
        db.execSQL("DROP TABLE IF EXISTS user_expected_results");
        onCreate(db);
    }

    // Methode zur Überprüfung der Anmeldeinformationen
    public boolean validateCredentials(String username, String password) {
        boolean isValid = false;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query("users", null, "username = ? AND password = ?",
                     new String[]{username, password}, null, null, null)) {
            isValid = cursor.moveToFirst();
        } catch (Exception e) {
            Log.e(TAG, "Error validating credentials: " + e.getMessage());
        }
        return isValid;
    }

    // Methode zur Registrierung eines neuen Benutzers
    public boolean registerUser(String username, String password, int founder, int leagueId) {
        long result = -1;
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("username", username);
            values.put("password", password);
            values.put("founder", founder);
            values.put("leagueId", leagueId);
            result = db.insert("users", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error registering user: " + e.getMessage());
        }
        return result != -1;
    }

    public Integer getUserFounder(String username) {
        Integer userFounder = 0;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query("users", new String[]{"founder"}, "username = ?",
                     new String[]{username}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                userFounder = cursor.getInt(cursor.getColumnIndexOrThrow("founder"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user founder status: " + e.getMessage());
        }
        return userFounder;
    }

    public void addLeagueToUser(String username, long leagueId) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("leagueId", leagueId);
            db.update("users", values, "username = ?", new String[]{username});
        } catch (Exception e) {
            Log.e(TAG, "Error adding league to user: " + e.getMessage());
        }
    }

    public void setUserFounder(String username, int founderValue) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("founder", founderValue);
            db.update("users", values, "username = ?", new String[]{username});
        } catch (Exception e) {
            Log.e(TAG, "Error setting user founder status: " + e.getMessage());
        }
    }

    public List<String> getAllLeagues() {
        List<String> leagues = new ArrayList<>();
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.query("leagues", new String[]{"leagueName"}, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                leagues.add(cursor.getString(cursor.getColumnIndexOrThrow("leagueName")));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting all leagues: " + e.getMessage());
        }
        return leagues;
    }

    public String getCurrentGameday(int leagueId) {
        String currentGameday = "";
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT DISTINCT gameday FROM games WHERE leagueId = ? AND goalsTeam1 IS NULL AND goalsTeam2 IS NULL AND result IS NULL LIMIT 1",
                     new String[]{String.valueOf(leagueId)})) {
            if (cursor != null && cursor.moveToFirst()) {
                currentGameday = cursor.getString(cursor.getColumnIndexOrThrow("gameday"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current gameday: " + e.getMessage());
        }
        return currentGameday;
    }

    public long addNewLeague(String leagueName) {
        long result = -1;
        try (SQLiteDatabase db = this.getWritableDatabase()) {
            ContentValues values = new ContentValues();
            values.put("leagueName", leagueName);
            result = db.insert("leagues", null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error adding new league: " + e.getMessage());
        }
        return result;
    }

    public int getLeagueIdByName(String leagueName) {
        int leagueId = -1;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT id FROM leagues WHERE leagueName = ?",
                     new String[]{leagueName})) {
            if (cursor != null && cursor.moveToFirst()) {
                leagueId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting league ID by name: " + e.getMessage());
        }
        return leagueId;
    }

    public int getLeagueIdByUsername(String username) {
        int leagueId = -1;
        try (SQLiteDatabase db = this.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT leagueId FROM users WHERE username = ?",
                     new String[]{username})) {
            if (cursor != null && cursor.moveToFirst()) {
                leagueId = cursor.getInt(cursor.getColumnIndexOrThrow("leagueId"));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting league ID by username: " + e.getMessage());
        }
        return leagueId;
    }

    public synchronized void chooseResult(SQLiteDatabase db, int gameId, int leagueId) {
        Cursor cursor = null;
        try {
            Log.d(TAG, "Choosing result for game ID: " + gameId + " in league ID: " + leagueId);
            String query = "SELECT goalsTeam1, goalsTeam2 FROM games WHERE id = ? AND leagueId = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(gameId), String.valueOf(leagueId)});
            if (cursor != null && cursor.moveToFirst()) {
                int goalsTeam1 = cursor.getInt(cursor.getColumnIndexOrThrow("goalsTeam1"));
                int goalsTeam2 = cursor.getInt(cursor.getColumnIndexOrThrow("goalsTeam2"));

                String result = (goalsTeam1 > goalsTeam2) ? "Team1" : (goalsTeam2 > goalsTeam1) ? "Team2" : "draw";

                ContentValues values = new ContentValues();
                values.put("result", result);
                db.update("games", values, "id = ? AND leagueId = ?", new String[]{String.valueOf(gameId), String.valueOf(leagueId)});
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in chooseResult: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public synchronized void updatePointsForAllUsers(SQLiteDatabase db, int gameId, int leagueId) {
        Cursor gameCursor = null;
        Cursor usersCursor = null;
        Cursor expectedCursor = null;
        try {
            db.beginTransactionNonExclusive();

            String gameQuery = "SELECT goalsTeam1, goalsTeam2 FROM games WHERE id = ? AND leagueId = ?";
            gameCursor = db.rawQuery(gameQuery, new String[]{String.valueOf(gameId), String.valueOf(leagueId)});

            if (gameCursor != null && gameCursor.moveToFirst()) {
                int goalsTeam1 = gameCursor.getInt(gameCursor.getColumnIndexOrThrow("goalsTeam1"));
                int goalsTeam2 = gameCursor.getInt(gameCursor.getColumnIndexOrThrow("goalsTeam2"));

                String usersQuery = "SELECT username FROM points WHERE leagueId = ?";
                usersCursor = db.rawQuery(usersQuery, new String[]{String.valueOf(leagueId)});

                if (usersCursor != null && usersCursor.moveToFirst()) {
                    do {
                        String username = usersCursor.getString(usersCursor.getColumnIndexOrThrow("username"));

                        String expectedQuery = "SELECT expectedGoalsTeam1, expectedGoalsTeam2 FROM user_expected_results WHERE gameId = ? AND userId = ?";
                        expectedCursor = db.rawQuery(expectedQuery, new String[]{String.valueOf(gameId), String.valueOf(getUserId(db, username))});

                        if (expectedCursor != null && expectedCursor.moveToFirst()) {
                            int expectedGoalsTeam1 = expectedCursor.getInt(expectedCursor.getColumnIndexOrThrow("expectedGoalsTeam1"));
                            int expectedGoalsTeam2 = expectedCursor.getInt(expectedCursor.getColumnIndexOrThrow("expectedGoalsTeam2"));

                            int scoreToAdd = calculateScore(goalsTeam1, goalsTeam2, expectedGoalsTeam1, expectedGoalsTeam2);

                            ContentValues values = new ContentValues();
                            values.put("score", getPointsForUser(db, username) + scoreToAdd);
                            db.update("points", values, "username = ? AND leagueId = ?", new String[]{username, String.valueOf(leagueId)});
                        }
                        if (expectedCursor != null) expectedCursor.close();
                    } while (usersCursor.moveToNext());
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error during points update: " + e.getMessage());
        } finally {
            if (gameCursor != null) gameCursor.close();
            if (usersCursor != null) usersCursor.close();
            if (expectedCursor != null) expectedCursor.close();
            db.endTransaction();
        }
    }

    private int calculateScore(int goalsTeam1, int goalsTeam2, int expectedGoalsTeam1, int expectedGoalsTeam2) {
        int scoreToAdd = 0;

        if ((goalsTeam1 > goalsTeam2 && expectedGoalsTeam1 > expectedGoalsTeam2) ||
                (goalsTeam1 < goalsTeam2 && expectedGoalsTeam1 < expectedGoalsTeam2) ||
                (goalsTeam1 == goalsTeam2 && expectedGoalsTeam1 == expectedGoalsTeam2)) {
            scoreToAdd += 2;

            if (goalsTeam1 == expectedGoalsTeam1 && goalsTeam2 == expectedGoalsTeam2) {
                scoreToAdd += 2;
            }
        }

        return scoreToAdd;
    }

    public int getPointsForUser(SQLiteDatabase db, String username) {
        int points = 0;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT score FROM points WHERE username = ?", new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {
                int scoreIndex = cursor.getColumnIndex("score");
                if (scoreIndex != -1) {
                    points = cursor.getInt(scoreIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting points for user: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return points;
    }

    public int getUserId(String username) {
        int userId = -1;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query("users", new String[]{"id"}, "username = ?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int userIdIndex = cursor.getColumnIndexOrThrow("id");
                userId = cursor.getInt(userIdIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Closed database after getting user ID for username: " + username);
        }
        return userId;
    }

    public void initializePointsTableForUser(String username, int leagueId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            int userId = getUserId(db, username);

            if (userId != -1) {
                ContentValues values = new ContentValues();
                values.put("userId", userId);
                values.put("username", username);
                values.put("score", 0);
                values.put("leagueId", leagueId);

                long result = db.insert("points", null, values);

                if (result == -1) {
                    Log.e("DatabaseHelper", "Error inserting into points table for user: " + username);
                } else {
                    Log.d("DatabaseHelper", "Inserted user " + username + " into points table for league " + leagueId);
                }
            } else {
                Log.e("DatabaseHelper", "User ID not found for username: " + username);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error initializing points table for user: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
                Log.d("DatabaseHelper", "Closed database after initializing points table for user: " + username);
            }
        }
    }

    public int getUserId(SQLiteDatabase db, String username) {
        int userId = -1;
        Cursor cursor = null;
        try {
            cursor = db.query("users", new String[]{"id"}, "username = ?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int userIdIndex = cursor.getColumnIndexOrThrow("id");
                userId = cursor.getInt(userIdIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return userId;
    }

    public String getUserLeague(String username) {
        String userLeague = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = this.getReadableDatabase();
            cursor = db.query("users", new String[]{"leagueId"}, "username = ?", new String[]{username}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int leagueIndex = cursor.getColumnIndexOrThrow("leagueId");
                userLeague = cursor.getString(leagueIndex);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user league: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Closed database after getting user league for username: " + username);
        }
        return userLeague;
    }

    public void insertInitialGames(int leagueId) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();

            // Begin a single transaction for all insertions
            db.beginTransaction();
            try {
                String[][] games = {
                        {"1", "Schalke 04", "1. FC Nürnberg"},
                        {"1", "Bayern München", "Borussia Dortmund"},
                        {"2", "Borussia Dortmund", "Schalke 04"},
                        {"2", "1.FC Nürnberg", "Bayern München"},
                        {"3", "RB Leipzig", "1. FC Union Berlin"},
                        {"3", "TSG Hoffenheim", "Bayer 04 Leverkusen"},
                        {"4", "1. FC Heidenheim", "SC Freiburg"},
                        {"4", "Werder Bremen", "Bayern München"},
                        {"5", "RB Leipzig", "FC Augsburg"},
                        {"5", "SC Freiburg", "FC St. Pauli"},

                };

                for (String[] game : games) {
                    ContentValues values = new ContentValues();
                    values.put("gameday", Integer.parseInt(game[0]));
                    values.put("team1", game[1]);
                    values.put("team2", game[2]);
                    values.put("leagueId", leagueId);
                    db.insert("games", null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting initial games: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Closed database after inserting initial games");
        }
    }

}

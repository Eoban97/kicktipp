package com.example.kicktipp.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kicktipp.R;
import com.example.kicktipp.database.DatabaseHelper;
import com.example.kicktipp.model.Game;
import com.example.kicktipp.model.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TippResultsActivity extends AppCompatActivity {
    private List<Game> games;
    private Button saveTipps;
    private Button backToMenu;
    private LinearLayout gamesLayout;
    private DatabaseHelper databaseHelper;
    private static final String TAG = "TippResultsActivity";
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Object dbLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipp_results);

        gamesLayout = findViewById(R.id.games_layout);
        databaseHelper = new DatabaseHelper(this);

        saveTipps = findViewById(R.id.save_results_button);
        saveTipps.setOnClickListener(v -> saveTipsToDatabase());

        backToMenu = findViewById(R.id.back_to_menu_button);
        backToMenu.setOnClickListener(view -> {
            Intent gotoSecond = new Intent(TippResultsActivity.this, MainActivity.class);
            startActivity(gotoSecond);
        });

        loadGamesAndGameday();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGamesAndGameday();
    }

    private void loadGamesAndGameday() {
        SQLiteDatabase db = null;
        try {
            games = getGamesFromDatabase();
            if (games != null) {
                displayGames(games);
            }

            updateGamedayTextView();
            loadSavedTips();
        } catch (Exception e) {
            Toast.makeText(this, "Fehler beim Laden der Spiele: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
            Log.d(TAG, "Datenbank nach Laden der Spiele geschlossen");
        }
    }

    @SuppressLint("Range")
    private List<Game> getGamesFromDatabase() {
        List<Game> gamesList = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            int leagueId = SessionManager.getUserLeagueId();
            String currentGameday = databaseHelper.getCurrentGameday(leagueId);

            synchronized (dbLock) {
                db = databaseHelper.getReadableDatabase();
                cursor = db.query("games", null, "leagueId = ? AND gameday = ?",
                        new String[]{String.valueOf(leagueId), currentGameday},
                        null, null, null);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    int gameday = cursor.getInt(cursor.getColumnIndex("gameday"));
                    String team1 = cursor.getString(cursor.getColumnIndex("team1"));
                    String team2 = cursor.getString(cursor.getColumnIndex("team2"));
                    gamesList.add(new Game(id, gameday, team1, team2));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Abrufen der Spiele: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Datenbank nach Abrufen der Spiele geschlossen");
        }
        return gamesList;
    }

    private void displayGames(List<Game> games) {
        gamesLayout.removeAllViews();
        for (Game game : games) {
            View gameView = getLayoutInflater().inflate(R.layout.game_item, null);

            TextView team1TextView = gameView.findViewById(R.id.team1_text_view);
            TextView team2TextView = gameView.findViewById(R.id.team2_text_view);
            team1TextView.setText(game.getTeam1());
            team2TextView.setText(game.getTeam2());

            gamesLayout.addView(gameView);
        }
    }

    private SQLiteDatabase getWritableDatabaseWithRetry(SQLiteOpenHelper helper) {
        SQLiteDatabase db = null;
        int retries = 10;
        while (retries > 0) {
            try {
                db = helper.getWritableDatabase();
                break;
            } catch (SQLiteDatabaseLockedException e) {
                retries--;
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {}
            }
        }
        return db;
    }

    private void saveTipsToDatabase() {
        databaseExecutor.execute(() -> {
            SQLiteDatabase db = null;
            try {
                synchronized (dbLock) {
                    db = getWritableDatabaseWithRetry(databaseHelper);
                    if (db == null) {
                        throw new Exception("Unable to open database");
                    }

                    db.beginTransaction();

                    boolean isAllSaved = true;

                    for (int i = 0; i < games.size(); i++) {
                        Game game = games.get(i);
                        View gameView = gamesLayout.getChildAt(i);

                        EditText tip1EditText = gameView.findViewById(R.id.goal1_edit_text);
                        EditText tip2EditText = gameView.findViewById(R.id.goal2_edit_text);

                        String tip1 = tip1EditText.getText().toString().trim();
                        String tip2 = tip2EditText.getText().toString().trim();

                        if (!tip1.isEmpty() && !tip2.isEmpty()) {
                            int homeGoals = Integer.parseInt(tip1);
                            int awayGoals = Integer.parseInt(tip2);
                            String expectedResult;

                            if (homeGoals > awayGoals) {
                                expectedResult = "Team1";
                            } else if (homeGoals < awayGoals) {
                                expectedResult = "Team2";
                            } else {
                                expectedResult = "draw";
                            }

                            ContentValues values = new ContentValues();
                            values.put("userId", SessionManager.getUserId());
                            values.put("gameId", game.getGameId());
                            values.put("expectedGoalsTeam1", homeGoals);
                            values.put("expectedGoalsTeam2", awayGoals);
                            values.put("expectedResult", expectedResult);

                            long result = db.insertWithOnConflict("user_expected_results", null, values, SQLiteDatabase.CONFLICT_REPLACE);

                            if (result == -1) {
                                isAllSaved = false;
                            }
                        }
                    }

                    if (isAllSaved) {
                        db.setTransactionSuccessful();
                        runOnUiThread(() -> Toast.makeText(TippResultsActivity.this, "Alle Tipps wurden erfolgreich gespeichert!", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(TippResultsActivity.this, "Einige Tipps konnten nicht gespeichert werden. Bitte versuchen Sie es erneut.", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(TippResultsActivity.this, "Fehler beim Speichern der Tipps: " + e.getMessage(), Toast.LENGTH_LONG).show());
            } finally {
                if (db != null && db.isOpen()) {
                    db.endTransaction();
                    db.close();
                }
            }
        });
    }

    @SuppressLint("Range")
    private void loadSavedTips() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            synchronized (dbLock) {
                db = databaseHelper.getReadableDatabase();
                Log.d(TAG, "Datenbank geöffnet, um gespeicherte Tipps zu laden");

                for (int i = 0; i < games.size(); i++) {
                    Game game = games.get(i);
                    View gameView = gamesLayout.getChildAt(i);

                    EditText tip1EditText = gameView.findViewById(R.id.goal1_edit_text);
                    EditText tip2EditText = gameView.findViewById(R.id.goal2_edit_text);

                    String query = "SELECT expectedGoalsTeam1, expectedGoalsTeam2 FROM user_expected_results WHERE gameId = ? AND userId = ?";
                    cursor = db.rawQuery(query, new String[]{String.valueOf(game.getGameId()), String.valueOf(SessionManager.getUserId())});

                    if (cursor != null && cursor.moveToFirst()) {
                        int tip1 = cursor.getInt(cursor.getColumnIndex("expectedGoalsTeam1"));
                        int tip2 = cursor.getInt(cursor.getColumnIndex("expectedGoalsTeam2"));

                        tip1EditText.setText(String.valueOf(tip1));
                        tip2EditText.setText(String.valueOf(tip2));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Laden der gespeicherten Tipps: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Datenbank nach Laden der gespeicherten Tipps geschlossen");
        }
    }

    private void updateGamedayTextView() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            synchronized (dbLock) {
                db = databaseHelper.getReadableDatabase();
                Log.d(TAG, "Datenbank geöffnet, um Spieltag-TextView zu aktualisieren");
                int leagueId = SessionManager.getUserLeagueId();
                String currentGameday = databaseHelper.getCurrentGameday(leagueId);
                TextView gamedayTextView = findViewById(R.id.spieltag_text_view);
                gamedayTextView.setText("Spieltag " + currentGameday);
            }
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Aktualisieren des Spieltag-TextViews: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
            Log.d(TAG, "Datenbank nach Aktualisieren des Spieltag-TextViews geschlossen");
        }
    }
}

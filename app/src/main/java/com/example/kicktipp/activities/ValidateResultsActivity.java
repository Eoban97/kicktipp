package com.example.kicktipp.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
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

public class ValidateResultsActivity extends AppCompatActivity {
    private List<Game> games;
    private Button saveResults;
    private Button backToMenu;
    private DatabaseHelper databaseHelper;
    private LinearLayout gamesLayout;
    private ExecutorService executorService;
    private static final String TAG = "ValidateResultsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_results);

        executorService = Executors.newSingleThreadExecutor();  // SingleThreadExecutor for sequential access

        databaseHelper = new DatabaseHelper(this);
        gamesLayout = findViewById(R.id.games_layout);
        updateGamedayTextView();

        saveResults = findViewById(R.id.save_results_button);
        saveResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executorService.submit(() -> saveResultsToDatabase());
            }
        });

        backToMenu = findViewById(R.id.back_to_menu_button2);
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(ValidateResultsActivity.this, MainActivity.class);
                startActivity(gotoSecond);
            }
        });

        loadGamesAndDisplay();
    }

    private void loadGamesAndDisplay() {
        games = getGamesFromDatabase();
        if (games == null || games.isEmpty()) {
            games = getTestGames();
        }
        displayGames(games);
    }

    @SuppressLint("Range")
    private synchronized List<Game> getGamesFromDatabase() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Game> gamesList = new ArrayList<>();
        try {
            int leagueId = SessionManager.getUserLeagueId();
            int currentGameday = Integer.parseInt(databaseHelper.getCurrentGameday(leagueId));  // Use the current gameday
            db = databaseHelper.getReadableDatabase(); // Direkter Zugriff auf die Datenbank
            Log.d(TAG, "Opened database to fetch games");
            String selection = "leagueId = ? AND gameday = ?";
            String[] selectionArgs = {String.valueOf(leagueId), String.valueOf(currentGameday)};
            cursor = db.query("games", null, selection, selectionArgs, null, null, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                int gameday = cursor.getInt(cursor.getColumnIndex("gameday"));
                String team1 = cursor.getString(cursor.getColumnIndex("team1"));
                String team2 = cursor.getString(cursor.getColumnIndex("team2"));
                gamesList.add(new Game(id, gameday, team1, team2));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching games: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close(); // Schließe die Datenbankverbindung
        }
        return gamesList;
    }

    private List<Game> getTestGames() {
        List<Game> testGames = new ArrayList<>();
        testGames.add(new Game(1, 1, "Team A", "Team B"));
        testGames.add(new Game(2, 1, "Team C", "Team D"));
        testGames.add(new Game(3, 1, "Team E", "Team F"));
        return testGames;
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

    private void saveResultsToDatabase() {
        executorService.execute(() -> {
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase(); // Direkter Zugriff auf die Datenbank

                db.beginTransaction();

                boolean isAllSaved = true;

                for (int i = 0; i < games.size(); i++) {
                    Game game = games.get(i);
                    View gameView = gamesLayout.getChildAt(i);

                    EditText goal1EditText = gameView.findViewById(R.id.goal1_edit_text);
                    EditText goal2EditText = gameView.findViewById(R.id.goal2_edit_text);

                    String team1Goals = goal1EditText.getText().toString().trim();
                    String team2Goals = goal2EditText.getText().toString().trim();

                    if (!team1Goals.isEmpty() && !team2Goals.isEmpty()) {
                        ContentValues values = new ContentValues();
                        values.put("goalsTeam1", Integer.parseInt(team1Goals));
                        values.put("goalsTeam2", Integer.parseInt(team2Goals));

                        int leagueId = SessionManager.getUserLeagueId();
                        String whereClause = "id = ? AND leagueId = ?";
                        String[] whereArgs = {String.valueOf(game.getGameId()), String.valueOf(leagueId)};
                        int rowsAffected = db.update("games", values, whereClause, whereArgs);

                        if (rowsAffected <= 0) {
                            isAllSaved = false;
                            Log.e("SaveResultError", "Error updating game ID: " + game.getGameId());
                        } else {
                            databaseHelper.chooseResult(db, game.getGameId(), leagueId);
                            databaseHelper.updatePointsForAllUsers(db, game.getGameId(), leagueId);
                        }
                    } else {
                        Log.e("SaveResultError", "Goals input missing for game ID: " + game.getGameId());
                        isAllSaved = false;
                    }
                }

                if (isAllSaved) {
                    db.setTransactionSuccessful();
                    runOnUiThread(() -> {
                        Toast.makeText(getApplicationContext(), "Results saved successfully", Toast.LENGTH_SHORT).show();
                        loadGamesAndDisplay();
                        updateGamedayTextView();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Error saving results", Toast.LENGTH_SHORT).show());
                }
            } catch (SQLiteDatabaseLockedException e) {
                Log.e("DatabaseError", "Error saving: Database is locked. " + e.getMessage());
                retrySavingResults();  // Retry mechanism
            } catch (Exception e) {
                Log.e("DatabaseError", "Error saving: " + e.getMessage());
            } finally {
                if (db != null && db.inTransaction()) {
                    db.endTransaction();
                }
                if (db != null && db.isOpen()) db.close(); // Schließe die Datenbankverbindung
            }
        });
    }

    private void retrySavingResults() {
        executorService.execute(() -> {
            try {
                Thread.sleep(200);
                saveResultsToDatabase();
            } catch (InterruptedException e) {
                Log.e("DatabaseError", "Error retrying save: " + e.getMessage());
            }
        });
    }

    public void updateGamedayTextView() {
        int leagueId = SessionManager.getUserLeagueId();
        String currentGameday = databaseHelper.getCurrentGameday(leagueId);
        TextView gamedayTextView = findViewById(R.id.spieltag_text_view);
        gamedayTextView.setText("Spieltag " + currentGameday);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

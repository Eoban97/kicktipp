package com.example.kicktipp.activities;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class ValidateResultsActivity extends AppCompatActivity {
    private List<Game> games; // Annahme: Game ist eine Klasse, die ein Spiel repräsentiert
    private Button saveResults;
    private Button backToMenu;
    private List<Game> getTestGames() {
        List<Game> testGames = new ArrayList<>();

        // Erstelle einige Testspiele
        testGames.add(new Game(1,1, "Team A", "Team B"));
        testGames.add(new Game(2,1, "Team C", "Team D"));
        testGames.add(new Game(3,1, "Team E", "Team F"));

        return testGames;
    }
    private DatabaseHelper databaseHelper;
    private LinearLayout gamesLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validate_results);
        databaseHelper = new DatabaseHelper(this);
        gamesLayout = findViewById(R.id.games_layout);
        updateGamedayTextView();

        saveResults=findViewById(R.id.save_results_button);
        saveResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResultsToDatabase(); // Rufe saveResultsToDatabase() auf, wenn der Button geklickt wird
                updateGamedayTextView();
            }
        });

        backToMenu=findViewById(R.id.back_to_menu_button2);
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(ValidateResultsActivity.this, MainActivity.class);
                startActivity(gotoSecond);
            }
        });

        // Datenbankabfrage, um die Spiele abzurufen
        games = getGamesFromDatabase();

        if (games == null) {
            games = getTestGames();
        }

        // Anzeigen der Spiele auf der Seite
        displayGames(games);
    }

    private List<Game> getGamesFromDatabase() {
        // Hier führst du deine Datenbankabfrage durch, um die Spiele abzurufen
        // Beispiel: return database.getGames();
        return databaseHelper.getCurrentMatchdayGames();
    }

    private void displayGames(List<Game> games) {
        LinearLayout gamesLayout = findViewById(R.id.games_layout);

        // Überprüfe, ob die games-Liste null ist
        if (games == null) {
            games = getTestGames(); // Verwende Testdaten, wenn die Liste null ist
        }

        for (Game game : games) {
            View gameView = getLayoutInflater().inflate(R.layout.game_item, null);

            // Setze die Namen der Teams für das aktuelle Spiel
            TextView team1TextView = gameView.findViewById(R.id.team1_text_view);
            TextView team2TextView = gameView.findViewById(R.id.team2_text_view);
            team1TextView.setText(game.getTeam1());
            team2TextView.setText(game.getTeam2());



            // Füge das Spiel und die Eingabefelder zur Anzeige hinzu
            gamesLayout.addView(gameView);


        }
    }

    private void saveResultsToDatabase() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        boolean isSavedSuccessfully = true; // Eine Variable, um den Erfolg der Speicherung zu verfolgen

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);

            // Finde die EditText-Views basierend auf ihren IDs
            EditText goal1EditText = findViewById(R.id.goal1_edit_text);
            EditText goal2EditText = findViewById(R.id.goal2_edit_text);

            // Extrahiere die eingegebenen Ergebnisse
            String team1Goals = goal1EditText.getText().toString().trim();
            String team2Goals = goal2EditText.getText().toString().trim();

            // Überprüfe, ob die Eingabefelder nicht leer sind
            if (!team1Goals.isEmpty() && !team2Goals.isEmpty()) {
                // Füge die Ergebnisse zur Datenbank hinzu
                ContentValues values = new ContentValues();
                values.put("goalsTeam1", Integer.parseInt(team1Goals));
                values.put("goalsTeam2", Integer.parseInt(team2Goals));

                // Hier setzen Sie die Bedingung für die Aktualisierung basierend auf dem Spiel-Id
                String whereClause = "id = ?";
                String[] whereArgs = {String.valueOf(game.getGameId())};
                int rowsAffected = db.update("games", values, whereClause, whereArgs);

                // Überprüfe, ob die Aktualisierung erfolgreich war
                if (rowsAffected <= 0) {
                    isSavedSuccessfully = false; // Markiere die Speicherung als fehlgeschlagen, wenn keine Zeilen aktualisiert wurden
                } else {
                    // Wenn die Aktualisierung erfolgreich war, wähle das Ergebnis für das Spiel aus
                    databaseHelper.chooseResult(db, game.getGameId());
                    updatePointsForAllUsers(db,game.getGameId());
                }
            }
        }

        db.close();

        // Nachdem die Ergebnisse gespeichert wurden, rufe die Methode zum Aktualisieren der Punkte auf


        // Schließe die Datenbankverbindung nachdem die Punkte aktualisiert wurden


        // Zeige eine Toast-Nachricht basierend auf dem Erfolg der Speicherung an
        if (isSavedSuccessfully) {
            // Erfolgreiche Speicherung
            Toast.makeText(getApplicationContext(), "Ergebnisse erfolgreich gespeichert", Toast.LENGTH_SHORT).show();
        } else {
            // Fehler bei der Speicherung
            Toast.makeText(getApplicationContext(), "Fehler beim Speichern der Ergebnisse", Toast.LENGTH_SHORT).show();
        }
    }


    // Methode zum Aktualisieren der Punkte für alle Benutzer in derselben Liga
    public void updatePointsForAllUsers(SQLiteDatabase db, int gameId) {
        // Abfrage, um das Ergebnis des Spiels abzurufen
        String gameQuery = "SELECT goalsTeam1, goalsTeam2 FROM games WHERE id = ?";
        Cursor gameCursor = db.rawQuery(gameQuery, new String[]{String.valueOf(gameId)});
        if (gameCursor != null && gameCursor.moveToFirst()) {
            @SuppressLint("Range") int goalsTeam1 = gameCursor.getInt(gameCursor.getColumnIndex("goalsTeam1"));
            @SuppressLint("Range") int goalsTeam2 = gameCursor.getInt(gameCursor.getColumnIndex("goalsTeam2"));

            // Überprüfen, ob das Ergebnis des Spiels vorhanden ist
            if (goalsTeam1 >= 0 && goalsTeam2 >= 0) {
                // Abfrage, um alle Benutzer in derselben Liga zu erhalten
                String usersQuery = "SELECT username FROM points WHERE league = ?";
                Cursor usersCursor = db.rawQuery(usersQuery, new String[]{SessionManager.getUserLeague()});
                if (usersCursor != null && usersCursor.moveToFirst()) {
                    do {
                        @SuppressLint("Range") String username = usersCursor.getString(usersCursor.getColumnIndex("username"));
                        databaseHelper.updatePoints(db,gameId, username); // Aufruf der Methode zum Aktualisieren der Punkte für den aktuellen Benutzer
                    } while (usersCursor.moveToNext());
                    usersCursor.close();
                }
            }
        }
        if (gameCursor != null) {
            gameCursor.close();
        }
    }



    public void updateGamedayTextView() {
        String currentGameday = databaseHelper.getCurrentGameday();
        TextView gamedayTextView = findViewById(R.id.spieltag_text_view);
        gamedayTextView.setText("Spieltag " + currentGameday);
    }







}

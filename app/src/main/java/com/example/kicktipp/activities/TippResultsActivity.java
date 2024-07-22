package com.example.kicktipp.activities;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static com.example.kicktipp.model.SessionManager.getUserId;

import android.content.ContentValues;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class TippResultsActivity extends AppCompatActivity {
    private List<Game> games; // Annahme: Game ist eine Klasse, die ein Spiel repräsentiert
    private Button saveTipps;
    private Button backToMenu;
    private LinearLayout gamesLayout;
    private List<Game> getTestGames() {
        List<Game> testGames = new ArrayList<>();

        // Erstelle einige Testspiele
        testGames.add(new Game(1,1, "Team A", "Team B"));
        testGames.add(new Game(2,1, "Team C", "Team D"));
        testGames.add(new Game(3,1, "Team E", "Team F"));

        return testGames;
    }
    private DatabaseHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tipp_results);

        gamesLayout = findViewById(R.id.games_layout);

        databaseHelper = new DatabaseHelper(this);
        updateGamedayTextView();

        saveTipps=findViewById(R.id.save_results_button);
        saveTipps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTipsToDatabase(); // Rufe saveResultsToDatabase() auf, wenn der Button geklickt wird
                updateGamedayTextView();
            }
        });

        backToMenu=findViewById(R.id.back_to_menu_button);
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(TippResultsActivity.this, MainActivity.class);
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


    private void saveTipsToDatabase() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        boolean isAllSaved = true; // Flag, um zu überprüfen, ob alle Tipps erfolgreich gespeichert wurden

        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            EditText tip1EditText = findViewById(R.id.goal1_edit_text);
            EditText tip2EditText = findViewById(R.id.goal2_edit_text);

            // Extrahiere die eingegebenen Tipps
            String tip1 = tip1EditText.getText().toString().trim();
            String tip2 = tip2EditText.getText().toString().trim();

            // Überprüfe, ob die Eingabefelder nicht leer sind
            if (!tip1.isEmpty() && !tip2.isEmpty()) {
                // Füge die Tipps zur Datenbank hinzu
                ContentValues values = new ContentValues();
                values.put("userId", getUserId());
                values.put("gameId", game.getGameId());
                values.put("expectedGoalsTeam1", Integer.parseInt(tip1));
                values.put("expectedGoalsTeam2", Integer.parseInt(tip2));
                long result = db.insert("user_expected_results", null, values);

                // Überprüfe das Ergebnis des Einfügens
                if (result == -1) {
                    isAllSaved = false; // Setze das Flag auf false, wenn ein Tipp nicht erfolgreich gespeichert wurde
                } else {
                    // Wenn das Einfügen erfolgreich war, wähle das erwartete Ergebnis für das Spiel aus
                    databaseHelper.chooseExpectedResult(db, game.getGameId(), getUserId());
                }
            }
        }



        // Überprüfe, ob alle Tipps erfolgreich gespeichert wurden
        if (isAllSaved) {
            // Zeige eine Erfolgsmeldung an
            Toast.makeText(this, "Alle Tipps wurden erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();
        } else {
            // Zeige eine Fehlermeldung an, wenn nicht alle Tipps erfolgreich gespeichert wurden
            Toast.makeText(this, "Einige Tipps konnten nicht gespeichert werden. Bitte versuchen Sie es erneut.", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    public void updateGamedayTextView() {
        String currentGameday = databaseHelper.getCurrentGameday();
        TextView gamedayTextView = findViewById(R.id.spieltag_text_view);
        gamedayTextView.setText("Spieltag " + currentGameday);
    }





}

package com.example.kicktipp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kicktipp.database.DatabaseHelper;
import com.example.kicktipp.R;
import com.example.kicktipp.model.SessionManager;

public class LeagueTableActivity extends AppCompatActivity {

    private Button backToMenu;
    private TextView leagueName;
    private TableLayout leagueTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_table);

        leagueName = findViewById(R.id.league_name_table_text_view);
        leagueTable = findViewById(R.id.leaguetable_table);

        leagueName.setText(SessionManager.getUserLeague());

        backToMenu = findViewById(R.id.back_button);
        backToMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(LeagueTableActivity.this, MainActivity.class);
                startActivity(gotoSecond);
            }
        });

        displayLeagueTable();
    }

    private void displayLeagueTable() {
        String currentUserLeague = SessionManager.getUserLeague();

        if (currentUserLeague != null && !currentUserLeague.isEmpty()) {
            SQLiteDatabase db = new DatabaseHelper(getApplicationContext()).getReadableDatabase();

            // Abfrage, um die Benutzer und ihre Punkte abzurufen und nach Punkten absteigend zu sortieren
            // Nur Benutzer aus derselben Liga wie der aktuelle Benutzer werden abgefragt
            String query = "SELECT username, score " +
                    "FROM points " +
                    "WHERE league = ? " +
                    "ORDER BY score DESC";
            Cursor cursor = db.rawQuery(query, new String[]{currentUserLeague});

            if (cursor != null) {
                int usernameIndex = cursor.getColumnIndex("username");
                int scoreIndex = cursor.getColumnIndex("score");

                if (usernameIndex != -1 && scoreIndex != -1 && cursor.getCount() > 0) {
                    int position = 0; // Startposition für die Platzierung
                    if (cursor.moveToFirst()) {
                        do {
                            position++; // Inkrementiere die Platzierung

                            // Deine Zeilenverarbeitung
                            String username = cursor.getString(usernameIndex);
                            int score = cursor.getInt(scoreIndex);

                            // Erstellen einer neuen Zeile für die Tabelle
                            TableRow row = new TableRow(this);
                            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                            row.setLayoutParams(lp);

                            // Erstellen der TextViews für Platzierung, Benutzername und Punktestand
                            TextView placementTextView = new TextView(this);
                            placementTextView.setText(String.valueOf(position));
                            placementTextView.setGravity(Gravity.CENTER);
                            TextView usernameTextView = new TextView(this);
                            usernameTextView.setText(username);
                            usernameTextView.setGravity(Gravity.CENTER);
                            TextView scoreTextView = new TextView(this);
                            scoreTextView.setText(String.valueOf(score));
                            scoreTextView.setGravity(Gravity.CENTER);

                            // Hinzufügen der TextViews zur Zeile
                            row.addView(placementTextView);
                            row.addView(usernameTextView);
                            row.addView(scoreTextView);

                            // Hinzufügen der Zeile zur Tabelle
                            leagueTable.addView(row);
                        } while (cursor.moveToNext());
                    }
                } else {
                    // Zeige eine Toast-Nachricht an, wenn keine Daten vorhanden sind
                    Toast.makeText(this, "Keine Daten vorhanden", Toast.LENGTH_SHORT).show();
                }

                // Cursor schließen
                cursor.close();
            }

            // Datenbankverbindung schließen
            db.close();
        }
    }






}

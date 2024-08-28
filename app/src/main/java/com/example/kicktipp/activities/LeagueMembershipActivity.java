package com.example.kicktipp.activities;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kicktipp.R;
import com.example.kicktipp.database.DatabaseHelper;
import com.example.kicktipp.model.SessionManager;

import java.util.List;

public class LeagueMembershipActivity extends AppCompatActivity {

    private EditText leagueNameEditText;
    private Spinner allLeagues;
    private DatabaseHelper databaseHelper;
    private static final String TAG = "LeagueMembershipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_membership);

        leagueNameEditText = findViewById(R.id.league_name_edit_text);
        allLeagues = findViewById(R.id.all_leagues_spinner);

        Button createLeague = findViewById(R.id.create_league_button);
        Button joinLeague = findViewById(R.id.join_league_button);

        databaseHelper = new DatabaseHelper(this);

        loadAllLeagues();

        createLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createLeagueForUser();
            }
        });

        joinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinSelectedLeague();
            }
        });
    }

    private synchronized void createLeagueForUser() {
        String username = SessionManager.getLoggedInUsername();
        if (username != null) {
            String leagueName = leagueNameEditText.getText().toString().trim();
            if (!leagueName.isEmpty()) {
                SQLiteDatabase db = null;
                try {
                    db = databaseHelper.getWritableDatabase();
                    long leagueId = databaseHelper.addNewLeague(leagueName);
                    if (leagueId != -1) {
                        databaseHelper.addLeagueToUser(username, leagueId);
                        SessionManager.setUserLeague(leagueName);
                        SessionManager.setUserLeagueId((int) leagueId);
                        databaseHelper.setUserFounder(username, 1);
                        databaseHelper.insertInitialGames((int) leagueId);
                        databaseHelper.initializePointsTableForUser(username, (int) leagueId);

                        Toast.makeText(LeagueMembershipActivity.this, "Liga erstellt und Spieldaten wurden hinzugefügt", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Liga erfolgreich erstellt und Benutzer zugewiesen: " + leagueName);


                        Intent intent = new Intent(LeagueMembershipActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LeagueMembershipActivity.this, "Fehler beim Erstellen der Liga", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Fehler beim Erstellen der Liga: " + e.getMessage());
                    Toast.makeText(LeagueMembershipActivity.this, "Fehler beim Erstellen der Liga: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    if (db != null && db.isOpen()) {
                        db.close();
                        Log.d(TAG, "Datenbank nach Ligaerstellung geschlossen");
                    }
                }
            } else {
                Toast.makeText(LeagueMembershipActivity.this, "Bitte geben Sie einen Liganamen ein", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private synchronized void joinSelectedLeague() {
        String username = SessionManager.getLoggedInUsername();
        if (username != null) {
            String selectedLeagueName = allLeagues.getSelectedItem().toString();
            SQLiteDatabase db = null;
            try {
                db = databaseHelper.getWritableDatabase();
                int leagueId = databaseHelper.getLeagueIdByName(selectedLeagueName);
                if (leagueId != -1) {
                    databaseHelper.addLeagueToUser(username, leagueId);
                    SessionManager.setUserLeague(selectedLeagueName);
                    SessionManager.setUserLeagueId(leagueId);
                    databaseHelper.initializePointsTableForUser(username, leagueId);

                    Log.d(TAG, "Benutzer " + username + " ist der Liga " + selectedLeagueName + " beigetreten");

                    Intent intent = new Intent(LeagueMembershipActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LeagueMembershipActivity.this, "Fehler beim Beitreten der Liga", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Fehler beim Beitreten der Liga: " + e.getMessage());
                Toast.makeText(LeagueMembershipActivity.this, "Fehler beim Beitreten der Liga: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                if (db != null && db.isOpen()) db.close();
                Log.d(TAG, "Datenbank nach Liga-Beitritt geschlossen");
            }
        }
    }



    private void loadAllLeagues() {
        try {
            List<String> leagues = databaseHelper.getAllLeagues();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, leagues);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            allLeagues.setAdapter(adapter);
            Log.d(TAG, "Alle Ligen wurden erfolgreich geladen");
        } catch (Exception e) {
            Log.e(TAG, "Fehler beim Laden der Ligen: " + e.getMessage());
            Toast.makeText(LeagueMembershipActivity.this, "Fehler beim Laden der Ligen", Toast.LENGTH_SHORT).show();
        }
    }
}

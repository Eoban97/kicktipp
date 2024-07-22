package com.example.kicktipp.activities;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_league_membership);

        leagueNameEditText = findViewById(R.id.league_name_edit_text);
        allLeagues = findViewById(R.id.all_leagues_spinner);

        Button createLeague = findViewById(R.id.create_league_button);
        Button joinLeague = findViewById(R.id.join_league_button);

        databaseHelper = new DatabaseHelper(this);

        // Lade und zeige alle existierenden Ligen im Spinner an
        loadAllLeagues();

        createLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hole den Benutzernamen des angemeldeten Benutzers
                String username = SessionManager.getLoggedInUsername();
                if (username != null) {
                    // Füge die eingegebene Liga dem Benutzer hinzu
                    String leagueName = leagueNameEditText.getText().toString();
                    if (!leagueName.isEmpty()) {
                        databaseHelper.addLeagueToUser(username, leagueName);
                        SessionManager.setUserLeague(leagueName);
                        databaseHelper.setUserFounder(username, 1);
                        databaseHelper.insertInitialGames();
                        databaseHelper.initializePointsTableForUser(username);
                        Toast.makeText(LeagueMembershipActivity.this, "Spieldaten wurden hinzugefügt", Toast.LENGTH_SHORT).show();
                        // Navigiere zur Hauptmenü-Seite
                        Intent intent = new Intent(LeagueMembershipActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LeagueMembershipActivity.this, "Please enter a league name", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        joinLeague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hole den Benutzernamen des angemeldeten Benutzers
                String username = SessionManager.getLoggedInUsername();
                if (username != null) {
                    // Füge die ausgewählte Liga dem Benutzer hinzu
                    String selectedLeague = allLeagues.getSelectedItem().toString();
                    databaseHelper.addLeagueToUser(username, selectedLeague);
                    // Navigiere zur Hauptmenü-Seite
                    SessionManager.setUserLeague(selectedLeague);
                    databaseHelper.setUserFounder(username, 0);
                    databaseHelper.initializePointsTableForUser(username);
                    Intent intent = new Intent(LeagueMembershipActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void loadAllLeagues() {
        List<String> leagues = databaseHelper.getAllLeagues();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, leagues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        allLeagues.setAdapter(adapter);
    }
}

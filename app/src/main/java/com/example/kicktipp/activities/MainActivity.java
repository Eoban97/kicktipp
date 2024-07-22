package com.example.kicktipp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kicktipp.R;
import com.example.kicktipp.database.DatabaseHelper;
import com.example.kicktipp.model.SessionManager;

public class MainActivity extends AppCompatActivity {

    private TextView username;
    private TextView leaguename;

    private Button tippResults;
    private Button leagueTableView;
    private Button validateresults;
    private Button logOut;

    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        // Überprüfen, ob ein Benutzer angemeldet ist
        if (!SessionManager.isLoggedIn()) {
            // Wenn kein Benutzer angemeldet ist, öffnen Sie die LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Beenden Sie die MainActivity
        }

        // Initialisiere den DatabaseHelper
        SessionManager.initDatabaseHelper(getApplicationContext());

        username = findViewById(R.id.username_text_view);
        leaguename = findViewById(R.id.league_name_text_view);

        // Setze die Eigenschaften der TextViews, um den Text umzubrechen und vollständig anzuzeigen
        username.setSingleLine(false);
        username.setHorizontallyScrolling(false);
        username.setMaxLines(2); // Oder eine größere Zahl, wenn der Benutzername sehr lang sein kann
        username.setEllipsize(null); // Der Text wird abgeschnitten, wenn er nicht vollständig angezeigt werden kann

        leaguename.setSingleLine(false);
        leaguename.setHorizontallyScrolling(false);
        leaguename.setMaxLines(2); // Oder eine größere Zahl, wenn der Liganame sehr lang sein kann
        leaguename.setEllipsize(null); // Der Text wird abgeschnitten, wenn er nicht vollständig angezeigt werden kann

        logo=findViewById(R.id.zweite_bundesliga_image_view);

        tippResults = findViewById(R.id.tipp_results_button);
        tippResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // In deiner Aktivität
               // DatabaseHelper dbHelper = new DatabaseHelper(MainActivity.this); // 'this' bezieht sich auf die aktuelle Aktivität
               // dbHelper.deleteAllData();

                Intent gotoSecond = new Intent(MainActivity.this, TippResultsActivity.class);
                startActivity(gotoSecond);
            }
        });

        leagueTableView = findViewById(R.id.view_league_table_button);
        leagueTableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(MainActivity.this, LeagueTableActivity.class);
                startActivity(gotoSecond);
            }
        });

        validateresults = findViewById(R.id.vaildate_results_button);
        // Überprüfen, ob der Benutzer ein Gründer ist, um den Button für die Ergebnisvalidierung anzuzeigen oder auszublenden
        if (SessionManager.isCurrentUserFounder()) {
            validateresults.setVisibility(View.VISIBLE);
        } else {
            validateresults.setVisibility(View.GONE);
        }
        validateresults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gotoSecond = new Intent(MainActivity.this, ValidateResultsActivity.class);
                startActivity(gotoSecond);
            }
        });

        logOut= findViewById(R.id.logout_button);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager.setUserLeague("");
                Intent gotoSecond = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(gotoSecond);
            }
        });

        // Anzeigen des Benutzernamens und der Liga des angemeldeten Benutzers
        username.setText("Username: " + SessionManager.getLoggedInUsername());
        leaguename.setText("League: " + SessionManager.getUserLeague());
    }
}

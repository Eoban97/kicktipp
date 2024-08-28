package com.example.kicktipp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        if (!SessionManager.isLoggedIn()) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Beenden Sie die MainActivity
        }

        SessionManager.initDatabaseHelper(getApplicationContext());

        username = findViewById(R.id.username_text_view);
        leaguename = findViewById(R.id.league_name_text_view);

        username.setSingleLine(false);
        username.setHorizontallyScrolling(false);
        username.setMaxLines(2);
        username.setEllipsize(null);

        leaguename.setSingleLine(false);
        leaguename.setHorizontallyScrolling(false);
        leaguename.setMaxLines(2);
        leaguename.setEllipsize(null);

        logo = findViewById(R.id.zweite_bundesliga_image_view);

        tippResults = findViewById(R.id.tipp_results_button);
        tippResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        logOut = findViewById(R.id.logout_button);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SessionManager.setUserLeague("");
                SessionManager.setUserLeagueId(-1); // Setze die Liga-ID zurück
                Intent gotoSecond = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(gotoSecond);
                finish();
            }
        });

        username.setText("Username: " + SessionManager.getLoggedInUsername());
        leaguename.setText("Liga: " + SessionManager.getUserLeague());
    }
}

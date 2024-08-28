package com.example.kicktipp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kicktipp.R;
import com.example.kicktipp.database.DatabaseHelper;
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

        // Setze den Namen der Liga im TextView
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
        int leagueId = SessionManager.getUserLeagueId();

        SQLiteDatabase db = new DatabaseHelper(getApplicationContext()).getReadableDatabase();

        String query = "SELECT username, score " +
                "FROM points " +
                "WHERE leagueId = ? " +
                "ORDER BY score DESC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(leagueId)});

        if (cursor != null && cursor.getCount() > 0) {
            int position = 1;
            while (cursor.moveToNext()) {

                int usernameIndex = cursor.getColumnIndex("username");
                int scoreIndex = cursor.getColumnIndex("score");

                if (usernameIndex != -1 && scoreIndex != -1) {

                    String username = cursor.getString(usernameIndex);
                    int score = cursor.getInt(scoreIndex);

                    TableRow row = new TableRow(this);
                    TableRow.LayoutParams lp = new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT);
                    row.setLayoutParams(lp);


                    TextView positionTextView = new TextView(this);
                    positionTextView.setText(String.valueOf(position));
                    positionTextView.setPadding(8, 8, 8, 8);
                    positionTextView.setGravity(Gravity.CENTER);
                    row.addView(positionTextView);


                    TextView usernameTextView = new TextView(this);
                    usernameTextView.setText(username);
                    usernameTextView.setPadding(8, 8, 8, 8);
                    usernameTextView.setGravity(Gravity.CENTER);
                    row.addView(usernameTextView);


                    TextView scoreTextView = new TextView(this);
                    scoreTextView.setText(String.valueOf(score));
                    scoreTextView.setPadding(8, 8, 8, 8);
                    scoreTextView.setGravity(Gravity.CENTER);
                    row.addView(scoreTextView);


                    leagueTable.addView(row);

                    position++;
                } else {
                    Log.e("LeagueTableActivity", "Spalten 'username' oder 'score' nicht gefunden.");
                }
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Keine Daten vorhanden", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }


}

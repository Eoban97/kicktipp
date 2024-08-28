package com.example.kicktipp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kicktipp.R;
import com.example.kicktipp.database.DatabaseHelper;
import com.example.kicktipp.model.SessionManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;

    private DatabaseHelper databaseHelper;
    private ExecutorService executorService;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        databaseHelper = new DatabaseHelper(this);
        executorService = Executors.newSingleThreadExecutor();

        usernameEditText = findViewById(R.id.username_edit_text);
        passwordEditText = findViewById(R.id.r_password_edit_text);

        Button loginButton = findViewById(R.id.login_button);
        Button registerButton = findViewById(R.id.register_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                executorService.execute(() -> {
                    boolean isValid = databaseHelper.validateCredentials(username, password);
                    runOnUiThread(() -> {
                        if (isValid) {
                            Log.d(TAG, "Anmeldeinformationen erfolgreich validiert für Benutzer: " + username);
                            SessionManager.setLoggedInUsername(username);
                            String userLeague = databaseHelper.getUserLeague(username);
                            SessionManager.setUserLeague(userLeague);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Ungültiger Benutzername oder Passwort für Benutzer: " + username);
                            Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter username and password", Toast.LENGTH_SHORT).show();
                    return;
                }

                executorService.execute(() -> {
                    boolean isRegistered = databaseHelper.registerUser(username, password, 0, 0);
                    runOnUiThread(() -> {
                        if (isRegistered) {
                            Log.d(TAG, "Benutzer erfolgreich registriert: " + username);
                            SessionManager.setLoggedInUsername(username);

                            Intent intent = new Intent(LoginActivity.this, LeagueMembershipActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Registrierung fehlgeschlagen für Benutzer: " + username);
                            Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

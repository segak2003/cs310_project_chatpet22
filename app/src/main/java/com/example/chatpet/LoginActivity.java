package com.example.chatpet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.data.repository.UserRepository;
import com.example.chatpet.feature4.PetGrowthActivity;

public class LoginActivity extends AppCompatActivity {
    UserRepository userRepository;
    EditText etUsername, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        userRepository = new UserRepository(this);

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            userRepository.isUsernameTaken(username, (taken) -> {
                if (!taken) {
                    etUsername.setError("Incorrect username");
                    etUsername.requestFocus();
                } else {
                    userRepository.validatePassword(username, password, (success) -> {
                        if (!success) {
                            etPassword.setError("Incorrect password");
                            etPassword.requestFocus();
                        } else {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            userRepository.setActiveUser(username);
                            Intent intent = new Intent(this, PetGrowthActivity.class);
                            intent.putExtra("USERNAME", username);
                            startActivity(intent);
                        }
                    });
                }
            });
        });
    }
}

package com.example.chatpet;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.data.repository.UserRepository;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {

    UserRepository userRepository;
    EditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    DatePicker dpBirthday;
    Button btnRegister;

    // Avatar radio groups
    RadioGroup rgFemales, rgMales;
    RadioButton rbFemaleYellow, rbFemaleWhite, rbFemaleBrown;
    RadioButton rbMaleYellow, rbMaleWhite, rbMaleBrown;

    // Avatar ID constants
    public static final int AVATAR_FEMALE_YELLOW = 0;
    public static final int AVATAR_FEMALE_WHITE = 1;
    public static final int AVATAR_FEMALE_BROWN = 2;
    public static final int AVATAR_MALE_YELLOW = 3;
    public static final int AVATAR_MALE_WHITE = 4;
    public static final int AVATAR_MALE_BROWN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);

        etFullName = findViewById(R.id.etFullName);
        dpBirthday = findViewById(R.id.dpBirthday);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        rgFemales = findViewById(R.id.rgFemales);
        rgMales = findViewById(R.id.rgMales);

        rbFemaleYellow = findViewById(R.id.rbFemaleYellow);
        rbFemaleWhite = findViewById(R.id.rbFemaleWhite);
        rbFemaleBrown = findViewById(R.id.rbFemaleBrown);

        rbMaleYellow = findViewById(R.id.rbMaleYellow);
        rbMaleWhite = findViewById(R.id.rbMaleWhite);
        rbMaleBrown = findViewById(R.id.rbMaleBrown);


        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            // --- Basic validation ---
            if (fullName.isEmpty()) {
                etFullName.setError("Full name is required");
                etFullName.requestFocus();
                return;
            }

            if (username.isEmpty()) {
                etUsername.setError("Username is required");
                etUsername.requestFocus();
                return;
            }

            // Check if username is taken (async)
            userRepository.isUsernameTaken(username, taken -> {
                if (taken) {
                    etUsername.setError("Username is taken");
                    etUsername.requestFocus();
                }
            });

            if (etUsername.getError() != null) {
                return; // Stop here if async check already marked an error
            }

            if (email.isEmpty()) {
                etEmail.setError("Email is required");
                etEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Please enter a valid email");
                etEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                etPassword.setError("Password is required");
                etPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                etPassword.setError("Password must be at least 6 characters");
                etPassword.requestFocus();
                return;
            }

            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Passwords do not match");
                etConfirmPassword.requestFocus();
                return;
            }

            // --- Avatar Selection ---
            int selectedAvatarId = -1;

            if (rbFemaleYellow.isChecked()) selectedAvatarId = AVATAR_FEMALE_YELLOW;
            if (rbFemaleWhite.isChecked()) selectedAvatarId = AVATAR_FEMALE_WHITE;
            if (rbFemaleBrown.isChecked()) selectedAvatarId = AVATAR_FEMALE_BROWN;

            if (rbMaleYellow.isChecked()) selectedAvatarId = AVATAR_MALE_YELLOW;
            if (rbMaleWhite.isChecked()) selectedAvatarId = AVATAR_MALE_WHITE;
            if (rbMaleBrown.isChecked()) selectedAvatarId = AVATAR_MALE_BROWN;

            if (selectedAvatarId == -1) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- Birthday ---
            int day = dpBirthday.getDayOfMonth();
            int month = dpBirthday.getMonth();
            int year = dpBirthday.getYear();

            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);
            Date birthday = cal.getTime();


            userRepository.createUser(username, password, fullName, email, birthday, selectedAvatarId, (userId) -> {
                userRepository.setActiveUserId(userId);
            });

            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

            // --- Go to profile ---
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("FULL_NAME", fullName);
            intent.putExtra("BIRTHDAY", birthday);
            intent.putExtra("AVATAR", selectedAvatarId);
            startActivity(intent);
        });
    }
}

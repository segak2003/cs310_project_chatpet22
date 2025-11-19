package com.example.chatpet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

import com.example.chatpet.data.repository.UserRepository;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    UserRepository userRepository;
    EditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword;
    DatePicker dpBirthday;
    Button btnRegister;
    RadioGroup rgAvatar;
    RadioButton rbFemale, rbMale;

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
        rgAvatar = findViewById(R.id.rgAvatar);
        rbFemale = findViewById(R.id.rbFemale);
        rbMale = findViewById(R.id.rbMale);


        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

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

            userRepository.isUsernameTaken(username, (taken) -> {
                if (taken) {
                    etUsername.setError("Username is taken");
                    etUsername.requestFocus();
                }
            });
            if (etUsername.getError() != null) {
                return;
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

            int selectedAvatarId = rgAvatar.getCheckedRadioButtonId();
            if (selectedAvatarId == -1) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
                return;
            }

            int day = dpBirthday.getDayOfMonth();
            int month = dpBirthday.getMonth();
            int year = dpBirthday.getYear();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            Date birthday = cal.getTime();

            userRepository.createUser(username, password, fullName, birthday, selectedAvatarId, (userId) -> {
                userRepository.setActiveUserId(userId);
            });

            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(this, com.example.chatpet.ProfileActivity.class);

            intent.putExtra("USERNAME", username);
            intent.putExtra("FULL_NAME", fullName);
            intent.putExtra("BIRTHDAY", birthday);
            intent.putExtra("AVATAR", selectedAvatarId);
            startActivity(intent);
        });
    }
}
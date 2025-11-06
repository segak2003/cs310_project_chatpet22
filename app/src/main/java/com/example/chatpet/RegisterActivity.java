package com.example.chatpet;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    EditText etFullName, etBirthday, etUsername, etEmail, etPassword, etConfirmPassword;
    Button btnRegister, btnAvatarFemale, btnAvatarMale;
    String selectedAvatar = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etBirthday = findViewById(R.id.etBirthday);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnAvatarFemale = findViewById(R.id.btnAvatarFemale);
        btnAvatarMale = findViewById(R.id.btnAvatarMale);

        etBirthday.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                        etBirthday.setText(date);
                    }, year, month, day);
            datePickerDialog.show();
        });

        btnAvatarFemale.setOnClickListener(v -> {
            selectedAvatar = "Female";
            btnAvatarFemale.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200));
            btnAvatarMale.setBackgroundColor(ContextCompat.getColor(this, R.color.darker_gray));
        });

        btnAvatarMale.setOnClickListener(v -> {
            selectedAvatar = "Male";
            btnAvatarMale.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200));
            btnAvatarFemale.setBackgroundColor(ContextCompat.getColor(this, R.color.darker_gray));
        });

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String birthday = etBirthday.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (fullName.isEmpty()) {
                etFullName.setError("Full name is required");
                etFullName.requestFocus();
                return;
            }

            if (birthday.isEmpty()) {
                etBirthday.setError("Birthday is required");
                etBirthday.requestFocus();
                return;
            }

            if (username.isEmpty()) {
                etUsername.setError("Username is required");
                etUsername.requestFocus();
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

            if (selectedAvatar.isEmpty()) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, com.example.chatpet.ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("FULL_NAME", fullName);
            intent.putExtra("BIRTHDAY", birthday);
            intent.putExtra("AVATAR", selectedAvatar);
            startActivity(intent);
        });
    }
}

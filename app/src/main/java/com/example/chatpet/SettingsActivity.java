package com.example.chatpet;

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
import androidx.lifecycle.Observer;

import com.example.chatpet.ProfileActivity;
import com.example.chatpet.R;
import com.example.chatpet.data.local.UserEntity;
import com.example.chatpet.data.repository.UserRepository;

import java.util.Calendar;
import java.util.Date;

public class SettingsActivity extends AppCompatActivity {

    UserRepository userRepository;
    UserEntity activeUser;

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    DatePicker dpBirthday;
    Button btnSave;

    // Avatar radio groups
    RadioGroup rgFemales, rgMales;
    RadioButton rbFemaleYellow, rbFemaleWhite, rbFemaleBrown;
    RadioButton rbMaleYellow, rbMaleWhite, rbMaleBrown;

    // Avatar IDs
    public static final int AVATAR_FEMALE_YELLOW = 0;
    public static final int AVATAR_FEMALE_WHITE = 1;
    public static final int AVATAR_FEMALE_BROWN = 2;
    public static final int AVATAR_MALE_YELLOW = 3;
    public static final int AVATAR_MALE_WHITE = 4;
    public static final int AVATAR_MALE_BROWN = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        userRepository = new UserRepository(this);

        // UI references
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        dpBirthday = findViewById(R.id.dpBirthday);
        btnSave = findViewById(R.id.btnSave);

        // Avatar radio buttons
        rgFemales = findViewById(R.id.rgFemales);
        rgMales = findViewById(R.id.rgMales);

        rbFemaleYellow = findViewById(R.id.rbFemaleYellow);
        rbFemaleWhite = findViewById(R.id.rbFemaleWhite);
        rbFemaleBrown = findViewById(R.id.rbFemaleBrown);

        rbMaleYellow = findViewById(R.id.rbMaleYellow);
        rbMaleWhite = findViewById(R.id.rbMaleWhite);
        rbMaleBrown = findViewById(R.id.rbMaleBrown);

        // Load active user
        userRepository.observeActiveUser().observe(this, new Observer<UserEntity>() {
            @Override
            public void onChanged(UserEntity user) {
                if (user == null) return;

                activeUser = user;

                // Fill fields
                etFullName.setText(user.name);
                etEmail.setText(user.username);  // username = email

                // Birthday
                Calendar cal = Calendar.getInstance();
                cal.setTime(user.birthday);
                dpBirthday.updateDate(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                );

                // Avatar
                highlightAvatar(user.avatar);
            }
        });

        // Save Updates
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void highlightAvatar(int avatarId) {
        switch (avatarId) {
            case AVATAR_FEMALE_YELLOW: rbFemaleYellow.setChecked(true); break;
            case AVATAR_FEMALE_WHITE: rbFemaleWhite.setChecked(true); break;
            case AVATAR_FEMALE_BROWN: rbFemaleBrown.setChecked(true); break;
            case AVATAR_MALE_YELLOW: rbMaleYellow.setChecked(true); break;
            case AVATAR_MALE_WHITE: rbMaleWhite.setChecked(true); break;
            case AVATAR_MALE_BROWN: rbMaleBrown.setChecked(true); break;
        }
    }

    private void saveChanges() {
        if (activeUser == null) return;

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        // VALIDATION
        if (fullName.isEmpty()) {
            etFullName.setError("Required");
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email");
            return;
        }

        boolean changingPassword = !password.isEmpty();

        if (changingPassword) {
            if (password.length() < 6) {
                etPassword.setError("At least 6 characters");
                return;
            }
            if (!password.equals(confirm)) {
                etConfirmPassword.setError("Does not match");
                return;
            }
        }

        // AVATAR SELECTED
        int avatarId = -1;

        if (rbFemaleYellow.isChecked()) avatarId = AVATAR_FEMALE_YELLOW;
        if (rbFemaleWhite.isChecked()) avatarId = AVATAR_FEMALE_WHITE;
        if (rbFemaleBrown.isChecked()) avatarId = AVATAR_FEMALE_BROWN;

        if (rbMaleYellow.isChecked()) avatarId = AVATAR_MALE_YELLOW;
        if (rbMaleWhite.isChecked()) avatarId = AVATAR_MALE_WHITE;
        if (rbMaleBrown.isChecked()) avatarId = AVATAR_MALE_BROWN;

        if (avatarId == -1) {
            Toast.makeText(this, "Select an avatar", Toast.LENGTH_SHORT).show();
            return;
        }

        // BIRTHDAY
        int day = dpBirthday.getDayOfMonth();
        int month = dpBirthday.getMonth();
        int year = dpBirthday.getYear();

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Date birthday = cal.getTime();

        // Build updated user object
        UserEntity updated = new UserEntity(
                email,
                changingPassword ? password : activeUser.password,
                fullName,
                birthday,
                avatarId,
                activeUser.createdAt
        );

        // Keep the same primary key
        updated.userId = activeUser.userId;

        // Save to DB
        userRepository.updateUserProfile(updated);

        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();

        // --- Go to PetGrowthActivity ---
        Intent intent = new Intent(this, PetGrowthActivity.class);
        startActivity(intent);
    }
}

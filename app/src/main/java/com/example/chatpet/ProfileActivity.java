package com.example.chatpet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.feature4.PetGrowthActivity;

public class ProfileActivity extends AppCompatActivity {
    EditText etPetName;
    TextView iconPetPreview;
    Button btnLeft, btnRight, btnNext;

    // Pet carousel
    int currentPetIndex = 0;
    String[] petIcons = {"🐈", "🐉"};
    String[] petTypes = {"CAT", "DRAGON"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etPetName = findViewById(R.id.etPetName);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        iconPetPreview = findViewById(R.id.iconPetPreview);
        btnNext = findViewById(R.id.btnNext);

        updatePetPreview();

        btnLeft.setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex - 1 + petIcons.length) % petIcons.length;
            updatePetPreview();
        });

        btnRight.setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex + 1) % petIcons.length;
            updatePetPreview();
        });

        btnNext.setOnClickListener(v -> {
            String petName = etPetName.getText().toString().trim();
            String selectedPet = petTypes[currentPetIndex];

            // Error handling.
            if (petName.isEmpty()) {
                etPetName.setError("Pet name is required");
                etPetName.requestFocus();
                return;
            }

            String message = "Welcome !\nYour pet " + petName + " the " + selectedPet + " is ready!";

            new AlertDialog.Builder(this)
                    .setTitle("Profile Created 🎉")
                    .setMessage(message)
                    .setPositiveButton("Continue", (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileActivity.this, PetGrowthActivity.class);
                        intent.putExtra("PET_TYPE", petTypes[currentPetIndex]);
                        intent.putExtra("PET_NAME", petName);
                        startActivity(intent);
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void updatePetPreview() {
        iconPetPreview.setText(petIcons[currentPetIndex]);
    }
}

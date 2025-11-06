package com.example.chatpet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.feature4.PetGrowthActivity;

public class ProfileActivity extends AppCompatActivity {
    EditText etPreferredName, etPetName;
    ImageView imgPetPreview;
    Button btnLeft, btnRight, btnNext;

    // Pet carousel
    int currentPetIndex = 0;
    int[] petImages = {R.drawable.pibble, R.drawable.applecat, R.drawable.wimyitsdog, R.drawable.bananacat};
    String[] petTypes = {"Pibble", "Apple Cat", "WIMYITS Dog", "Banana Cat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etPreferredName = findViewById(R.id.etPreferredName);
        etPetName = findViewById(R.id.etPetName);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        imgPetPreview = findViewById(R.id.imgPetPreview);
        btnNext = findViewById(R.id.btnNext);

        updatePetPreview();

        btnLeft.setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex - 1 + petImages.length) % petImages.length;
            updatePetPreview();
        });

        btnRight.setOnClickListener(v -> {
            currentPetIndex = (currentPetIndex + 1) % petImages.length;
            updatePetPreview();
        });

        btnNext.setOnClickListener(v -> {
            String preferredName = etPreferredName.getText().toString().trim();
            String petName = etPetName.getText().toString().trim();
            String selectedPet = petTypes[currentPetIndex];

            // Error handling.
            if (preferredName.isEmpty()) {
                etPreferredName.setError("Preferred name is required");
                etPreferredName.requestFocus();
                return;
            }

            if (petName.isEmpty()) {
                etPetName.setError("Pet name is required");
                etPetName.requestFocus();
                return;
            }

            String message = "Welcome " + preferredName + "!\nYour pet " + petName + " the " + selectedPet + " is ready!";

            new AlertDialog.Builder(this)
                    .setTitle("Profile Created 🎉")
                    .setMessage(message)
                    .setPositiveButton("Continue", (dialog, which) -> {
                        dialog.dismiss();
                        Intent intent = new Intent(ProfileActivity.this, PetGrowthActivity.class);
                        startActivity(intent);
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void updatePetPreview() {
        imgPetPreview.setImageResource(petImages[currentPetIndex]);
    }
}

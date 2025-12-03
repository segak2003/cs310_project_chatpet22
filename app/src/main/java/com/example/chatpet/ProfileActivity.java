package com.example.chatpet;

import static com.example.chatpet.Pet.Type.CAT;
import static com.example.chatpet.Pet.Type.DRAGON;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.data.repository.PetRepository;
import com.example.chatpet.feature4.PetGrowthActivity;
import com.example.chatpet.PetInteractionController;

public class ProfileActivity extends AppCompatActivity {
    PetRepository petRepository;
    SharedPreferences prefs;
    private static final String PREFS_NAME = "chatpet_prefs";
    private static final String ACTIVE_USER_KEY = "active_user_id";
    private static final String ACTIVE_PET_KEY = "active_pet_id";

    EditText etPetName;
    TextView iconPetPreview;
    Button btnLeft, btnRight, btnNext;

    // Pet carousel
    int currentPetIndex = 0;
    String[] petIcons = {"🐈", "🐉"};
    String[] petTypes = {"CAT", "DRAGON"};

    private PetInteractionController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        controller = new PetInteractionController(this);


        petRepository = new PetRepository(this);
        prefs = this.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

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

            long activeUserId = prefs.getLong(ACTIVE_USER_KEY, -1L);
            if (activeUserId == -1L) {
                Toast.makeText(this, "No active user", Toast.LENGTH_SHORT).show();
                return;
            }

            petRepository.createPetForUser(activeUserId, petName, selectedPet, (petId) -> {
                petRepository.setActivePetId(petId);
            });

            controller.getPet().name = etPetName.getText().toString().trim();
            if (selectedPet.equals("CAT")) {
                controller.getPet().type = CAT;
            }
            else{
                controller.getPet().type = DRAGON;
            }
            String message = "Welcome!\nYour pet " + petName + " the " + selectedPet + " is ready!";

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

package com.example.chatpet;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatpet.R;

import java.util.Arrays;
import java.util.List;

/**
 * Minimal screen for feeding and sleeping the pet.
 * Expects an Intent extra "EXTRA_PET_ID" to load/update the current pet.
 */
public class InteractActivity extends AppCompatActivity {

    public static final String EXTRA_PET_ID = "EXTRA_PET_ID";

    private Spinner foodSpinner;
    private Button feedButton;
    private EditText minutesInput;
    private Button sleepButton;

    private String petId;

//    Pet pet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interact);

//        pet = new Pet(); // Delete when data connected

        // Fix when data connected
        petId = getIntent().getStringExtra(EXTRA_PET_ID);
        if (petId == null) {
            Toast.makeText(this, "No pet selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        foodSpinner = findViewById(R.id.spinner_food);
        feedButton = findViewById(R.id.button_feed);
        minutesInput = findViewById(R.id.input_minutes);
        sleepButton = findViewById(R.id.button_sleep);

        // Very small menu; you can move this to strings.xml later
        List<String> foods = Arrays.asList("Kibble", "Fish", "Pizza");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, foods
        );
        foodSpinner.setAdapter(adapter);

        feedButton.setOnClickListener(v -> {
            String selectedFood = (String) foodSpinner.getSelectedItem();
            hideKeyboard(v);
//            handleFeed(selectedFood);
        });

        sleepButton.setOnClickListener(v -> {
            hideKeyboard(v);
//            handleSleep();
        });
    }

//    private void handleFeed(String food) {
//        // Load pet (assumes in-memory or DB-backed repo)]
//        if (pet == null) {
//            Toast.makeText(this, "Could not find pet.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        int foodAmount = foodToHungerDelta(food);
//        pet.feed(foodAmount);
//
//        Toast.makeText(this, "Fed " + pet.getName() + " (" + food + ")", Toast.LENGTH_SHORT).show();
//        goBackToChat();
//    }
//
//    private void handleSleep() {
//        String minutesStr = minutesInput.getText().toString().trim();
//        if (TextUtils.isEmpty(minutesStr)) {
//            minutesInput.setError("Enter minutes");
//            return;
//        }
//        int minutes;
//        try {
//            minutes = Integer.parseInt(minutesStr);
//        } catch (NumberFormatException e) {
//            minutesInput.setError("Invalid number");
//            return;
//        }
//        if (minutes <= 0) {
//            minutesInput.setError("Must be > 0");
//            return;
//        }
//
//        if (pet == null) {
//            Toast.makeText(this, "Could not find pet.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        pet.sleep(minutes);
//
//        Toast.makeText(this, "Tucked in " + pet.getName() + " for " + minutes + " min.", Toast.LENGTH_SHORT).show();
//        goBackToChat();
//    }

    private int foodToHungerDelta(String food) {
        // Tiny, hardcoded mapping for now
        switch (food) {
            case "Fish": return 20;
            case "Pizza": return 15;
            case "Kibble":
            default: return 10;
        }
    }

    private void goBackToChat() {
        // Navigate back to the chat page. If ChatPageActivity expects a pet id, pass it.
        Intent intent = new Intent(this, ChatPage.class);
        intent.putExtra(EXTRA_PET_ID, petId);
        // Avoid building a giant back stack if user re-enters this screen
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void hideKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    // --- Static helper to start this Activity ---
    public static Intent buildIntent(Context ctx, String petId) {
        Intent i = new Intent(ctx, InteractActivity.class);
        i.putExtra(EXTRA_PET_ID, petId);
        return i;
    }
}
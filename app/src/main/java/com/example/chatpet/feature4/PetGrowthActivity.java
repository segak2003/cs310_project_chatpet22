package com.example.chatpet.feature4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatpet.R;

/**
 * Legacy wrapper Activity for feature4.
 * Just forwards to the main PetGrowthActivity in the root package.
 */
public class PetGrowthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Forward whatever extras we got (PET_TYPE, PET_NAME, etc.)
        Intent forward = new Intent(
                this,
                com.example.chatpet.PetGrowthActivity.class
        );
        forward.putExtras(getIntent());

        startActivity(forward);
        finish(); // close this wrapper activity
    }
}

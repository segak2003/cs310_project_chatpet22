package com.example.chatpet.feature4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatpet.ChatPage;
import com.example.chatpet.Pet;
import com.example.chatpet.PetInteractionController;
import com.example.chatpet.PetJournalActivity;
import com.example.chatpet.PointManager;
import com.example.chatpet.PointsDelta;
import com.example.chatpet.R;

public class PetGrowthActivity extends AppCompatActivity {

    private PetInteractionController controller;

    private TextView tvEmoji, tvStage, tvPoints, tvReply, tvName, tvDelta;
    private ProgressBar barHunger, barHappiness, barEnergy;
    private Button btnChat, btnFeed, btnTuck, btnJournal, btnSettings;

    private Handler barsHandler = new Handler();
    private Runnable barsRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = new PetInteractionController(this);
        setContentView(R.layout.activity_pet_growth);

        startBarsDecay();

        if (controller.getPet().type == Pet.Type.NONE) {
            Intent startIntent = getIntent();
            String petType = startIntent.getStringExtra("PET_TYPE");
            String petName = startIntent.getStringExtra("PET_NAME");

            controller.getPet().type = Pet.Type.valueOf(petType);
            controller.getPet().name = petName;
        }

        tvEmoji = findViewById(R.id.tvEmoji);
        tvName = findViewById(R.id.tvName);
        tvStage = findViewById(R.id.tvStage);
        tvPoints = findViewById(R.id.tvPoints);
        tvReply = findViewById(R.id.tvReply);
        tvDelta = findViewById(R.id.tvDelta);

        barHunger = findViewById(R.id.barHunger);
        barHappiness = findViewById(R.id.barHappiness);
        barEnergy = findViewById(R.id.barEnergy);

        btnChat = findViewById(R.id.btnChat);
        btnFeed = findViewById(R.id.btnFeed);
        btnTuck = findViewById(R.id.btnTuck);
        btnJournal = findViewById(R.id.btnJournal);
        btnSettings = findViewById(R.id.btnSettings);

        btnChat.setOnClickListener(v -> {handleInteraction(PointManager.InteractionType.CHAT);
        Intent intent = new Intent(PetGrowthActivity.this, ChatPage.class);
        startActivity(intent);
        tvReply.setText("That was a great conversation!");});
        btnJournal.setOnClickListener(v -> {Intent intent = new Intent(PetGrowthActivity.this, PetJournalActivity.class);
            startActivity(intent);});
        btnFeed.setOnClickListener(v -> {handleInteraction(PointManager.InteractionType.FEED);
            tvReply.setText("Yum yum!");
    }   );
        btnTuck.setOnClickListener(v -> {
            handleInteraction(PointManager.InteractionType.TUCK);
            tvReply.setText("Zzz");
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(PetGrowthActivity.this, com.example.chatpet.SettingsActivity.class);
            startActivity(intent);
        });

        refreshUI();
    }

    private void handleInteraction(PointManager.InteractionType type) {
        PointsDelta d;
        if (type == PointManager.InteractionType.CHAT) d = controller.onChatCompleted();
        else if (type == PointManager.InteractionType.FEED) d = controller.onFeedCompleted();
        else d = controller.onTuckCompleted();

        tvReply.setText(controller.replyFor(type));
        showDeltaAnimation("+" + d.delta + " pts");

        if (d.leveledUp) {
            Toast.makeText(this,
                    "LEVEL UP! " + d.fromStage + " → " + d.toStage,
                    Toast.LENGTH_LONG).show();
        }
        refreshUI();
    }

    private void refreshUI() {
        Pet p = controller.getPet();
        tvEmoji.setText(p.spriteEmoji());
        tvName.setText(p.name + " the " + p.type);
        tvStage.setText("Stage: " + p.stage + "  (Lv " + p.level + ")");
        tvPoints.setText("Points: " + p.points);

        barHunger.setProgress(p.hunger);
        barHappiness.setProgress(p.happiness);
        barEnergy.setProgress(p.energy);
    }

    private void showDeltaAnimation(String text) {
        tvDelta.setText(text);
        tvDelta.setAlpha(0f);
        tvDelta.setTranslationY(40f);
        tvDelta.setVisibility(View.VISIBLE);
        tvDelta.animate()
                .alpha(1f)
                .translationYBy(-40f)
                .setDuration(500)
                .withEndAction(() -> tvDelta.animate()
                        .alpha(0f)
                        .setDuration(400)
                        .start())
                .start();
    }

    private void startBarsDecay() {
        barsRunnable = new Runnable() {
            @Override
            public void run() {
                decreasePoints(controller.getPet());
                // Schedule again after 30 seconds
                barsHandler.postDelayed(this, 30_000);
            }
        };

        // Start the first execution
        barsHandler.postDelayed(barsRunnable, 30_000);
    }

    private void decreasePoints(Pet pet) {
        // Decrease points safely
        if (pet.hunger > 14) {
            pet.hunger -= 15;
        }
        else if(pet.hunger > 0){
            pet.hunger--;
        }

        if (pet.energy > 14) {
            pet.energy -= 15;
        }
        else if(pet.energy > 0){
            pet.energy--;
        }
        if (pet.happiness > 14) {
            pet.happiness -= 15;
        }
        else if(pet.happiness > 0){
            pet.happiness--;
        }

        if(pet.happiness == 0 || pet.energy == 0 || pet.hunger == 0){
            if (pet.points > 4){
                pet.points -= 5;
            }
        }

        // Optional: update UI
        // For example, update a TextView showing points
        // pointsTextView.setText(String.valueOf(pet.points));
        runOnUiThread(this::refreshUI);
    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        barsHandler.removeCallbacks(barsRunnable);
    }
}

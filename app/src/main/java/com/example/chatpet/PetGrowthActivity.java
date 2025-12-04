package com.example.chatpet;


import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class PetGrowthActivity extends AppCompatActivity {


    private PetInteractionController controller;


    private TextView tvEmoji, tvStage, tvPoints, tvReply, tvName, tvDelta;
    private ProgressBar barHunger, barHappiness, barEnergy;


    // main action buttons
    private Button btnChat, btnFeed, btnTuck, btnJournal, btnSettings;


    // layouts
    private LinearLayout layoutMainButtons;
    private LinearLayout layoutMainButtonsTwo;
    private LinearLayout layoutFeedChoices;


    // food buttons
    private Button btnFoodKibble, btnFoodFish, btnFoodPizza;


    // reset button
    private Button btnReset;


    // periodic decay
    private Handler barsHandler = new Handler();
    private Runnable barsRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = new PetInteractionController(this);
        setContentView(R.layout.activity_pet_growth);


        // If pet not initialized yet, read from Intent
        if (controller.getPet().type == Pet.Type.NONE) {
            Intent startIntent = getIntent();
            String petType = startIntent.getStringExtra("PET_TYPE");
            String petName = startIntent.getStringExtra("PET_NAME");


            if (petType != null) {
                controller.getPet().type = Pet.Type.valueOf(petType);
            }
            if (petName != null) {
                controller.getPet().name = petName;
            }
        }


        // text + bars
        tvEmoji = findViewById(R.id.tvEmoji);
        tvName = findViewById(R.id.tvName);
        tvStage = findViewById(R.id.tvStage);
        tvPoints = findViewById(R.id.tvPoints);
        tvReply = findViewById(R.id.tvReply);
        tvDelta = findViewById(R.id.tvDelta);


        barHunger = findViewById(R.id.barHunger);
        barHappiness = findViewById(R.id.barHappiness);
        barEnergy = findViewById(R.id.barEnergy);


        // layouts
        layoutMainButtons = findViewById(R.id.layoutMainButtons);
        layoutMainButtonsTwo = findViewById(R.id.layoutMainButtonsTwo);
        layoutFeedChoices = findViewById(R.id.layoutFeedChoices);
        layoutFeedChoices.setVisibility(View.GONE);

        // main buttons
        btnChat = findViewById(R.id.btnChat);
        btnFeed = findViewById(R.id.btnFeed);
        btnTuck = findViewById(R.id.btnTuck);
        btnJournal = findViewById(R.id.btnJournal);
        btnSettings = findViewById(R.id.btnSettings);


        // food buttons
        btnFoodKibble = findViewById(R.id.btnFoodKibble);
        btnFoodFish = findViewById(R.id.btnFoodFish);
        btnFoodPizza = findViewById(R.id.btnFoodPizza);


        // reset button
        btnReset = findViewById(R.id.btnReset);


        // --- LISTENERS ---


        // Chat: apply interaction + open ChatPage
        btnChat.setOnClickListener(v -> {
            // Can't chat if energy is too low
            if (controller.getPet().energy <= 5) {
                Toast.makeText(this, "Too tired to chat!", Toast.LENGTH_SHORT).show();
                tvReply.setText("I'm too tired to talk... zZz");
            } else {
                handleInteraction(PointManager.InteractionType.CHAT);
                Intent intent = new Intent(PetGrowthActivity.this, ChatPage.class);
                startActivity(intent);
            }
        });


        // Tuck in
        btnTuck.setOnClickListener(v ->
                handleInteraction(PointManager.InteractionType.TUCK));

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(com.example.chatpet.PetGrowthActivity.this, com.example.chatpet.SettingsActivity.class);
            startActivity(intent);
        });

        // Feed: show food choices row, hide main buttons & journal
        btnFeed.setOnClickListener(v -> {
            if (controller.getPet().hunger >= 100) {
                Toast.makeText(this, "Your pet is already full!", Toast.LENGTH_SHORT).show();
                tvReply.setText("I'm stuffed... I can't eat anymore!");
                return;
            }
            layoutMainButtons.setVisibility(View.GONE);
            layoutMainButtonsTwo.setVisibility(View.GONE);
            btnJournal.setVisibility(View.GONE);
            layoutFeedChoices.setVisibility(View.VISIBLE);
            tvReply.setText("What should I eat? 😋");
        });


        // food selection interactions
        btnFoodKibble.setOnClickListener(v -> {
            tvReply.setText("Yum! Kibble was tasty! 🍖");
            handleInteraction(PointManager.InteractionType.FEED);
            restoreMainButtons();
        });


        btnFoodFish.setOnClickListener(v -> {
            tvReply.setText("Mmm, fresh fish! 🐟");
            handleInteraction(PointManager.InteractionType.FEED);
            restoreMainButtons();
        });


        btnFoodPizza.setOnClickListener(v -> {
            tvReply.setText("Pizza time! 🍕");
            handleInteraction(PointManager.InteractionType.FEED);
            restoreMainButtons();
        });


        // Journal button -> PetJournalActivity
        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(PetGrowthActivity.this, PetJournalActivity.class);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(PetGrowthActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


        // reset button listener
        btnReset.setEnabled(false);
        btnReset.setAlpha(0.4f);


        // apply offline decay since last session
        controller.applyTimeDecay();


        // start periodic bar + points decay while activity is running
        startBarsDecay();


        // initial UI
        refreshUI();
    }


    private void handleInteraction(PointManager.InteractionType type) {
        Pet current = controller.getPet();


        // Guardrails based on meters
        if (type == PointManager.InteractionType.TUCK && current.energy >= 100) {
            Toast.makeText(this, "Your pet is fully rested!", Toast.LENGTH_SHORT).show();
            tvReply.setText("I'm already fully rested! ⚡");
            return;
        }


        if (type == PointManager.InteractionType.TUCK) {
            startSleepPause();
        }


        PointsDelta d;
        if (type == PointManager.InteractionType.CHAT) {
            d = controller.onChatCompleted();
        } else if (type == PointManager.InteractionType.FEED) {
            d = controller.onFeedCompleted();
        } else {
            d = controller.onTuckCompleted();
        }


        if (type == PointManager.InteractionType.CHAT) {
            tvReply.setText(controller.replyFor(type));
        }


        showDeltaAnimation("+" + d.delta + " pts");


        if (d.leveledUp) {
            Toast.makeText(this,
                    "LEVEL UP! " + d.fromStage + " → " + d.toStage,
                    Toast.LENGTH_LONG).show();
        }


        refreshUI();


    }


    private void startSleepPause() {
        btnChat.setEnabled(false);
        btnFeed.setEnabled(false);
        btnTuck.setEnabled(false);
        layoutFeedChoices.setEnabled(false);


        tvReply.setText("Your pet is sleeping... 😴");


        tvEmoji.postDelayed(() -> {
            btnChat.setEnabled(true);
            btnFeed.setEnabled(true);
            btnTuck.setEnabled(true);
            layoutFeedChoices.setEnabled(true);


            tvReply.setText("All rested up and ready! 💪");
        }, 3000);
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


    private void restoreMainButtons() {
        layoutMainButtons.setVisibility(View.VISIBLE);
        layoutMainButtonsTwo.setVisibility(View.VISIBLE);
        layoutFeedChoices.setVisibility(View.GONE);
        btnJournal.setVisibility(View.VISIBLE);
    }


    // ===== periodic decay: meters + points =====
    private void startBarsDecay() {
        barsRunnable = new Runnable() {
            @Override
            public void run() {
                decreasePoints(controller.getPet());
                // Schedule again after 2 minutes
                barsHandler.postDelayed(this, 6_000);
            }
        };


        // Run once after 12 seconds, then every 6 seconds
        barsHandler.postDelayed(barsRunnable, 12_000);
    }


    private void decreasePoints(Pet pet) {
        // Gentle meter decay
        if (pet.hunger > 0) {
            pet.hunger -= 2;
        }


        if (pet.energy > 0) {
            pet.energy -= 2;
        }


        if (pet.happiness > 0) {
            pet.happiness -= 2;
        }


        // Re-check level in case losing points should affect stage/level
        PointManager.checkLevelUp(pet);


        runOnUiThread(this::refreshUI);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        barsHandler.removeCallbacks(barsRunnable);
    }
}
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

    // main action buttons (Feed removed from here)
    private Button btnChat, btnTuck, btnJournal;

    // layouts
    private LinearLayout layoutMainButtons;
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

        // If pet not initialized yet, read from Intent (pet type + name)
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

        // --- find views ---

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
        layoutFeedChoices = findViewById(R.id.layoutFeedChoices);

        // main buttons (note: no btnFeed here anymore)
        btnChat = findViewById(R.id.btnChat);
        btnTuck = findViewById(R.id.btnTuck);
        btnJournal = findViewById(R.id.btnJournal);

        // food buttons (always visible row)
        btnFoodKibble = findViewById(R.id.btnFoodKibble);
        btnFoodFish = findViewById(R.id.btnFoodFish);
        btnFoodPizza = findViewById(R.id.btnFoodPizza);

        // set food labels based on pet type (cat vs dragon)
        setupFoodButtonsForPetType();

        // reset button (dev only)
        btnReset = findViewById(R.id.btnReset);

        // --- listeners ---

        // Chat: apply interaction + open ChatPage
        btnChat.setOnClickListener(v -> {
            handleInteraction(PointManager.InteractionType.CHAT);
            Intent intent = new Intent(PetGrowthActivity.this, ChatPage.class);
            startActivity(intent);
        });

        // Tuck in
        btnTuck.setOnClickListener(v ->
                handleInteraction(PointManager.InteractionType.TUCK));

        // Food selection interactions: directly feed
        btnFoodKibble.setOnClickListener(v ->
                handleInteraction(PointManager.InteractionType.FEED));

        btnFoodFish.setOnClickListener(v ->
                handleInteraction(PointManager.InteractionType.FEED));

        btnFoodPizza.setOnClickListener(v ->
                handleInteraction(PointManager.InteractionType.FEED));

        // Journal button -> PetJournalActivity
        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(PetGrowthActivity.this, PetJournalActivity.class);
            startActivity(intent);
        });

        // reset button disabled in production
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
        if (type == PointManager.InteractionType.FEED && current.hunger >= 100) {
            Toast.makeText(this, "Your pet is already full!", Toast.LENGTH_SHORT).show();
            tvReply.setText("I'm stuffed... I can't eat anymore!");
            return;
        }

        if (type == PointManager.InteractionType.TUCK && current.energy >= 100) {
            Toast.makeText(this, "Your pet is fully rested!", Toast.LENGTH_SHORT).show();
            tvReply.setText("I'm already fully rested! ⚡");
            return;
        }

        // Can't chat if energy is too low
        if (type == PointManager.InteractionType.CHAT && current.energy <= 5) {
            Toast.makeText(this, "Too tired to chat!", Toast.LENGTH_SHORT).show();
            tvReply.setText("I'm too tired to talk... zZz");
            return;
        }

        PointsDelta d;
        if (type == PointManager.InteractionType.CHAT) {
            d = controller.onChatCompleted();
        } else if (type == PointManager.InteractionType.FEED) {
            d = controller.onFeedCompleted();
        } else {
            d = controller.onTuckCompleted();
        }

        // Use stage-based replies from PetInteractionController for all 3 types
        tvReply.setText(controller.replyFor(type));

        if (d.delta > 0) {
            showDeltaAnimation("+" + d.delta + " pts");
        }

        if (d.leveledUp) {
            Toast.makeText(this,
                    "LEVEL UP! " + d.fromStage + " → " + d.toStage,
                    Toast.LENGTH_LONG).show();
        }

        refreshUI();

        if (type == PointManager.InteractionType.TUCK) {
            startSleepPause();
        }
    }

    private void setupFoodButtonsForPetType() {
        Pet pet = controller.getPet();

        if (pet.type == Pet.Type.DRAGON) {
            // Dragon pet foods
            btnFoodKibble.setText("Rice 🍚");
            btnFoodFish.setText("Pepper 🌶️");
            btnFoodPizza.setText("Steak 🥩");
        }
        else  {
            // cat pet foods
            btnFoodKibble.setText("Kibble 🍖");
            btnFoodFish.setText("Fish 🐟");
            btnFoodPizza.setText("Pizza 🍕");
        }
    }

    private void startSleepPause() {
        btnChat.setEnabled(false);
        btnTuck.setEnabled(false);
        layoutFeedChoices.setEnabled(false);

        tvReply.setText("Your pet is sleeping... 😴");

        tvEmoji.postDelayed(() -> {
            btnChat.setEnabled(true);
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

    // ===== periodic decay: meters + points =====
    private void startBarsDecay() {
        barsRunnable = new Runnable() {
            @Override
            public void run() {
                decreasePoints(controller.getPet());
                // Schedule again after 2 minutes
                barsHandler.postDelayed(this, 60_000);
            }
        };

        // Run once immediately, then every 2 minutes
        barsHandler.post(barsRunnable);
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

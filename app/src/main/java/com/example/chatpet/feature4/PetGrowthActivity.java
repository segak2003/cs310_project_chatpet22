package com.example.chatpet.feature4;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatpet.R;

public class PetGrowthActivity extends AppCompatActivity {

    private PetInteractionController controller;

    private TextView tvEmoji, tvStage, tvPoints, tvReply, tvName, tvDelta;
    private ProgressBar barHunger, barHappiness, barEnergy;

    // main action buttons
    private Button btnChat, btnFeed, btnTuck;

    // new food choice layouts + buttons
    private LinearLayout layoutMainButtons;
    private LinearLayout layoutFeedChoices;
    private LinearLayout layoutFeedCancelRow;
    private Button btnFoodKibble, btnFoodFish, btnFoodPizza, btnFoodCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controller = new PetInteractionController(this);
        setContentView(R.layout.activity_pet_growth);

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
        layoutFeedCancelRow = findViewById(R.id.layoutFeedCancelRow);

        // main buttons
        btnChat = findViewById(R.id.btnChat);
        btnFeed = findViewById(R.id.btnFeed);
        btnTuck = findViewById(R.id.btnTuck);

        // food buttons
        btnFoodKibble = findViewById(R.id.btnFoodKibble);
        btnFoodFish = findViewById(R.id.btnFoodFish);
        btnFoodPizza = findViewById(R.id.btnFoodPizza);
        btnFoodCancel = findViewById(R.id.btnFoodCancel);

        // === listeners ===

        // chat and tuck stay the same
        btnChat.setOnClickListener(v -> handleInteraction(PointManager.InteractionType.CHAT));

        btnTuck.setOnClickListener(v -> handleInteraction(PointManager.InteractionType.TUCK));

        // feed now opens the food selection row
        btnFeed.setOnClickListener(v -> {
            layoutMainButtons.setVisibility(View.GONE);
            layoutFeedChoices.setVisibility(View.VISIBLE);
            layoutFeedCancelRow.setVisibility(View.VISIBLE);
            tvReply.setText("What should I eat? 😋");
        });

        // each food option counts as a FEED interaction
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

        btnFoodCancel.setOnClickListener(v -> {
            tvReply.setText("Maybe later… 😅");
            restoreMainButtons();
        });

        refreshUI();
    }

    private void handleInteraction(PointManager.InteractionType type) {
        Pet current = controller.getPet();

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

        PointsDelta d;
        if (type == PointManager.InteractionType.CHAT) {
            d = controller.onChatCompleted();
        } else if (type == PointManager.InteractionType.FEED) {
            d = controller.onFeedCompleted();
        } else {
            d = controller.onTuckCompleted();
        }

        // reply text is set in the callers for FEED, but this is fine for CHAT/TUCK
        if (type == PointManager.InteractionType.CHAT ||
                type == PointManager.InteractionType.TUCK) {
            tvReply.setText(controller.replyFor(type));
        }

        showDeltaAnimation("+" + d.delta + " pts");

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


    private void startSleepPause() {
        // disable all buttons during "sleep"
        btnChat.setEnabled(false);
        btnFeed.setEnabled(false);
        btnTuck.setEnabled(false);

        layoutFeedChoices.setEnabled(false);
        layoutFeedCancelRow.setEnabled(false);

        tvReply.setText("Your pet is sleeping... 😴");

        // Re-enable after 3 seconds
        tvEmoji.postDelayed(() -> {
            btnChat.setEnabled(true);
            btnFeed.setEnabled(true);
            btnTuck.setEnabled(true);

            layoutFeedChoices.setEnabled(true);
            layoutFeedCancelRow.setEnabled(true);

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
        layoutFeedChoices.setVisibility(View.GONE);
        layoutFeedCancelRow.setVisibility(View.GONE);
    }
}

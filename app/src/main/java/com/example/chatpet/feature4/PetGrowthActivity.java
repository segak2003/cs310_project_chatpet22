package com.example.chatpet.feature4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatpet.ChatPage;
import com.example.chatpet.PetJournalActivity;
import com.example.chatpet.R;

public class PetGrowthActivity extends AppCompatActivity {

    private PetInteractionController controller;

    private TextView tvEmoji, tvStage, tvPoints, tvReply, tvName, tvDelta;
    private ProgressBar barHunger, barHappiness, barEnergy;
    private Button btnChat, btnFeed, btnTuck, btnJournal;

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

        btnChat = findViewById(R.id.btnChat);
        btnFeed = findViewById(R.id.btnFeed);
        btnTuck = findViewById(R.id.btnTuck);
        btnJournal = findViewById(R.id.btnJournal);

        btnChat.setOnClickListener(v -> {handleInteraction(PointManager.InteractionType.CHAT);
        Intent intent = new Intent(PetGrowthActivity.this, ChatPage.class);
        startActivity(intent);});
        btnJournal.setOnClickListener(v -> {Intent intent = new Intent(PetGrowthActivity.this, PetJournalActivity.class);
            startActivity(intent);});
        btnFeed.setOnClickListener(v -> handleInteraction(PointManager.InteractionType.FEED));
        btnTuck.setOnClickListener(v -> handleInteraction(PointManager.InteractionType.TUCK));

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
}

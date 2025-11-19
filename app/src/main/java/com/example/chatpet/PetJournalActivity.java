package com.example.chatpet;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Button;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.JournalEntryDao;
import com.example.chatpet.data.local.JournalEntryEntity;
import com.example.chatpet.data.local.PetDao;
import com.example.chatpet.data.local.PetEntity;
import com.example.chatpet.data.local.UserDao;
import com.example.chatpet.data.local.UserEntity;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PetJournalActivity extends AppCompatActivity {

    private ExecutorService dbExecutor;
    private JournalEntryDao journalDao;
    private LinearLayout journalHistoryContainer;
    private TextView currentEntryDisplay;
    private Button refreshBtn;

    // TODO: Get this from SharedPreferences or Intent extras later
    private static final long PET_ID = 1L;

    private Random randomGenerator; // I like having this as a field for reuse

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_journal);

        // Initialize views - probably should extract this to a separate method
        initializeViews();

        // Set up database stuff
        setupDatabase();

        // Load the journal entries
        loadJournalEntries();

        // Set up button click listener
        refreshBtn.setOnClickListener(v -> {
            // Generate a new entry when button is pressed
            generateNewJournalEntry();
        });
    }

    private void initializeViews() {
        journalHistoryContainer = findViewById(R.id.journal_history_container);
        currentEntryDisplay = findViewById(R.id.current_entry_display);
        refreshBtn = findViewById(R.id.refresh_btn);
        Button backBtn = findViewById(R.id.back_btn);

        randomGenerator = new Random();
        backBtn.setOnClickListener(v -> finish());
    }

    private void setupDatabase() {
        // Using single thread executor for database operations
        dbExecutor = Executors.newSingleThreadExecutor();

        ChatPetDatabase database = ChatPetDatabase.getInstance(this);
        journalDao = database.journalEntryDao();
    }

    private void loadJournalEntries() {
        dbExecutor.execute(() -> {
            List<JournalEntryEntity> entries = journalDao.getByPetId(PET_ID);

            if (entries.isEmpty()) {
                // First time user - let's create a welcome entry
                JournalEntryEntity welcomeEntry = createWelcomeEntry();

                try {
                    journalDao.insert(welcomeEntry);
                    entries.add(welcomeEntry);
                } catch (android.database.sqlite.SQLiteConstraintException e) {
                    // Pet doesn't exist yet, create defaults
                    createDefaultUserAndPet();
                    // Try inserting the welcome entry again
                    journalDao.insert(welcomeEntry);
                    entries.add(welcomeEntry);
                }
            }

            // Update UI on main thread
            runOnUiThread(() -> {
                displayAllEntries(entries);
                if (!entries.isEmpty()) {
                    JournalEntryEntity latestEntry = entries.get(entries.size() - 1);
                    showCurrentEntry(latestEntry);
                }
            });
        });
    }

    private void createDefaultUserAndPet() {
        ChatPetDatabase database = ChatPetDatabase.getInstance(this);

        // First create a default user (pet needs a user to exist)
        UserDao userDao = database.userDao();
        UserEntity defaultUser = new UserEntity(
                "GuestUser",                 // username
                "default_password",          // password (not used for this default)
                "Guest Jones",
                new Date(System.currentTimeMillis()),
                0,
                System.currentTimeMillis()   // createdAt
        );
        long userId = userDao.insert(defaultUser); // This returns the auto-generated user_id

        // Then create a default pet
        PetDao petDao = database.petDao();
        PetEntity defaultPet = new PetEntity(
                userId,                      // userId (from the insert above)
                "Buddy",                     // name
                "dog",                       // animal
                1,                           // level
                0,
                50,                          // happiness
                50,                          // hunger
                50,                          // energy
                "default_personality",       // personalitySeed
                System.currentTimeMillis()   // createdAt
        );
        petDao.insert(defaultPet);
    }

    private void generateNewJournalEntry() {
        dbExecutor.execute(() -> {
            JournalEntryEntity newEntry = createRandomEntry();
            journalDao.insert(newEntry);

            // Refresh the display
            List<JournalEntryEntity> updatedEntries = journalDao.getByPetId(PET_ID);

            runOnUiThread(() -> {
                displayAllEntries(updatedEntries);
                showCurrentEntry(newEntry);
            });
        });
    }

    private JournalEntryEntity createWelcomeEntry() {
        String welcomeMessage = "Welcome to your pet journal! 🎉 I'm so excited to start documenting our adventures together. This is where I'll share my thoughts and feelings about our time spent together!";
        return new JournalEntryEntity(PET_ID, "My First Journal Entry", welcomeMessage, System.currentTimeMillis());
    }

    private JournalEntryEntity createRandomEntry() {
        // Some variety in the journal entries - maybe I should move this to a separate class later
        String[] activities = {
                "went for an amazing walk", "played with my favorite toy", "learned something new",
                "got belly rubs", "discovered a new smell", "had a delicious treat",
                "took a long nap in the sunshine", "watched the birds outside"
        };

        String[] emotions = {
                "I felt so happy", "It made me excited", "I was curious about everything",
                "I felt so loved", "It was the best part of my day", "I couldn't stop wagging my tail"
        };

        String activity = activities[randomGenerator.nextInt(activities.length)];
        String emotion = emotions[randomGenerator.nextInt(emotions.length)];

        String content = "Today " + activity + "! " + emotion + ". I love spending time with my human! 🐾";

        return new JournalEntryEntity(PET_ID, "Today's Adventure", content, System.currentTimeMillis());
    }

    private void showCurrentEntry(JournalEntryEntity entry) {
        if (entry != null) {
            String formattedDate = formatDate(entry.createdAt);
            String displayText = entry.title + "\n\n" + entry.content + "\n\n— " + formattedDate;
            currentEntryDisplay.setText(displayText);
        }
    }

    private void displayAllEntries(List<JournalEntryEntity> entries) {
        journalHistoryContainer.removeAllViews();

        // Show entries in reverse order (newest first in history)
        for (int i = entries.size() - 1; i >= 0; i--) {
            JournalEntryEntity entry = entries.get(i);
            TextView entryView = createEntryView(entry);
            journalHistoryContainer.addView(entryView);
        }
    }

    private TextView createEntryView(JournalEntryEntity entry) {
        TextView entryView = new TextView(this);

        String formattedDate = formatDate(entry.createdAt);
        String entryText = entry.title + "\n" + entry.content + "\n\n📅 " + formattedDate;

        entryView.setText(entryText);
        entryView.setPadding(20, 16, 20, 16);
        entryView.setTextSize(14f);
        entryView.setLineSpacing(4f, 1.0f); // Better readability

        // Add some spacing between entries
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        entryView.setLayoutParams(params);

        // Make it clickable and show in top display when clicked
        entryView.setClickable(true);
        entryView.setFocusable(true);

        // Use TypedValue to get the theme attribute properly
        android.util.TypedValue outValue = new android.util.TypedValue();
        getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        entryView.setBackgroundResource(outValue.resourceId);

        entryView.setOnClickListener(v -> showCurrentEntry(entry));

        return entryView;
    }

    // Helper method to format timestamps nicely
    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbExecutor != null) {
            dbExecutor.shutdown(); // Always clean up resources!
        }
    }
}
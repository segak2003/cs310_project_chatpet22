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
    private PetDao petDao;
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
        petDao = database.petDao();
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
        // Get pet stats to generate dynamic content
        PetEntity pet = petDao.getById(PET_ID);

        String content;
        String title;

        if (pet != null) {
            int hunger = pet.hunger;
            int happiness = pet.happiness;
            int energy = pet.energy;

            // Generate content based on pet levels
            content = generateContentBasedOnStats(hunger, happiness, energy);
            title = generateTitleBasedOnStats(hunger, happiness, energy);
        } else {
            // Fallback if pet not found
            content = "Today was an interesting day! I'm looking forward to more adventures! 🐾";
            title = "Today's Adventure";
        }

        return new JournalEntryEntity(PET_ID, title, content, System.currentTimeMillis());
    }

    private String generateContentBasedOnStats(int hunger, int happiness, int energy) {
        StringBuilder content = new StringBuilder();

        // Hunger-based content
        if (hunger < 30) {
            String[] hungryMessages = {
                    "I'm feeling quite hungry today. My tummy is rumbling and I keep thinking about food.",
                    "I could really use a good meal right now. Everything smells so delicious!",
                    "My stomach feels so empty. I hope my human remembers to feed me soon."
            };
            content.append(hungryMessages[randomGenerator.nextInt(hungryMessages.length)]);
            if (happiness < 30) {
                content.append(" Being hungry is making me feel a bit sad.");
            }
        } else if (hunger < 70) {
            String[] normalMessages = {
                    "I had a decent meal today. Feeling pretty satisfied!",
                    "My appetite is just right. Not too hungry, not too full.",
                    "Food was good today. I'm content with what I ate."
            };
            content.append(normalMessages[randomGenerator.nextInt(normalMessages.length)]);
        } else {
            String[] fullMessages = {
                    "I'm so full and satisfied! That meal was absolutely delicious!",
                    "My belly is happy and full. I couldn't eat another bite!",
                    "I ate so well today! Feeling completely satisfied and content."
            };
            content.append(fullMessages[randomGenerator.nextInt(fullMessages.length)]);
        }

        content.append(" ");

        // Energy-based content
        if (energy < 30) {
            String[] tiredMessages = {
                    "I'm feeling so tired and sleepy. All I want to do is curl up and rest.",
                    "My energy is really low today. I need a good long nap.",
                    "I'm exhausted and can barely keep my eyes open. Time for sleep!"
            };
            content.append(tiredMessages[randomGenerator.nextInt(tiredMessages.length)]);
            if (happiness < 30) {
                content.append(" I don't even have the energy to be happy right now.");
            }
        } else if (energy < 70) {
            String[] normalMessages = {
                    "My energy level feels just right for some light activities.",
                    "I'm feeling moderately energetic. Ready for some gentle play!",
                    "I have enough energy to enjoy the day without overdoing it."
            };
            content.append(normalMessages[randomGenerator.nextInt(normalMessages.length)]);
        } else {
            String[] energeticMessages = {
                    "I'm bursting with energy! I want to run and play all day long!",
                    "I feel so energetic and alive! Let's go on an adventure!",
                    "I'm full of energy and ready for anything! Let's play!"
            };
            content.append(energeticMessages[randomGenerator.nextInt(energeticMessages.length)]);
        }

        content.append(" ");

        // Happiness-based content
        if (happiness < 30) {
            String[] sadMessages = {
                    "I'm feeling a bit down today. I could use some extra love and attention.",
                    "I'm not my usual cheerful self. Maybe some playtime would help.",
                    "I'm feeling sad and lonely. I hope things get better soon."
            };
            content.append(sadMessages[randomGenerator.nextInt(sadMessages.length)]);
        } else if (happiness < 70) {
            String[] contentMessages = {
                    "I'm feeling okay today. Life is pretty good!",
                    "I'm in a decent mood. Nothing special, but I'm content.",
                    "I'm feeling alright. Just a normal, peaceful day."
            };
            content.append(contentMessages[randomGenerator.nextInt(contentMessages.length)]);
        } else {
            String[] happyMessages = {
                    "I'm so incredibly happy! Life is wonderful and I love my human so much!",
                    "I'm filled with joy and happiness! Every moment is a blessing!",
                    "I'm the happiest pet in the world! I love everything about today!"
            };
            content.append(happyMessages[randomGenerator.nextInt(happyMessages.length)]);
        }

        content.append(" 🐾");

        return content.toString();
    }

    private String generateTitleBasedOnStats(int hunger, int happiness, int energy) {
        // Generate title based on overall state
        int avgStat = (hunger + happiness + energy) / 3;

        if (avgStat < 40) {
            String[] titles = {"A Tough Day", "Feeling Low", "Need Some Care", "Not My Best Day"};
            return titles[randomGenerator.nextInt(titles.length)];
        } else if (avgStat < 70) {
            String[] titles = {"Today's Adventure", "A Normal Day", "Daily Thoughts", "Just Another Day"};
            return titles[randomGenerator.nextInt(titles.length)];
        } else {
            String[] titles = {"Best Day Ever!", "Feeling Amazing!", "Life is Great!", "So Happy Today!"};
            return titles[randomGenerator.nextInt(titles.length)];
        }
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
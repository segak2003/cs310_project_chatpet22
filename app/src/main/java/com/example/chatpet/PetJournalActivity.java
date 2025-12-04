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
                "example@email.com",
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
            int level = pet.level;
            String animal = pet.animal;

            // Generate content based on pet levels
            content = generateContentBasedOnStats(hunger, happiness, energy, level, animal);
            title = generateTitleBasedOnStats(hunger, happiness, energy, level, animal);
        } else {
            // Fallback if pet not found
            content = "Today was an interesting day! I'm looking forward to more adventures! 🐾";
            title = "Today's Adventure";
        }

        return new JournalEntryEntity(PET_ID, title, content, System.currentTimeMillis());
    }

    // Public 3-parameter version for tests (uses defaults)
    String generateContentBasedOnStats(int hunger, int happiness, int energy) {
        return generateContentBasedOnStats(hunger, happiness, energy, 1, "pet");
    }

    // Internal 5-parameter version with level and type
    private String generateContentBasedOnStats(int hunger, int happiness, int energy, int level, String petType) {
        StringBuilder content = new StringBuilder();

        // Hunger-based content
        if (hunger < 30) {
            String[] hungryMessages = {
                    "I'm feeling quite hungry today. My tummy is rumbling and I keep thinking about food.",
                    "I could really use a good meal right now. Everything smells so delicious!",
                    "My stomach feels so empty. I hope my human remembers to feed me soon.",
                    "I'm so hungry I can barely focus on anything else.",
                    "The hunger pangs are really getting to me today.",
                    "I keep looking at my empty food bowl with longing eyes."
            };
            content.append(hungryMessages[randomGenerator.nextInt(hungryMessages.length)]);
            if (happiness < 30) {
                content.append(" Being hungry is making me feel a bit sad.");
            }
        } else if (hunger < 70) {
            String[] normalMessages = {
                    "I had a decent meal today. Feeling pretty satisfied!",
                    "My appetite is just right. Not too hungry, not too full.",
                    "Food was good today. I'm content with what I ate.",
                    "My meals have been regular and satisfying.",
                    "I'm at a comfortable fullness level right now.",
                    "The food portions feel just about perfect today."
            };
            content.append(normalMessages[randomGenerator.nextInt(normalMessages.length)]);
        } else {
            String[] fullMessages = {
                    "I'm so full and satisfied! That meal was absolutely delicious!",
                    "My belly is happy and full. I couldn't eat another bite!",
                    "I ate so well today! Feeling completely satisfied and content.",
                    "What a feast! I feel wonderfully full and happy!",
                    "Every bite was amazing! I'm perfectly stuffed!",
                    "I'm living my best life with such great food!"
            };
            content.append(fullMessages[randomGenerator.nextInt(fullMessages.length)]);
        }

        content.append(" ");

        // Energy-based content
        if (energy < 30) {
            String[] tiredMessages = {
                    "I'm feeling so tired and sleepy. All I want to do is curl up and rest.",
                    "My energy is really low today. I need a good long nap.",
                    "I'm exhausted and can barely keep my eyes open. Time for sleep!",
                    "Every movement feels like it takes so much effort.",
                    "I'm dragging myself around, desperately needing rest.",
                    "My eyelids are so heavy... I need to recharge soon."
            };
            content.append(tiredMessages[randomGenerator.nextInt(tiredMessages.length)]);
            if (happiness < 30) {
                content.append(" I don't even have the energy to be happy right now.");
            }
        } else if (energy < 70) {
            String[] normalMessages = {
                    "My energy level feels just right for some light activities.",
                    "I'm feeling moderately energetic. Ready for some gentle play!",
                    "I have enough energy to enjoy the day without overdoing it.",
                    "I'm at a nice comfortable energy level today.",
                    "Not too tired, not too hyper - just right!",
                    "I feel balanced and ready for whatever comes my way."
            };
            content.append(normalMessages[randomGenerator.nextInt(normalMessages.length)]);
        } else {
            String[] energeticMessages = {
                    "I'm bursting with energy! I want to run and play all day long!",
                    "I feel so energetic and alive! Let's go on an adventure!",
                    "I'm full of energy and ready for anything! Let's play!",
                    "I can't sit still! There's so much I want to do!",
                    "I'm practically bouncing off the walls with excitement!",
                    "So much energy! Let's make the most of this day!"
            };
            content.append(energeticMessages[randomGenerator.nextInt(energeticMessages.length)]);
        }

        content.append(" ");

        // Happiness-based content
        if (happiness < 30) {
            String[] sadMessages = {
                    "I'm feeling a bit down today. I could use some extra love and attention.",
                    "I'm not my usual cheerful self. Maybe some playtime would help.",
                    "I'm feeling sad and lonely. I hope things get better soon.",
                    "My spirits are low and I'm feeling blue.",
                    "I wish I could shake this melancholy feeling.",
                    "Everything seems a little gray today. I need some cheering up."
            };
            content.append(sadMessages[randomGenerator.nextInt(sadMessages.length)]);
        } else if (happiness < 70) {
            String[] contentMessages = {
                    "I'm feeling okay today. Life is pretty good!",
                    "I'm in a decent mood. Nothing special, but I'm content.",
                    "I'm feeling alright. Just a normal, peaceful day.",
                    "Things are going reasonably well for me.",
                    "I'm in a pleasant, neutral state of mind.",
                    "Not ecstatic, but certainly not unhappy either."
            };
            content.append(contentMessages[randomGenerator.nextInt(contentMessages.length)]);
        } else {
            String[] happyMessages = {
                    "I'm so incredibly happy! Life is wonderful and I love my human so much!",
                    "I'm filled with joy and happiness! Every moment is a blessing!",
                    "I'm the happiest pet in the world! I love everything about today!",
                    "Pure joy fills my heart! Everything is absolutely perfect!",
                    "I'm overflowing with happiness and gratitude!",
                    "Life couldn't be better! I'm on top of the world!"
            };
            content.append(happyMessages[randomGenerator.nextInt(happyMessages.length)]);
        }

        content.append(" ");

        // Add level-based reflections with pet type context
        String petName = (petType != null && !petType.equals("pet")) ? petType : "little one";
        if (level < 5) {
            String[] youngMessages = {
                    "I'm still just a young " + petName + ", learning so much about the world!",
                    "As a baby " + petName + ", everything is new and exciting to me!",
                    "I wonder what adventures await me as I grow into a stronger " + petName + "?",
                    "Being a young " + petName + " is so fun - there's so much to discover!",
                    "I'm still small and inexperienced, but I'm learning every day!"
            };
            content.append(youngMessages[randomGenerator.nextInt(youngMessages.length)]);
        } else if (level < 10) {
            String[] matureMessages = {
                    "I'm getting wiser each day as a " + petName + "!",
                    "My experiences as a " + petName + " have taught me a lot.",
                    "I feel myself growing stronger and more capable!",
                    "I've learned so much since I was just a baby " + petName + ".",
                    "I'm becoming quite the seasoned " + petName + "!"
            };
            content.append(matureMessages[randomGenerator.nextInt(matureMessages.length)]);
        } else {
            String[] veteranMessages = {
                    "As a veteran " + petName + ", I've seen so many wonderful things in my life.",
                    "Being at this level makes me feel accomplished as a " + petName + "!",
                    "I'm proud of how far we've come together - I'm a true elder " + petName + " now!",
                    "I've lived a full life as a " + petName + " and have no regrets!",
                    "My wisdom as an experienced " + petName + " grows with each passing day!"
            };
            content.append(veteranMessages[randomGenerator.nextInt(veteranMessages.length)]);
        }

        content.append(" ");

        // Add pet type-specific behaviors
        if (petType != null) {
            switch (petType.toLowerCase()) {
                case "dog":
                    String[] dogMessages = {
                            "I wagged my tail extra hard today! 🐕",
                            "I dreamed about chasing squirrels! 🐕",
                            "My ears perked up at every sound! 🐕"
                    };
                    content.append(dogMessages[randomGenerator.nextInt(dogMessages.length)]);
                    break;
                case "cat":
                    String[] catMessages = {
                            "I found the perfect sunbeam to nap in! 🐈",
                            "I knocked something off the counter today. No regrets! 🐈",
                            "I groomed myself for what felt like hours! 🐈"
                    };
                    content.append(catMessages[randomGenerator.nextInt(catMessages.length)]);
                    break;
                case "dragon":
                    String[] dragonMessages = {
                            "My scales are shimmering beautifully today! 🐉",
                            "I practiced my roar - getting fiercer! 🐉",
                            "I felt the ancient wisdom flowing through me! 🐉"
                    };
                    content.append(dragonMessages[randomGenerator.nextInt(dragonMessages.length)]);
                    break;
                case "bird":
                    String[] birdMessages = {
                            "I chirped my favorite song today! 🐦",
                            "My feathers are looking especially fluffy! 🐦",
                            "I hopped around exploring everything! 🐦"
                    };
                    content.append(birdMessages[randomGenerator.nextInt(birdMessages.length)]);
                    break;
                default:
                    content.append("Today was uniquely special! 🐾");
                    break;
            }
        } else {
            content.append("🐾");
        }

        return content.toString();
    }

    // Public 3-parameter version for tests (uses defaults)
    String generateTitleBasedOnStats(int hunger, int happiness, int energy) {
        return generateTitleBasedOnStats(hunger, happiness, energy, 1, "pet");
    }

    // Internal 5-parameter version with level and type
    private String generateTitleBasedOnStats(int hunger, int happiness, int energy, int level, String petType) {

        // Generate title based on overall state
        int avgStat = (hunger + happiness + energy) / 3;
        
        // Add variety based on level and pet type
        String levelPrefix = "";
        if (level < 5) {
            levelPrefix = randomGenerator.nextInt(10) > 7 ? "Baby's " : "";
        } else if (level >= 10) {
            levelPrefix = randomGenerator.nextInt(10) > 7 ? "Veteran's " : "";
        }

        if (avgStat < 40) {
            String[] titles = {"A Tough Day", "Feeling Low", "Need Some Care", "Not My Best Day"};
            return levelPrefix + titles[randomGenerator.nextInt(titles.length)];
        } else if (avgStat < 70) {
            String[] titles = {"Today's Adventure", "A Normal Day", "Daily Thoughts", "Just Another Day"};
            return levelPrefix + titles[randomGenerator.nextInt(titles.length)];
        } else {
            String[] titles = {"Best Day Ever!", "Feeling Amazing!", "Life is Great!", "So Happy Today!"};
            return levelPrefix + titles[randomGenerator.nextInt(titles.length)];
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
    String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Public methods for black box testing
    public long getPetId() {
        return PET_ID;
    }

    public String getCurrentEntryText() {
        if (currentEntryDisplay != null) {
            CharSequence text = currentEntryDisplay.getText();
            return text != null ? text.toString() : "";
        }
        return "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbExecutor != null) {
            dbExecutor.shutdown(); // Always clean up resources!
        }
    }

    public long getPetId() {
        return PET_ID;
    }

    public void refreshJournal() {
        generateNewJournalEntry();
    }

    public String getCurrentEntryText() {
        if (currentEntryDisplay != null) {
            return currentEntryDisplay.getText().toString();
        }
        return "";
    }

}
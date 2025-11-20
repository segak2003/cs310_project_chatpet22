package com.example.chatpet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import java.util.Random;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, 
        manifest = "../app/src/main/AndroidManifest.xml",
        qualifiers = "en-rUS")
public class PetJournalBlackBoxTest {

    private PetJournalActivity petActivity;

    @Before
    public void setUp() throws Exception {
        // Create activity instance without full lifecycle to avoid UI dependencies
        // Note: We only need to test the logic methods, not the UI stuff
        petActivity = new PetJournalActivity();

        // Need to initialize randomGenerator field using reflection since onCreate() isn't called
        // This field is normally set up in initializeViews() method
        Field randomGenField = PetJournalActivity.class.getDeclaredField("randomGenerator");
        randomGenField.setAccessible(true);
        randomGenField.set(petActivity, new Random());
    }

    @Test
    public void testGenerateContentWithLowStats() {
        int hungerLevel = 20;
        int happinessLevel = 20;
        int energyLevel = 20;

        String generatedContent = petActivity.generateContentBasedOnStats(hungerLevel, happinessLevel, energyLevel);

        // Basic checks first
        assert generatedContent != null;
        assert !generatedContent.isEmpty();

        // Content should reflect low stats with appropriate keywords
        // I'm checking for different variations of hungry messages
        assert generatedContent.contains("hungry") || generatedContent.contains("stomach") || generatedContent.contains("tummy");
        assert generatedContent.contains("tired") || generatedContent.contains("sleepy") || generatedContent.contains("exhausted");
        assert generatedContent.contains("sad") || generatedContent.contains("down") || generatedContent.contains("lonely");
        assert generatedContent.contains("🐾"); // Should always have the paw emoji
    }

    @Test
    public void testGenerateContentWithHighStats() {
        int hungerStat = 80;
        int happinessStat = 85;
        int energyStat = 90;

        String contentResult = petActivity.generateContentBasedOnStats(hungerStat, happinessStat, energyStat);

        // Verify content is not null or empty
        assert contentResult != null;
        assert !contentResult.isEmpty();

        // Convert to lowercase for easier checking - learned this trick from a colleague
        String lowerContent = contentResult.toLowerCase();

        // Content should reflect high stats with positive keywords
        assert lowerContent.contains("full") || lowerContent.contains("satisfied") || lowerContent.contains("delicious");
        assert lowerContent.contains("energy") || lowerContent.contains("energetic") || lowerContent.contains("play");
        assert lowerContent.contains("happy") || lowerContent.contains("joy") || lowerContent.contains("wonderful") || lowerContent.contains("love");
        assert contentResult.contains("🐾");
    }

    @Test
    public void testGenerateContentWithMediumStats() {
        int medHunger = 50;
        int medHappiness = 55;
        int medEnergy = 60;

        String mediumContent = petActivity.generateContentBasedOnStats(medHunger, medHappiness, medEnergy);

        // Basic validation
        assert mediumContent != null;
        assert !mediumContent.isEmpty();

        // Content should reflect moderate stats with neutral keywords
        String contentLower = mediumContent.toLowerCase();
        assert contentLower.contains("decent") ||
                contentLower.contains("okay") ||
                contentLower.contains("normal") ||
                contentLower.contains("satisfied") ||
                contentLower.contains("right");
        assert mediumContent.contains("🐾");
    }

    @Test
    public void testGenerateTitleBasedOnStats() {
        // Testing low stats first (average < 40)
        String titleForLowStats = petActivity.generateTitleBasedOnStats(20, 30, 25);
        assert titleForLowStats != null;
        assert !titleForLowStats.isEmpty();
        // Low stat titles should be less positive - checking against expected strings
        assert titleForLowStats.contains("Tough") || titleForLowStats.contains("Low") ||
                titleForLowStats.contains("Care") || titleForLowStats.contains("Not My Best");

        // Testing medium stats next (average 40-70)
        String titleForMedStats = petActivity.generateTitleBasedOnStats(50, 55, 60);
        assert titleForMedStats != null;
        assert !titleForMedStats.isEmpty();

        // Testing high stats last (average > 70)
        String titleForHighStats = petActivity.generateTitleBasedOnStats(80, 85, 90);
        assert titleForHighStats != null;
        assert !titleForHighStats.isEmpty();
        // High stat titles should be positive
        assert titleForHighStats.contains("Best") || titleForHighStats.contains("Amazing") ||
                titleForHighStats.contains("Great") || titleForHighStats.contains("Happy");
    }

    @Test
    public void testFormatDate() {
        // Test with a known timestamp: Jan 1, 2024, 12:00:00 PM
        long testTimestamp = 1704117600000L; // Jan 1, 2024, 12:00 PM UTC

        String dateFormatted = petActivity.formatDate(testTimestamp);

        // Verify formatted date is not null or empty
        assert dateFormatted != null;
        assert !dateFormatted.isEmpty();

        // Verify it contains expected components
        assert dateFormatted.contains("2024"); // Should have the year
        assert dateFormatted.contains("Jan") || dateFormatted.contains("01"); // Month in some format
        assert dateFormatted.contains("at"); // Time separator word

        // Let's also test with current timestamp for good measure
        long nowTimestamp = System.currentTimeMillis();
        String currentDateFormatted = petActivity.formatDate(nowTimestamp);

        assert currentDateFormatted != null;
        assert !currentDateFormatted.isEmpty();
        assert currentDateFormatted.contains("at"); // Should have time component
    }

}

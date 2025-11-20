package com.example.chatpet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PetJournalActivityWhiteBoxTest {

    @Test
    public void getPetId_returnsCorrectId() {
        try (ActivityScenario<PetJournalActivity> scenario = ActivityScenario.launch(PetJournalActivity.class)) {
            scenario.onActivity(activity -> {
                // TODO: double check this ID value later
                long expectedPetId = 1L; // this should match whatever we set in the activity
                long actualPetId = activity.getPetId();
                assertEquals("Pet ID should match the expected value", expectedPetId, actualPetId);
            });
        }
    }

    @Test
    public void getCurrentEntryText_returnsNonNull() {
        try (ActivityScenario<PetJournalActivity> scenario = ActivityScenario.launch(PetJournalActivity.class)) {
            scenario.onActivity(activity -> {
                String entryText = activity.getCurrentEntryText();
                assertNotNull("Current entry text should not be null", entryText);
            });
        }
    }

    @Test
    public void generateTitleBasedOnStats_lowStats_returnsLowMoodTitle() {
        try (ActivityScenario<PetJournalActivity> scenario = ActivityScenario.launch(PetJournalActivity.class)) {
            scenario.onActivity(activity -> {
                // Using low values to trigger sad titles
                String actualTitle = activity.generateTitleBasedOnStats(10, 20, 15);

                // Note: these are the expected low mood titles from the activity
                String[] lowMoodTitles = {"A Tough Day", "Feeling Low", "Need Some Care", "Not My Best Day"};

                boolean titleFound = false;
                for (String expectedTitle : lowMoodTitles) {
                    if (expectedTitle.equals(actualTitle)) {
                        titleFound = true;
                        break;
                    }
                }

                assertEquals("Title should be one of the low-stat titles", true, titleFound);
            });
        }
    }

    @Test
    public void generateTitleBasedOnStats_highStats_returnsPositiveTitle() {
        try (ActivityScenario<PetJournalActivity> scenario = ActivityScenario.launch(PetJournalActivity.class)) {
            scenario.onActivity(activity -> {
                // High stats should produce happy titles
                String generatedTitle = activity.generateTitleBasedOnStats(90, 95, 85);

                String[] happyTitles = {
                        "Best Day Ever!", "Feeling Amazing!", "Life is Great!", "So Happy Today!"
                };

                boolean foundMatch = false;
                for (String happyTitle : happyTitles) {
                    if (happyTitle.equals(generatedTitle)) {
                        foundMatch = true;
                        break;
                    }
                }

                assertEquals("Title should match one of the high-stat titles", true, foundMatch);
            });
        }
    }

    @Test
    public void welcomeEntryCreatedWhenNoneExist() {
        try (ActivityScenario<PetJournalActivity> scenario = ActivityScenario.launch(PetJournalActivity.class)) {
            scenario.onActivity(activity -> {
                // Wait a bit for the database to initialize properly
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    // ignore interruption
                }

                String currentText = activity.getCurrentEntryText();

                assertNotNull("Current entry should not be null after initial load", currentText);

                // Check if it contains the welcome message
                boolean hasWelcomeMessage = currentText.contains("Welcome to your pet journal");

                assertEquals("Initial entry should be the welcome entry", true, hasWelcomeMessage);
            });
        }
    }
}
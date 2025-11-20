package com.example.chatpet;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;

import android.content.Intent;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.chatpet.R;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Black-box UI tests for PetGrowthActivity using Espresso.
 *
 * These live under src/androidTest/ and run on an emulator or device.
 */
@RunWith(AndroidJUnit4.class)
public class PetGrowthActivityTest {

    /**
     * Launch PetGrowthActivity with some initial PET_TYPE and PET_NAME,
     * similar to how ProfileActivity starts it.
     */
    @Rule
    public ActivityScenarioRule<PetGrowthActivity> activityRule =
            new ActivityScenarioRule<>(
                    new Intent(
                            ApplicationProvider.getApplicationContext(),
                            PetGrowthActivity.class
                    )
                            .putExtra("PET_TYPE", "DRAGON")
                            .putExtra("PET_NAME", "Flame")
            );

    /**
     * Test 1: Initial UI elements are visible.
     *
     * Black-box: we only check what the user sees, not internal fields.
     */
    @Test
    public void initialUi_showsEmojiNameAndMeters() {
        onView(withId(R.id.tvEmoji)).check(matches(isDisplayed()));
        onView(withId(R.id.tvName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvStage)).check(matches(isDisplayed()));
        onView(withId(R.id.tvPoints)).check(matches(isDisplayed()));
        onView(withId(R.id.barHunger)).check(matches(isDisplayed()));
        onView(withId(R.id.barHappiness)).check(matches(isDisplayed()));
        onView(withId(R.id.barEnergy)).check(matches(isDisplayed()));
    }

    /**
     * Test 2: Tapping Feed hides main buttons + Journal and shows food choices.
     */
    @Test
    public void feedButton_showsFoodChoicesAndHidesMainButtonsAndJournal() {
        // Ensure main buttons are visible initially
        onView(withId(R.id.layoutMainButtons))
                .check(matches(withEffectiveVisibility(VISIBLE)));
        onView(withId(R.id.btnJournal))
                .check(matches(withEffectiveVisibility(VISIBLE)));

        // Click the Feed button
        onView(withId(R.id.btnFeed)).perform(click());

        // Main row should be GONE
        onView(withId(R.id.layoutMainButtons))
                .check(matches(withEffectiveVisibility(GONE)));

        // Food choices row should be visible
        onView(withId(R.id.layoutFeedChoices))
                .check(matches(withEffectiveVisibility(VISIBLE)));

        // Journal button should be hidden while choosing food
        onView(withId(R.id.btnJournal))
                .check(matches(withEffectiveVisibility(GONE)));
    }

    /**
     * Test 3: Choosing a food restores main buttons and Journal.
     */
    @Test
    public void choosingFood_restoresMainButtonsAndJournal() {
        // Open food choices
        onView(withId(R.id.btnFeed)).perform(click());

        // Choose Kibble
        onView(withId(R.id.btnFoodKibble)).perform(click());

        // Main buttons should be visible again
        onView(withId(R.id.layoutMainButtons))
                .check(matches(withEffectiveVisibility(VISIBLE)));

        // Journal button visible again
        onView(withId(R.id.btnJournal))
                .check(matches(withEffectiveVisibility(VISIBLE)));

        // Reply text should mention eating (black-box behavior)
        onView(withId(R.id.tvReply))
                .check(matches(withText(Matchers.containsString("Yum"))));
    }

    /**
     * Test 4: Tapping Tuck updates reply text to mention sleeping.
     */
    @Test
    public void tuckButton_showsSleepingMessage() {
        onView(withId(R.id.btnTuck)).perform(click());

        // Reply text should mention sleeping
        onView(withId(R.id.tvReply))
                .check(matches(withText(Matchers.containsString("sleeping"))));
    }


    @Test
    public void choosingPizza_updatesReplyTextAndRestoresLayout() {
        // Open the food choices
        onView(withId(R.id.btnFeed)).perform(click());

        // Tap the Pizza option
        onView(withId(R.id.btnFoodPizza)).perform(click());

        // Reply text should reflect the pizza choice
        onView(withId(R.id.tvReply))
                .check(matches(withText("Pizza time! 🍕")));

        // Main buttons row should be visible again
        onView(withId(R.id.layoutMainButtons))
                .check(matches(withEffectiveVisibility(VISIBLE)));

        // Food choices row should be hidden
        onView(withId(R.id.layoutFeedChoices))
                .check(matches(withEffectiveVisibility(GONE)));

        // Journal button should be visible again
        onView(withId(R.id.btnJournal))
                .check(matches(withEffectiveVisibility(VISIBLE)));
    }

}

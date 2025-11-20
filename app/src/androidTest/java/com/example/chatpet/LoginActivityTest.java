package com.example.chatpet;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void testEmptyLogin() {
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Username is required")));
    }

    @Test
    public void testIncorrectLogin() {
        onView(withId(R.id.etUsername)).perform(typeText("nonexistent"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("wrongpass"), closeSoftKeyboard());
        onView(withId(R.id.btnLogin)).perform(click());
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Incorrect username")));
    }
}

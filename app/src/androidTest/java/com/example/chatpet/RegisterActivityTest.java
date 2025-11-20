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
public class RegisterActivityTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule =
            new ActivityScenarioRule<>(RegisterActivity.class);

    @Test
    public void testEmptyFields() {
        onView(withId(R.id.btnRegister)).perform(click());
        onView(withId(R.id.etFullName)).check(matches(hasErrorText("Full name is required")));
    }

    @Test
    public void testEmptyEmail() {
        onView(withId(R.id.etFullName)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etUsername)).perform(typeText("johndoe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(typeText(""), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.rbMaleYellow)).perform(click());
        onView(withId(R.id.btnRegister)).perform(click());
        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Email is required")));
    }

    @Test
    public void testPasswordTooShort() {
        onView(withId(R.id.etFullName)).perform(typeText("John Doe"), closeSoftKeyboard());
        onView(withId(R.id.etUsername)).perform(typeText("johndoe"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(typeText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.etConfirmPassword)).perform(typeText("123"), closeSoftKeyboard());
        onView(withId(R.id.rbMaleYellow)).perform(click());
        onView(withId(R.id.btnRegister)).perform(click());
        onView(withId(R.id.etPassword))
                .check(matches(hasErrorText("Password must be at least 6 characters")));
    }
}

package com.example.chatpet;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class RegisterActivityHelper {

    public int getSelectedAvatar(boolean fYellow, boolean fWhite, boolean fBrown, boolean mYellow, boolean mWhite, boolean mBrown) {
        if (fYellow) return RegisterActivity.AVATAR_FEMALE_YELLOW;
        if (fWhite) return RegisterActivity.AVATAR_FEMALE_WHITE;
        if (fBrown) return RegisterActivity.AVATAR_FEMALE_BROWN;
        if (mYellow) return RegisterActivity.AVATAR_MALE_YELLOW;
        if (mWhite) return RegisterActivity.AVATAR_MALE_WHITE;
        if (mBrown) return RegisterActivity.AVATAR_MALE_BROWN;
        return -1;
    }

    public boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public boolean isEmailValid(String email) {
        return Pattern.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+", email);
    }

    public boolean doPasswordsMatch(String pass1, String pass2) {
        return pass1.equals(pass2);
    }

    public Date getBirthdayFromPicker(int day, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.getTime();
    }
}

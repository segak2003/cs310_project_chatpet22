package com.example.chatpet;

import org.junit.Before;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import static org.junit.Assert.*;

public class RegisterActivityUnitTest {

    private RegisterActivityHelper helper;

    @Before
    public void setUp() {
        helper = new RegisterActivityHelper();
    }

    @Test
    public void testAvatarSelection() {
        int avatar = helper.getSelectedAvatar(false,false,false,false,false,false);
        assertEquals(-1, avatar);
    }

    @Test
    public void testPasswordValidation() {
        assertTrue(helper.isPasswordValid("123456"));
        assertFalse(helper.isPasswordValid("123"));
    }

    @Test
    public void testEmailValidation() {
        assertTrue(helper.isEmailValid("john@example.com"));
        assertFalse(helper.isEmailValid("invalidemail"));
    }

    @Test
    public void testPasswordsMatch() {
        assertTrue(helper.doPasswordsMatch("password123","password123"));
        assertFalse(helper.doPasswordsMatch("password123","password321"));
    }

    @Test
    public void testBirthdayConversion() {
        Calendar cal = Calendar.getInstance();
        cal.set(2000,0,1);
        Date birthday = helper.getBirthdayFromPicker(1,0,2000);
        assertEquals(cal.getTime(), birthday);
    }
}

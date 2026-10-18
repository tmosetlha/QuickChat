package com.tmui.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Register (Part 1).
 *
 * NOTE: PASSWORD_VALID / PASSWORD_INVALID / CELLPHONE_VALID / CELLPHONE_INVALID
 * are placeholders — the exact literals for these rows were cut off in your
 * screenshots. Check your brief PDF and swap them in if different, since
 * marking goes against exact test data.
 *
 * @author Tshiamo
 */
public class RegisterTest {

    private Register register;

    private static final String PASSWORD_VALID = "Password1!";
    private static final String PASSWORD_INVALID = "password";
    private static final String CELLPHONE_VALID = "+27821234567";
    private static final String CELLPHONE_INVALID = "0821234567";

    @BeforeEach
    public void setUp() {
        register = new Register();
    }

    @Test
    public void testUserNameCorrectlyFormatted() {
        assertTrue(register.checkUserName("kyl_1"));
    }

    @Test
    public void testUserNameIncorrectlyFormatted() {
        assertFalse(register.checkUserName("kyle!!!!!!"));
    }

    @Test
    public void testPasswordMeetsComplexity() {
        assertTrue(register.checkPasswordComplexity(PASSWORD_VALID));
    }

    @Test
    public void testPasswordDoesNotMeetComplexity() {
        assertFalse(register.checkPasswordComplexity(PASSWORD_INVALID));
    }

    @Test
    public void testCellPhoneNumberCorrectlyFormatted() {
        assertTrue(register.checkCellPhoneNumber(CELLPHONE_VALID));
    }

    @Test
    public void testCellPhoneNumberIncorrectlyFormatted() {
        assertFalse(register.checkCellPhoneNumber(CELLPHONE_INVALID));
    }

    @Test
    public void testRegisterUserSuccess() {
        String result = register.registerUser("kyl_1", PASSWORD_VALID, CELLPHONE_VALID, "Kyle", "Smith");
        assertEquals(
            "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.",
            result
        );
    }
}
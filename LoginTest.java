package com.tmui.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Login (Part 1).
 *
 * @author Tshiamo
 */
public class LoginTest {

    private Register register;
    private Login login;

    private static final String PASSWORD_VALID = "Password1!";
    private static final String CELLPHONE_VALID = "+27821234567";

    @BeforeEach
    public void setUp() {
        register = new Register();
        register.registerUser("kyl_1", PASSWORD_VALID, CELLPHONE_VALID, "Kyle", "Smith");
        login = new Login(register);
    }

    @Test
    public void testLoginSuccess() {
        boolean success = login.loginUser("kyl_1", PASSWORD_VALID);
        assertTrue(success);
        assertEquals("Welcome Kyle, Smith it is great to see you again.", login.returnLoginStatus(success));
    }

    @Test
    public void testLoginFailure() {
        boolean success = login.loginUser("kyl_1", "wrongPassword1!");
        assertFalse(success);
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus(success));
    }
}
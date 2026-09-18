package com.tmui.quickchat;

/**
 * Handles login for QuickChat (Part 1).
 *
 * Checks login attempts against a Register instance holding the
 * already-registered user's details.
 *
 * @author Tshiamo
 */
public class Login {

    private final Register register;

    public Login(Register register) {
        this.register = register;
    }

    /**
     * Verifies the entered username and password match what Register
     * captured at registration.
     */
    public boolean loginUser(String username, String password) {
        if (!register.isUserIsRegistered()) {
            return false;
        }
        return register.getStoredUsername().equals(username)
                && register.getStoredPassword().equals(password);
    }

    /**
     * Returns the message for a successful or failed login attempt.
     */
    public String returnLoginStatus(boolean loginSuccess) {
        if (loginSuccess) {
            return "Welcome " + register.getStoredFirstName() + ", " + register.getStoredLastName() + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }
}
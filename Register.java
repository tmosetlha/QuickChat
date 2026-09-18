package com.tmui.quickchat;

import java.util.regex.Pattern;

/**
 * Handles user registration for QuickChat (Part 1).
 *
 * Validates username, password and South African cell phone number
 * formatting, then stores the registered user's details.
 *
 * Cell phone regex reference: pattern for South African mobile numbers
 * (+27 followed by a 9-digit subscriber number) adapted from
 * https://regex101.com/library?page=177
 *
 * @author Tshiamo
 */
public class Register {

    private String storedUsername;
    private String storedPassword;
    private String storedCellphone;
    private String storedFirstName;
    private String storedLastName;
    private boolean userIsRegistered = false;

    // '+27' followed by exactly 9 digits — standard SA mobile format.
    private static final Pattern CELLPHONE_PATTERN = Pattern.compile("^\\+27\\d{9}$");

    /**
     * Username must contain an underscore and be no more than five
     * characters long.
     */
    public boolean checkUserName(String username) {
        if (username == null) {
            return false;
        }
        return username.length() <= 5 && username.contains("_");
    }

    /**
     * Password must be at least eight characters long and contain a
     * capital letter, a number, and a special character.
     */
    public boolean checkPasswordComplexity(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasCapital = true;
            } else if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        return hasCapital && hasNumber && hasSpecial;
    }

    /**
     * Cell phone number must contain the SA international code (+27)
     * followed by the subscriber number, no more than ten characters.
     */
    public boolean checkCellPhoneNumber(String cellphone) {
        if (cellphone == null) {
            return false;
        }
        return CELLPHONE_PATTERN.matcher(cellphone).matches();
    }

    /**
     * Validates username, password and cell phone number, in that
     * order, and stores the user if all three pass. Returns the exact
     * message for whichever check fails first, or a success message
     * if all pass.
     */
    public String registerUser(String username, String password, String cellphone,
                                String firstName, String lastName) {

        if (!checkUserName(username)) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber(cellphone)) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }

        this.storedUsername = username;
        this.storedPassword = password;
        this.storedCellphone = cellphone;
        this.storedFirstName = firstName;
        this.storedLastName = lastName;
        this.userIsRegistered = true;

        return "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.";
    }

    public String getStoredUsername() {
        return storedUsername;
    }

    public String getStoredPassword() {
        return storedPassword;
    }

    public String getStoredCellphone() {
        return storedCellphone;
    }

    public String getStoredFirstName() {
        return storedFirstName;
    }

    public String getStoredLastName() {
        return storedLastName;
    }

    public boolean isUserIsRegistered() {
        return userIsRegistered;
    }
}
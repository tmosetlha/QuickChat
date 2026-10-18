package com.tmui.quickchat;

import java.util.Scanner;

/**
 * QuickChat console application — Part 1: Registration and login.
 *
 * @author Tshiamo
 */
public class QuickChat {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Register register = new Register();

        System.out.println("=== Welcome to QuickChat ===");
        System.out.println();
        System.out.println("--- Registration ---");

        String username;
        while (true) {
            System.out.print("Enter a username: ");
            username = scanner.nextLine();
            if (register.checkUserName(username)) {
                System.out.println("Username successfully captured.");
                break;
            }
            System.out.println("Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.");
        }

        String password;
        while (true) {
            System.out.print("Enter a password: ");
            password = scanner.nextLine();
            if (register.checkPasswordComplexity(password)) {
                System.out.println("Password successfully captured.");
                break;
            }
            System.out.println("Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
        }

        String cellphone;
        while (true) {
            System.out.print("Enter your cell phone number (e.g. +27821234567): ");
            cellphone = scanner.nextLine();
            if (register.checkCellPhoneNumber(cellphone)) {
                System.out.println("Cell phone number successfully added.");
                break;
            }
            System.out.println("Cell phone number incorrectly formatted or does not contain international code.");
        }

        // Needed to build the "Welcome <first>, <last>" login message.
        System.out.print("Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter your last name: ");
        String lastName = scanner.nextLine();

        register.registerUser(username, password, cellphone, firstName, lastName);
        System.out.println();
        System.out.println("Registration complete.");

        Login login = new Login(register);

        System.out.println();
        System.out.println("--- Login ---");
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter your username: ");
            String loginUsername = scanner.nextLine();
            System.out.print("Enter your password: ");
            String loginPassword = scanner.nextLine();

            loggedIn = login.loginUser(loginUsername, loginPassword);
            System.out.println(login.returnLoginStatus(loggedIn));
        }

        scanner.close();
    }
}
package com.tmui.quickchat;

import java.util.Scanner;

/**
 * QuickChat console application.
 *
 * Part 1: registration and login.
 * Part 2: sending messages (numeric menu, message entry, send /
 * disregard / store).
 * Part 3: stored messages menu (arrays, search, delete and report).
 *
 * @author Tshiamo
 */
public class QuickChat {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Register register = new Register();

        System.out.println("=== Welcome to QuickChat ===");
        registerUser(scanner, register);

        Login login = new Login(register);
        loginUser(scanner, login);

        // Only reached once the user has logged in successfully.
        runMessaging(scanner, register.getStoredFirstName() + " " + register.getStoredLastName());

        scanner.close();
    }

    // ------------------------------------------------------------------
    // Part 1 - registration and login
    // ------------------------------------------------------------------

    private static void registerUser(Scanner scanner, Register register) {
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
    }

    private static void loginUser(Scanner scanner, Login login) {
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
    }

    // ------------------------------------------------------------------
    // Part 2 - sending messages
    // ------------------------------------------------------------------

    private static void runMessaging(Scanner scanner, String sender) {
        System.out.println();
        System.out.println("Welcome to QuickChat.");

        // Part 3: the arrays that hold sent, disregarded and stored messages.
        MessageStore store = new MessageStore();
        int loaded = store.loadStoredMessages();
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from storedmessages.json.");
        }

        // The user sets how many messages they will enter, once, at the start.
        int limit = askForMessageCount(scanner);
        int entered = 0;

        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    if (entered >= limit) {
                        System.out.println("You have already entered all " + limit + " of your messages.");
                    } else {
                        entered = enterMessages(scanner, store, entered, limit);
                    }
                }
                case "2" -> System.out.println("Coming Soon.");
                case "3" -> running = false;
                case "4" -> runStoredMessagesMenu(scanner, store, sender);
                default -> System.out.println("Invalid option; please enter 1, 2, 3 or 4.");
            }
        }

        System.out.println();
        System.out.println("Thank you for using QuickChat.");
    }

    // Keeps asking for messages until the set number has been entered.
    private static int enterMessages(Scanner scanner, MessageStore store, int entered, int limit) {
        while (entered < limit) {
            System.out.println();
            System.out.println("--- Message " + (entered + 1) + " of " + limit + " ---");

            String recipient = askForRecipient(scanner);
            String text = askForMessageText(scanner);
            Message message = new Message(recipient, text);

            int choice = askForSendChoice(scanner);
            String result = message.sentMessage(choice);
            System.out.println(result);

            if (choice == Message.SEND) {
                store.addMessage(message, Message.SEND);
                System.out.println();
                System.out.println(message.getDetails());
            } else if (choice == Message.DISREGARD) {
                waitForZero(scanner);
                System.out.println("Message deleted.");
                store.addMessage(message, Message.DISREGARD);
            } else if (choice == Message.STORE && result.equals("Message successfully stored")) {
                store.addMessage(message, Message.STORE);
            }
            entered++;
        }

        // All messages entered: show everything that was sent, then the total.
        System.out.println();
        System.out.println("--- Messages sent this session ---");
        System.out.println(Message.printMessages());
        System.out.println();
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
        return entered;
    }

    // ------------------------------------------------------------------
    // Part 3 - stored messages menu
    // ------------------------------------------------------------------

    private static void runStoredMessagesMenu(Scanner scanner, MessageStore store, String sender) {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println();
            System.out.println("--- Stored Messages ---");
            System.out.println("a) Display the sender and recipient of all messages");
            System.out.println("b) Display the longest message");
            System.out.println("c) Search for a message ID");
            System.out.println("d) Search for all messages for a recipient");
            System.out.println("e) Delete a message using its message hash");
            System.out.println("f) Display a report of all messages");
            System.out.println("g) Back to the main menu");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim().toLowerCase()) {
                case "a" -> System.out.println(store.displaySendersAndRecipients(sender));
                case "b" -> System.out.println("Longest message: " + store.getLongestMessage());
                case "c" -> {
                    System.out.print("Enter the message ID: ");
                    Message found = store.searchByMessageID(scanner.nextLine());
                    if (found == null) {
                        System.out.println("No message found with that ID.");
                    } else {
                        System.out.println("Recipient: " + found.getRecipient());
                        System.out.println("Message: " + found.getMessageText());
                    }
                }
                case "d" -> {
                    System.out.print("Enter the recipient's cell phone number: ");
                    String[] found = store.searchByRecipient(scanner.nextLine());
                    if (found.length == 0) {
                        System.out.println("No messages found for that recipient.");
                    } else {
                        for (int i = 0; i < found.length; i++) {
                            System.out.println((i + 1) + ". " + found[i]);
                        }
                    }
                }
                case "e" -> {
                    System.out.print("Enter the message hash: ");
                    System.out.println(store.deleteByHash(scanner.nextLine()));
                }
                case "f" -> System.out.println(store.displayReport());
                case "g" -> inMenu = false;
                default -> System.out.println("Invalid option; please enter a, b, c, d, e, f or g.");
            }
        }
    }

    private static int askForMessageCount(Scanner scanner) {
        while (true) {
            System.out.print("How many messages do you wish to enter? ");
            try {
                int count = Integer.parseInt(scanner.nextLine().trim());
                if (count >= 1) {
                    return count;
                }
            } catch (NumberFormatException e) {
                // falls through to the message below
            }
            System.out.println("Please enter a whole number of 1 or more.");
        }
    }

    private static String askForRecipient(Scanner scanner) {
        while (true) {
            System.out.print("Enter the recipient's cell phone number (e.g. +27718693002): ");
            String recipient = scanner.nextLine().trim();
            System.out.println(Message.checkRecipientCell(recipient));
            if (Message.isRecipientValid(recipient)) {
                return recipient;
            }
        }
    }

    private static String askForMessageText(Scanner scanner) {
        while (true) {
            System.out.print("Enter your message (max 250 characters): ");
            String text = scanner.nextLine();

            if (text.trim().isEmpty()) {
                System.out.println("A message cannot be empty.");
            } else if (!Message.isMessageLengthValid(text)) {
                System.out.println("Please enter a message of less than 250 characters.");
                System.out.println(Message.checkMessageLength(text));
            } else {
                System.out.println(Message.checkMessageLength(text));
                return text;
            }
        }
    }

    private static int askForSendChoice(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("What would you like to do with this message?");
            System.out.println("1) Send Message");
            System.out.println("2) Disregard Message");
            System.out.println("3) Store Message to send later");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine().trim()) {
                case "1": return Message.SEND;
                case "2": return Message.DISREGARD;
                case "3": return Message.STORE;
                default: System.out.println("Invalid option; please enter 1, 2 or 3.");
            }
        }
    }

    // The brief: after "Press 0 to delete the message", the user presses 0.
    private static void waitForZero(Scanner scanner) {
        while (!scanner.nextLine().trim().equals("0")) {
            System.out.println("Press 0 to delete the message");
        }
    }
}
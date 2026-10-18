package com.tmui.quickchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents one QuickChat message (Part 2 - Sending messages).
 *
 * Holds the message details, builds the message hash, validates the
 * recipient and the message length, and keeps a record of every message
 * sent while the program is running.
 *
 * 
 * @author Tshiamo
 */
public class Message {

    // Choices accepted by sentMessage().
    public static final int SEND = 1;
    public static final int DISREGARD = 2;
    public static final int STORE = 3;

    public static final int MAX_MESSAGE_LENGTH = 250;

    // ---- Session-wide state: shared by every Message while the program runs ----
    private static final List<Message> sentMessages = new ArrayList<>();
    private static int totalMessagesSent = 0;
    private static int nextMessageNumber = 0;          // first message is number 0
    private static Path storageFile = Paths.get("storedmessages.json");

    // Reuses the cell phone check written in Part 1.
    private static final Register VALIDATOR = new Register();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- This message's own details ----
    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String messageText;
    private final String messageHash;

    /**
     * Normal use: the ten-digit ID and the message number are generated
     * automatically.
     */
    public Message(String recipient, String messageText) {
        this(generateMessageID(), nextMessageNumber++, recipient, messageText);
    }

    /**
     * Lets the ID and message number be fixed, so unit tests can predict
     * the message hash.
     */
    public Message(String messageID, int messageNumber, String recipient, String messageText) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.messageText = (messageText == null) ? "" : messageText;
        this.messageHash = createMessageHash();
    }

    // ------------------------------------------------------------------
    // Validation
    // ------------------------------------------------------------------

    /** The message ID must not be more than ten characters. */
    public boolean checkMessageID() {
        return messageID != null && !messageID.isEmpty() && messageID.length() <= 10;
    }

    /** True when the recipient number has an international code and a valid length. */
    public static boolean isRecipientValid(String recipient) {
        return VALIDATOR.checkCellPhoneNumber(recipient);
    }

    /** Returns the exact success / failure message for a recipient number. */
    public static String checkRecipientCell(String recipient) {
        if (isRecipientValid(recipient)) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    /** True when the message is 250 characters or fewer. */
    public static boolean isMessageLengthValid(String text) {
        return text != null && text.length() <= MAX_MESSAGE_LENGTH;
    }

    /** Returns the exact success / failure message for the message length. */
    public static String checkMessageLength(String text) {
        int length = (text == null) ? 0 : text.length();
        if (length <= MAX_MESSAGE_LENGTH) {
            return "Message ready to send.";
        }
        int excess = length - MAX_MESSAGE_LENGTH;
        return "Message exceeds 250 characters by " + excess + "; please reduce the size.";
    }

    // ------------------------------------------------------------------
    // Hash
    // ------------------------------------------------------------------

    /**
     * Builds the message hash: first two digits of the ID, a colon, the
     * message number, a colon, then the first and last words of the
     * message - all in capitals. Example: 00:0:HITONIGHT
     */
    public String createMessageHash() {
        String prefix = (messageID.length() >= 2) ? messageID.substring(0, 2) : messageID;

        String[] words = messageText.trim().split("\\s+");
        String firstWord = stripPunctuation(words[0]);
        String lastWord = stripPunctuation(words[words.length - 1]);

        return (prefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Removes commas, question marks etc. so "tonight?" becomes "tonight".
    private static String stripPunctuation(String word) {
        return word.replaceAll("[^A-Za-z0-9]", "");
    }

    // ------------------------------------------------------------------
    // Send / disregard / store
    // ------------------------------------------------------------------

    /**
     * Applies the user's choice (1 = send, 2 = disregard, 3 = store) and
     * returns the message the console must show.
     */
    public String sentMessage(int choice) {
        return switch (choice) {
            case SEND -> {
                if (!sentMessages.contains(this)) {
                    sentMessages.add(this);
                    totalMessagesSent++;
                }
                yield "Message successfully sent";
            }
            case DISREGARD -> "Press 0 to delete the message";
            case STORE -> storeMessage();
            default -> "Invalid option; please choose 1 (Send), 2 (Disregard) or 3 (Store).";
        };
    }

    /**
     * Stores this message in a JSON file, keeping any messages that were
     * stored earlier.
     */
    public String storeMessage() {
        try {
            JsonArray stored = new JsonArray();

            if (Files.exists(storageFile)) {
                try (Reader reader = Files.newBufferedReader(storageFile, StandardCharsets.UTF_8)) {
                    JsonElement existing = JsonParser.parseReader(reader);
                    if (existing != null && existing.isJsonArray()) {
                        stored = existing.getAsJsonArray();
                    }
                }
            }

            stored.add(GSON.toJsonTree(this));

            try (Writer writer = Files.newBufferedWriter(storageFile, StandardCharsets.UTF_8)) {
                GSON.toJson(stored, writer);
            }
            return "Message successfully stored";
        } catch (IOException | JsonParseException e) {
            return "Message could not be stored: " + e.getMessage();
        }
    }

    // ------------------------------------------------------------------
    // Reporting
    // ------------------------------------------------------------------

    /** Message ID, Message Hash, Recipient, Message - in that order. */
    public String getDetails() {
        return "Message ID: " + messageID + "\n"
                + "Message Hash: " + messageHash + "\n"
                + "Recipient: " + recipient + "\n"
                + "Message: " + messageText;
    }

    /** Every message sent while the program has been running. */
    public static String printMessages() {
        if (sentMessages.isEmpty()) {
            return "No messages have been sent.";
        }
        StringBuilder all = new StringBuilder();
        for (int i = 0; i < sentMessages.size(); i++) {
            if (i > 0) {
                all.append("\n\n");
            }
            all.append(sentMessages.get(i).getDetails());
        }
        return all.toString();
    }

    /** Total number of messages sent so far. */
    public static int returnTotalMessages() {
        return totalMessagesSent;
    }

    // ------------------------------------------------------------------
    // Helpers (used by the app and by unit tests)
    // ------------------------------------------------------------------

    /** Clears the session record so every unit test starts clean. */
    public static void resetSession() {
        sentMessages.clear();
        totalMessagesSent = 0;
        nextMessageNumber = 0;
        storageFile = Paths.get("storedmessages.json");
    }

    /** Points JSON storing at a different file (unit tests use a temp file). */
    public static void setStorageFile(Path file) {
        storageFile = file;
    }

    private static String generateMessageID() {
        long id = ThreadLocalRandom.current().nextLong(0L, 10_000_000_000L);
        return String.format("%010d", id);
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }
}
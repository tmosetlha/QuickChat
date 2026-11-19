package com.tmui.quickchat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Part 3 - Store data and display task report.
 *
 * Keeps the five arrays from the brief (sent messages, disregarded messages,
 * stored messages, message hashes and message IDs) and the features that use
 * them: display senders and recipients, find the longest message, search by
 * message ID or recipient, delete by message hash, and print a full report.
 *
 * The arrays are filled while the program runs (nothing is hard-coded). The
 * stored-messages array is also filled by reading the JSON file written in
 * Part 2. JSON reading approach based on the Gson documentation:
 * 
 * The sent and stored messages are the messages that still exist, so the
 * menu features work on both. Disregarded messages are kept in their own
 * array but are not searched or reported.
 *
 * @author Tshiamo
 */
public class MessageStore {

    private static final int START_SIZE = 10;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---- The five arrays from the brief ----
    private Message[] sentMessages = new Message[START_SIZE];
    private Message[] disregardedMessages = new Message[START_SIZE];
    private Message[] storedMessages = new Message[START_SIZE];
    private String[] messageHashes = new String[START_SIZE];
    private String[] messageIDs = new String[START_SIZE];

    // How many slots of each array are in use.
    private int sentCount = 0;
    private int disregardedCount = 0;
    private int storedCount = 0;
    private int trackedCount = 0;      // hashes and IDs always grow together

    private Path storageFile = Paths.get("storedmessages.json");

    // ------------------------------------------------------------------
    // Filling the arrays
    // ------------------------------------------------------------------

    /**
     * Puts a message into the sent, disregarded or stored array (using the
     * Message.SEND / DISREGARD / STORE choice) and records its hash and ID.
     * Returns false if the choice is not recognised.
     */
    public boolean addMessage(Message message, int choice) {
        if (message == null) {
            return false;
        }
        switch (choice) {
            case Message.SEND -> {
                sentMessages = grow(sentMessages, sentCount);
                sentMessages[sentCount++] = message;
            }
            case Message.DISREGARD -> {
                disregardedMessages = grow(disregardedMessages, disregardedCount);
                disregardedMessages[disregardedCount++] = message;
            }
            case Message.STORE -> {
                storedMessages = grow(storedMessages, storedCount);
                storedMessages[storedCount++] = message;
            }
            default -> {
                return false;
            }
        }

        messageHashes = grow(messageHashes, trackedCount);
        messageIDs = grow(messageIDs, trackedCount);
        messageHashes[trackedCount] = message.getMessageHash();
        messageIDs[trackedCount] = message.getMessageID();
        trackedCount++;
        return true;
    }

    /**
     * Same as above but takes the flag used in the brief's test data:
     * "Sent", "Stored" or "Disregard".
     */
    public boolean addMessage(Message message, String flag) {
        if (flag == null) {
            return false;
        }
        return switch (flag.trim().toLowerCase()) {
            case "sent" -> addMessage(message, Message.SEND);
            case "stored" -> addMessage(message, Message.STORE);
            case "disregard", "disregarded" -> addMessage(message, Message.DISREGARD);
            default -> false;
        };
    }

    /**
     * Reads the JSON file written by Message.storeMessage() into the stored
     * messages array. Messages that are already loaded are skipped. Returns
     * how many messages were loaded.
     */
    public int loadStoredMessages() {
        if (!Files.exists(storageFile)) {
            return 0;
        }
        int loaded = 0;
        try (Reader reader = Files.newBufferedReader(storageFile, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonArray()) {
                return 0;
            }
            for (JsonElement item : root.getAsJsonArray()) {
                if (!item.isJsonObject()) {
                    continue;
                }
                JsonObject saved = item.getAsJsonObject();
                if (!saved.has("messageID") || !saved.has("recipient") || !saved.has("messageText")) {
                    continue;
                }
                String id = saved.get("messageID").getAsString();
                if (indexOfID(id) >= 0) {
                    continue;                      // already in the arrays
                }
                int number = saved.has("messageNumber") ? saved.get("messageNumber").getAsInt() : 0;
                Message message = new Message(id, number,
                        saved.get("recipient").getAsString(), saved.get("messageText").getAsString());
                addMessage(message, Message.STORE);
                loaded++;
            }
        } catch (IOException | JsonParseException | IllegalStateException | NumberFormatException e) {
            return loaded;
        }
        return loaded;
    }

    /** Writes the stored messages array back to the JSON file. */
    public boolean saveStoredMessages() {
        JsonArray all = new JsonArray();
        for (int i = 0; i < storedCount; i++) {
            all.add(GSON.toJsonTree(storedMessages[i]));
        }
        try (Writer writer = Files.newBufferedWriter(storageFile, StandardCharsets.UTF_8)) {
            GSON.toJson(all, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------
    // Menu features (a to f)
    // ------------------------------------------------------------------

    /** a. The sender and recipient of every sent and stored message. */
    public String displaySendersAndRecipients(String sender) {
        if (sentCount + storedCount == 0) {
            return "There are no sent or stored messages.";
        }
        StringBuilder lines = new StringBuilder();
        for (int i = 0; i < sentCount; i++) {
            lines.append("Sender: ").append(sender)
                 .append(" | Recipient: ").append(sentMessages[i].getRecipient())
                 .append(" (Sent)\n");
        }
        for (int i = 0; i < storedCount; i++) {
            lines.append("Sender: ").append(sender)
                 .append(" | Recipient: ").append(storedMessages[i].getRecipient())
                 .append(" (Stored)\n");
        }
        return lines.toString().trim();
    }

    /** b. The text of the longest sent or stored message. */
    public String getLongestMessage() {
        Message longest = null;
        for (int i = 0; i < sentCount; i++) {
            if (longest == null || sentMessages[i].getMessageText().length() > longest.getMessageText().length()) {
                longest = sentMessages[i];
            }
        }
        for (int i = 0; i < storedCount; i++) {
            if (longest == null || storedMessages[i].getMessageText().length() > longest.getMessageText().length()) {
                longest = storedMessages[i];
            }
        }
        return (longest == null) ? "There are no sent or stored messages." : longest.getMessageText();
    }

    /** c. Finds a sent or stored message by its ID (null if there is none). */
    public Message searchByMessageID(String messageID) {
        if (messageID == null) {
            return null;
        }
        String id = messageID.trim();
        if (indexOfID(id) < 0) {
            return null;                           // not in the message ID array
        }
        int position = indexOfMessage(sentMessages, sentCount, id);
        if (position >= 0) {
            return sentMessages[position];
        }
        position = indexOfMessage(storedMessages, storedCount, id);
        return (position >= 0) ? storedMessages[position] : null;
    }

    /** d. The text of every sent or stored message for one recipient. */
    public String[] searchByRecipient(String recipient) {
        if (recipient == null) {
            return new String[0];
        }
        String wanted = recipient.trim();

        int matches = 0;
        for (int i = 0; i < sentCount; i++) {
            if (wanted.equals(sentMessages[i].getRecipient())) {
                matches++;
            }
        }
        for (int i = 0; i < storedCount; i++) {
            if (wanted.equals(storedMessages[i].getRecipient())) {
                matches++;
            }
        }

        String[] found = new String[matches];
        int next = 0;
        for (int i = 0; i < sentCount; i++) {
            if (wanted.equals(sentMessages[i].getRecipient())) {
                found[next++] = sentMessages[i].getMessageText();
            }
        }
        for (int i = 0; i < storedCount; i++) {
            if (wanted.equals(storedMessages[i].getRecipient())) {
                found[next++] = storedMessages[i].getMessageText();
            }
        }
        return found;
    }

    /**
     * e. Deletes a message using its message hash. The message leaves its
     * array and its hash and ID leave the hash and ID arrays. A deleted
     * stored message is also removed from the JSON file.
     */
    public String deleteByHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            return "Please enter a message hash.";
        }
        String wanted = hash.trim().toUpperCase();

        int position = -1;
        for (int i = 0; i < trackedCount; i++) {
            if (messageHashes[i].equals(wanted)) {
                position = i;
                break;
            }
        }
        if (position < 0) {
            return "No message found with hash " + hash.trim() + ".";
        }

        String id = messageIDs[position];
        String text = null;

        int found = indexOfMessage(sentMessages, sentCount, id);
        if (found >= 0) {
            text = sentMessages[found].getMessageText();
            sentCount = removeAt(sentMessages, sentCount, found);
        } else {
            found = indexOfMessage(storedMessages, storedCount, id);
            if (found >= 0) {
                text = storedMessages[found].getMessageText();
                storedCount = removeAt(storedMessages, storedCount, found);
                saveStoredMessages();              // keep the JSON file in step
            } else {
                found = indexOfMessage(disregardedMessages, disregardedCount, id);
                if (found >= 0) {
                    text = disregardedMessages[found].getMessageText();
                    disregardedCount = removeAt(disregardedMessages, disregardedCount, found);
                }
            }
        }

        removeAt(messageIDs, trackedCount, position);
        trackedCount = removeAt(messageHashes, trackedCount, position);

        if (text == null) {
            return "No message found with hash " + hash.trim() + ".";
        }
        // The brief shows the deleted message without its final full stop.
        String shown = text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
        return "Message: \"" + shown + "\" successfully deleted.";
    }

    /** f. A report with the full details of every sent and stored message. */
    public String displayReport() {
        if (sentCount + storedCount == 0) {
            return "There are no sent or stored messages to report.";
        }
        StringBuilder report = new StringBuilder("=== Message Report ===\n");
        appendReport(report, sentMessages, sentCount, "Sent");
        appendReport(report, storedMessages, storedCount, "Stored");
        report.append("\nTotal: ").append(sentCount).append(" sent, ")
              .append(storedCount).append(" stored");
        return report.toString();
    }

    private static void appendReport(StringBuilder report, Message[] list, int count, String status) {
        for (int i = 0; i < count; i++) {
            Message m = list[i];
            report.append("\nStatus: ").append(status)
                  .append("\nMessage ID: ").append(m.getMessageID())
                  .append("\nMessage Hash: ").append(m.getMessageHash())
                  .append("\nRecipient: ").append(m.getRecipient())
                  .append("\nMessage: ").append(m.getMessageText())
                  .append("\n");
        }
    }

    // ------------------------------------------------------------------
    // Reading the arrays (copies, so callers cannot change them)
    // ------------------------------------------------------------------

    public Message[] getSentMessages() {
        return Arrays.copyOf(sentMessages, sentCount);
    }

    public Message[] getDisregardedMessages() {
        return Arrays.copyOf(disregardedMessages, disregardedCount);
    }

    public Message[] getStoredMessages() {
        return Arrays.copyOf(storedMessages, storedCount);
    }

    public String[] getMessageHashes() {
        return Arrays.copyOf(messageHashes, trackedCount);
    }

    public String[] getMessageIDs() {
        return Arrays.copyOf(messageIDs, trackedCount);
    }

    public String[] getSentMessageTexts() {
        return texts(sentMessages, sentCount);
    }

    public String[] getStoredMessageTexts() {
        return texts(storedMessages, storedCount);
    }

    /** Points JSON reading and writing at a different file (unit tests use a temporary file). */
    public void setStorageFile(Path file) {
        this.storageFile = file;
    }

    // ------------------------------------------------------------------
    // Array helpers
    // ------------------------------------------------------------------

    // Doubles the array when every slot is used.
    private static Message[] grow(Message[] array, int used) {
        return (used == array.length) ? Arrays.copyOf(array, array.length * 2) : array;
    }

    private static String[] grow(String[] array, int used) {
        return (used == array.length) ? Arrays.copyOf(array, array.length * 2) : array;
    }

    // Closes the gap left by a removed item; returns the new number of items.
    private static int removeAt(Object[] array, int used, int index) {
        for (int i = index; i < used - 1; i++) {
            array[i] = array[i + 1];
        }
        array[used - 1] = null;
        return used - 1;
    }

    private int indexOfID(String id) {
        for (int i = 0; i < trackedCount; i++) {
            if (messageIDs[i].equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static int indexOfMessage(Message[] list, int used, String id) {
        for (int i = 0; i < used; i++) {
            if (list[i].getMessageID().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private static String[] texts(Message[] list, int used) {
        String[] result = new String[used];
        for (int i = 0; i < used; i++) {
            result[i] = list[i].getMessageText();
        }
        return result;
    }
}
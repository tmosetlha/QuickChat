package com.tmui.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MessageStore (Part 3 - Store data and display task report),
 * using the five test messages from the brief.
 *
 * @author Tshiamo
 */
public class MessageStoreTest {

    // Test data from the brief. The IDs are fixed here so the hashes and
    // searches can be predicted (message 4's ID is the value in the brief).
    private static final String LATE = "Where are you? You are late! I have asked you to be on time.";
    private static final String LEAVING = "Ok, I am leaving without you.";

    private MessageStore store;
    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    public void setUp() {
        Message.resetSession();
        store = new MessageStore();

        message1 = new Message("1111111111", 0, "+27834557896", "Did you get the cake?");
        message2 = new Message("2222222222", 1, "+27838884567", LATE);
        message3 = new Message("3333333333", 2, "+27838884567", "Yohoooo, I am at your gate.");
        message4 = new Message("0838884567", 3, "0838884567", "It is dinner time!");
        message5 = new Message("5555555555", 4, "+27838884567", LEAVING);
    }

    @AfterEach
    public void tearDown() {
        Message.resetSession();
    }

    // Fills the arrays with messages 1 to 4 (the sent-array and longest-message tests).
    private void addMessages1To4() {
        store.addMessage(message1, "Sent");
        store.addMessage(message2, "Stored");
        store.addMessage(message3, "Disregard");
        store.addMessage(message4, "Sent");
    }

    // Fills the arrays with all five messages, using a temporary JSON file.
    private void addAllMessages(Path tempDir) {
        store.setStorageFile(tempDir.resolve("storedmessages.json"));
        addMessages1To4();
        store.addMessage(message5, "Stored");
    }

    // ---- Tests from the brief ----

    @Test
    public void testSentMessagesArrayCorrectlyPopulated() {
        addMessages1To4();
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time!"},
                store.getSentMessageTexts());
    }

    @Test
    public void testDisplayTheLongestMessage() {
        addMessages1To4();
        assertEquals(LATE, store.getLongestMessage());
    }

    @Test
    public void testSearchForMessageID() {
        addMessages1To4();
        Message found = store.searchByMessageID("0838884567");
        assertNotNull(found);
        assertEquals("It is dinner time!", found.getMessageText());
    }

    @Test
    public void testSearchAllMessagesForARecipient(@TempDir Path tempDir) {
        addAllMessages(tempDir);
        assertArrayEquals(new String[]{LATE, LEAVING}, store.searchByRecipient("+27838884567"));
    }

    @Test
    public void testDeleteAMessageUsingItsHash(@TempDir Path tempDir) {
        addAllMessages(tempDir);
        String result = store.deleteByHash(message2.getMessageHash());

        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time\" successfully deleted.",
                result);
        assertArrayEquals(new String[]{LEAVING}, store.getStoredMessageTexts());
    }

    @Test
    public void testDisplayReportShowsSentMessages() {
        addMessages1To4();
        String report = store.displayReport();

        for (Message sent : new Message[]{message1, message4}) {
            assertTrue(report.contains(sent.getMessageHash()));
            assertTrue(report.contains(sent.getRecipient()));
            assertTrue(report.contains(sent.getMessageText()));
        }
    }

    // ---- The other arrays and features ----

    @Test
    public void testDisregardedArrayHoldsDisregardedMessages() {
        addMessages1To4();
        Message[] disregarded = store.getDisregardedMessages();
        assertEquals(1, disregarded.length);
        assertEquals("Yohoooo, I am at your gate.", disregarded[0].getMessageText());
    }

    @Test
    public void testHashAndIdArraysHoldEveryMessage(@TempDir Path tempDir) {
        addAllMessages(tempDir);
        assertArrayEquals(new String[]{"1111111111", "2222222222", "3333333333", "0838884567", "5555555555"},
                store.getMessageIDs());
        assertEquals(5, store.getMessageHashes().length);
        assertEquals("11:0:DIDCAKE", store.getMessageHashes()[0]);
    }

    @Test
    public void testDisregardedMessagesAreNotSearchedOrReported() {
        addMessages1To4();
        assertNull(store.searchByMessageID("3333333333"));
        assertFalse(store.displayReport().contains("Yohoooo"));
    }

    @Test
    public void testDisplaySendersAndRecipients() {
        addMessages1To4();
        String shown = store.displaySendersAndRecipients("Kyle Smith");
        assertTrue(shown.contains("Sender: Kyle Smith | Recipient: +27834557896 (Sent)"));
        assertTrue(shown.contains("Recipient: +27838884567 (Stored)"));
        assertFalse(shown.contains("+27838884567 (Sent)"));
    }

    @Test
    public void testDeleteSentMessageRemovesItsHashAndId() {
        addMessages1To4();
        store.deleteByHash(message1.getMessageHash());
        assertEquals(1, store.getSentMessages().length);
        assertEquals(3, store.getMessageHashes().length);
        assertNull(store.searchByMessageID("1111111111"));
    }

    @Test
    public void testDeleteWithUnknownHash() {
        addMessages1To4();
        assertEquals("No message found with hash ZZ:9:NONE.", store.deleteByHash("ZZ:9:NONE"));
        assertEquals(4, store.getMessageHashes().length);
    }

    @Test
    public void testDeleteByHashIgnoresCapitalLetters() {
        addMessages1To4();
        String result = store.deleteByHash(message4.getMessageHash().toLowerCase());
        assertEquals("Message: \"It is dinner time!\" successfully deleted.", result);
    }

    @Test
    public void testReportListsFullDetailsOfSentAndStoredMessages(@TempDir Path tempDir) {
        addAllMessages(tempDir);
        String report = store.displayReport();
        assertTrue(report.contains("Message ID: 2222222222"));
        assertTrue(report.contains("Status: Stored"));
        assertTrue(report.contains("Total: 2 sent, 2 stored"));
    }

    @Test
    public void testEmptyStoreMessages() {
        assertEquals("There are no sent or stored messages.", store.getLongestMessage());
        assertEquals("There are no sent or stored messages to report.", store.displayReport());
        assertEquals(0, store.searchByRecipient("+27838884567").length);
        assertNull(store.searchByMessageID("1111111111"));
    }

    @Test
    public void testArraysGrowWhenFull() {
        for (int i = 0; i < 25; i++) {
            store.addMessage(new Message(String.format("%010d", i), i, "+27834557896", "Message number " + i), Message.SEND);
        }
        assertEquals(25, store.getSentMessages().length);
        assertEquals(25, store.getMessageHashes().length);
        assertEquals("Message number 24", store.getSentMessageTexts()[24]);
    }

    @Test
    public void testFlagsAreRecognised() {
        assertTrue(store.addMessage(message1, "Sent"));
        assertTrue(store.addMessage(message2, "stored"));
        assertTrue(store.addMessage(message3, "Disregard"));
        assertFalse(store.addMessage(message4, "Unknown"));
        assertFalse(store.addMessage(message4, 99));
        assertEquals(3, store.getMessageIDs().length);
    }

    // ---- Reading and writing the JSON file ----

    @Test
    public void testStoredArrayIsFilledFromTheJsonFile(@TempDir Path tempDir) {
        Path file = tempDir.resolve("storedmessages.json");
        Message.setStorageFile(file);
        message2.sentMessage(Message.STORE);
        message5.sentMessage(Message.STORE);

        store.setStorageFile(file);
        assertEquals(2, store.loadStoredMessages());
        assertArrayEquals(new String[]{LATE, LEAVING}, store.getStoredMessageTexts());
        assertEquals(message2.getMessageHash(), store.getMessageHashes()[0]);

        // Loading again must not add the same messages twice.
        assertEquals(0, store.loadStoredMessages());
        assertEquals(2, store.getStoredMessages().length);
    }

    @Test
    public void testLoadingWithNoFileLoadsNothing(@TempDir Path tempDir) {
        store.setStorageFile(tempDir.resolve("missing.json"));
        assertEquals(0, store.loadStoredMessages());
    }

    @Test
    public void testDeletingAStoredMessageUpdatesTheJsonFile(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("storedmessages.json");
        Message.setStorageFile(file);
        message2.sentMessage(Message.STORE);
        message5.sentMessage(Message.STORE);

        store.setStorageFile(file);
        store.loadStoredMessages();
        store.deleteByHash(message2.getMessageHash());

        String json = Files.readString(file);
        assertFalse(json.contains("Where are you?"));
        assertTrue(json.contains("Ok, I am leaving without you."));

        MessageStore fresh = new MessageStore();
        fresh.setStorageFile(file);
        assertEquals(1, fresh.loadStoredMessages());
    }
}
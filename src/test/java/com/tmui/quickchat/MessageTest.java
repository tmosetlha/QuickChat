package com.tmui.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Message (Part 2 - Sending messages), using the test
 * data from the brief.
 *
 * @author Tshiamo
 */
public class MessageTest {

    // Test data from the brief
    private static final String RECIPIENT_1 = "+27718693002";
    private static final String MESSAGE_1 = "Hi Mike, can you join us for dinner tonight?";
    private static final String RECIPIENT_2 = "08575975889";
    private static final String MESSAGE_2 = "Hi Keegan, did you receive the payment?";

    @BeforeEach
    public void setUp() {
        Message.resetSession();
    }

    // ---- Message length (success and failure) ----

    @Test
    public void testMessageLengthSuccess() {
        assertEquals("Message ready to send.", Message.checkMessageLength(MESSAGE_1));
    }

    @Test
    public void testMessageLengthFailure() {
        String tooLong = "a".repeat(255);   // 5 characters over the limit
        assertEquals("Message exceeds 250 characters by 5; please reduce the size.",
                Message.checkMessageLength(tooLong));
    }

    // ---- Recipient number (reuses the Part 1 cell phone check) ----

    @Test
    public void testRecipientCorrectlyFormatted() {
        assertEquals("Cell phone number successfully captured.",
                Message.checkRecipientCell(RECIPIENT_1));
    }

    @Test
    public void testRecipientIncorrectlyFormatted() {
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                Message.checkRecipientCell(RECIPIENT_2));
    }

    // ---- Message ID ----

    @Test
    public void testGeneratedMessageIdIsTenDigits() {
        Message message = new Message(RECIPIENT_1, MESSAGE_1);
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
    }

    @Test
    public void testMessageIdLongerThanTenCharactersFails() {
        Message message = new Message("12345678901", 0, RECIPIENT_1, MESSAGE_1);
        assertFalse(message.checkMessageID());
    }

    // ---- Message hash ----

    @Test
    public void testMessageHashTestCase1() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void testMessageHashesInALoop() {
        String[] ids = {"0012345678", "0187654321"};
        String[] recipients = {RECIPIENT_1, RECIPIENT_2};
        String[] texts = {MESSAGE_1, MESSAGE_2};
        String[] expected = {"00:0:HITONIGHT", "01:1:HIPAYMENT"};

        for (int i = 0; i < texts.length; i++) {
            Message message = new Message(ids[i], i, recipients[i], texts[i]);
            assertEquals(expected[i], message.createMessageHash());
        }
    }

    // ---- Send, disregard and totals ----

    @Test
    public void testSendMessage() {
        Message message = new Message(RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully sent", message.sentMessage(Message.SEND));
    }

    @Test
    public void testDisregardMessage() {
        Message message = new Message(RECIPIENT_2, MESSAGE_2);
        assertEquals("Press 0 to delete the message", message.sentMessage(Message.DISREGARD));
    }

    @Test
    public void testTotalMessagesAfterSendAndDiscard() {
        Message first = new Message(RECIPIENT_1, MESSAGE_1);
        Message second = new Message(RECIPIENT_2, MESSAGE_2);

        first.sentMessage(Message.SEND);
        second.sentMessage(Message.DISREGARD);

        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void testPrintMessagesListsSentMessages() {
        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        message.sentMessage(Message.SEND);

        String printed = Message.printMessages();
        assertTrue(printed.contains("00:0:HITONIGHT"));
        assertTrue(printed.contains(RECIPIENT_1));
        assertTrue(printed.contains(MESSAGE_1));
    }

    // ---- JSON storing ----

    @Test
    public void testStoreMessageWritesJson(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("storedmessages.json");
        Message.setStorageFile(file);

        Message message = new Message("0012345678", 0, RECIPIENT_1, MESSAGE_1);
        assertEquals("Message successfully stored", message.sentMessage(Message.STORE));

        String json = Files.readString(file);
        assertTrue(json.contains("00:0:HITONIGHT"));
        assertTrue(json.contains(MESSAGE_1));
    }
}
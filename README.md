# QuickChat

[![QuickChat CI](https://github.com/tmosetlha/QuickChat/actions/workflows/maven.yml/badge.svg)](https://github.com/tmosetlha/QuickChat/actions/workflows/maven.yml)

A Java console chat application: user registration and login, then sending, disregarding and storing messages, and a Stored Messages menu to search, delete and report on them. Built with Maven, tested with JUnit 5, and checked automatically by GitHub Actions on every push.

## Features

### Registration and login

- **Username:** must contain an underscore and be no more than five characters long
- **Password:** at least eight characters, with a capital letter, a number and a special character
- **Cell phone number:** South African international format (`+27` followed by nine digits), checked with a regular expression
- **Login:** the entered details are checked against the registered user, and a successful login greets the user by name

### Sending messages

- Messages can only be sent after a successful login
- The user says how many messages they want to enter, then uses a numeric menu:
  1. Send Messages
  2. Show recently sent messages (shows "Coming Soon.")
  3. Quit
- Every message has:
  - a random **ten-digit Message ID**
  - a **recipient** cell phone number (validated, must include the international code)
  - the **message text**, up to 250 characters
  - a **Message Hash**: the first two digits of the ID, a colon, the message number, a colon, then the first and last words of the message in capitals, for example `00:0:HITONIGHT`
- For each message the user can **send** it, **disregard** it (press `0` to delete), or **store** it to send later in `storedmessages.json`
- After a message is sent its full details are shown (Message ID, Message Hash, Recipient, Message). The total number of messages sent is shown once all messages have been entered.

### Stored messages

- A fourth main menu option, **Stored Messages**, opens a menu (a to f) that works on the sent and stored messages
- The messages are kept in five arrays that are filled while the program runs: **sent messages**, **disregarded messages**, **stored messages** (read from `storedmessages.json` when the program starts), **message hashes** and **message IDs**. The arrays grow automatically when they are full.
- The menu options:
  - **a.** display the sender and recipient of every message
  - **b.** display the longest message
  - **c.** search for a message ID and display its recipient and message
  - **d.** search for all the messages for a particular recipient
  - **e.** delete a message using its message hash (a deleted stored message is also removed from the JSON file)
  - **f.** display a report with the full details of every message

## Project structure

```
QuickChat/
├── .github/workflows/maven.yml      # GitHub Actions: build and run all unit tests on every push
├── pom.xml                          # Maven configuration (Java 21, JUnit 5, Gson)
└── src/
    ├── main/java/com/tmui/quickchat/
    │   ├── QuickChat.java           # Console application and menu
    │   ├── Register.java            # Registration and validation rules
    │   ├── Login.java               # Login checks and status messages
    │   ├── Message.java             # Message details, hash, send / disregard / store, JSON storage
    │   └── MessageStore.java        # The five arrays and the stored-messages features
    └── test/java/com/tmui/quickchat/
        ├── RegisterTest.java
        ├── LoginTest.java
        ├── MessageTest.java
        └── MessageStoreTest.java
```

## Requirements

- JDK 21 or newer
- Maven (bundled with NetBeans)

## Build, run and test

**NetBeans:** right-click the project and choose *Clean and Build*. Run the application by right-clicking `QuickChat.java` and choosing *Run File*. Right-click the project and choose *Test* to run the unit tests.

**Command line:**

```
mvn clean test
mvn compile exec:java
```

## Unit tests

JUnit 5 tests cover:

- username, password and cell phone validation, registration, and login messages
- message length, recipient format, message ID and message hash, using the brief's test data
- sending, disregarding and storing messages, the running total, and JSON storage
- the five arrays, the longest message, searching by ID and by recipient, deleting by hash, the report, and reading and writing the JSON file, using the brief's five test messages

## Continuous integration

`.github/workflows/maven.yml` builds the project with Maven on JDK 21 and runs all unit tests on every push and on pull requests to `main`.

## Git workflow

`main` always holds working code. Each part is built on its own feature branch (`part2-messages` and `part3-stored-messages`) and merged into `main` once the automated tests pass.

## References

- South African mobile number regular expression, adapted from the regex101 community library: <https://regex101.com/library?page=177>
- JSON storage with Gson: <https://github.com/google/gson>

## Author

Unathi Mauvha - ST10481873
package com.example.chatpet;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.UserDao;
import com.example.chatpet.data.local.UserEntity;
import com.example.chatpet.data.local.MessageDao;
import com.example.chatpet.data.local.MessageEntity;
import com.example.chatpet.data.local.JournalEntryDao;
import com.example.chatpet.data.local.JournalEntryEntity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28,
        manifest = Config.NONE,
        qualifiers = "en-rUS")
public class DatabaseUnitTest {

    private ChatPetDatabase db;
    private UserDao userDao;
    private MessageDao messageDao;
    private JournalEntryDao journalDao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(context, ChatPetDatabase.class)
                // OK for tests
                .allowMainThreadQueries()
                .build();

        userDao = db.userDao();
        messageDao = db.messageDao();
        journalDao = db.journalEntryDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    // 1) Insert User → Retrieve by Username
    @Test
    public void insertUser_andGetByUsername_returnsSameUser() {
        // Arrange
        Date birthday = new Date(100, 0, 1); // Jan 1 2000 (year is 1900 + 100)
        long createdAt = System.currentTimeMillis();

        // Adjust this constructor to match your actual UserEntity
        UserEntity user = new UserEntity(
                "ryan",
                "password123",
                "Ryan B",
                "ryanb@email.com",
                birthday,
                /* avatarResId = */ 1,
                createdAt
        );

        // Act
        long userId = userDao.insert(user);
        UserEntity loaded = userDao.getByUsername("ryan");

        // Assert
        assertNotEquals(0, userId);
        assertNotNull(loaded);
        assertEquals("ryan", loaded.username);
        assertEquals("Ryan B", loaded.name);
        assertEquals("ryanb@email.com", loaded.email);
        assertEquals(birthday, loaded.birthday);
    }

    // 2) Prevent Duplicate Username Insertion
    @Test
    public void insertDuplicateUsername_throwsOrIsRejected() {
        Date birthday = new Date(100, 0, 1);
        long createdAt = System.currentTimeMillis();

        UserEntity first = new UserEntity(
                "duplicateUser",
                "pass1",
                "First User",
                "first@email.com",
                birthday,
                1,
                createdAt
        );

        UserEntity second = new UserEntity(
                "duplicateUser",
                "pass2",
                "Second User",
                "second@email.com",
                birthday,
                2,
                createdAt + 1000
        );

        long firstId = userDao.insert(first);
        assertNotEquals(0, firstId);

        boolean duplicateRejected = false;
        try {
            long secondId = userDao.insert(second);
            // If you used OnConflictStrategy.IGNORE, insert may return -1
            if (secondId == -1) {
                duplicateRejected = true;
            }
        } catch (Exception e) {
            // If you used UNIQUE constraint without IGNORE, an exception is expected
            duplicateRejected = true;
        }

        assertTrue("Duplicate username should be rejected or throw", duplicateRejected);
    }

    // 3) Insert Messages → Retrieve in Chronological Order
    @Test
    public void insertMessages_forPet_returnedInChronologicalOrder() {
        long petId = 42L;

        long t1 = System.currentTimeMillis();
        long t2 = t1 + 1000;
        long t3 = t1 + 2000;

        MessageEntity m1 = new MessageEntity(petId, "Hi 1", true, t2);
        MessageEntity m2 = new MessageEntity(petId, "Hi 2", true, t1);
        MessageEntity m3 = new MessageEntity(petId, "Hi 3", false, t3);

        messageDao.insert(m1);
        messageDao.insert(m2);
        messageDao.insert(m3);

        // Adjust method name to match your DAO (e.g. getForPetOrderedByTimestampAsc)
        List<MessageEntity> messages = messageDao.getByPetId(petId);

        assertEquals(3, messages.size());
        // Expect ascending order by timestamp: m2 (t1), m1 (t2), m3 (t3)
        assertEquals("Hi 2", messages.get(0).content);
        assertEquals("Hi 1", messages.get(1).content);
        assertEquals("Hi 3", messages.get(2).content);
    }

    // 4) Message countTodayForPet (same behavior as MessageRepository.countMessagesTodayForPet)
    @Test
    public void countMessagesTodayForPet_returnsCorrectCount() {
        long petId = 100L;

        long now = System.currentTimeMillis();

        // Start of "today"
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();

        // Message yesterday (should NOT be counted)
        long yesterday = dayStart - 1000;
        messageDao.insert(new MessageEntity(petId, "Yesterday", true, yesterday));

        // Messages today (should be counted)
        messageDao.insert(new MessageEntity(petId, "Today 1", true, dayStart + 10));
        messageDao.insert(new MessageEntity(petId, "Today 2", true, dayStart + 20));

        // Adjust method name to match your DAO
        int count = messageDao.countForPetInRange(petId, dayStart, now);

        assertEquals(2, count);
    }

    // 5) JournalEntry: get entries in time range (used by “today” logic)
    @Test
    public void getEntriesForPetInRange_returnsOnlyInRange() {
        long petId = 7L;

        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();
        long dayEnd = dayStart + 24L * 60L * 60L * 1000L; // start of tomorrow

        // Entry yesterday (out of range)
        JournalEntryEntity yesterday = new JournalEntryEntity(
                petId,
                "Yesterday entry",
                "",
                dayStart - 1000
        );

        // Entries today (in range)
        JournalEntryEntity today1 = new JournalEntryEntity(
                petId,
                "Today entry 1",
                "",
                dayStart + 1000
        );
        JournalEntryEntity today2 = new JournalEntryEntity(
                petId,
                "Today entry 2",
                "",
                dayStart + 2000
        );

        journalDao.insert(yesterday);
        journalDao.insert(today1);
        journalDao.insert(today2);

        // Adjust method name to match your DAO
        List<JournalEntryEntity> todaysEntries =
                journalDao.getByPetIdInRange(petId, dayStart, dayEnd);

        assertEquals(2, todaysEntries.size());
        assertEquals("Today entry 1", todaysEntries.get(0).title);
        assertEquals("Today entry 2", todaysEntries.get(1).title);
    }
}
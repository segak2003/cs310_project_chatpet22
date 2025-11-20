package com.example.chatpet;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.chatpet.data.local.JournalEntryEntity;
import com.example.chatpet.data.local.PetEntity;
import com.example.chatpet.data.local.UserEntity;
import com.example.chatpet.data.repository.JournalEntryRepository;
import com.example.chatpet.data.repository.MessageRepository;
import com.example.chatpet.data.repository.PetRepository;
import com.example.chatpet.data.repository.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DatabaseBlackBoxTest {

    private Context context;
    private UserRepository userRepo;
    private PetRepository petRepo;
    private MessageRepository messageRepo;
    private JournalEntryRepository journalRepo;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        userRepo = new UserRepository(context);
        petRepo = new PetRepository(context);
        messageRepo = new MessageRepository(context);
        journalRepo = new JournalEntryRepository(context);
    }

    // 1) createUser + getUserByUsername + setActiveUser / getActiveUser
    @Test
    public void userRepository_createAndActivateUser_works() throws Exception {
        String username = "testuser_repo";
        String password = "password123";
        String name = "Test User";
        Date birthday = new Date(100, 0, 1); // Jan 1 2000
        int avatar = 1;

        CountDownLatch createLatch = new CountDownLatch(1);
        final long[] createdId = new long[1];

        userRepo.createUser(username, password, name, birthday, avatar, userId -> {
            createdId[0] = userId;
            createLatch.countDown();
        });

        assertTrue("createUser callback not called",
                createLatch.await(5, TimeUnit.SECONDS));
        assertTrue("User ID should be > 0", createdId[0] > 0);

        // Blocking getters should see the same user
        UserEntity loaded = userRepo.getUserByUsername(username);
        assertNotNull("User should be found by username", loaded);
        assertEquals(createdId[0], loaded.userId);
        assertEquals(name, loaded.name);
        assertEquals(birthday, loaded.birthday);

        // Now set as active user
        CountDownLatch activeLatch = new CountDownLatch(1);
        final long[] activeId = new long[1];

        userRepo.setActiveUser(username, id -> {
            activeId[0] = id;
            activeLatch.countDown();
        });

        assertTrue("setActiveUser callback not called",
                activeLatch.await(5, TimeUnit.SECONDS));
        assertEquals("Active user id should match created user id",
                createdId[0], activeId[0]);

        UserEntity active = userRepo.getActiveUser();
        assertNotNull("getActiveUser should return a user", active);
        assertEquals(createdId[0], active.userId);
    }

    // 2) validatePassword returns true for correct password and false for wrong one
    @Test
    public void userRepository_validatePassword_correctAndIncorrect() throws Exception {
        String username = "login_user";
        String correctPassword = "correct_pw";
        String wrongPassword = "wrong_pw";

        CountDownLatch createLatch = new CountDownLatch(1);
        userRepo.createUser(username, correctPassword, "Login User", new Date(), 1, id -> {
            createLatch.countDown();
        });
        assertTrue("createUser callback not called",
                createLatch.await(5, TimeUnit.SECONDS));

        // Correct password
        CountDownLatch okLatch = new CountDownLatch(1);
        final boolean[] okResult = new boolean[1];

        userRepo.validatePassword(username, correctPassword, success -> {
            okResult[0] = success;
            okLatch.countDown();
        });

        assertTrue("validatePassword (correct) callback not called",
                okLatch.await(5, TimeUnit.SECONDS));
        assertTrue("Correct password should validate", okResult[0]);

        // Wrong password
        CountDownLatch badLatch = new CountDownLatch(1);
        final boolean[] badResult = new boolean[1];

        userRepo.validatePassword(username, wrongPassword, success -> {
            badResult[0] = success;
            badLatch.countDown();
        });

        assertTrue("validatePassword (wrong) callback not called",
                badLatch.await(5, TimeUnit.SECONDS));
        assertFalse("Wrong password should NOT validate", badResult[0]);
    }

    // 3) createPetForUser + getPetForUser + setActivePetByUserId / getActivePet
    @Test
    public void petRepository_createAndActivatePet_works() throws Exception {
        // First create a user
        CountDownLatch userLatch = new CountDownLatch(1);
        final long[] userIdHolder = new long[1];

        userRepo.createUser("pet_owner", "pw", "Owner", new Date(), 1, id -> {
            userIdHolder[0] = id;
            userLatch.countDown();
        });

        assertTrue("User create callback not called",
                userLatch.await(5, TimeUnit.SECONDS));
        long userId = userIdHolder[0];
        assertTrue(userId > 0);

        // Create pet for that user
        CountDownLatch petLatch = new CountDownLatch(1);
        final long[] petIdHolder = new long[1];

        petRepo.createPetForUser(userId, "Fluffy", "dog", petId -> {
            petIdHolder[0] = petId;
            petLatch.countDown();
        });

        assertTrue("createPetForUser callback not called",
                petLatch.await(5, TimeUnit.SECONDS));
        long petId = petIdHolder[0];
        assertTrue(petId > 0);

        // Synchronous getter
        PetEntity pet = petRepo.getPetForUser(userId);
        assertNotNull("Pet should be returned for user", pet);
        assertEquals(petId, pet.petId);
        assertEquals("Fluffy", pet.name);
        assertEquals("dog", pet.animal);

        // Set active pet by user
        petRepo.setActivePetByUserId(userId);

        // Give the background executor a moment
        Thread.sleep(500);

        PetEntity activePet = petRepo.getActivePet();
        assertNotNull("Active pet should not be null", activePet);
        assertEquals(petId, activePet.petId);
    }

    // 4) MessageRepository: sendUserMessage + savePetReply + getMessagesForPet + countMessagesTodayForPet
    @Test
    public void messageRepository_sendAndCountMessages_works() throws Exception {
        // Create user and pet first
        CountDownLatch userLatch = new CountDownLatch(1);
        final long[] userIdHolder = new long[1];

        userRepo.createUser("msg_user", "pw", "Msg User", new Date(), 1, id -> {
            userIdHolder[0] = id;
            userLatch.countDown();
        });
        assertTrue(userLatch.await(5, TimeUnit.SECONDS));
        long userId = userIdHolder[0];

        CountDownLatch petLatch = new CountDownLatch(1);
        final long[] petIdHolder = new long[1];
        petRepo.createPetForUser(userId, "Chatter", "cat", id -> {
            petIdHolder[0] = id;
            petLatch.countDown();
        });
        assertTrue(petLatch.await(5, TimeUnit.SECONDS));
        long petId = petIdHolder[0];

        // Use repository methods (synchronous)
        long m1 = messageRepo.sendUserMessage(petId, "Hello, pet!");
        long m2 = messageRepo.savePetReply(petId, "Meow!");
        assertTrue(m1 > 0);
        assertTrue(m2 > 0);

        List<com.example.chatpet.data.local.MessageEntity> messages =
                messageRepo.getMessagesForPet(petId);

        // There may be pre-existing messages in the DB if tests reuse it,
        // but we at least expect the two we just inserted.
        assertTrue("Should have at least 2 messages", messages.size() >= 2);

        int todayCount = messageRepo.countMessagesTodayForPet(petId);
        assertTrue("Today message count should be >= 2", todayCount >= 2);
    }

    // 5) JournalEntryRepository: createJournalEntry + getEntriesTodayForPet + deleteEntry
    @Test
    public void journalEntryRepository_createTodayAndDelete_works() throws Exception {
        // Create user and pet first
        CountDownLatch userLatch = new CountDownLatch(1);
        final long[] userIdHolder = new long[1];

        userRepo.createUser("journal_user", "pw", "Journal User", new Date(), 1, id -> {
            userIdHolder[0] = id;
            userLatch.countDown();
        });
        assertTrue(userLatch.await(5, TimeUnit.SECONDS));
        long userId = userIdHolder[0];

        CountDownLatch petLatch = new CountDownLatch(1);
        final long[] petIdHolder = new long[1];
        petRepo.createPetForUser(userId, "DiaryPet", "dragon", id -> {
            petIdHolder[0] = id;
            petLatch.countDown();
        });
        assertTrue(petLatch.await(5, TimeUnit.SECONDS));
        long petId = petIdHolder[0];

        // Create a journal entry for today
        long entryId = journalRepo.createJournalEntry(
                petId,
                "Great day",
                "We had a wonderful time today!"
        );
        assertTrue(entryId > 0);

        List<JournalEntryEntity> todays = journalRepo.getEntriesTodayForPet(petId);
        assertFalse("Should have at least one entry for today", todays.isEmpty());

        JournalEntryEntity entry = null;
        for (JournalEntryEntity e : todays) {
            if (e.entryId == entryId) {
                entry = e;
                break;
            }
        }
        assertNotNull("Created entry should be present in today's list", entry);
        assertEquals("Great day", entry.title);

        // Now delete it (async)
        journalRepo.deleteEntry(entryId);
        Thread.sleep(500); // allow ioExecutor to run

        JournalEntryEntity afterDelete = journalRepo.getEntryById(entryId);
        assertNull("Entry should be deleted", afterDelete);
    }
}
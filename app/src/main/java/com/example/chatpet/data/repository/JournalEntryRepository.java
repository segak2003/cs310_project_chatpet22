package com.example.chatpet.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.JournalEntryDao;
import com.example.chatpet.data.local.JournalEntryEntity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class JournalEntryRepository {

    private final JournalEntryDao journalDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public JournalEntryRepository(Context context) {
        ChatPetDatabase db = ChatPetDatabase.getInstance(context.getApplicationContext());
        this.journalDao = db.journalEntryDao();
    }

    /** Observe all journal entries for a pet, newest first. */
    public LiveData<List<JournalEntryEntity>> observeEntriesForPet(long petId) {
        return journalDao.observeByPetId(petId);
    }

    /** Get all journal entries for a pet. */
    public List<JournalEntryEntity> getEntriesForPet(long petId) {
        return journalDao.getByPetId(petId);
    }

    /** Get a single entry by its ID. */
    public JournalEntryEntity getEntryById(long entryId) {
        return journalDao.getById(entryId);
    }

    /** Get entries in a time range (e.g., last week/month). */
    public List<JournalEntryEntity> getEntriesForPetInRange(long petId, long startMs, long endMs) {
        return journalDao.getByPetIdInRange(petId, startMs, endMs);
    }

    /** Get entries created today. */
    public List<JournalEntryEntity> getEntriesTodayForPet(long petId) {
        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        // Set to start of *today* in the device's local timezone
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();

        return journalDao.getByPetIdInRange(petId, dayStart, now);
    }

    public long createJournalEntry(long petId, String title, String content) {
        // If your JournalEntryEntity constructor still has mood/updatedAt params,
        // you can just pass null and createdAtMs for updatedAt.
        long now = System.currentTimeMillis();
        JournalEntryEntity entry = new JournalEntryEntity(
                petId,
                title,
                content,
                now
        );
        return journalDao.insert(entry);
    }

    /** Delete a single entry. */
    public void deleteEntry(long entryId) {
        ioExecutor.execute(() -> {
            JournalEntryEntity entry = journalDao.getById(entryId);
            if (entry != null) {
                journalDao.delete(entry);
            }
        });
    }

    /** Delete all entries for a pet (on reset). */
    public void deleteEntriesForPet(long petId) {
        ioExecutor.execute(() -> journalDao.deleteByPetId(petId));
    }


}

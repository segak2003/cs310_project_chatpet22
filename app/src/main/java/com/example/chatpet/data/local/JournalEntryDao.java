package com.example.chatpet.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JournalEntryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(JournalEntryEntity entry);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    List<Long> insertAll(List<JournalEntryEntity> entries);

    @Update
    int update(JournalEntryEntity entry);

    @Delete
    int delete(JournalEntryEntity entry);

    @Query("SELECT * FROM journal_entries WHERE entry_id = :entryId LIMIT 1")
    JournalEntryEntity getById(long entryId);

    @Query("SELECT * FROM journal_entries WHERE pet_id = :petId ORDER BY created_at ASC")
    List<JournalEntryEntity> getByPetId(long petId);

    @Query("SELECT * FROM journal_entries WHERE pet_id = :petId ORDER BY created_at ASC")
    LiveData<List<JournalEntryEntity>> observeByPetId(long petId);

    @Query("SELECT * FROM journal_entries WHERE pet_id = :petId AND created_at BETWEEN :startMs AND :endMs ORDER BY created_at ASC")
    List<JournalEntryEntity> getByPetIdInRange(long petId, long startMs, long endMs);

    @Query("DELETE FROM journal_entries WHERE pet_id = :petId")
    int deleteByPetId(long petId);

    @Query("SELECT COUNT(*) FROM journal_entries WHERE pet_id = :petId")
    int countForPet(long petId);
}
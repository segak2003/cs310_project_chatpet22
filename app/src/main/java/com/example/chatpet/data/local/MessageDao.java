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
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(MessageEntity message);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    List<Long> insertAll(List<MessageEntity> messages);

    @Update
    int update(MessageEntity message);

    @Delete
    int delete(MessageEntity message);

    @Query("SELECT * FROM messages WHERE message_id = :messageId LIMIT 1")
    MessageEntity getById(long messageId);

    @Query("SELECT * FROM messages WHERE pet_id = :petId ORDER BY created_at ASC")
    List<MessageEntity> getByPetId(long petId);

    @Query("SELECT * FROM messages WHERE pet_id = :petId ORDER BY created_at ASC")
    LiveData<List<MessageEntity>> observeByPetId(long petId);

    @Query("SELECT * FROM messages WHERE pet_id = :petId ORDER BY created_at ASC LIMIT :limit")
    List<MessageEntity> getByPetIdLimited(long petId, int limit);

    @Query("SELECT * FROM messages WHERE pet_id = :petId AND created_at BETWEEN :startMs AND :endMs ORDER BY created_at ASC")
    List<MessageEntity> getByPetIdInRange(long petId, long startMs, long endMs);

    @Query("DELETE FROM messages WHERE pet_id = :petId")
    int deleteByPetId(long petId);

    @Query("DELETE FROM messages WHERE pet_id = :petId AND created_at < :beforeMs")
    int deleteOlderThan(long petId, long beforeMs);

    @Query("SELECT COUNT(*) FROM messages WHERE pet_id = :petId")
    int countForPet(long petId);

    @Query("SELECT COUNT(*) FROM messages WHERE pet_id = :petId AND created_at BETWEEN :startMs AND :endMs")
    int countForPetInRange(long petId, long startMs, long endMs);
}
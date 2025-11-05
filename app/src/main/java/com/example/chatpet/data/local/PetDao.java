package com.example.chatpet.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(PetEntity pet);

    @Update
    int update(PetEntity pet);

    @Delete
    int delete(PetEntity pet);

    @Query("SELECT * FROM pets WHERE pet_id = :petId LIMIT 1")
    PetEntity getById(long petId);

    @Query("SELECT * FROM pets WHERE user_id = :userId LIMIT 1")
    PetEntity getByUserId(long userId);

    @Query("SELECT * FROM pets ORDER BY created_at DESC")
    List<PetEntity> getAll();

    @Query("DELETE FROM pets WHERE user_id = :userId")
    int deleteByUserId(long userId);

    @Query("SELECT COUNT(*) FROM pets")
    int count();

}
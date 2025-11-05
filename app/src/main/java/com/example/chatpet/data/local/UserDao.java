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
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(UserEntity user);


    @Update
    int update(UserEntity user);

    @Delete
    int delete(UserEntity user);

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    UserEntity getById(long userId);

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    List<UserEntity> getAll();

    @Query("SELECT * FROM users WHERE user_id = :userId LIMIT 1")
    LiveData<UserEntity> observeById(long userId);

    @Query("SELECT COUNT(*) FROM users")
    int count();
}
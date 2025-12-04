package com.example.chatpet.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"username"}, unique = true)
        }
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public long userId;

    @NonNull
    public String username;

    @NonNull
    public String password;

    @NonNull
    public String name;

    @NonNull
    public String email;

    @NonNull
    public Date birthday;

    public int avatar;

    @NonNull
    @ColumnInfo(name = "created_at")
    public long createdAt; // epoch millis

    public UserEntity(@NonNull String username, @NonNull String password,
                      @NonNull String name, @NonNull String email,
                      @NonNull Date birthday, int avatar, long createdAt) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
        this.avatar = avatar;
        this.createdAt = createdAt;
    }
}
package com.example.chatpet.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {
                @Index(value = {"username"}, unique = true),
                @Index(value = {"email"}, unique = true)
        }
)
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "user_id")
    public long userId;

    @NonNull
    public String username;

    @NonNull
    public String email;

    @NonNull
    public String password;

    @NonNull
    @ColumnInfo(name = "created_at")
    public long createdAt; // epoch millis

    public UserEntity(@NonNull String username, @NonNull String email, @NonNull String password, long createdAt) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }
}
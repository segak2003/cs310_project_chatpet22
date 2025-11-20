package com.example.chatpet.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "messages",
        indices = {
                @Index(value = {"pet_id"}),
                @Index(value = {"created_at"})
        }
)
public class MessageEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    public long messageId;

    @ColumnInfo(name = "pet_id")
    public long petId;

    @NonNull
    public String content;

    @ColumnInfo(name = "from_user")
    public boolean fromUser;

    @NonNull
    @ColumnInfo(name = "created_at")
    public long createdAt;

    public MessageEntity(long petId,
                         @NonNull String content,
                         boolean fromUser,
                         long createdAt) {
        this.petId = petId;
        this.content = content;
        this.fromUser = fromUser;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public boolean isFromUser() {
        return fromUser;
    }
}

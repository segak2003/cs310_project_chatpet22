package com.example.chatpet.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "journal_entries",
        foreignKeys = @ForeignKey(
                entity = PetEntity.class,
                parentColumns = "pet_id",
                childColumns = "pet_id",
                onDelete = CASCADE,  // delete entries when pet is deleted
                onUpdate = CASCADE
        ),
        indices = {
                @Index(value = {"pet_id"}),
                @Index(value = {"created_at"})
        }
)
public class JournalEntryEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "entry_id")
    public long entryId;

    @ColumnInfo(name = "pet_id")
    public long petId;

    @NonNull
    public String title;

    @NonNull
    public String content;

    @NonNull
    @ColumnInfo(name = "created_at")
    public long createdAt;

    public JournalEntryEntity(long petId,
                              @NonNull String title,
                              @NonNull String content,
                              long createdAt) {
        this.petId = petId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
    }
}
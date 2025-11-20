package com.example.chatpet.data.local;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(
        tableName = "pets",
        indices = {
                @Index(value = {"user_id"}, unique = true), // enforces 1:1 User->Pet
                @Index(value = {"name"})
        }
)
public class PetEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "pet_id")
    public long petId;

    @ColumnInfo(name = "user_id")
    public long userId;

    @NonNull
    public String name;

    @NonNull
    public String animal; // e.g., "cat", "dog", "dragon"

    public int level;      // progression level
    public int levelPoints;// points earned toward next level
    public int happiness;  // happiness points
    public int hunger;     // hunger points
    public int energy;     // energy points

    @ColumnInfo(name = "personality_seed")
    public String personalitySeed;

    @NonNull
    @ColumnInfo(name = "created_at")
    public long createdAt;

    public PetEntity(long userId,
                     @NonNull String name,
                     @NonNull String animal,
                     int level,
                     int levelPoints,
                     int happiness,
                     int hunger,
                     int energy,
                     String personalitySeed,
                     long createdAt) {
        this.userId = userId;
        this.name = name;
        this.animal = animal;
        this.level = level;
        this.levelPoints = levelPoints;
        this.happiness = happiness;
        this.hunger = hunger;
        this.energy = energy;
        this.personalitySeed = personalitySeed;
        this.createdAt = createdAt;
    }
}
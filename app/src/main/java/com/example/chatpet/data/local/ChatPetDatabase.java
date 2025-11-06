package com.example.chatpet.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/**
 * Central database class for ChatPet.
 * Holds all app entities and provides DAOs for data access.
 */
@Database(
        entities = {
                UserEntity.class,
                PetEntity.class,
                MessageEntity.class,
                JournalEntryEntity.class
        },
        version = 1,
        exportSchema = true
)
public abstract class ChatPetDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract PetDao petDao();
    public abstract MessageDao messageDao();
    public abstract JournalEntryDao journalEntryDao();

    private static volatile ChatPetDatabase INSTANCE;

    public static ChatPetDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatPetDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    ChatPetDatabase.class,
                                    "chatpet.db"
                            )
                            // Rebuild database if no migration defined (OK for dev)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
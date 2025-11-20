package com.example.chatpet.feature4;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.chatpet.data.local.PetEntity;
import com.example.chatpet.data.local.UserEntity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PetRepository {
    private static final String PREFS = "chatpet_prefs";
    private final SharedPreferences sp;

    // DB-backed repositories (Room)
    private final com.example.chatpet.data.repository.UserRepository userRepo;
    private final com.example.chatpet.data.repository.PetRepository dbPetRepo;

    // Background executor so we never block the UI thread with DB work
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public PetRepository(Context ctx) {
        Context app = ctx.getApplicationContext();
        this.sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        this.userRepo = new com.example.chatpet.data.repository.UserRepository(app);
        this.dbPetRepo = new com.example.chatpet.data.repository.PetRepository(app);
    }

    // ✅ Keep behavior EXACTLY as before: load from SharedPreferences
    public Pet load() {
        Pet p = new Pet();
        p.name = sp.getString("name", p.name);
        p.type = Pet.Type.valueOf(sp.getString("type", p.type.name()));
        p.hunger = sp.getInt("hunger", p.hunger);
        p.happiness = sp.getInt("happiness", p.happiness);
        p.energy = sp.getInt("energy", p.energy);
        p.points = sp.getInt("points", p.points);
        p.level = sp.getInt("level", p.level);
        p.stage = Pet.Stage.valueOf(sp.getString("stage", p.stage.name()));
        p.lastUpdatedMs = sp.getLong("lastUpdatedMs", p.lastUpdatedMs);
        return p;
    }

    public void save(Pet p) {
        sp.edit()
                .putString("name", p.name)
                .putString("type", p.type.name())
                .putInt("hunger", p.hunger)
                .putInt("happiness", p.happiness)
                .putInt("energy", p.energy)
                .putInt("points", p.points)
                .putInt("level", p.level)
                .putString("stage", p.stage.name())
                .putLong("lastUpdatedMs", p.lastUpdatedMs)
                .apply();

        ioExecutor.execute(() -> {
            UserEntity activeUser = userRepo.getActiveUser();
            if (activeUser == null) {
                return; // nobody logged in
            }

            long userId = activeUser.userId;
            PetEntity petEntity = dbPetRepo.getPetForUser(userId);
            if (petEntity == null) {
                return;
            }

            petEntity.name = (p.name != null ? p.name : petEntity.name);

            String animal = petEntity.animal;
            if (p.type == Pet.Type.CAT) {
                animal = "cat";
            } else if (p.type == Pet.Type.DRAGON) {
                animal = "dragon";
            }
            petEntity.animal = animal;

            petEntity.level = p.level;
            petEntity.levelPoints = p.points;
            petEntity.happiness = p.happiness;
            petEntity.hunger = p.hunger;
            petEntity.energy = p.energy;

            dbPetRepo.updatePet(petEntity);
        });
    }

    public void clear() {
        // Clear local state
        sp.edit().clear().apply();

        // Also reset the DB pet stats for the active user
        ioExecutor.execute(() -> {
            UserEntity activeUser = userRepo.getActiveUser();
            if (activeUser == null) {
                return;
            }

            long userId = activeUser.userId;
            PetEntity petEntity = dbPetRepo.getPetForUser(userId);
            if (petEntity == null) {
                return;
            }

            dbPetRepo.resetPetState(petEntity.petId);
        });
    }
}

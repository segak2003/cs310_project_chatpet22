package com.example.chatpet.data.repository;

import static java.lang.Math.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.PetEntity;
import com.example.chatpet.data.local.PetDao;
import com.example.chatpet.data.local.UserEntity;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PetRepository {

    private static final String PREFS_NAME = "chatpet_prefs";
    private static final String ACTIVE_PET_KEY = "active_pet_id";

    private final PetDao petDao;
    private final SharedPreferences prefs;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    private static enum Food {
        mango(5),
        bagel(10),
        burger(15);

        private final int hungerPoints;

        private Food(int hungerPoints) {
            this.hungerPoints = hungerPoints;
        }

        public int getHungerPoints() {
            return this.hungerPoints;
        }
    }

    public PetRepository(Context context) {
        Context appContext = context.getApplicationContext();
        ChatPetDatabase db = ChatPetDatabase.getInstance(appContext);
        this.petDao = db.petDao();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

    }

    public interface Callback<T> {
        void onComplete(T value);
    }

    public void createPetForUser(long userId, String petName, String animal, Callback<Long> callback) {
        ioExecutor.execute(() -> {
            long createdAt = System.currentTimeMillis();

            // Default initial stats
            int initialLevel = 1;
            int initialLevelPoints = 0;
            int initialHappiness = 50;
            int initialHunger = 50;
            int initialEnergy = 50;

            String personalitySeed = generatePersonalitySeed(userId, petName, animal);

            PetEntity pet = new PetEntity(
                    userId,
                    petName,
                    animal,
                    initialLevel,
                    initialLevelPoints,
                    initialHappiness,
                    initialHunger,
                    initialEnergy,
                    personalitySeed,
                    createdAt
            );

            long petId = petDao.insert(pet);

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onComplete(petId);
            });
        });
    }

    /** Get the pet row for a given user (1:1 relationship). */
    public PetEntity getPetForUser(long userId) {
        return petDao.getByUserId(userId);
    }

    /** Observe the pet for a given user (for dashboard UI). */
    public LiveData<PetEntity> observePetForUser(long userId) {
        return petDao.observeByUserId(userId);
    }

    /** Get pet directly by ID. */
    public PetEntity getPetById(long petId) {
        return petDao.getById(petId);
    }

    /** Update any pet fields (name, stats, level, etc.). */
    public void updatePet(PetEntity pet) {
        ioExecutor.execute(() -> petDao.update(pet));
    }

    /** For debugging / admin. */
    public List<PetEntity> getAllPets() {
        return petDao.getAll();
    }


//    public void feedPet(long userId, int foodPoints) {
//        ioExecutor.execute(() -> {
//            PetEntity pet = petDao.getByUserId(userId);
//            if (pet == null) return;
//
//            int hungerBefore = pet.hunger;
//            updateHunger(userId, foodPoints);
//            int effectiveFoodPoints = pet.hunger - hungerBefore;
//            if (effectiveFoodPoints > 0) {
//                updateLevelPoints(userId, effectiveFoodPoints);
//            }
//        });
//    }
//
//    public void sleepPet(long userId, int energyPoints) {
//        ioExecutor.execute(() -> {
//            PetEntity pet = petDao.getByUserId(userId);
//            if (pet == null) return;
//
//            int energyBefore = pet.energy;
//            updateEnergy(userId, energyPoints);
//            int effectiveEnergyPoints = pet.energy - energyBefore;
//            if (effectiveEnergyPoints > 0) {
//                updateLevelPoints(userId, effectiveEnergyPoints);
//            }
//        });
//    }


    private void updateLevel(long userId) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getByUserId(userId);
            if (pet == null) return;

            int newLevel = 1;
            if (pet.levelPoints >= 200) {
                newLevel = 3;
            } else if (pet.levelPoints >= 100) {
                newLevel = 2;
            } else {
                newLevel = 1;
            }

            if (newLevel != pet.level) {
                pet.level = newLevel;
                petDao.update(pet);
            }
        });
    }

    private void updateLevelPoints(long userId, int levelPointsDelta) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getByUserId(userId);
            if (pet == null) return;

            pet.levelPoints = pet.levelPoints + levelPointsDelta;
            updateLevel(userId);

            petDao.update(pet);
        });
    }

    public void updateHappiness(long userId, int happinessPoints) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getByUserId(userId);
            if (pet == null) return;

            int happinessBefore = pet.happiness;
            pet.happiness = Math.clamp(pet.happiness + happinessPoints, 0, 100);
            int effectiveHappinessPoints = pet.happiness - happinessBefore;
            if (effectiveHappinessPoints > 0) {
                updateLevelPoints(userId, effectiveHappinessPoints);
            }

            petDao.update(pet);
        });
    }

    public void updateHunger(long userId, int foodPoints) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getByUserId(userId);
            if (pet == null) return;

            int hungerBefore = pet.hunger;
            pet.hunger = Math.clamp(pet.hunger + foodPoints, 0, 100);
            int effectiveHungerPoints = pet.hunger - hungerBefore;
            if (effectiveHungerPoints > 0) {
                updateLevelPoints(userId, effectiveHungerPoints);
            }

            petDao.update(pet);
        });
    }

    public void updateEnergy(long userId, int energyPoints) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getByUserId(userId);
            if (pet == null) return;

            int energyBefore = pet.energy;
            pet.energy = Math.clamp(pet.energy + energyPoints, 0, 100);
            int effectiveEnergyPoints = pet.energy - energyBefore;
            if (effectiveEnergyPoints > 0) {
                updateLevelPoints(userId, effectiveEnergyPoints);
            }

            petDao.update(pet);
        });
    }

    public void resetPetState(long petId) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getById(petId);
            if (pet == null) return;

            pet.level = 1;
            pet.levelPoints = 0;
            pet.happiness = 50;
            pet.hunger = 50;
            pet.energy = 50;

            petDao.update(pet);
        });
    }

    public void deletePet(long petId) {
        ioExecutor.execute(() -> {
            PetEntity pet = petDao.getById(petId);
            if (pet != null) {
                petDao.delete(pet);
                // If this was the active pet, clear the active pet ID.
                long activeId = getActivePetIdInternal();
                if (activeId == petId) {
                    clearActivePetId();
                }
            }
        });
    }

    private String generatePersonalitySeed(long userId, String petName, String animalType) {
        return String.format(
                Locale.US,
                "%d-%s-%s-%d",
                userId,
                petName != null ? petName.toLowerCase() : "pet",
                animalType != null ? animalType.toLowerCase() : "creature",
                System.currentTimeMillis()
        );
    }

    private long getActivePetIdInternal() {
        return prefs.getLong(ACTIVE_PET_KEY, -1L);
    }

    public void setActivePetId(long petId) {
        prefs.edit().putLong(ACTIVE_PET_KEY, petId).apply();
    }

    private void clearActivePetId() {
        prefs.edit().remove(ACTIVE_PET_KEY).apply();
    }

    public PetEntity getActivePet() {
        long activePetId = getActivePetIdInternal();
        if (activePetId <= 0) {
            return null;
        }
        return petDao.getById(activePetId);
    }

    public LiveData<PetEntity> observeActiveUser() {
        long activePetId = getActivePetIdInternal();
        if (activePetId <= 0) {
            MutableLiveData<PetEntity> empty = new MutableLiveData<>();
            empty.setValue(null);
            return empty;
        }
        return petDao.observeById(activePetId);
    }

}

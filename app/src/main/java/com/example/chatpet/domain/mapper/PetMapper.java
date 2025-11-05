package com.example.chatpet.domain.mapper;

import com.example.chatpet.data.local.PetEntity;
import com.example.chatpet.domain.Pet;

/**
 * Utility class to map between PetEntity (data layer) and Pet (domain layer).
 */
public class PetMapper {

    private PetMapper() {
        // private constructor to prevent instantiation
    }

    /**
     * Converts a PetEntity from the database into a domain Pet model.
     */
    public static Pet toDomain(PetEntity entity) {
        if (entity == null) return null;

        return new Pet(
                entity.petId,
                entity.userId,
                entity.name,
                entity.animal,
                entity.level,
                entity.happiness,
                entity.hunger,
                entity.energy
        );
    }

    /**
     * Converts a domain Pet back into a PetEntity for persistence.
     * The createdAt field is not updated here — that should be handled elsewhere if needed.
     */
    public static PetEntity toEntity(Pet domain) {
        if (domain == null) return null;

        return new PetEntity(
                domain.getUserId(),
                domain.getName(),
                domain.getAnimal(),
                domain.getLevel(),
                domain.getHappiness(),
                domain.getHunger(),
                domain.getEnergy(),
                null, // personalitySeed not handled here yet
                System.currentTimeMillis() // created_at (default to now if not tracked)
        );
    }
}
package com.example.chatpet.feature4;

import android.content.Context;

public class PetInteractionController {
    private final PetRepository repo;
    private Pet pet;

    public PetInteractionController(Context ctx) {
        this.repo = new PetRepository(ctx);
        this.pet = repo.load();
    }

    public Pet getPet() {
        return pet;
    }

    // Reset pet back to default (clears SharedPreferences and reloads)
    public void resetPet() {
        repo.clear();
        this.pet = repo.load();
    }

    // Apply time-based decay to hunger, happiness, and energy
    public void applyTimeDecay() {
        long now = System.currentTimeMillis();
        long diffMs = now - pet.lastUpdatedMs;

        // convert to minutes
        long minutes = diffMs / 60000L;
        if (minutes <= 0) {
            return; // nothing to decay
        }

        // for demo: 1 point decay per minute, capped so it doesn't go crazy
        int decay = (int) Math.min(30, minutes); // max -30 at once

        pet.hunger = PointManager.clamp(pet.hunger - decay);
        pet.happiness = PointManager.clamp(pet.happiness - decay);
        pet.energy = PointManager.clamp(pet.energy - decay);

        // update lastUpdatedMs to now and save
        pet.lastUpdatedMs = now;
        repo.save(pet);
    }

    public PointsDelta onChatCompleted() {
        // small meter effects for demo
        pet.happiness = PointManager.clamp(pet.happiness + 8);
        pet.energy = PointManager.clamp(pet.energy - 3);
        PointsDelta d = PointManager.applyInteraction(pet, PointManager.InteractionType.CHAT);
        commit();
        return d;
    }

    public PointsDelta onFeedCompleted() {
        pet.hunger = PointManager.clamp(pet.hunger + 15);
        PointsDelta d = PointManager.applyInteraction(pet, PointManager.InteractionType.FEED);
        commit();
        return d;
    }

    public PointsDelta onTuckCompleted() {
        pet.energy = PointManager.clamp(pet.energy + 20);
        pet.happiness = PointManager.clamp(pet.happiness + 2);
        PointsDelta d = PointManager.applyInteraction(pet, PointManager.InteractionType.TUCK);
        commit();
        return d;
    }

    private void commit() {
        pet.lastUpdatedMs = System.currentTimeMillis();
        repo.save(pet);
    }

    public String replyFor(PointManager.InteractionType type) {
        if (type == PointManager.InteractionType.CHAT) {
            if (pet.stage == Pet.Stage.BABY) return "Hi!!";
            if (pet.stage == Pet.Stage.TEEN) return "Yo, what's up?";
            if (pet.stage == Pet.Stage.ADULT) return "Great chat, partner.";
            return "Wisdom grows with words.";
        } else if (type == PointManager.InteractionType.FEED) {
            if (pet.stage == Pet.Stage.BABY) return "Nom nom! 🍼";
            if (pet.stage == Pet.Stage.TEEN) return "Pizza time!";
            if (pet.stage == Pet.Stage.ADULT) return "That hit the spot.";
            return "A hearty meal, thank you.";
        } else { // TUCK
            if (pet.stage == Pet.Stage.BABY) return "Sleepy... zZz";
            if (pet.stage == Pet.Stage.TEEN) return "Power nap unlocked.";
            if (pet.stage == Pet.Stage.ADULT) return "I'll recharge fast.";
            return "Rest sharpens the mind.";
        }
    }
}

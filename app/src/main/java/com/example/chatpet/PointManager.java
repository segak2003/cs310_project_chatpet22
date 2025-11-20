package com.example.chatpet;


public class PointManager {
    public enum InteractionType { CHAT, FEED, TUCK }


    // Max points we allow (Elder cap)
    public static final int MAX_POINTS = 700;


    public static PointsDelta applyInteraction(Pet pet, InteractionType type) {
        // If we're already at max, no more points are added
        if (pet.points >= MAX_POINTS) {
            return new PointsDelta(0, false, pet.stage, pet.stage);
        }


        int base = (type == InteractionType.FEED) ? 10 :
                (type == InteractionType.CHAT) ? 8 : 7;


        double q = qualityMultiplier(pet.hunger, pet.happiness, pet.energy);
        int delta = Math.max(1, (int) Math.round(base * q));


        Pet.Stage from = pet.stage;


        // Clamp so we don't go over MAX_POINTS
        int newPoints = pet.points + delta;
        if (newPoints > MAX_POINTS) {
            delta = MAX_POINTS - pet.points; // adjust shown +pts
            newPoints = MAX_POINTS;
        }
        pet.points = newPoints;


        checkLevelUp(pet);
        return new PointsDelta(delta, from != pet.stage, from, pet.stage);
    }


    public static double qualityMultiplier(int hunger, int happiness, int energy) {
        double avg = (hunger + happiness + energy) / 3.0; // 0..100
        double m = (avg / 100.0) * 1.5;                   // 0..1.5
        if (m < 0.25) m = 0.25;
        if (m > 2.5)  m = 2.5;
        return m;
    }


    public static void checkLevelUp(Pet pet) {
        int pts = pet.points;
        if (pts >= 700) {
            pet.stage = Pet.Stage.ELDER;
            pet.level = 4;
        } else if (pts >= 300) {
            pet.stage = Pet.Stage.ADULT;
            pet.level = 3;
        } else if (pts >= 150) {
            pet.stage = Pet.Stage.TEEN;
            pet.level = 2;
        } else {
            pet.stage = Pet.Stage.BABY;
            pet.level = 1;
        }
    }


    // Convenience to keep meters in 0..100
    public static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}

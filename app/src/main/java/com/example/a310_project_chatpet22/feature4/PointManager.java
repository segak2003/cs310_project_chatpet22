package com.example.a310_project_chatpet22.feature4;

public class PointManager {
    public enum InteractionType { CHAT, FEED, TUCK }

    public static PointsDelta applyInteraction(Pet pet, InteractionType type) {
        int base = (type == InteractionType.FEED) ? 10 :
                (type == InteractionType.CHAT) ? 8 : 7;

        double q = qualityMultiplier(pet.hunger, pet.happiness, pet.energy);
        int delta = Math.max(1, (int) Math.round(base * q));

        Pet.Stage from = pet.stage;
        pet.points += delta;
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
        if (pts >= 701) { pet.stage = Pet.Stage.ELDER; pet.level = 4; }
        else if (pts >= 301) { pet.stage = Pet.Stage.ADULT; pet.level = 3; }
        else if (pts >= 101) { pet.stage = Pet.Stage.TEEN;  pet.level = 2; }
        else { pet.stage = Pet.Stage.BABY; pet.level = 1; }
    }

    // Convenience to keep meters in 0..100
    public static int clamp(int v) { return Math.max(0, Math.min(100, v)); }
}

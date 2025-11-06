package com.example.chatpet.domain;

public class Pet {

    private final long id;
    private final long userId;
    private final String name;
    private final String animal;

    private int level;
    private int happiness; // 0-100
    private int hunger;   // 0–100
    private int energy;   // 0–100

    public Pet() {
        id = 0;
        userId = 0;
        name = "";
        animal = "";
        level = 0;
        happiness = 0;
        hunger = 0;
        energy = 0;
    }

    public Pet(long id, long userId, String name, String animal, int level, int happiness, int hunger, int energy) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.animal = animal;
        this.level = level;
        this.happiness = clamp(happiness, 0, 100);
        this.hunger = clamp(hunger, 0, 100);
        this.energy = clamp(energy, 0, 100);
    }


    /**
     * Feeds the pet a specified food amount.
     * Pet gains hunger points equal to food amount.
     *
     * @param foodAmount How much to feed (0–100 scale).
     */
    public void feed(int foodAmount) {
        if (foodAmount <= 0) return;

        hunger += foodAmount;
        hunger = clamp(hunger, 0, 100);
    }

    /**
     * Lets the pet sleep for specified number of minutes.
     * Pet gains 10 energy points for every minute of sleep.
     *
     * @param minutes Number of minutes slept.
     */
    public void sleep(int minutes) {
        if (minutes <= 0) return;

        energy += minutes * 10; // 10 energy points per minute
        energy = clamp(energy, 0, 100);
    }


    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }


    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getAnimal() {
        return animal;
    }

    public int getLevel() {
        return level;
    }

    public int getHappiness() {
        return happiness;
    }

    public int getHunger() {
        return hunger;
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", animal='" + animal + '\'' +
                ", hunger=" + hunger +
                ", energy=" + energy +
                ", happiness=" + happiness +
                '}';
    }
}

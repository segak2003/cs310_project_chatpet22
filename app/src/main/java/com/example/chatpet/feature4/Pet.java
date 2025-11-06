package com.example.chatpet.feature4;

import java.io.Serializable;

public class Pet implements Serializable {
    public enum Stage { BABY, TEEN, ADULT, ELDER }

    public String id = "default";
    public String type = "dragon";
    public String name = "Flame";

    // meters: 0..100
    public int hunger = 50;
    public int happiness = 50;
    public int energy = 50;

    public int points = 0;
    public int level = 1;
    public Stage stage = Stage.BABY;

    public long lastUpdatedMs = System.currentTimeMillis();

    public String spriteEmoji() {
        // Simple “graphics” using emoji per stage
        switch (stage) {
            case BABY: return "🐣";
            case TEEN: return "🐲";
            case ADULT: return "🐉";
            case ELDER: default: return "🐉✨";
        }
    }


}

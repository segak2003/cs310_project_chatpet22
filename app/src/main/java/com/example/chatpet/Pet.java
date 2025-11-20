package com.example.chatpet;


import java.io.Serializable;


public class Pet implements Serializable {
    public Pet(){
        id = "default";
        type = Type.NONE;
        name = "";
    }
    public Pet(Type type) {
        this.type = type;
    }

    public enum Stage { BABY, TEEN, ADULT, ELDER }
    public enum Type { CAT, DRAGON, NONE }


    public String id = "default";
    public Type type = Type.NONE;
    public String name = "";


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
        switch (type) {
            case CAT:
                switch (stage) {
                    case BABY: return "🐱";
                    case TEEN: return "😺";
                    case ADULT: return "🐈";
                    case ELDER: default: return "🐈✨";
                }
            case DRAGON:
                switch (stage) {
                    case BABY:
                        return "🐣";
                    case TEEN:
                        return "🐲";
                    case ADULT:
                        return "🐉";
                    case ELDER:
                    default:
                        return "🐉✨";
                }
        }
        return "❌";
    }

}

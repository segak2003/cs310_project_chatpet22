package com.example.chatpet.feature4;

import android.content.Context;
import android.content.SharedPreferences;

public class PetRepository {
    private static final String PREFS = "chatpet_prefs";
    private final SharedPreferences sp;

    public PetRepository(Context ctx) {
        this.sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Pet load() {
        Pet p = new Pet();
        p.name = sp.getString("name", p.name);
        p.type = sp.getString("type", p.type);
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
                .putString("type", p.type)
                .putInt("hunger", p.hunger)
                .putInt("happiness", p.happiness)
                .putInt("energy", p.energy)
                .putInt("points", p.points)
                .putInt("level", p.level)
                .putString("stage", p.stage.name())
                .putLong("lastUpdatedMs", p.lastUpdatedMs)
                .apply();
    }









    public void clear() {
        sp.edit().clear().apply();
    }

}




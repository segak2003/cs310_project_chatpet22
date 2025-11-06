package com.example.chatpet.feature4;

public class PointsDelta {
    public final int delta;
    public final boolean leveledUp;
    public final Pet.Stage fromStage;
    public final Pet.Stage toStage;

    public PointsDelta(int delta, boolean leveledUp, Pet.Stage from, Pet.Stage to) {
        this.delta = delta;
        this.leveledUp = leveledUp;
        this.fromStage = from;
        this.toStage = to;
    }
}

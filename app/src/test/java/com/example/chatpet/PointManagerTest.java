package com.example.chatpet;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * White-box tests for PointManager.
 *
 * These are LOCAL unit tests (run under src/test/, no Android framework).
 */
public class PointManagerTest {

    /**
     * Helper: fresh default pet object for tests.
     */
    private Pet newPet() {
        Pet p = new Pet();
        p.name = "Flame";
        p.type = Pet.Type.DRAGON;
        p.hunger = 50;
        p.happiness = 50;
        p.energy = 50;
        p.points = 0;
        p.level = 1;
        p.stage = Pet.Stage.BABY;
        return p;
    }

    /**
     * Test 1: clamp() keeps values inside 0..100.
     */
    @Test
    public void clamp_keepsValuesWithin0to100() {
        assertEquals(0, PointManager.clamp(-5));
        assertEquals(100, PointManager.clamp(150));
        assertEquals(42, PointManager.clamp(42));
    }

    /**
     * Test 2: checkLevelUp() sets the correct stage and level based on points.
     */
    @Test
    public void checkLevelUp_setsCorrectStageForPoints() {
        Pet p = newPet();

        p.points = 0;
        PointManager.checkLevelUp(p);
        assertEquals(Pet.Stage.BABY, p.stage);
        assertEquals(1, p.level);

        p.points = 160;
        PointManager.checkLevelUp(p);
        assertEquals(Pet.Stage.TEEN, p.stage);
        assertEquals(2, p.level);

        p.points = 350;
        PointManager.checkLevelUp(p);
        assertEquals(Pet.Stage.ADULT, p.stage);
        assertEquals(3, p.level);

        p.points = 700;
        PointManager.checkLevelUp(p);
        assertEquals(Pet.Stage.ELDER, p.stage);
        assertEquals(4, p.level);
    }

    /**
     * Test 3: qualityMultiplier() never goes below 0.25 or above 2.5.
     */
    @Test
    public void qualityMultiplier_isClampedBetweenMinAndMax() {
        double low = PointManager.qualityMultiplier(0, 0, 0);
        double high = PointManager.qualityMultiplier(100, 100, 100);

        assertTrue("Multiplier should not go below 0.25", low >= 0.25);
        assertTrue("Multiplier should not go above 2.5", high <= 2.5);
    }

    /**
     * Test 4: applyInteraction(CHAT) increases points based on meters.
     */
    @Test
    public void applyInteraction_chat_increasesPointsBasedOnMeters() {
        Pet p = newPet();
        // good meters → decent multiplier
        p.hunger = 90;
        p.happiness = 90;
        p.energy = 90;

        int before = p.points;
        PointsDelta d = PointManager.applyInteraction(p, PointManager.InteractionType.CHAT);

        assertTrue("Chat should yield a positive delta", d.delta > 0);
        assertEquals("Points should increase by delta", before + d.delta, p.points);
    }

    /**
     * Test 5: applyInteraction() respects MAX_POINTS cap.
     */
    @Test
    public void applyInteraction_respectsMaxPointsCap() {
        Pet p = newPet();
        p.points = PointManager.MAX_POINTS;

        PointsDelta d = PointManager.applyInteraction(p, PointManager.InteractionType.FEED);

        // No more points should be added once at max
        assertEquals(PointManager.MAX_POINTS, p.points);
        assertEquals("Delta should be 0 when already at MAX_POINTS", 0, d.delta);
        assertFalse("No level change expected at max points", d.leveledUp);
    }
}

package com.example.chatpet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.core.app.ActivityScenario;

import org.junit.Test;

import com.example.chatpet.Pet;

public class ChatPageTest {

    @Test
    public void catHelloResponse_returnsCorrectBotReply() {
        try (ActivityScenario<ChatPage> scenario = ActivityScenario.launch(ChatPage.class)) {
            scenario.onActivity(activity -> {
                Pet testPet = new Pet(Pet.Type.CAT);
                activity.setPetForTesting(testPet, activity); // pass Activity as Context
                String response = activity.getBotResponse("hi");
                assertEquals("Hewwo to you too! ", response);
            });
        }

    }

    @Test
    public void catBotResponse_loveYou_returnsLoveReply() {
        try (ActivityScenario<ChatPage> scenario = ActivityScenario.launch(ChatPage.class)) {
            scenario.onActivity(activity -> {
                activity.setPetForTesting(new Pet(Pet.Type.CAT), activity);
                String response = activity.getBotResponse("love you");
                assertEquals("I wuv you more! ", response);
            });
        }
    }

    @Test
    public void dragonBotResponse_hi_returnsDragonGreeting() {
        try (ActivityScenario<ChatPage> scenario = ActivityScenario.launch(ChatPage.class)) {
            scenario.onActivity(activity -> {
                activity.setPetForTesting(new Pet(Pet.Type.DRAGON), activity);
                String response = activity.getBotResponse("hi");
                assertEquals("Roarrr to you too! ", response);
            });
        }
    }

    @Test
    public void unknownMessage_returnsEmptyBotReply() {
        try (ActivityScenario<ChatPage> scenario = ActivityScenario.launch(ChatPage.class)) {
            scenario.onActivity(activity -> {
                activity.setPetForTesting(new Pet(Pet.Type.CAT), activity);
                String response = activity.getBotResponse("this is something unknown");
                // Unknown messages return empty string
                assertEquals("What do you mean? ", response);
            });
        }
    }

    @Test
    public void multipleTriggers_combinedResponse() {
        try (ActivityScenario<ChatPage> scenario = ActivityScenario.launch(ChatPage.class)) {
            scenario.onActivity(activity -> {
                activity.setPetForTesting(new Pet(Pet.Type.CAT), activity);
                String response = activity.getBotResponse("hi how are you love you");
                // All matching triggers should combine
                String expected = "Hewwo to you too! I'm doing much better now that I'm talking to you :) I wuv you more! ";
                assertEquals(expected, response);
            });
        }
    }
}
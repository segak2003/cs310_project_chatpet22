package com.example.chatpet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.xr.runtime.Config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import com.example.chatpet.feature4.Pet;

@RunWith(RobolectricTestRunner.class)
public class ChatPageTest {

    private ChatPage chatPage;

    @Before
    public void setUp() {
        // Create the ChatPage activity via Robolectric
        chatPage = Robolectric.buildActivity(ChatPage.class).create().get();

        // Set a Pet for testing (black-box perspective: only care about behavior)
        chatPage.setPetForTesting(new Pet(Pet.Type.CAT), chatPage);
    }

    @Test
    public void testBotResponseGreeting() {
        String response = chatPage.getBotResponse("Hi there!");
        assertNotNull(response);
        assertEquals("Hewwo to you too! ", response);
    }

    @Test
    public void testBotResponseLove() {
        String response = chatPage.getBotResponse("I love you");
        assertNotNull(response);
        assertEquals("I wuv you more! ", response);
    }

    @Test
    public void testBotResponseHowAreYou() {
        String response = chatPage.getBotResponse("How are you?");
        assertNotNull(response);
        assertEquals("I'm doing much better now that I'm talking to you :) ", response);
    }

    @Test
    public void testBotResponseUnknownMessage() {
        String response = chatPage.getBotResponse("Random message with no trigger");
        assertNotNull(response);
        assertEquals("", response); // Unknown messages should return empty string
    }

    @Test
    public void testBotResponseMultipleTriggers() {
        String response = chatPage.getBotResponse("Hi love you how are you");
        assertNotNull(response);
        String expected = "Hewwo to you too! I'm doing much better now that I'm talking to you :) I wuv you more! ";
        assertEquals(expected, response);
    }

    @Test
    public void testDragonBotResponses() {
        chatPage.setPetForTesting(new Pet(Pet.Type.DRAGON), chatPage);

        String greeting = chatPage.getBotResponse("hello");
        assertEquals("Roarrr to you too! ", greeting);

        String love = chatPage.getBotResponse("love you");
        assertEquals("I 🔥 you more! ", love);

        String flyRequest = chatPage.getBotResponse("what do");
        assertEquals("Can we go on a fly through the sky later please!!!", flyRequest);
    }
}

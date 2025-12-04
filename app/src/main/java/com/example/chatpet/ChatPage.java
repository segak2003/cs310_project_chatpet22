package com.example.chatpet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.lifecycle.Observer;


//import com.example.a310_project_chatpet22.R;

//import com.example.chatpet.R;

import com.example.chatpet.data.local.MessageEntity;
import com.example.chatpet.data.repository.MessageRepository;
import com.example.chatpet.Pet;
import com.example.chatpet.PetInteractionController;

import java.util.ArrayList;
import java.util.List;

public class ChatPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private MessageAdapter adapter;
    private final List<MessageEntity> messages = new ArrayList<>();

    private MessageRepository messageRepository;
    private long activePetId;

    private PetInteractionController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(com.example.chatpet.R.layout.activity_chat_page);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            int bottom = Math.max(imeInsets.bottom, systemBars.bottom);
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, bottom);
            return insets;
        });

        controller = new PetInteractionController(this);

        messageRepository = new MessageRepository(this);
        SharedPreferences prefs = getSharedPreferences("chatpet_prefs", Context.MODE_PRIVATE);
        activePetId = prefs.getLong("active_pet_id", -1L);

        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        ImageButton btnBack = findViewById(R.id.buttonBack);

        adapter = new MessageAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (activePetId > 0) {
            messageRepository.observeMessagesForPet(activePetId)
                    .observe(this, new Observer<List<MessageEntity>>() {
                        @Override
                        public void onChanged(List<MessageEntity> newMessages) {
                            messages.clear();
                            if (newMessages != null) {
                                messages.addAll(newMessages);
                            }
                            adapter.notifyDataSetChanged();
                            if (!messages.isEmpty()) {
                                recyclerView.scrollToPosition(messages.size() - 1);
                            }
                        }
                    });
        }

        buttonSend.setOnClickListener(v -> {
            Log.i("ChatPage", "Send Clicked!");
            String userText = editTextMessage.getText().toString().trim();
            if (userText.isEmpty() || activePetId <= 0) {
                return;
            }

            editTextMessage.setText("");

            // Insert user message into DB on a background thread
            new Thread(() -> {
                messageRepository.sendUserMessage(activePetId, userText);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                String botResponse = getBotResponse(userText);
                messageRepository.savePetReply(activePetId, botResponse);
            }).start();
        });

        btnBack.setOnClickListener(v -> {
            // Create a new intent to go to your target page
            Intent intent = new Intent(ChatPage.this, PetGrowthActivity.class);

            // Start that activity
            startActivity(intent);
        });
    }

    String getBotResponse(String userMessage) {
        String lowerUserMessage = userMessage.toLowerCase();
        String botReply = "";

        Pet p = controller.getPet();
        Pet.Stage s = p.stage;
        boolean contains = false;
        if(p.type == Pet.Type.CAT){
            if(s == Pet.Stage.BABY){
                if(lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello") ){
                    botReply += "Meow to you too! ";
                    contains = true;
                }
                if(lowerUserMessage.contains("how are you")){
                    botReply += "I'm still learning about life cuz I'm a baby kitten but I'm doing better now that I'm talking to you! ";
                    contains = true;
                }
                if(lowerUserMessage.contains("love")){
                    botReply += "I wuv you more! Meow Meow";
                    contains = true;
                }
                if(lowerUserMessage.contains("what do you want to")){
                    botReply += "I want to snuggle wif you! Meow ";
                    contains = true;
                }
                if(lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")){
                    botReply += "Nooo please come back soon :( I'm gonna miss you! ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "I'm just a kitten, I don't know what that means!";
                    }

                }
            }
            else if(s == Pet.Stage.TEEN){
                if(lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello") ){
                    botReply += "Meow back I guess. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("how are you")){
                    botReply += "I'm chill or whatever. Being a teenager is rough. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("love")){
                    botReply += "Whateverrr I guess I love you too";
                    contains = true;
                }
                if(lowerUserMessage.contains("what do you want to")){
                    botReply += "I just wanna chill and play catch the laser with my friends. No offense to you lol. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")){
                    botReply += "Adios. Meow. ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me.";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "Whateverrrrrrrr";
                    }

                }
            }
            else if(s == Pet.Stage.ADULT){
                if(lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello") ){
                    botReply += "Meow to you too! ";
                    contains = true;
                }
                if(lowerUserMessage.contains("how are you")){
                    botReply += "I'm doing meowy well. Finally got a job so I moved out of my mom's cardboard box. Life's good. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("love")){
                    botReply += "I love you too! Meow.";
                    contains = true;
                }
                if(lowerUserMessage.contains("what do you want to")){
                    botReply += "I think I'll whack a yarn ball around. It's the trend for young professionals like me these days. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")){
                    botReply += "Yes, til tomorrow meowning. ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "Why don't we talk about something else. Perhaps you want me to take a look at your stock profile? Just kidding let's talk about something chill. Work is too stressful.";
                    }

                }
            }
            else if(s == Pet.Stage.ELDER){
                if(lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello") ){
                    botReply += "Meeeowww! ";
                    contains = true;
                }
                if(lowerUserMessage.contains("how are you")){
                    botReply += "Feeling lazy. Being old is kind of nice. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("love")){
                    botReply += "I love you too. One thing I've learned throughout my long life is that you can never tell your family you love them enough. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("what do you want to")){
                    botReply += "I think I'll just curl up and bathe in the sun. Old cats like me have bad joints. ";
                    contains = true;
                }
                if(lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")){
                    botReply += "Don't stay away too long... Cats don't live forever, you know... ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "I must be getting old. I can't hear ya! ";
                    }

                }
            }

        }
        else {
            if(s == Pet.Stage.BABY){
                if (lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello")) {
                    botReply += "Wowrrr to you too! ";
                    contains = true;
                }
                if (lowerUserMessage.contains("love you")) {
                    botReply += "I 🔥 you more! ";
                    contains = true;
                }
                if (lowerUserMessage.contains("how are you")) {
                    botReply += "Being a baby dwagon is fire!!!! I'm doing awesum :) ";
                    contains = true;
                }
                if (lowerUserMessage.contains("what do")) {
                    botReply += "Can we go on a fly through the sky later pwease!!! ";
                    contains = true;
                }
                if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")) {
                    botReply += "Nooo please come back soon :( I will miss you.";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "I'm just a baby I don't understand big words like that!";
                    }

                }
            }
            else if(s == Pet.Stage.TEEN){
                if (lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello")) {
                    botReply += "Roar back or whatever... ";
                    contains = true;
                }
                if (lowerUserMessage.contains("love you")) {
                    botReply += "I 🔥 you too I guess. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("how are you")) {
                    botReply += "Chill. I just posted my new wing detailing on DragonGram and got lots of likes so it's pretty chill. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("what do")) {
                    botReply += "Tbh I'd rather fly around with my dragon friends than with you... no offense you're just kinda old lol ";
                    contains = true;
                }
                if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")) {
                    botReply += "Alright cool bye ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "Whateverrrrrr ";
                    }

                }
            }
            else if(s == Pet.Stage.ADULT){
                if (lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello")) {
                    botReply += "Helloar! That's how they've been greeting people at my new office. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("love you")) {
                    botReply += "I 🔥 you too! ";
                    contains = true;
                }
                if (lowerUserMessage.contains("how are you")) {
                    botReply += "I'm doing swell, just made a bunch of dabloons at JP Dragon and Chase. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("what do")) {
                    botReply += "It's been a long day at work. Let's just have a coffee. I can brew it with my own fire now lol. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")) {
                    botReply += "Goodbye! Stay fire. ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "Why don't we fly the conversation back to simpler terms. ";
                    }

                }
            }
            else if(s == Pet.Stage.ELDER){
                if (lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello")) {
                    botReply += "Hello! Apologies for the boring greeting, I can't breathe much fire these days. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("love you")) {
                    botReply += "I 🔥 you too! Don't forget to say that to your loved ones... The only thing I regret in my old age is not saying I love you more. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("how are you")) {
                    botReply += "Oh, you know, about as well as an ancient dragon like me can be. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("what do")) {
                    botReply += "Recently I've been enjoying spending time with my granddragons. Want to join? Just be careful, they haven't learned to control their fire breathing yet. ";
                    contains = true;
                }
                if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")) {
                    botReply += "Goodbye! Don't stay away too long... dragons aren't immortal you know. ";
                    contains = true;
                }
                if(!contains){
                    if(p.hunger < 20){
                        botReply += "I'm too hungry to understand you... please feed me!!!";
                    }
                    else if(p.happiness < 20){
                        botReply += "I'm too unhappy to understand you right now... maybe come back again after this chat to talk more.";
                    }
                    else if(p.energy < 20){
                        botReply += "I'm too tired to talk right now. Let me take a nap first!";
                    }
                    else{
                        botReply += "I'm getting old, I don't think I know what you're saying. ";
                    }

                }
            }
        }

        return botReply;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);

                // Also get send button rect
                Rect sendButtonRect = new Rect();
                buttonSend.getGlobalVisibleRect(sendButtonRect);

                // If touch is outside both EditText and Send Button
                if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY()) &&
                        !sendButtonRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {

                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }


    // ChatPage.java
    void setPetForTesting(Pet pet, Context context) {
        this.controller = new MockController(pet, context);
    }

    private static class MockController extends PetInteractionController {
        private final Pet pet;
        MockController(Pet pet, Context context) {
            super(context); // Pass a valid context
            this.pet = pet;
        }
        @Override public Pet getPet() { return pet; }
    }

}
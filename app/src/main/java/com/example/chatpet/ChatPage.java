package com.example.chatpet;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
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

//import com.example.a310_project_chatpet22.R;

//import com.example.chatpet.R;

import com.example.chatpet.feature4.Pet;
import com.example.chatpet.feature4.PetGrowthActivity;
import com.example.chatpet.feature4.PetInteractionController;

import java.util.ArrayList;
import java.util.List;

public class ChatPage extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private MessageAdapter adapter;
    private List<Message> messageList;

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



        recyclerView = findViewById(R.id.recyclerViewMessages);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        ImageButton btnBack = findViewById(R.id.buttonBack);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonSend.setOnClickListener(v -> {
            String msg = editTextMessage.getText().toString().trim();
            if (!msg.isEmpty()) {
                addMessage(msg, true);
                getBotResponse(msg);
                editTextMessage.setText("");
            }
        });

        btnBack.setOnClickListener(v -> {
            // Create a new intent to go to your target page
            Intent intent = new Intent(ChatPage.this, PetGrowthActivity.class);

            // Start that activity
            startActivity(intent);
        });
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new Message(text, isUser));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    // For now, fake a bot reply (you can replace this with API call later)
    private void getBotResponse(String userMessage) {
        String lowerUserMessage = userMessage.toLowerCase();
        String botReply = "";

        if(controller.getPet().type == Pet.Type.CAT){
            if(lowerUserMessage.contains("hi") ||lowerUserMessage.contains("hello") ){
                botReply += "Hewwo to you too! ";
            }
            if(lowerUserMessage.contains("love you")){
                botReply += "I wuv you more! ";
            }
            if(lowerUserMessage.contains("how are you")){
                botReply += "I'm doing much better now that I'm talking to you :) ";
            }
            if(lowerUserMessage.contains("what do")){
                botReply += "Can we go on a walk later pweaseeeeee!!!";
            }
            if(lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")){
                botReply += "Nooo please come back soon :( ";
            }
        }
        else {
            if (lowerUserMessage.contains("hi") || lowerUserMessage.contains("hello")) {
                botReply += "Roarrr to you too! ";
            }
            if (lowerUserMessage.contains("love you")) {
                botReply += "I 🔥 you more! ";
            }
            if (lowerUserMessage.contains("how are you")) {
                botReply += "I'm doing much better now that I'm talking to you :) ";
            }
            if (lowerUserMessage.contains("what do")) {
                botReply += "Can we go on a fly through the sky later please!!!";
            }
            if (lowerUserMessage.contains("bye") || lowerUserMessage.contains("see you")) {
                botReply += "Nooo please come back soon :( I will miss you.";
            }
        }

        addMessage(botReply, false);
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


}
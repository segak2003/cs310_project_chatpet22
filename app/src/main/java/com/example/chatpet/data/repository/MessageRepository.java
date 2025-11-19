package com.example.chatpet.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.MessageDao;
import com.example.chatpet.data.local.MessageEntity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MessageRepository {

    private final MessageDao messageDao;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public MessageRepository(Context context) {
        Context appContext = context.getApplicationContext();
        ChatPetDatabase db = ChatPetDatabase.getInstance(appContext);
        this.messageDao = db.messageDao();
    }

    /** Observe all messages for a pet, ordered chronologically. */
    public LiveData<List<MessageEntity>> observeMessagesForPet(long petId) {
        return messageDao.observeByPetId(petId);
    }

    /** Get all messages for a pet (synchronously, for background use). */
    public List<MessageEntity> getMessagesForPet(long petId) {
        return messageDao.getByPetId(petId);
    }

    /** Get recent N messages, e.g., to build prompts for the LLM. */
    public List<MessageEntity> getRecentMessagesForPet(long petId, int limit) {
        return messageDao.getByPetIdLimited(petId, limit);
    }

    /** Get messages within a time window (for journal summaries). */
    public List<MessageEntity> getMessagesForPetInRange(long petId, long startMs, long endMs) {
        return messageDao.getByPetIdInRange(petId, startMs, endMs);
    }

    /**
     * Insert a single message row.
     *
     * NOTE: This runs synchronously. Call from a background thread
     * unless you enabled allowMainThreadQueries() in Room.
     */
    public long insertMessage(MessageEntity message) {
        return messageDao.insert(message);
    }

    /**
     * Insert a batch of messages.
     *
     * NOTE: This runs synchronously. Call from a background thread.
     */
    public List<Long> insertMessages(List<MessageEntity> messages) {
        return messageDao.insertAll(messages);
    }

    /** User sends a message to the pet. */
    public long sendUserMessage(long petId, String content) {
        long now = System.currentTimeMillis();
        MessageEntity msg = new MessageEntity(
                petId,
                content,
                true,   // fromUser
                now
        );
        return insertMessage(msg);
    }

    /**
     * Save the pet/LLM reply to the chat.
     * Typically called after you get a response from the backend.
     */
    public long savePetReply(long petId, String content) {
        long now = System.currentTimeMillis();
        MessageEntity msg = new MessageEntity(
                petId,
                content,
                false,  // fromUser = false means pet/system
                now
        );
        return insertMessage(msg);
    }

    /** Delete all messages for a pet (e.g., on reset). */
    public void deleteMessagesForPet(long petId) {
        ioExecutor.execute(() -> messageDao.deleteByPetId(petId));
    }

    /** Delete messages older than a certain timestamp. */
    public void deleteMessagesForPetOlderThan(long petId, long cutoffMs) {
        ioExecutor.execute(() -> messageDao.deleteOlderThan(petId, cutoffMs));
    }

    /**
     * Count how many messages were sent today for a given pet.
     * This is useful for journal summaries like “we chatted 3 times today”.
     */
    public int countMessagesTodayForPet(long petId) {
        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        // Set to start of *today* in the device's local timezone
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long dayStart = cal.getTimeInMillis();

        return messageDao.countForPetInRange(petId, dayStart, now);
    }

}

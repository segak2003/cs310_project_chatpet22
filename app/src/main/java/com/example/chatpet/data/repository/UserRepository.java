package com.example.chatpet.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.chatpet.data.local.ChatPetDatabase;
import com.example.chatpet.data.local.UserDao;
import com.example.chatpet.data.local.UserEntity;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UserRepository {

    private static final String PREFS_NAME = "chatpet_prefs";
    private static final String ACTIVE_USER_KEY = "active_user_id";

    private final UserDao userDao;
    private final SharedPreferences prefs;
    private final Executor ioExecutor = Executors.newSingleThreadExecutor();

    public UserRepository(Context context) {
        Context appContext = context.getApplicationContext();
        ChatPetDatabase db = ChatPetDatabase.getInstance(appContext);
        this.userDao = db.userDao();
        this.prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public interface Callback<T> {
        void onComplete(T value);
    }

    /** Create a new user account. */
    public void createUser(String username, String password, String name, Date birthday, int avatar, Callback<Long> callback) {
        ioExecutor.execute(() -> {
            long createdAt = System.currentTimeMillis();
            UserEntity user = new UserEntity(username, password, name, birthday, avatar, createdAt);
            long  userId = userDao.insert(user);

            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onComplete(userId);
            });
        });
    }

    /** Get a user by their primary key */
    public UserEntity getUserById(long userId) {
        return userDao.getById(userId);
    }

    /** Observe a user */
    public LiveData<UserEntity> observeUserById(long userId) {
        return userDao.observeById(userId);
    }

    /** Look up a user by username */
    public UserEntity getUserByUsername(String username) {
        return userDao.getByUsername(username);
    }

    public boolean isUsernameTaken(String username) {
        UserEntity existing = userDao.getByUsername(username);
        return existing != null;
    }

    public void isUsernameTaken(String username, Callback<Boolean> callback) {
        ioExecutor.execute(() -> {
            boolean taken = isUsernameTaken(username); // blocking version
            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(taken));
        });
    }

    public void validatePassword(String username, String password, Callback<Boolean> callback) {
        ioExecutor.execute(() -> {
            UserEntity user = userDao.getByUsername(username);
            boolean success = (user != null) && (user.password.equals(password));
            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(success));
        });
    }

    public void updateUserProfile(UserEntity updatedUser) {
        ioExecutor.execute(() -> userDao.update(updatedUser));
    }

    public List<UserEntity> getAllUsers() {
        return userDao.getAll();
    }

    private long getActiveUserIdInternal() {
        return prefs.getLong(ACTIVE_USER_KEY, -1L);
    }

    public void setActiveUserId(long userId) {
        prefs.edit().putLong(ACTIVE_USER_KEY, userId).apply();
    }

    private void clearActiveUserId() {
        prefs.edit().remove(ACTIVE_USER_KEY).apply();
    }

    public void deleteUser(long userId) {
        ioExecutor.execute(() -> {
            UserEntity user = userDao.getById(userId);
            if (user != null) {
                userDao.delete(user);
                // If this was the active user, clear the active user ID.
                long activeId = getActiveUserIdInternal();
                if (activeId == userId) {
                    clearActiveUserId();
                }
            }
        });
    }

    public UserEntity getActiveUser() {
        long activeUserId = getActiveUserIdInternal();
        if (activeUserId <= 0) {
            return null;
        }
        return userDao.getById(activeUserId);
    }

    public LiveData<UserEntity> observeActiveUser() {
        long activeUserId = getActiveUserIdInternal();
        if (activeUserId <= 0) {
            MutableLiveData<UserEntity> empty = new MutableLiveData<>();
            empty.setValue(null);
            return empty;
        }
        return userDao.observeById(activeUserId);
    }

    public void setActiveUser(String username, Callback<Long> callback) {
        ioExecutor.execute(() -> {
            UserEntity user = userDao.getByUsername(username);
            setActiveUserId(user.userId);

            new Handler(Looper.getMainLooper()).post(() -> callback.onComplete(user.userId));
        });
    }

}

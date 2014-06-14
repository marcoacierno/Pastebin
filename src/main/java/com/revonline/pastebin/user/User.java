package com.revonline.pastebin.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class User
{
    private final static String KEY_NAME = "name";
    private final static String KEY_KEY = "key";

    private String userName;
    private String userKey;
    private boolean logged;
    private SharedPreferences preferences;

    public String getUserName() {
        return userName;
    }

    public String getUserKey() {
        return userKey;
    }

    public boolean isLogged() {
        return logged;
    }

    public User(Context context)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        update();
    }

    public void update()
    {
        userName = preferences.getString(KEY_NAME, null);
        userKey = preferences.getString(KEY_KEY, null);

        logged = !(userKey == null);
    }

    public void setUserKey(String key)
    {
        this.userKey = key;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_KEY, key);
        editor.commit();
    }

    public void setUserName(String name)
    {
        this.userName = name;

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NAME, name);
        editor.commit();
    }
//
//    public void login()
//    {
//
//    }

    public void logout()
    {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_KEY);
        editor.remove(KEY_NAME);
        editor.commit();

        userName = null;
        userKey = null;
        logged = false;
    }

//    public void loginUser(String name, String password)
//    {
//
//    }
}

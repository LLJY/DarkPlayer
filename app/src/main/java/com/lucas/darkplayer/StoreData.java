package com.lucas.darkplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StoreData {
    private final String STORAGE = "com.lucas.darkplayer.STORAGE";
    private SharedPreferences preferences;
    private Context context;

    public StoreData(Context context){
        this.context = context;
    }

    public boolean loadRepeat(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("repeat", false);
    }

    public void storeRepeat(boolean repeat){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("repeat", repeat);
        editor.apply();
    }

    public void clearCached(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}

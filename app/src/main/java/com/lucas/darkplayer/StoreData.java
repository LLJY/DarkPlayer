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

    public void storeAudioData(ArrayList<SongData> arrayList){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("songArrayList", json);
        editor.apply();

    }
    public boolean loadShuffled(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getBoolean("shuffled", false);
    }

    public ArrayList<SongData> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("songData", null);
        Type type = new TypeToken<ArrayList<SongData>>(){
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeAudioIndex(int index){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.apply();
    }

    public void storeShuffled(boolean shuffled){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("shuffled", shuffled);
        editor.apply();
    }

    public void storeAudioShuffleList(int[] shuffleList){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shuffleList);
        editor.putString("shuffleList", json);
        editor.apply();
    }

    public int[] loadShuffleList() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("shuffleList", null);
        Type type = new TypeToken<int[]>(){
        }.getType();
        return gson.fromJson(json, type);
    }

    public void storeSession(Boolean playing){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("playing", playing);
        editor.apply();
    }

    public int loadAudioIndex(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("audioIndex", -1);
    }

    public void clearCached(){
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}

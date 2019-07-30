package com.lucas.darkplayer;

import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

import com.lucas.darkplayer.Playlist;
import com.lucas.darkplayer.PlaylistData;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface PlaylistDao{

    @Query("SELECT playlist_name from playlists")
    Cursor queryPlaylists();

    @Query("SELECT * from playlists WHERE playlist_name like :playlistName")
    Cursor querySongsFromPlaylist(String playlistName);

    @Insert()
    void insertPlaylist(Playlist playlist);

    @Update
    void updatePlaylist(Playlist playlist);

    @Delete
    void deletePlaylist(Playlist playlist);

}
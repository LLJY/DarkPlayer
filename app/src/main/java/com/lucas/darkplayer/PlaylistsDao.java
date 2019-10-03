package com.lucas.darkplayer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

@Dao
public interface PlaylistsDao {

    @Query("SELECT * from playlists")
    Cursor queryPlaylists();

    @Query("SELECT id from playlists WHERE name like :name")
    Cursor queryPlaylistID(String name);

    @Query("SELECT MAX(id) from playlists")
    int queryLastInsert();

    @Insert()
    void insertPlaylist(Playlists playlists);

    @Update()
    void updatePlaylist(Playlists playlists);

    @Delete()
    void deletePlaylist(Playlists playlists);

    @Query("DELETE FROM playlists WHERE id = :id")
    void deletePlaylistByID(int id);

    @Query("DELETE FROM playlists")
    void resetPlaylist();

}
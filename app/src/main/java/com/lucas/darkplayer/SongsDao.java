package com.lucas.darkplayer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.database.Cursor;

@Dao
public interface SongsDao {

    @Query("SELECT playlist_name from songs")
    Cursor queryPlaylists();

    @Query("SELECT * from Songs WHERE playlist_name like :playlistName")
    Cursor querySongsFromPlaylist(String playlistName);

    @Insert()
    void insertPlaylist(Songs songs);

    @Update()
    void updatePlaylist(Songs songs);

    @Delete()
    void deletePlaylist(Songs songs);

    @Query("DELETE FROM Songs")
    void resetPlaylist();

}
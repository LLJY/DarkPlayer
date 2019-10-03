package com.lucas.darkplayer;
/*      Copyright (C) 2019  Lucas Lee Jing Yi
*
*        This program is free software: you can redistribute it and/or modify
*        it under the terms of the GNU General Public License as published by
*        the Free Software Foundation, either version 3 of the License, or
*        (at your option) any later version.
*
*        This program is distributed in the hope that it will be useful,
*        but WITHOUT ANY WARRANTY; without even the implied warranty of
*        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*        GNU General Public License for more details.
*
*        You should have received a copy of the GNU General Public License
*        along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

//Room database to store songs
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "playlists")
public class Playlists {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public int id;
    @ColumnInfo(name = "name")
    private String playlistName;
    //this is the associated album art with the playlist, will belong to the first song.
    @ColumnInfo(name = "album_art")
    private String albumArt;

    public Playlists(int id, String playlistName, String albumArt){
        this.id = id;
        this.playlistName = playlistName;
        this.albumArt = albumArt;

    }

    public int getPlaylistID(){
        return id;
    }

    public String getAlbumArt(){
        return albumArt;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getPlaylistName() {
        return playlistName;
    }

}


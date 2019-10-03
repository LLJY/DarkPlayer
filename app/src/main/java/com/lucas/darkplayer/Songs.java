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
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(foreignKeys = {
        @ForeignKey(entity = Playlists.class,
        parentColumns = "id",
        childColumns = "playlist_id",
        onDelete = ForeignKey.CASCADE)},
        tableName = "songs",
        indices = @Index(value = "playlist_id"))
public class Songs {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "index")
    private int index;
    @ColumnInfo(name = "playlist_id")
    private int playlistID;
    @ColumnInfo(name = "song_id")
    private String songId;
    @ColumnInfo(name = "title")
    private String title;
    @ColumnInfo(name = "album")
    private String album;
    @ColumnInfo(name = "artist")
    private String artist;
    //String can always be converted into URI
    //use String in database for consistency
    @ColumnInfo(name = "album_art")
    private String albumArt;
    @ColumnInfo(name = "duration")
    private String duration;

    public Songs(int index, int playlistID, String songId, String title, String album, String artist, String albumArt, String duration){
        this.index = index;
        this.playlistID = playlistID;
        this.songId = songId;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumArt = albumArt;
        this.duration = duration;

    }

    public int getPlaylistID() {
        return playlistID;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public int getIndex() {
        return index;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDuration(){
        return duration;
    }

}


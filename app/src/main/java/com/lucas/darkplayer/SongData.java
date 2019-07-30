package com.lucas.darkplayer;
/*      Copyright (C) 2019  Lucas Lee Jing Yi

        This program is free software: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        You should have received a copy of the GNU General Public License
        along with this program.  If not, see <https://www.gnu.org/licenses/>. */
import android.media.session.MediaSessionManager;
import android.net.Uri;

import java.io.Serializable;

public class SongData implements Serializable {
    private final Uri albumArt;
    private String songId;
    private String title;
    private String album;
    private String artist;
    //private Uri songUrl;

    public SongData(String songId, String title, String album, String artist,  Uri albumArt){
        this.songId = songId;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.albumArt = albumArt;

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

    public String getAlbum() {
        return album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }

}

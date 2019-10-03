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

import java.io.Serializable;

public class PlaylistData implements Serializable {
    private String playlistName;
    private String albumArt;

    public PlaylistData(String playlistName, String albumArt){
        this.playlistName = playlistName;
        this.albumArt = albumArt;

    }

    public String getPlaylistName() {
        return playlistName;
    }

    public String getAlbumArt() {
        return albumArt;
    }
}

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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
/*
*   The purpose of this class is to sort out the data from the database
*   as the database queries return as cursors, so I used this class to
*   sort out the data into ArrayLists for RecyclerView.
 */
public class PlaylistDBController {

    public static ArrayList<PlaylistData> getPlaylists(Context context){
        ArrayList<PlaylistData> playlists = new ArrayList<>();
        ArrayList<String> name = new ArrayList<>();
        PlaylistDB db;
        db = PlaylistDB.getInstance(context);
        Cursor cursor = db.playlistDao().queryPlaylists();
        while (cursor.moveToNext()){
            String playlist = cursor.getString(cursor.getColumnIndex("playlist_name"));
            if(!name.contains(playlist)) {
                name.add(playlist);
                playlists.add(new PlaylistData(playlist));
            }
        }
        cursor.close();
        return playlists;
    }

    public static ArrayList<SongData> getSongsFromPlaylist(Context context, String playlistName){
        ArrayList<SongData> audioList = new ArrayList<>();
        PlaylistDB db;
        db = PlaylistDB.getInstance(context);
        Cursor cursor = db.playlistDao().querySongsFromPlaylist(playlistName);
        while(cursor.moveToNext()){
            String data = cursor.getString(cursor.getColumnIndex("song_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String albumArtUri = cursor.getString(cursor.getColumnIndex("album_art"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            audioList.add(new SongData(data, title, album, artist, albumArtUri, duration));
        }
        cursor.close();
        return audioList;

    }

    public static ArrayList<Playlist> getPlaylistObjects(Context context, String playlistName){
        ArrayList<Playlist> audioList = new ArrayList<>();
        PlaylistDB db;
        db = PlaylistDB.getInstance(context);
        Cursor cursor = db.playlistDao().querySongsFromPlaylist(playlistName);
        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("playlist_name"));
            int index = cursor.getInt(cursor.getColumnIndex("index"));
            String data = cursor.getString(cursor.getColumnIndex("song_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String albumArtUri = cursor.getString(cursor.getColumnIndex("album_art"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            audioList.add(new Playlist(name, index, data, title, album, artist, albumArtUri, duration));
        }
        cursor.close();
        PlaylistDB.destroyInstance();
        return audioList;

    }

    public static ArrayList<SongData> findAudio(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<SongData> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                String albumArtUri = albumUri.toString();
                list.add(new SongData(data, title, album, artist, albumArtUri, duration));
            }
        }
        cursor.close();
        PlaylistDB.destroyInstance();
        return list;
    }

    public static void insertPlaylist(Context context, PlaylistData playlist){

    }

    public static void deletePlaylist(Context context, String playlistName){
        ArrayList<Playlist> playlists =new ArrayList<>();
        PlaylistDB db;
        db = PlaylistDB.getInstance(context);
        playlists = getPlaylistObjects(context, playlistName);
        for(int i=0; i<playlists.size(); i++){
            if(playlists.get(i).getPlaylistName().equals(playlistName)){
                db.playlistDao().deletePlaylist(playlists.get(i));
            }
        }
        PlaylistDB.destroyInstance();
    }

    public static void nukeDatabase(Context context) {
        PlaylistDB db;
        db = PlaylistDB.getInstance(context);
        db.playlistDao().resetPlaylist();
        PlaylistDB.destroyInstance();

    }

}

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
import android.util.Log;

import java.util.ArrayList;
/*
*   The purpose of this class is to sort out the data from the database
*   as the database queries return as cursors, so I used this class to
*   sort out the data into ArrayLists for RecyclerView.
 */
public class PlaylistDBController {

    public static ArrayList<PlaylistData> getPlaylists(Context context){
        ArrayList<PlaylistData> playlists = new ArrayList<>();
        PlaylistsDB db;
        db = PlaylistsDB.getInstance(context);
        Cursor cursor = db.playlistsDao().queryPlaylists();
        while (cursor.moveToNext()){
            String playlistName = cursor.getString(cursor.getColumnIndex("name"));
            String playlistArt = cursor.getString(cursor.getColumnIndex("album_art"));
            playlists.add(new PlaylistData(playlistName, playlistArt));
        }
        cursor.close();
        PlaylistsDB.destroyInstance();
        return playlists;
    }

    public static ArrayList<SongData> getSongsFromPlaylist(Context context, int playlistID){
        ArrayList<SongData> audioList = new ArrayList<>();
        SongsDB db;
        db = SongsDB.getInstance(context);
        Cursor cursor = db.songsDao().querySongsFromPlaylist(playlistID);
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
        SongsDB.destroyInstance();
        return audioList;

    }

    public static ArrayList<Songs> getPlaylistObjects(Context context, int playlistID){
        ArrayList<Songs> audioList = new ArrayList<>();
        SongsDB db;
        db = SongsDB.getInstance(context);
        Cursor cursor = db.songsDao().querySongsFromPlaylist(playlistID);
        while(cursor.moveToNext()){
            int index = cursor.getInt(cursor.getColumnIndex("index"));
            String data = cursor.getString(cursor.getColumnIndex("song_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String albumArtUri = cursor.getString(cursor.getColumnIndex("album_art"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            audioList.add(new Songs(index, data, title, album, artist, albumArtUri, duration, id));
        }
        cursor.close();
        SongsDB.destroyInstance();
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
        SongsDB.destroyInstance();
        return list;
    }

    public static void insertPlaylist(Context context, Playlists playlist, SongData[] songs){
        SongsDB songsDB;
        PlaylistsDB playlistsDB;
        songsDB = SongsDB.getInstance(context);
        int index = 0;
        //insert playlists first
        playlistsDB = PlaylistsDB.getInstance(context);
        playlistsDB.playlistsDao().insertPlaylist(playlist);
        index = playlistsDB.playlistsDao().queryLastInsert()-1;
        Log.e("DEBUG:", Integer.toString(index));
        //then insert songs
        for(int i =0; i < songs.length; i++)
        {
            Songs song = new Songs(0, songs[i].getSongId(),songs[i].getTitle(),songs[i].getAlbum(),songs[i].getArtist(),songs[i].getAlbumArt().toString(),songs[i].getDuration(), index);
            songsDB.songsDao().insertPlaylist(song);
        }
        SongsDB.destroyInstance();
        PlaylistsDB.destroyInstance();

    }

    public static int getPlaylistID(Context context, String playlistName){
        PlaylistsDB db;
        //put a default -1 value if it errors out
        int id = -1;
        db = PlaylistsDB.getInstance(context);
        Cursor cursor = db.playlistsDao().queryPlaylistID(playlistName);
        //we should only receive one value, so use if
        if (cursor.moveToNext()){
            id = cursor.getInt(cursor.getColumnIndex("id"));
        }
        cursor.close();
        PlaylistsDB.destroyInstance();
        return id;

    }

    public static void deletePlaylist(Context context, String playlistName){
        ArrayList<Songs> songs =new ArrayList<>();
        SongsDB db;
        PlaylistsDB playlistsDB;
        playlistsDB = PlaylistsDB.getInstance(context);
        db = SongsDB.getInstance(context);
        //get playlist id to delete objects by
        int id = getPlaylistID(context, playlistName);
        songs = getPlaylistObjects(context, id);
        for(int i = 0; i< songs.size(); i++){
            if(songs.get(i).getPlaylistID() == id){
                db.songsDao().deletePlaylist(songs.get(i));
            }
        }
        playlistsDB.playlistsDao().deletePlaylistByID(id);
        SongsDB.destroyInstance();
    }

    public static void nukeDatabase(Context context) {
        //nuke both songdb and playlistdb entries
        SongsDB db;
        PlaylistsDB db1;
        db = SongsDB.getInstance(context);
        db1 = PlaylistsDB.getInstance(context);
        db.songsDao().resetPlaylist();
        db1.playlistsDao().resetPlaylist();
        SongsDB.destroyInstance();
        PlaylistsDB.destroyInstance();

    }

}

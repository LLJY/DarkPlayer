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
            int number = cursor.getInt(cursor.getColumnIndex("number_of_songs"));
            playlists.add(new PlaylistData(playlistName, playlistArt, number));
        }
        cursor.close();
        db.destroyInstance();
        return playlists;
    }

    public static ArrayList<SongData> getSongsFromPlaylist(Context context, int playlistID){
        ArrayList<SongData> audioList = new ArrayList<>();
        PlaylistsDB db;
        db = PlaylistsDB.getInstance(context);
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
        db.destroyInstance();
        return audioList;

    }

    public static ArrayList<Songs> getPlaylistObjects(Context context, int playlistID){
        ArrayList<Songs> audioList = new ArrayList<>();
        PlaylistsDB db;
        db = PlaylistsDB.getInstance(context);
        Cursor cursor = db.songsDao().querySongsFromPlaylist(playlistID);
        while(cursor.moveToNext()){
            int index = cursor.getInt(cursor.getColumnIndex("index"));
            int id = cursor.getInt(cursor.getColumnIndex("playlist_id"));
            String data = cursor.getString(cursor.getColumnIndex("song_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String albumArtUri = cursor.getString(cursor.getColumnIndex("album_art"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            audioList.add(new Songs(index, id, data, title, album, artist, albumArtUri, duration));
        }
        cursor.close();
        db.destroyInstance();
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
        return list;
    }

    public static void insertPlaylist(Context context, Playlists playlist, SongData[] songs){ ;
        PlaylistsDB playlistsDB;
        playlistsDB = PlaylistsDB.getInstance(context);
        int id = 0;
        //insert playlists first
        playlistsDB.playlistsDao().insertPlaylist(playlist);
        id = playlistsDB.playlistsDao().queryLastInsert();
        //log for sanity check
        Log.e("tesststaAETAST:", Integer.toString(playlistsDB.playlistsDao().queryLastInsert()));
        Log.e("INDEX:", Integer.toString(id));

        //then insert songs
        for(int i =0; i < songs.length; i++)
        {
            Songs song = new Songs(0,id, songs[i].getSongId(),songs[i].getTitle(),songs[i].getAlbum(),songs[i].getArtist(),songs[i].getAlbumArt().toString(),songs[i].getDuration());
            playlistsDB.songsDao().insertPlaylist(song);
        }
        playlistsDB.destroyInstance();

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
        db.destroyInstance();
        return id;

    }

    public static void deletePlaylist(Context context, String playlistName){
        ArrayList<Songs> songs =new ArrayList<>();
        PlaylistsDB db;
        db = PlaylistsDB.getInstance(context);
        //get playlist id to delete objects by
        int id = getPlaylistID(context, playlistName);
        songs = getPlaylistObjects(context, id);
        for(int i = 0; i< songs.size(); i++){
            if(songs.get(i).getPlaylistID() == id){
                db.songsDao().deletePlaylist(songs.get(i));
            }
        }
        db.playlistsDao().deletePlaylistByID(id);
        db.destroyInstance();
    }

    public static void nukeDatabase(Context context) {
        //nuke both songdb and playlistdb entries
        PlaylistsDB db;
        db = PlaylistsDB.getInstance(context);
        db.songsDao().resetPlaylist();
        db.playlistsDao().resetPlaylist();
        PlaylistsDB.destroyInstance();

    }

}

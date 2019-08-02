package com.lucas.darkplayer.Deprecated;
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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.lucas.darkplayer.PlaylistData;
import com.lucas.darkplayer.SongData;

import java.util.ArrayList;

public class PlaylistDBHandler extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "songdb";
    private static final String TABLE_SONGS = "songdetails";
    private static final String KEY_PLAYLIST = "playlistname";
    private static final String KEY_INDEX = "id";
    private static final String KEY_SONGID = "song_id";
    private static final String KEY_TITLE= "title";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUMART = "album_art";

    public PlaylistDBHandler(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_SONGS + "("
                + KEY_PLAYLIST + " TEXT PRIMARY KEY," + KEY_TITLE + " TEXT,"
                + KEY_INDEX + " INTEGER,"
                + KEY_ALBUM + " TEXT,"
                + KEY_ARTIST + " TEXT,"
                + KEY_SONGID + " TEXT,"
                + KEY_ALBUMART + " TEXT"+ ")";
        db.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        onCreate(db);
    }

    public void insertPlaylist(String songId, String playlist, int index, String title, String album, String artist, Uri albumArt){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_SONGID, songId);
        cValues.put(KEY_PLAYLIST, playlist);
        cValues.put(KEY_INDEX, index);
        cValues.put(KEY_TITLE, title);
        cValues.put(KEY_ALBUM, album);
        cValues.put(KEY_ARTIST, artist);
        cValues.put(KEY_ALBUMART, albumArt.toString());
        long rowId = db.insert(TABLE_SONGS,null,cValues);
        db.close();
    }

    public ArrayList <SongData> GetSongs(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<SongData> audioList = new ArrayList<>();
        String query = "SELECT song_id, playlistname, id, title, album, artist, album_art FROM "+ TABLE_SONGS;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            String data = cursor.getString(cursor.getColumnIndex(KEY_SONGID));
            //cursor.getString(cursor.getColumnIndex(KEY_INDEX)));
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String album = cursor.getString(cursor.getColumnIndex(KEY_ALBUM));
            String artist = cursor.getString(cursor.getColumnIndex(KEY_ARTIST));
            String albumArtUri = cursor.getString(cursor.getColumnIndex(KEY_ALBUMART));
            audioList.add(new SongData(data, title, album, artist, albumArtUri));
        }
        return audioList;
    }

    public ArrayList <SongData> GetSongsFromPlaylist(String playlist){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<SongData> audioList = new ArrayList<>();
        Cursor cursor = db.query(TABLE_SONGS, new String[]{KEY_SONGID, KEY_INDEX, KEY_TITLE, KEY_ALBUM, KEY_ARTIST, KEY_ALBUMART}, KEY_PLAYLIST+ "=?",new String[]{String.valueOf(playlist)},null, null, null, null);
        if (cursor.moveToNext()){
            String data = cursor.getString(cursor.getColumnIndex(KEY_SONGID));
            //cursor.getString(cursor.getColumnIndex(KEY_INDEX)));
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String album = cursor.getString(cursor.getColumnIndex(KEY_ALBUM));
            String artist = cursor.getString(cursor.getColumnIndex(KEY_ARTIST));
            String albumArtUri = cursor.getString(cursor.getColumnIndex(KEY_ALBUMART));
            audioList.add(new SongData(data, title, album, artist, albumArtUri));
        }
        return audioList;
    }

    public ArrayList <PlaylistData> GetPlaylists(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<PlaylistData> playlists = new ArrayList<>();
        String query = "SELECT playlistname FROM "+ TABLE_SONGS;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            String playlist = cursor.getString(cursor.getColumnIndex(KEY_PLAYLIST));
            playlists.add(new PlaylistData(playlist));
        }
        return playlists;
    }

    public void deleteSong(int index){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SONGS, KEY_INDEX+" = ?",new String[]{String.valueOf(index)});
        db.close();
    }
}

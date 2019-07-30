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
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Playlist.class}, version = 2)
public abstract class PlaylistDB extends RoomDatabase {
    private static final String DB_NAME="playlistdb";
    private static PlaylistDB instance;
    public abstract PlaylistDao playlistDao();

    public static synchronized PlaylistDB getInstance(Context context){
        if(instance==null){
            instance= Room.databaseBuilder(context.getApplicationContext(), PlaylistDB.class, DB_NAME).fallbackToDestructiveMigration().allowMainThreadQueries().build();
        }
        return instance;
    }
    public static void destroyInstance(){
        instance = null;
    }
}

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

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CreatePlaylistActivity extends AppCompatActivity {
    Button addPlaylist;
    static ArrayList<Integer> added= new ArrayList<>();
    List<SongData> data;
    CreatePlaylistRecyclerAdapter adapter;
    PlaylistDB db;
    PlaylistDBController dbc;
    PlaylistFragment pF;
    SongFragment main;
    EditText playlistName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_playlist);
        data = PlaylistDBController.findAudio(this);
        db = PlaylistDB.getInstance(this);
        try {
            pF = new PlaylistFragment();
            final RecyclerView recyclerView;
            recyclerView = findViewById(R.id.create_playlist_recycler);
            recyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {
                            if(added.contains(position)) {
                                added.remove(Integer.valueOf(position));
                                adapter.notifyItemChanged(position);
                            }else{
                                added.add(position);
                                adapter.notifyItemChanged(position);
                            }
                        }

                        @Override
                        public void onLongItemClick(View view, int position) {
                            // do whatever
                        }

                    })
            );
            adapter = new CreatePlaylistRecyclerAdapter(data, this.getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        playlistName = findViewById(R.id.playlist_name);
        addPlaylist=findViewById(R.id.done_button);
        addPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playlistName.getText().toString().matches("")){
                    Toast.makeText(getApplicationContext(), "Please Add Playlist Name!", Toast.LENGTH_LONG).show();
                }else if(added.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Playlist is EMPTY, Please Add Songs!!", Toast.LENGTH_LONG).show();
                }else{
                    for(int i=0; i<added.size(); i++){
                        int l = added.get(i);
                        Playlist playlist = new Playlist(playlistName.getText().toString(),0,data.get(l).getSongId(),data.get(l).getTitle(),data.get(l).getAlbum(),data.get(l).getArtist(),data.get(l).getAlbumArt().toString());
                        db.playlistDao().insertPlaylist(playlist);

                    }
                    onBackPressed();

                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        added.clear();
        finish();
    }
}


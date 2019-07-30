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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {
    Button newPlaylist;
    ArrayList<PlaylistData> playlists = new ArrayList<>();
    PlaylistRecyclerAdapter adapter;
    PlaylistDBController db;
    SwipeRefreshLayout srl;
    RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_layout, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        newPlaylist = view.findViewById(R.id.add_playlist);
        srl = view.findViewById(R.id.srl);
        recyclerView = view.findViewById(R.id.playlistrecycler);
        new DatabaseAccess().execute();
        recyclerView.setVisibility(View.GONE);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String title = ((TextView) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.playlist_name)).getText().toString();
                        Intent intent = new Intent(getActivity(), PlaylistPlayerActivity.class);
                        intent.putExtra("playlistName", title);
                        startActivity(intent);
                        //start Activity to hold new fragment to avoid
                        //fragment in fragment weirdness
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        String title = ((TextView) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.playlist_name)).getText().toString();
                        Bundle bundle = new Bundle();
                        bundle.putString("playlistName", title);
                        PlaylistDialogFragment dialog = new PlaylistDialogFragment();
                        dialog.setArguments(bundle);
                        dialog.show(getFragmentManager(), "dialog");

                    }

                })
        );
        newPlaylist.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), CreatePlaylistActivity.class);
            startActivity(intent);
            }
        });
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new DatabaseAccess().execute();
            }
        });
    }
    private class DatabaseAccess extends AsyncTask<PlaylistData, Void, ArrayList<PlaylistData>> {
        /* we will use AsyncTask for database queries to reduce lag.
         */

        @Override
        protected ArrayList<PlaylistData> doInBackground(PlaylistData...data){
            ArrayList<PlaylistData> playlist = new ArrayList<>();
            playlist=db.getPlaylists(getActivity());
            return playlist;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<PlaylistData> data){
            super.onPostExecute(data);
            playlists=data;
            if(!data.isEmpty()) {
                adapter = new PlaylistRecyclerAdapter(data, getActivity().getApplication());
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                recyclerView.setVisibility(View.VISIBLE);
                srl.setRefreshing(false);
            }

        }
    }

}

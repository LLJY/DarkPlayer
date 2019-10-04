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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {
    //initialise variables
    Button newPlaylist;
    ArrayList<PlaylistData> playlists = new ArrayList<>();
    PlaylistRecyclerAdapter adapter;
    PlaylistDBController db;
    SwipeRefreshLayout srl;
    RecyclerView recyclerView;
    TextView noPlaylistsText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_layout, container, false);
        newPlaylist = view.findViewById(R.id.add_playlist);
        srl = view.findViewById(R.id.srl);
        recyclerView = view.findViewById(R.id.playlistrecycler);
        int px = Math.round(CommonMethods.convertDpToPixel(12, getActivity()));
        recyclerView.addItemDecoration(new SpacesItemDecoration(2, px, false));
        noPlaylistsText = view.findViewById(R.id.no_songs_playlist);
        //We are starting, updateData so dataset(playlists) is not null
        updateData();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().show();
        }
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        //starts fragment to start playlist screen
                        String title = ((TextView) recyclerView.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.playlist_name)).getText().toString();
                        Intent intent = new Intent(getActivity(), PlaylistPlayerActivity.class);
                        intent.putExtra("playlistName", title);
                        startActivity(intent);
                        /*
                         * start Activity to hold new fragment to avoid
                         * fragment in fragment weirdness
                         */
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        //launch dialog for user delete confirmation.
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
                updateData();
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
         * When fragment restarts, update recyclerview
         * so that user does not have to manually refresh
         * +1 for user experience
         */

        updateData();
    }

    private void updateData() {
        /*
         * This function is responsible for updating data
         * by querying the database for data in a seperate thread.
         * then updating recyclerview to show the new data.
         * In my case, notifydatasetchanged does not work, hence
         * I am redefining adapter.
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                 * run database queries in a seperate tread
                 * to avoid stalling the ui thread
                 */
                final ArrayList<PlaylistData> playlist = PlaylistDBController.getPlaylists(getActivity());
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!getActivity().isFinishing()) {
                            /*
                             * Now that db query has completed we can go ahead and initialise the rest
                             * of the user interface.
                             */
                            playlists = playlist;
                            if (!playlist.isEmpty()) {
                                /*
                                 * If playlist is not empty initialise the app as usual.
                                 * Otherwise, set recyclerview to not be visible to avoid crashes
                                 * also set textview warning to visible so that it shows up when
                                 * no playlists are available
                                 */
                                adapter = new PlaylistRecyclerAdapter(playlist, getActivity().getApplication());
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
                                recyclerView.setVisibility(View.VISIBLE);
                                noPlaylistsText.setVisibility(View.GONE);
                                srl.setRefreshing(false);
                            } else {
                                /*
                                 * Set no Playlists warning
                                 */
                                recyclerView.setVisibility(View.GONE);
                                noPlaylistsText.setVisibility(View.VISIBLE);
                                srl.setRefreshing(false);
                            }
                        }
                    }
                });
            }
        }).run();
    }
}


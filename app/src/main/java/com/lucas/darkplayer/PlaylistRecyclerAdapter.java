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
import android.content.Context;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

public class PlaylistRecyclerAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {

    List<PlaylistData> list = Collections.emptyList();
    Context context;

    public PlaylistRecyclerAdapter(List<PlaylistData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public PlaylistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_row_layout, parent, false);
        PlaylistViewHolder holder = new PlaylistViewHolder(view);
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(PlaylistViewHolder holder, int position) {
        holder.title.setText(list.get(position).getPlaylistName());
        //holder.imageView.setImageURI(null);
        //holder.imageView.setImageURI(list.get(position).getAlbumArt());
    }

    public void updateItem(int position){
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void insert(int position, PlaylistData data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(SongData data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }
}

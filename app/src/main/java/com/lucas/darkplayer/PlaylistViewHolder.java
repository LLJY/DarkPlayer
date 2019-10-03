package com.lucas.darkplayer;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView title;
    TextView songNumber;
    ImageView imageView;

    PlaylistViewHolder(View itemView) {
        super(itemView);
        cv = itemView.findViewById(R.id.cardView);
        title = itemView.findViewById(R.id.playlist_name);
        songNumber = itemView.findViewById(R.id.playlist_number_of_songs);
        imageView = itemView.findViewById(R.id.playlist_image);
    }
}
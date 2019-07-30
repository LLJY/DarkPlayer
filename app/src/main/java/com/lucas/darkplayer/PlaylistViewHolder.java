package com.lucas.darkplayer;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PlaylistViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView title;
    ImageView imageView;

    PlaylistViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        title = (TextView) itemView.findViewById(R.id.playlist_name);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}
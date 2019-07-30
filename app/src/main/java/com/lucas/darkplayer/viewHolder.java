package com.lucas.darkplayer;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class viewHolder extends RecyclerView.ViewHolder {

    FrameLayout cv;
    TextView title;
    TextView description;
    ImageView imageView;
    ImageView playPause;

    viewHolder(View itemView) {
        super(itemView);
        cv = (FrameLayout) itemView.findViewById(R.id.frameView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.artist);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
        playPause = (ImageView) itemView.findViewById(R.id.playPause);
    }
}
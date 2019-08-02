package com.lucas.darkplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class PlaylistPlayerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);
        Intent intent = getIntent();
        String title = intent.getStringExtra("playlistName");
        Bundle bundle = new Bundle();
        setTitle(title);
        bundle.putBoolean("playlist", true);
        bundle.putString("playlistName", title);
        Fragment nextFrag = new SongFragment();
        nextFrag.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.plist_container, nextFrag ); // give your fragment container id in first parameter
        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
        transaction.commit();
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(PlaylistPlayerActivity.this, DefaultTab.class);
        //clear activity history to avoid back button weirdness
        //where there will be an infinite loop of activities when back is pressed
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }
}

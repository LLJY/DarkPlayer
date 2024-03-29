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

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SongFragment extends Fragment implements Serializable {
    RecyclerView recyclerView;
    SeekBar songStat;
    static int[] shuffleList;
    boolean permissionGranted, shuffled=false, fromPlaylist=false, shakeToShuffle, serviceBound=false, loop=false;
    public static boolean doNotUpdate=false;
    //initialise variables
    RecyclerAdapter adapter;
    ImageButton playPause, playPause2, shuffle, repeat;
    ImageView shuffleOn, repeatOn;
    static int songInList = 0;
    TextView song, artist, seekCurr, seekEnd, noSongs, noSongs1;
    String playlistName;
    private SensorManager mSensorManager;
    private ShakeListener mSensorListener;
    private Handler mHandler = new Handler();
    ImageView img, img2;
    public static PlaybackStatus pStatus = PlaybackStatus.STOPPED;
    int previousSong = 0;
    public static PlayerService player;
    int current;
    static ArrayList<SongData> audioList;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updateIndex = intent.getBooleanExtra("updateIndex",false);
            boolean updatePlayerStatus = intent.getBooleanExtra("updatePlayerStatus", false);
            if(updateIndex){
                //store previous song so rV updates properly
                previousSong=shuffleList[songInList];
                songInList = intent.getIntExtra("index",0);
                shuffleList = intent.getIntArrayExtra("shuffleList");
                onSongChange(previousSong);
            }
            if(updatePlayerStatus){
                updatePlayerStatus();
            }


        }
    };

    @Override
    public void onDestroy() {
        /*
         * unbind service and stop player when
         * Fragment is destroyed
         */
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        /*
         * This was meant for another feature where the session would be given to
         * sharedpreferences onDestroy.
         * It is a terrible way to do things but it's the most straightforward
         * for now... As I have a terrible understanding of Android API's
         * and the Fragment LifeCycle.
         */
        super.onDestroyView();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Bundle sState = savedInstanceState;
        View view = inflater.inflate(R.layout.activity_main, container, false);
        //get the arguments and values as early as possible
        Bundle arguments = getArguments();
        try {
            fromPlaylist = arguments.getBoolean("playlist", false);
            playlistName = arguments.getString("playlistName");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        //set ID's for ui elements
        shakeToShuffle = prefs.getBoolean("sts", false);
        loop = prefs.getBoolean("sts", false);
        img = view.findViewById(R.id.albumArtBig);
        img2 = view.findViewById(R.id.albumArtTop);
        song = view.findViewById(R.id.title);
        artist = view.findViewById(R.id.artist);
        seekCurr = view.findViewById(R.id.seekTime);
        recyclerView = view.findViewById(R.id.recyclerView);
        seekEnd = view.findViewById(R.id.seekEnd);
        shuffleOn = view.findViewById(R.id.shuffle_on);
        repeatOn = view.findViewById(R.id.loop_on);
        playPause = view.findViewById(R.id.button2);
        playPause2 = view.findViewById(R.id.button3);
        noSongs = view.findViewById(R.id.no_songs);
        noSongs1 = view.findViewById(R.id.no_songs1);
        final ImageButton prev = view.findViewById(R.id.button1);
        final ImageButton next = view.findViewById(R.id.button);
        repeat = view.findViewById(R.id.loop);
        shuffle = view.findViewById(R.id.shuffle);

        /*
         *
         */
        if (shuffled) {
            shuffleOn.setVisibility(View.VISIBLE);
        } else {
            shuffleOn.setVisibility(View.GONE);
        }
        repeatOn.setVisibility(View.GONE);
        try {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionGranted = false;
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                permissionGranted = true;
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }
        songStat = view.findViewById(R.id.seekBar);
        songStat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar,
                                          int progress,
                                          boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(player.pStatus!=PlaybackState.STATE_STOPPED){
                    current = songStat.getProgress();
                    player.resumePosition = current;
                    player.mediaPlayer.seekTo(current);
                }
            }
        });

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayerStatus(false);
            }
        });

        playPause2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayerStatus(false);
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextSong();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevSong();
            }
        });
        //Check if playing from playlist.
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<SongData> a;
                if (permissionGranted) {
                    if (!fromPlaylist) {
                        a = PlaylistDBController.findAudio(getActivity());
                    } else {
                        //get ID from the name by calling function from controller.
                        a = PlaylistDBController.getSongsFromPlaylist(getActivity(), PlaylistDBController.getPlaylistID(getActivity(), playlistName));
                    }
                } else {
                    a = new ArrayList<>();
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        audioList = a;
                        //initialize sharedprefs
                        final StoreData storage = new StoreData(getActivity().getApplicationContext());
                        //set repeat on or off depending on value in sharedprefs
                        if(storage.loadRepeat()){
                            repeatOn.setVisibility(View.VISIBLE);
                        }else{
                            repeatOn.setVisibility(View.GONE);
                        }
                        if (!getActivity().isFinishing()) {
                            if (permissionGranted && a != null && a.size() != 0 ) {
                                if (fromPlaylist) {
                                    //always initialize with zero when playlist is opened
                                    songInList = 0;
                                    shuffleList = new int[audioList.size()];
                                    defaultShuffleList();
                                }else {
                                    try {
                                        if (sState.getBoolean("shuffled")) {
                                            int testList[] = sState.getIntArray("shuffleList");
                                            if (testList.length == audioList.size()) {
                                                shuffleList = testList;
                                                shuffled = true;
                                            } else {
                                                shuffled = false;
                                            }
                                            songInList = CommonMethods.indexOf(shuffleList, sState.getInt("index"));
                                        }
                                    } catch (NullPointerException e) {
                                        e.printStackTrace();
                                        shuffleList = new int[audioList.size()];
                                        defaultShuffleList();
                                    }
                                }
                                repeat.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (storage.loadRepeat()) {
                                            storage.storeRepeat(false);
                                            repeatOn.setVisibility(View.GONE);
                                        } else {
                                            storage.storeRepeat(true);
                                            repeatOn.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                                shuffle.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (shuffled) {
                                            songInList = shuffleList[songInList];
                                            shuffleList = new int[audioList.size()];
                                            defaultShuffleList();
                                            shuffled = false;
                                            shuffleOn.setVisibility(View.GONE);
                                        } else {
                                            shuffled=true;
                                            previousSong = shuffleList[songInList];
                                            player.shuffle();
                                            shuffleOn.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                                song.setText(audioList.get(shuffleList[songInList]).getTitle());
                                artist.setText(audioList.get(shuffleList[songInList]).getArtist());
                                mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
                                mSensorListener = new ShakeListener();
                                mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {
                                    @Override
                                    public void onShake() {
                                        if (shakeToShuffle && !shuffled) {
                                            //check if shake to shuffle is enabled
                                            Toast.makeText(getActivity(), "Shuffling!", Toast.LENGTH_LONG).show();
                                            //shuffle when shaken
                                            previousSong = shuffleList[songInList];
                                            player.shuffle();
                                            shuffleOn.setVisibility(View.VISIBLE);
                                        }else if(shakeToShuffle && shuffled){
                                            shuffled=false;
                                            defaultShuffleList();
                                            shuffleOn.setVisibility(View.GONE);
                                        }
                                    }
                                });
                                recyclerView.addOnItemTouchListener(
                                        new RecyclerItemClickListener(getActivity(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(View view, int position) {
                                                /*
                                                 * if audioList is not the same and clicked, set doNotUpdate to false as we are about to update
                                                 * audioList inside setPlayerStatus()
                                                 */

                                                if(player != null && player.audioList != audioList) {
                                                    doNotUpdate = false;
                                                }
                                                //store prev to update item so that playing button is shown
                                                previousSong = shuffleList[songInList];
                                                onSongChange(previousSong);
                                                if (CommonMethods.indexOf(shuffleList, position) != -1) {
                                                    songInList = CommonMethods.indexOf(shuffleList, position);
                                                }
                                                setPlayerStatus(true);
                                            }

                                            @Override
                                            public void onLongItemClick(View view, int position) {
                                                // do whatever
                                            }

                                        })
                                );
                                recyclerView.setVisibility(View.VISIBLE);
                                noSongs.setVisibility(View.GONE);
                                noSongs1.setVisibility(View.GONE);
                                adapter = new RecyclerAdapter(audioList, getActivity());
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                                //set first song
                                onSongChange(previousSong);
                            } else {
                                recyclerView.setVisibility(View.GONE);
                                noSongs.setVisibility(View.VISIBLE);
                                noSongs1.setVisibility(View.VISIBLE);

                            }
                        }
                    }
                });
            }
        }).run();

        return view;
    }
    private void defaultShuffleList() {
        if(shuffleList != null && shuffleList.length != 0) {
            /*
            * if shuffleList already exists, we will call the method in player instead
            * and let it handle regenerating the new list
             */
            try {
                player.defaultShuffleList();
            }catch (NullPointerException e){
                e.printStackTrace();
                /*
                * NullPointerException likely means that the service has not started or is not bound
                * we will manually generate the list in that case.
                 */
                shuffleList = new int[audioList.size()];
                for (int i = 0; i < audioList.size(); i++) {
                    shuffleList[i] = i;
                }
            }
        }else {
            shuffleList = new int[audioList.size()];
            for (int i = 0; i < audioList.size(); i++) {
                shuffleList[i] = i;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(!fromPlaylist) {
            outState.putIntArray("shuffleList", shuffleList);
            outState.putBoolean("shuffled", shuffled);
            outState.putInt("index", songInList);
            outState.putBoolean("ServiceState", serviceBound);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int duration;
                if(serviceBound){
                    try {
                        //it rarely but sometimes errors out, there is no way around it, hence just add a try-catch
                        if (player.mediaPlayer != null) {
                            duration = player.mediaPlayer.getDuration();
                            current = player.mediaPlayer.getCurrentPosition();
                            songStat.setMax(duration);
                            songStat.setProgress(current);
                            String time = String.format(Locale.ENGLISH, "%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(duration),
                                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                            );
                            String time1 = String.format(Locale.ENGLISH, "%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(current),
                                    TimeUnit.MILLISECONDS.toSeconds(current) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current))
                            );
                            seekCurr.setText(time1);
                            seekEnd.setText(time);
                        }
                    }catch(IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                mHandler.postDelayed(this, 1000);
            }
        });
    }


    private void startService(ArrayList<SongData> audioList, int index){
        Bundle bundle = new Bundle();
        bundle.putSerializable("audioList", audioList);
        Intent startIntent = new Intent(getActivity(), PlayerService.class);
        startIntent.putExtra("bundle", bundle);
        startIntent.putExtra("updateIndex", true);
        startIntent.putExtra("index", index);
        startIntent.putExtra("shuffleList", shuffleList);
        getActivity().startService(startIntent);
        if(!serviceBound) {
            getActivity().bindService(startIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    private void playAudio(boolean reset) {
        /*
         * completely reset the player to match whatever is loaded into audioList
         * this is to ensure that when an item is clicked when it a playlist, it does not continue playing the previous songs.
         */
        updatePlayerService();
        player.playIndex(songInList);
        onSongChange(previousSong);

    }

    private void updatePlayerService() {
        /*
         * function that updates the values in PlayerService with values from SongFragment
         */
        player.index = songInList;
        player.shuffleList = shuffleList;
        player.audioList = audioList;
    }

    private void updatePlayerStatus(){

        switch(player.pStatus){
            case PlaybackState.STATE_PAUSED:
                playPause.setImageResource(R.drawable.play);
                playPause2.setImageResource(R.drawable.play);
                //we are defining pStatus solely for the purpose of updating the animated thingy for every item.
                pStatus=PlaybackStatus.PAUSED;
                break;
            case PlaybackState.STATE_PLAYING:
                playPause.setImageResource(R.drawable.pause);
                playPause2.setImageResource(R.drawable.pause);
                pStatus=PlaybackStatus.PLAYING;
                break;
            case PlaybackState.STATE_STOPPED:
                playPause.setImageResource(R.drawable.play);
                playPause2.setImageResource(R.drawable.play);
                pStatus=PlaybackStatus.STOPPED;
                break;
        }
        //update as we need the item to display whether or not it is playing.
        if(adapter != null){
            adapter.updateItem(shuffleList[songInList]);
            }

    }

    public void onSongChange(int prev) {
        if (adapter != null) {
            adapter.notifyItemChanged(shuffleList[songInList]);
            adapter.notifyItemChanged(prev);
        }
        if(player == null || player.mediaPlayer == null) {
            /*
             *listen to the player for any song changes by default
             * if it is null, just use whatever we have as values.
             */
            img.setImageURI(audioList.get(shuffleList[songInList]).getAlbumArt());
            img2.setImageURI(audioList.get(shuffleList[songInList]).getAlbumArt());
            song.setText(audioList.get(shuffleList[songInList]).getTitle());
            artist.setText(audioList.get(shuffleList[songInList]).getArtist());
        }else{
            img.setImageURI(player.audioList.get(player.shuffleList[player.index]).getAlbumArt());
            img2.setImageURI(player.audioList.get(player.shuffleList[player.index]).getAlbumArt());
            song.setText(player.audioList.get(player.shuffleList[player.index]).getTitle());
            artist.setText(player.audioList.get(player.shuffleList[player.index]).getArtist());
        }
    }

    private void setPlayerStatus(boolean skip) {
        /*
         * checks current playback status and updates the player accordingly
         */
        switch (player.pStatus) {
            case PlaybackState.STATE_PAUSED:
                if (skip) {
                    playAudio(true);
                } else {
                    player.resumePlayer();
                }
                break;
            case PlaybackState.STATE_PLAYING:
                if (skip) {
                    player.reset();
                    playAudio(true);
                } else {
                    player.pausePlayer();
                }
                break;
            case PlaybackState.STATE_STOPPED:
                startService(audioList, songInList);
                break;
        }
    }

    public void nextSong() {
        //if we do not do this, the service will crash due to index out of bounds error
        if(player.pStatus == PlaybackState.STATE_PLAYING) {
            if(player != null && player.audioList != audioList){
                updatePlayerService();
            }

            player.next();
        }else{
            songInList++;
            playAudio(true);
        }
    }

    public void prevSong() {
        //if we do not do this, the service will crash due to index out of bounds error
        if(player != null && player.audioList != audioList){
            updatePlayerService();
        }
        if(player.pStatus == PlaybackState.STATE_PLAYING) {
            player.prev();
        }else{
            songInList--;
            playAudio(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissionGranted) {
            try {
                mSensorManager.registerListener(mSensorListener,
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_UI);
                getActivity().registerReceiver(receiver, new IntentFilter("seekto"));
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }
        if(player != null && player.audioList != audioList){
            doNotUpdate = true;
        }else{
            doNotUpdate = false;
        }
        if(player != null && player.mediaPlayer != null){
            //get songindex and stuff when fragment resumes so that we don't get weird shit
            shuffleList=player.shuffleList;
            songInList=player.index;
            updatePlayerStatus();
            onSongChange(previousSong);

        }

    }

    @Override
    public void onPause() {
        if (permissionGranted) {
            try {
                mSensorManager.unregisterListener(mSensorListener);
                getActivity().unregisterReceiver(receiver);
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        }
        if (serviceBound) {
            //unbind on pause
            getActivity().unbindService(serviceConnection);
        }
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}

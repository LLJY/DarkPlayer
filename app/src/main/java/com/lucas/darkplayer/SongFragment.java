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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import android.content.ContentUris;
import android.net.Uri;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class SongFragment extends Fragment{
    RecyclerAdapter adapter;
    RecyclerView recyclerView;
    private static SongFragment instance = null;
    SeekBar songStat;
    boolean mBound = false;
    public static int curr=0;
    boolean completed=false;
    ImageButton playPause, playPause2,shuffle,repeat;
    ImageView shuffleOn,repeatOn;
    boolean shuffled=false;
    boolean loop=false;
    boolean onResume=true;
    static int songInList = 0;
    static int shuffleList[];
    static int defaultList[];
    static TextView song,artist;
    TextView seekCurr,seekEnd;
    String playlistName;
    boolean fromPlaylist=false;
    private SensorManager mSensorManager;
    private ShakeListener mSensorListener;
    private SlidingPaneLayout mLayout;
    ImageView img,img2;
    private boolean sts;
    public static PlaybackStatus pStatus = PlaybackStatus.STOPPED;
    private PlayerService player;
    boolean serviceBound = false;
    static ArrayList<SongData> audioList;
    PlaylistDBController db;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            player = binder.getService();
            Toast.makeText(getActivity(), "Service Bound!", Toast.LENGTH_LONG).show();
            serviceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }

    };


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (serviceBound) {
            getActivity().unbindService(serviceConnection);
            //service is active
            player.stopSelf();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        StoreData storage = new StoreData(getActivity().getApplicationContext());
        storage.storeAudioShuffleList(shuffleList);
        }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.activity_main, container, false);
        instance = this;
        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get the arguments and values as early as possible
        Bundle arguments = getArguments();
        try {
            fromPlaylist = arguments.getBoolean("playlist");
            playlistName = arguments.getString("playlistName");
        }catch(NullPointerException e){
            e.printStackTrace();
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        StoreData storage = new StoreData(getActivity().getApplicationContext());
        sts=prefs.getBoolean("sts",false);
        if(!fromPlaylist) {
            songInList = storage.loadAudioIndex();
            shuffled = storage.loadShuffled();
        }else{
            songInList=0;
        }
        img = view.findViewById(R.id.albumArtBig);
        img2 = view.findViewById(R.id.albumArtTop);
        song = view.findViewById(R.id.title);
        artist = view.findViewById(R.id.artist);
        seekCurr = view.findViewById(R.id.seekTime);
        seekEnd = view.findViewById(R.id.seekEnd);
        shuffleOn = view.findViewById(R.id.shuffle_on);
        repeatOn = view.findViewById(R.id.loop_on);
        playPause = view.findViewById(R.id.button2);
        playPause2 = view.findViewById(R.id.button3);
        final ImageButton prev = view.findViewById(R.id.button1);
        final ImageButton next = view.findViewById(R.id.button);
        repeat = view.findViewById(R.id.loop);
        shuffle = view.findViewById(R.id.shuffle);
        if (shuffled) {
            shuffleOn.setVisibility(View.VISIBLE);
        }else{
            shuffleOn.setVisibility(View.GONE);
        }
        repeatOn.setVisibility(View.GONE);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        songStat=view.findViewById(R.id.seekBar);
        songStat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged (SeekBar seekBar,
                                           int progress,
                                           boolean fromUser) {
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekAudio(true);
            }
        });

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayerStatus(shuffleList[songInList],false);
            }
        });

        playPause2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlayerStatus(shuffleList[songInList],false);
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
        if(!fromPlaylist) {
            //generate song data
            audioList = findAudio();
        }else{
            new DatabaseAccess().execute();
        }
        //create shufflelist as a sequential array
        //as we are not shuffled yet

        if(!fromPlaylist && shuffled){
            shuffleList=storage.loadShuffleList();
        }else{
            shuffleList = new int[audioList.size()];
            for (int l = 0; l < audioList.size(); l++) {
                shuffleList[l] = l;
            }
        }
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loop){
                    loop=false;
                    repeatOn.setVisibility(View.GONE);
                }else{
                    loop=true;
                    repeatOn.setVisibility(View.VISIBLE);
                }
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(shuffled){
                    songInList=shuffleList[songInList];
                    for(int l=0; l<audioList.size(); l++){
                        shuffleList[l] = l;
                    }
                    shuffled=false;
                    shuffleOn.setVisibility(View.GONE);
                }else{
                    shuffleList = shuffle(shuffleList, audioList.size());
                    setPlayerStatus(shuffleList[songInList], true);
                    shuffleOn.setVisibility(View.VISIBLE);
                }
                StoreData storage = new StoreData(getActivity().getApplicationContext());
                storage.storeShuffled(shuffled);
                storage.storeAudioShuffleList(shuffleList);
            }
        });
        song.setText(audioList.get(shuffleList[songInList]).getTitle());
        artist.setText(audioList.get(shuffleList[songInList]).getArtist());
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new ShakeListener();
        mSensorListener.setOnShakeListener(new ShakeListener.OnShakeListener() {

            public void onShake() {
                if(sts) {
                    //check if shake to shuffle is enabled
                    Toast.makeText(getActivity(), "Shuffling!", Toast.LENGTH_LONG).show();
                    //shuffle when shaken
                    shuffleList = shuffle(shuffleList, audioList.size());
                    setPlayerStatus(shuffleList[songInList], true);
                    StoreData storage = new StoreData(getActivity().getApplicationContext());
                    storage.storeShuffled(shuffled);
                    storage.storeAudioShuffleList(shuffleList);
                }
            }
        });
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        //store prev to update item so that playing button is shown
                        int prev = shuffleList[songInList];
                        if(indexOf(shuffleList, position) != -1)
                            songInList = indexOf(shuffleList, position);
                        adapter.notifyItemChanged(prev);
                        setPlayerStatus(shuffleList[songInList],true);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        // do whatever
                    }

                })
        );
        if(fromPlaylist) {
            recyclerView.setVisibility(View.GONE);
        }else {
        adapter = new RecyclerAdapter(audioList, getActivity().getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        onSongChange();
        }


    }
    public ArrayList<SongData> findAudio() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        ArrayList<SongData> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC;
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                Long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
                list.add(new SongData(data, title, album, artist, albumArtUri));
            }
        }
        cursor.close();
        return list;
    }

    private int[] shuffle(int playlist[], int max){
        //please run this function in seperate thread to avoid stalls
        shuffled = true;
        shuffleOn.setVisibility(View.VISIBLE);
        int temp;
        int rand;
        for(int l=0; l<max-1; l++) {
            rand = ThreadLocalRandom.current().nextInt(0, max-1);
            temp = playlist[rand];
            playlist[rand] = playlist[l];
            playlist[l] = temp;
            }
        songInList = 0;
        return playlist;
        }

    private int indexOf(int array[], int element){
        for(int l=0; l<array.length; l++){
            if(array[l]==element)
                return l;
        }
        return -1;
    }

    private void playAudio(String media, boolean reset){
        StoreData storage = new StoreData(getActivity().getApplicationContext());
        if(!serviceBound){
            Intent playerIntent=new Intent(getActivity(),PlayerService.class);
            playerIntent.putExtra("media",media);
            playerIntent.putExtra("reset",reset);
            playerIntent.putExtra("pause",false);
            onSongChange();
            getActivity().startService(playerIntent);
            getActivity().bindService(playerIntent,serviceConnection,Context.BIND_AUTO_CREATE);
        }
        storage.storeAudioIndex(songInList);
        if(pStatus == PlaybackStatus.PLAYING){
            storage.storeSession(true);
        }
        else{
            storage.storeSession(false);
        }
        storage.storeAudioData(audioList);

    }

    private void seekAudio(boolean seek)
    {
        Intent seekIntent= new Intent(getActivity(),PlayerService.class);
        seekIntent.putExtra("seek",seek);
        seekIntent.putExtra("seekTo", songStat.getProgress());
        getActivity().startService(seekIntent);
    }


    public void onSongChange() {
        int prev=0;
        int next=0;

        try {
            prev = shuffleList[songInList - 1];
            next = shuffleList[songInList + 1];
        }
        catch(IndexOutOfBoundsException e) {
            prev=0;
            next=0;
        }
        if(adapter !=null) {
            adapter.notifyItemChanged(shuffleList[songInList]);
            adapter.notifyItemChanged(prev);
            adapter.notifyItemChanged(next);
        }
        img.setImageURI(audioList.get(shuffleList[songInList]).getAlbumArt());
        img2.setImageURI(audioList.get(shuffleList[songInList]).getAlbumArt());
        song.setText(audioList.get(shuffleList[songInList]).getTitle());
        artist.setText(audioList.get(shuffleList[songInList]).getArtist());
    }

    public void pauseResumeReset(boolean pause) {
        StoreData storage = new StoreData(getActivity().getApplicationContext());
        img.setImageURI(audioList.get(shuffleList[songInList]).getAlbumArt());
        Intent pauseIntent=new Intent(getActivity(),PlayerService.class);
        pauseIntent.putExtra("pause",pause);
        getActivity().startService(pauseIntent);
        if(pStatus == PlaybackStatus.PLAYING){
            storage.storeSession(true);
        }else{
            storage.storeSession(false);
        }
    }

    private void setPlayerStatus(int position, boolean skip){
        switch (pStatus) {
            case PAUSED:
                if(skip){
                    playAudio(audioList.get(position).getSongId(), true);
                    pStatus = PlaybackStatus.PLAYING;
                }else{
                    pauseResumeReset(true);
                }
                playPause.setImageResource(R.drawable.pause);
                playPause2.setImageResource(R.drawable.pause);
                break;
            case PLAYING:
                if(skip){
                    playAudio(audioList.get(position).getSongId(), true);
                    pStatus = PlaybackStatus.PLAYING;
                    playPause.setImageResource(R.drawable.pause);
                    playPause2.setImageResource(R.drawable.pause);
                }else{
                    pauseResumeReset(true);
                    playPause.setImageResource(R.drawable.play);
                    playPause2.setImageResource(R.drawable.play);
                }
                break;
            case STOPPED:
                playAudio(audioList.get(position).getSongId(), false);
                playPause.setImageResource(R.drawable.pause);
                playPause2.setImageResource(R.drawable.pause);
                pStatus = PlaybackStatus.PLAYING;
                break;
        }
    }

    public void nextSong(){
        if(songInList < audioList.size()-1) {
            //increment song when next is pressed
            songInList++;
            //use song in shuffle playlist
            //it is usually sequential unless shuffled
            setPlayerStatus(shuffleList[songInList],true);
        }
    }
    public void prevSong(){
        if(songInList > 0) {
            //decrement song
            songInList--;
            setPlayerStatus(shuffleList[songInList],true);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int duration = intent.getExtras().getInt("Duration");
            int current = intent.getExtras().getInt("Current");
            boolean playing = intent.getBooleanExtra("IsPlaying", false);
            completed = intent.getBooleanExtra("Completed",false);
            if(completed && !loop){
                completed=false;
                nextSong();
            }else if(completed && loop){
                //just decrement the index and call nextSong
                //so the same song will be played
                completed=false;
                songInList--;
                nextSong();
            }
            if(playing){
                pStatus = PlaybackStatus.PLAYING;
                playPause.setImageResource(R.drawable.pause);
                playPause2.setImageResource(R.drawable.pause);
                pStatus = PlaybackStatus.PLAYING;songStat.setMax(duration);
                songStat.setProgress(current);
                String time = String.format(Locale.ENGLISH,"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration),
                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
                );
                String time1 = String.format(Locale.ENGLISH,"%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(current),
                        TimeUnit.MILLISECONDS.toSeconds(current) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(current))
                );
                seekCurr.setText(time1);
                seekEnd.setText(time);

            }

        }
    };


    @Override
    public void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        getActivity().registerReceiver(receiver, new IntentFilter("seekto"));
    }

    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public void onStart(){
        super.onStart();
        Intent intent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(serviceConnection);
        mBound = false;
    }

    public static SongFragment getInstance() {
        return instance;
    }

    private class DatabaseAccess extends AsyncTask<SongData, Void, ArrayList<SongData>>{
        /* we will use AsyncTask for database queries to reduce lag.
         */

        @Override
        protected ArrayList<SongData> doInBackground(SongData...data){
            ArrayList<SongData> songs = new ArrayList<>();
            songs = db.getSongsFromPlaylist(getActivity(), playlistName);
            return songs;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<SongData> data){
            super.onPostExecute(data);
            audioList = data;
            adapter = new RecyclerAdapter(audioList, getActivity().getApplication());
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setVisibility(View.VISIBLE);
            onSongChange();

        }
    }

    }

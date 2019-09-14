package com.lucas.darkplayer;
/*        Copyright (C) 2019  Lucas Lee Jing Yi
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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, Serializable,

        AudioManager.OnAudioFocusChangeListener {
    private final IBinder myBinder = new LocalBinder();
    public PlayerService(){
    }
    /*
     * This is a service that handles playing music.
     * The original idea was to start this as a foreground service
     * and couple it with NotificationCompat.
     * But refactoring such that PlayerService handles
     * Play, Pause, Resume, Shuffle, Loop. Proved to be a
     * pain in the ass and it broke the app quite badly.
     * For now, we will settle for SongFragment controlling PlayerService.
     */
    //initialise variables
    public MediaPlayer mediaPlayer;
    private boolean changeOnShuffle;
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private String mediaFile;
    private AudioManager audioManager;
    public int pStatus = PlaybackStateCompat.STATE_STOPPED;
    public int resumePosition;
    PlaybackStateCompat state;
    public int[] shuffleList;
    private ArrayList<SongData> audioList;
    int seekTo=0;
    public int index = 0;
    private StoreData storage = new StoreData(this);
    boolean seek=false;
    boolean shuffled=false;
    MediaSessionCompat mSession;
    Notification notification;
    Intent localintent = new Intent("seekto");
    private Handler mHandler = new Handler();
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //unlike the mess of what we did previously, service and ui should communicate via broadcasts
            //and not by calling methods and variables directly from each other as we did previously.
            boolean updateIndex=intent.getBooleanExtra("updateIndex",false);
            if(updateIndex){
                //if index has changed in ui, update it in service
                //basically this happens when the user selects a song from rV.
                index = intent.getIntExtra("index",0);
                shuffleList = intent.getIntArrayExtra("shuffleList");
                index = shuffleList[index];
                reset();
                mediaFile=audioList.get(shuffleList[index]).getSongId();
                initMediaPlayer();
            }
        }
    };
    private void initMediaPlayer() {
        /*
         * Initialises MediaPlayer and calls preparedAsync
         */
        if(mediaPlayer == null || !mediaPlayer.isPlaying() && pStatus != PlaybackStateCompat.STATE_PAUSED ) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnSeekCompleteListener(this);
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                if (mediaFile != null) {
                    mediaPlayer.setDataSource(mediaFile);
                } else {
                    stopSelf();
                }
            } catch (IOException e) {
                e.printStackTrace();
                stopSelf();
            }
            try {
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                stopSelf();
            }
        }
    }
    private void startPlaying() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            localintent.putExtra("index", index);
            localintent.putExtra("shuffleList", shuffleList);
            localintent.putExtra("updateIndex", true);
            sendBroadcast(localintent);
            pStatus=PlaybackState.STATE_PLAYING;
            updatePlayerStatus();
            if(seek){
                seek=false;
                mediaPlayer.seekTo(seekTo);
            }
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
    }
    public void stopPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            pStatus=PlaybackState.STATE_STOPPED;
            updatePlayerStatus();
        }
    }
    public void pausePlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            pStatus=PlaybackState.STATE_PAUSED;
            updatePlayerStatus();

        }
    }
    public void resumePlayer() {
        if (!mediaPlayer.isPlaying()) {
            if(seek){
                seek=false;
                mediaPlayer.seekTo(seekTo);
            }else{
                mediaPlayer.seekTo(resumePosition);
            }
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.start();
            pStatus=PlaybackState.STATE_PLAYING;
            updatePlayerStatus();
        }
    }
    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pausePlayer();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumePlayer();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }
    private void updatePlayerStatus(){
        localintent.putExtra("updatePlayerStatus", true);
        sendBroadcast(localintent);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //send intent over to SongFragment to tell it that song has completed
        if(!storage.loadRepeat()) {
            next();
            localintent.putExtra("updateIndex", true);
            sendBroadcast(localintent);
        }else{
            //decrement and then let next() increment so we remain at the same song.
            index--;
            next();
            //we need not update index here as we are on the same song.
        }

    }
    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //Start playing when it's prepared
        startPlaying();
        buildNotification();
        startForeground(42069, notification);

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int u) {
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Log.d("MediaPlayer Error", "ERROR: MEDIA UNSUPPORTED" + u);
                return true;
            case MediaPlayer.MEDIA_ERROR_IO:
                Log.d("MediaPlayer Error", "ERROR: IO ERROR" + u);
                return true;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "ERROR: NOT VALID FOR PROGRESSIVE PLAYBACK" + u);
                return true;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "ERROR: SERVER DIED" + u);
                return true;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Log.d("MediaPlayer Error", "ERROR: TIMED OUT" + u);
                return true;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "ERROR: UNKNOWN" + u);
                return true;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                Log.d("MediaPlayer Error", "ERROR: NOT SEEK-ABLE" + u);
                return true;
            case MediaPlayer.MEDIA_INFO_AUDIO_NOT_PLAYING:
                Log.d("MediaPlayer Error", "ERROR: MEDIA AUDIO NOT PLAYING" + u);
                return true;
        }
        return true;
    }

    @Override
    public void onAudioFocusChange(int status) {
        switch (status) {
            case AudioManager.AUDIOFOCUS_GAIN:
                resumePlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pausePlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pausePlayer();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f,0.1f);
                break;
        }

    }
    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        //Focus gained
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        //Could not gain focus
    }

    private boolean removeAudioFocus() {
        if (audioManager != null) {
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocus(this);
        }else{
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                    audioManager.abandonAudioFocus(this);
        }
    }

    private void initMediaSession(){
        mSession = new MediaSessionCompat(this, MEDIA_SESSION_SERVICE);
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
                updatePlayerStatus();
                return super.onMediaButtonEvent(mediaButtonIntent);
            }

            @Override
            public void onPrepare() {
                updatePlayerStatus();
                super.onPrepare();
            }

            @Override
            public void onPlay() {
                if(pStatus == PlaybackStateCompat.STATE_PAUSED){
                    resumePlayer();
                }else {
                    startPlaying();
                }
                super.onPlay();
            }

            @Override
            public void onPause() {
                pausePlayer();
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                next();
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                prev();
                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                stopSelf();
                super.onStop();
            }

            @Override
            public void onSeekTo(long pos) {
                int seek = (int) pos;
                mediaPlayer.seekTo(seek);
                super.onSeekTo(pos);
            }
        });
        mSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        if(!mSession.isActive()){
            mSession.setActive(true);
        }
    }

    private void buildNotification(){
        Uri imageUri = audioList.get(shuffleList[index]).getAlbumArt();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        }catch (Exception e){
            e.printStackTrace();
        }
        notification = new NotificationCompat.Builder(this, "com.lucas.darkplayer.MYFUCKINGNOTIFICATION")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(audioList.get(shuffleList[index]).getTitle())
                .setContentText(audioList.get(shuffleList[index]).getArtist())
                .setLargeIcon(bitmap)
                .addAction(android.R.drawable.ic_media_previous, "Previous",MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
                .addAction(android.R.drawable.ic_media_pause, "Pause",MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE))
                .addAction(android.R.drawable.ic_media_next, "Next",MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mSession.getSessionToken()))
                .build();

    }

    public void next() {
        if(index<audioList.size()-1) {
            index++;
            mediaFile = audioList.get(shuffleList[index]).getSongId();
            reset();
            initMediaPlayer();
        }
    }

    public void prev() {
        if(index>=0) {
            index--;
            mediaFile = audioList.get(shuffleList[index]).getSongId();
            reset();
            initMediaPlayer();
        }
    }

    public void reset(){
        /*
        * function to reset mediaplayer and stop playing songs
        * so that a new song can be loaded
        * might not actually be needed, I might rethink this later
        * but leave it for now
         */
        stopPlaying();
        mediaPlayer.reset();
    }

    public void playIndex(int index){
        /*
        * function to reset and then play a song from the selected index
        * this might replace reset() later as in most cases, reset() is used followed by playing a new song.
         */
        reset();
        mediaFile= audioList.get(index).getSongId();
        initMediaPlayer();
    }

    public void defaultShuffleList() {
        shuffled=false;
        if(shuffleList != null && shuffleList.length != 0) {
            index = shuffleList[index];
        }
        shuffleList = new int[audioList.size()];
        for (int i = 0; i < audioList.size(); i++) {
            shuffleList[i] = i;
        }
    }

    public void shuffle() {
        //please run this function in seperate thread to avoid stalls
        shuffled = true;
        int temp;
        int rand;
        for (int i = 0; i < audioList.size(); i++) {
            rand = ThreadLocalRandom.current().nextInt(0, audioList.size()-1);
            temp = shuffleList[rand];
            shuffleList[rand] = shuffleList[i];
            shuffleList[i] = temp;
        }
        int curr = index;
        index = 0;
        //reset and play new song from shuffleList
        if(changeOnShuffle) {
            playIndex(shuffleList[index]);
        }else{
            index = CommonMethods.indexOf(shuffleList, curr);
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get values from sharedprefs
        MediaButtonReceiver.handleIntent(mSession, intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        changeOnShuffle = prefs.getBoolean("changeOnShuffle", false);
        //getting intents from SongFragment to tell PlayerService what to do.
        IntentFilter filter = new IntentFilter("player");
        registerReceiver(receiver, filter);
        Bundle bundle = intent.getBundleExtra("bundle");
        //store array as a test list as we are unsure if it is a null array.
        ArrayList <SongData> testList = null;
        try {
            testList = (ArrayList<SongData>) bundle.getSerializable("audioList");
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        boolean updateIndex = intent.getBooleanExtra("updateIndex",false);
        if(testList != null){
            audioList=testList;
        }
        if(updateIndex){
            shuffleList = intent.getIntArrayExtra("shuffleList");
            index = intent.getIntExtra("index", 0);
            if(shuffleList != null){
                index = shuffleList[index];
            }
        }
        seek = intent.getBooleanExtra("seek", false);
        seekTo = intent.getIntExtra("seekTo", 0);

        if(seek){
            /*
             * seek when SongFragment sends seekIntent
             */
            if(mediaPlayer!= null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekTo);
                    seek = false;
                }
            }
        }

        try {
            //An audio file is passed to the service through putExtra();
            mediaFile = audioList.get(shuffleList[index]).getSongId();
        } catch (NullPointerException e) {
            stopSelf();
        }

        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaFile != null && mediaFile != "")
            initMediaPlayer();
            initMediaSession();
            try {
                t.start();
            }catch (IllegalThreadStateException e){
                e.printStackTrace();
            }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopPlaying();
            mediaPlayer.release();
        }
        removeAudioFocus();
        try {
            unregisterReceiver(receiver);
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            //this thread updates playbackState so that seekbar in notification works.
            if(mediaPlayer != null) {
                state = new PlaybackStateCompat.Builder()
                        .setActions(
                                PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                        .setState(pStatus, mediaPlayer.getCurrentPosition(), 1, SystemClock.elapsedRealtime())
                        .build();
                mSession.setPlaybackState(state);
                mHandler.postDelayed(this, 1000);
            }
        }
    });


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
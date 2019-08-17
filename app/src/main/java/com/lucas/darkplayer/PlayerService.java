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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

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
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private String mediaFile;
    private AudioManager audioManager;
    private int resumePosition;
    private ArrayList<SongData> audioList;
    int seekTo=0;
    int index = 0;
    Boolean seek=false;
    Intent localintent = new Intent("seekto");
    private Handler mHandler = new Handler();
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updateIndex=intent.getBooleanExtra("updateIndex",false);
            if(updateIndex){
                index = intent.getIntExtra("index",0);
                reset();
                mediaFile=audioList.get(index).getSongId();
                initMediaPlayer();
            }

        }
    };
    private void initMediaPlayer() {
        /*
         * Initialises MediaPlayer and calls preparedAsync
         */
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
            }else {
                stopSelf();
            }
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        try {
            mediaPlayer.prepareAsync();
        }catch(Exception e){
            stopSelf();
        }
    }
    private void startPlaying() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            localintent.putExtra("index", index);
            sendBroadcast(localintent);
            if(seek){
                seek=false;
                mediaPlayer.seekTo(seekTo);
            }
            try{
                t.start();
            }catch(IllegalThreadStateException e){
                e.printStackTrace();
            }
            mediaPlayer.setVolume(1.0f, 1.0f);
        }
    }
    public void stopPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }
    public void pausePlayer() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
            localintent.putExtra("IsPlaying", mediaPlayer.isPlaying());
            sendBroadcast(localintent);

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
            try{
                t.start();
            }catch(IllegalThreadStateException e){
                e.printStackTrace();
            }
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

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //send intent over to SongFragment to tell it that song has completed
        localintent.putExtra("Completed", true);
        sendBroadcast(localintent);

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
                Log.d("MediaPlayer Error", "ERROR: NOT SEEKABLE" + u);
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

    public void next() {
        if(index<audioList.size()-1) {
            index++;
            mediaFile = audioList.get(index).getSongId();
            reset();
            initMediaPlayer();
        }
    }

    public void prev() {
        if(index>=0) {
            index--;
            mediaFile = audioList.get(index).getSongId();
            reset();
            initMediaPlayer();
        }
    }

    public void reset(){
        stopPlaying();
        mediaPlayer.reset();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //getting intents from SongFragment to tell PlayerService what to do.
        Bundle bundle = intent.getBundleExtra("bundle");
        ArrayList<SongData> testList = (ArrayList<SongData>) bundle.getSerializable("audioList");
        boolean updateIndex = intent.getBooleanExtra("updateIndex",false);
        if(testList != null){
            audioList=testList;
        }
        if(updateIndex){
            index = intent.getIntExtra("index", 0);
        }
        seek = intent.getBooleanExtra("seek", false);
        seekTo = intent.getIntExtra("seekTo", 0);
        IntentFilter filter = new IntentFilter("player");
        registerReceiver(receiver, filter);

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
            mediaFile = audioList.get(index).getSongId();
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
        unregisterReceiver(receiver);
    }

    //LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(PlayerService.this);
    Thread t = new Thread(new Runnable() {
        /* this thread serves double purpose
         * to update the seekbar and to let the fragment know
         * that the music is playing and to update playback status.
         * This is to prevent the playback status from becoming inconsistent
         * when the Fragment is eventually killed due to TabAdapter.
         */
        public void run() {
            if(mediaPlayer != null && mediaPlayer.isPlaying()) {
                localintent.putExtra("Duration", mediaPlayer.getDuration());
                localintent.putExtra("Current", mediaPlayer.getCurrentPosition());
                localintent.putExtra("IsPlaying", mediaPlayer.isPlaying());
                localintent.putExtra("Completed", false);
                sendBroadcast(localintent);
            }
            mHandler.postDelayed(this, 100);
        }
    });


    public class LocalBinder extends Binder {
        public PlayerService getService() {
            return PlayerService.this;
        }
    }
}
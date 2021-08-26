package com.luxlunaris.noadpadlight.ui;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.luxlunaris.noadpadlight.R;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AudioPlayerFragment extends DialogFragment {


    /**
     * Plays/pauses the recording.
     */
    Button playButton;

    ProgressBar progressBar;

    TextView audioDurationText;


    /**
     * Audio file to be played back.
     */
    File audioFile;

    /**
     * Media player object.
     */
    MediaPlayer player;

    /**
     * Internal state.
     */
    final int STATE_IDLE = -1;
    final int  STATE_PLAYING = 3;
    final int  STATE_PLAYING_PAUSED = 5;
    private int state = STATE_IDLE;


    Timer progressBarTimer;

    @Override
    public void onResume() {
        super.onResume();

        if(state==STATE_PLAYING){
            playButton.setText(R.string.pause);
        }

        //if(player!=null){
        //    progressBar.setProgress(player.getCurrentPosition());
       // }

    }

    public AudioPlayerFragment() {
        // Required empty public constructor
    }

    public static AudioPlayerFragment newInstance() {
        AudioPlayerFragment fragment = new AudioPlayerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_player, container, false);
        playButton = view.findViewById(R.id.play);
        progressBar = view.findViewById(R.id.audioPlaybackProgressBar);
        audioDurationText = view.findViewById(R.id.audioDurationText);
        playButton.setOnClickListener(new PlayHandler());
        playButton.setBackgroundColor(Color.WHITE);
        playButton.setTextColor(Color.BLACK);
        return view;
    }

    /**
     * Set the audio file from which to play the soundtrack.
     * @param audioFile
     */
    public void setAudioPlaybackFile(File audioFile){

        if(!audioFile.equals(this.audioFile)){
            state = STATE_IDLE;
        }

        this.audioFile = audioFile;
    }

    /**
     * Sets the play button's text back to "play" when audio file is over.
     */
    class PlayingDoneHandler implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            state = STATE_IDLE;
            playButton.setText(R.string.play);
        }
    }

    /**
     * Handles the clicking of the play button.
     */
    class PlayHandler implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            switch (state){

                case STATE_PLAYING:
                    pausePlayer();
                    playButton.setText(R.string.play);
                    //((Button)v).setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
                    break;
                case STATE_PLAYING_PAUSED:
                    resumePlayer();
                    playButton.setText(R.string.pause);
                    //((Button)v).setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_pause, 0, 0, 0);
                    break;
                default:
                    startPlayer();
                    playButton.setText(R.string.pause);
                    break;
            }
        }
    }


    /**
     * Starts playing.
     */
    private void startPlayer(){

        if(state!=STATE_IDLE){
            return;
        }

        if(player!=null){
            player.release();
        }


        player = new MediaPlayer();
        player.setOnCompletionListener(new PlayingDoneHandler());


        try{
            player.setDataSource(audioFile.getPath());
            player.prepare();
            player.start();

        }catch (IOException e){
            e.printStackTrace();
        }

        Toast.makeText(getContext(), ((float)player.getDuration()/1000)+" " , Toast.LENGTH_LONG).show();

        int duration  =player.getDuration();
        progressBar.setMax(duration);
        audioDurationText.setText(((float)duration/1000)+"");


        progressBarTimer = new Timer();
        progressBarTimer.schedule(new ProgressBarUpdater(),0 ,100 );


        state = STATE_PLAYING;

    }

    /**
     * Pauses the player.
     */
    private void pausePlayer(){
        player.pause();
        state = STATE_PLAYING_PAUSED;
    }

    /**
     * Resumes the player.
     */
    private void resumePlayer(){
        player.start();
        state = STATE_PLAYING;
    }

    /**
     * Stops the player. (For good).
     */
    private void stopPlayer(){
        if(player!=null){
            //player.stop();
            player.release();
        }
        player = null;

        state = STATE_IDLE;
    }


    /**
     * Updates the state of the playback-progress bar.
     */
    class ProgressBarUpdater extends TimerTask{
        @Override
        public void run() {

            if(player!=null && state == STATE_PLAYING){
                progressBar.setProgress(player.getCurrentPosition());
            }

        }
    }

















}
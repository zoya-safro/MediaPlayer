package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.res.AssetFileDescriptor;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.BreakIterator;

public class MainActivity extends AppCompatActivity implements Runnable {


    private MediaPlayer mediaPlayer = new MediaPlayer();
    private SeekBar seekBar;
    private boolean wasPlaying = false;
    private FloatingActionButton fabPlayPause;
    private TextView seekBarHint;
    private Object currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        fabPlayPause = findViewById(R.id.fabPlayPause);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);

        fabPlayPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.fabPlayPause:
                        playSong();
                        break;
                    case R.id.fabBack:
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                        break;
                    case R.id.fabRepeat:

                        boolean isRepeat = false;
                        ImageSwitcher fabRepeat = null;
                        if (!isRepeat && mediaPlayer !=null) {
                            mediaPlayer.setLooping(true);
                            fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                            isRepeat = true;
                        }else if (isRepeat && mediaPlayer != null) {
                            mediaPlayer.setLooping(false);
                            fabRepeat.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                            isRepeat = false;
                        }
                        break;
                }
            }
        });
       
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility((View.VISIBLE));
                int timeTrack = (int) Math.ceil(progress/1000f);
                if (timeTrack < 10) {
                    seekBarHint.setText("00:0" + timeTrack);
                } else if (timeTrack < 60){
                    seekBarHint.setText("00: " + timeTrack);
                }else if (timeTrack >= 60) {
                    seekBarHint.setText("01: " + (timeTrack - 60));
                }
                double percentTrack = progress / (double) seekBar.getMax();
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth()*percentTrack*0.92));
                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    clearMediaPlayer();
                   fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    MainActivity.this.seekBar.setProgress(0);
                }
            }



            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBarHint.setVisibility(View.VISIBLE);
            }
            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });

    }
    public void playSong () {
        mediaPlayer.setVolume(0.5f, 0.5f);
        try {
            Thread updateSeekBar = null;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                wasPlaying = true;
                mediaPlayer.pause();
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                updateSeekBar.interrupt(); // остановка потока
            } else {
                if (!wasPlaying) {
                    if (mediaPlayer == null) {
                        mediaPlayer = new MediaPlayer();
                    }
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                    AssetFileDescriptor descriptor = getAssets().openFd("А.С. Пушкин - Няне.mp3");
                    mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                    // запись метаданных в окно вывода информации metaDataAudio
                    MediaMetadataRetriever mediaMetadata = new MediaMetadataRetriever(); // осздание объекта специального класса для считывания метаданных
                    mediaMetadata.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength()); // считывание метаданных по имеющемуся дескриптору

                    mediaMetadata.release(); // закрытие объекта загрузки метаданных


                    descriptor.close();
                    mediaPlayer.prepare();
                    mediaPlayer.setLooping(false);
                    seekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                    updateSeekBar = new Thread(this); // запуск потока
                    updateSeekBar.start();
                } else { // если была остановлена музыка, продолжаем с этой позиции
                    mediaPlayer.start();
                    updateSeekBar = new Thread(this); // запуск потока
                    updateSeekBar.start();
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        // Музыка играет, меняем картинку на иконку "пауза"
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                    } else {
                        // Музыка не играет, меняем картинку на иконку "воспроизведение"
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                    }
                }
                wasPlaying = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }









    private void clearMediaPlayer() {
    }

    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition();
        int total = mediaPlayer.getDuration();
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {
                Thread.sleep(1000);
                currentPosition = mediaPlayer.getCurrentPosition();
    }catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }catch (Exception e) {
                return;
            }
    seekBar.setProgress(currentPosition);
        }
    }
}




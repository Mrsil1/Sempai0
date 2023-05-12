package com.example.myplayerapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Runnable {

    /**
     * Проигрыватель
     */
    private MediaPlayer mp;

    /**
     * Кнопки управления проигрывателем
     */
    private Button buttonPlay;
    private Button buttonPause;
    private Button buttonStop;
    private Button buttonNext;

    private SeekBar seekBar;

    /**
     * Номер песни
     */
    private int num = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == 24) {
            mp.setVolume(1.0f,1.0f);
        } else if (keyCode == 25) {
            mp.setVolume(0.5f,0.5f);
        }

        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] masTitleMusic = getResources().getStringArray(R.array.music_array);

        seekBar = findViewById(R.id.seekBar);

        // Кнопка паузы
        buttonPause = findViewById(R.id.buttonPause);
        buttonPause.setEnabled(false);
        buttonPause.setOnClickListener(listener -> {
            mp.pause();

            buttonPlay.setEnabled(true);
            buttonPause.setEnabled(false);
            buttonNext.setEnabled(true);
        });

        // Кнопка остановки проигрывателя
        buttonStop = findViewById(R.id.buttonStop);
        buttonStop.setEnabled(false);
        buttonStop.setOnClickListener(listener -> {
            mp.stop();

            buttonPlay.setEnabled(true);
            buttonPause.setEnabled(false);
            buttonStop.setEnabled(false);
        });

        // Кнопка запуска проигрывателя
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(listener -> {
            if (mp != null && mp.isPlaying()) mp.release();

/*            mp = MediaPlayer.create(getApplicationContext(), R.raw.omori);
            mp.start();*/

            try {
                mp = new MediaPlayer();

                mp.setVolume(0.75f,0.75f);

                AssetFileDescriptor descriptor = getAssets().openFd(masTitleMusic[num]);
                mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close(); // закрытие дескриптора

                mp.prepare();
                mp.start();

                seekBar.setProgress(0);
                seekBar.setMax(mp.getDuration());

                new Thread(this).start();

            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage());
            }

            buttonPlay.setEnabled(false);
            buttonPause.setEnabled(true);
            buttonStop.setEnabled(true);
            buttonNext.setEnabled(true);
        });

        // Кнопка остановки проигрывателя
        buttonNext = findViewById(R.id.buttonNext);
        buttonNext.setEnabled(false);
        buttonNext.setOnClickListener(listener -> {

            //Увеличиваем номер песни
            num++;

            if (mp != null && mp.isPlaying()) mp.release();

            try {
                mp = new MediaPlayer();

                AssetFileDescriptor descriptor = getAssets().openFd(masTitleMusic[num]);
                mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                descriptor.close(); // закрытие дескриптора

                mp.prepare();
                mp.start();

                seekBar.setProgress(0);
                seekBar.setMax(mp.getDuration());

            } catch (IOException e) {
                Log.e("MainActivity", e.getMessage());
            }

            buttonPlay.setEnabled(false);
            buttonPause.setEnabled(true);
            buttonStop.setEnabled(true);
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.release();
    }

    @Override
    public void run() {
        int currentPosition = mp.getCurrentPosition();
        int total = mp.getDuration();

        while (mp != null && mp.isPlaying() && currentPosition < total) {

            try {
                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mp.getCurrentPosition();

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            seekBar.setProgress(currentPosition);
        }
    }
}
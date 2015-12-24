package com.kszr.midiplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import midi.MidiFile;
import midi.MidiTrack;
import midi.event.MidiEvent;
import midi.event.ProgramChange;

/**
 * The main activity for the app.
 */
public class MainActivity extends AppCompatActivity {
    private static final int OPEN_FILE_REQUEST_CODE = 1;
    private static final int CHANGE_PROGRAM_REQUEST_CODE = 2;

    private MediaPlayer mediaPlayer = null;
    private MidiFile midiFile = null;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions();
        setUpButtonListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OPEN_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            String filepath = data.getData().getPath();
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(data.getData()));
            Log.i("Filetype", extension);
            try {
                if (!extension.equals("mid"))
                    throw new Exception("Not a MIDI file!");
                midiFile = new MidiFile(getContentResolver().openInputStream(data.getData()));
                prepareMediaPlayer();
                Log.i("MainActivity", "Opened " + filepath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(requestCode == CHANGE_PROGRAM_REQUEST_CODE && resultCode == RESULT_OK) {
            String instrument = data.getStringExtra("instrument");
            int program = data.getIntExtra("program", -1);
            if(instrument != null && program > 0) {
                try {
                    changeProgram(program);
                    Log.i("MainActivity", "Instrument: " + instrument + ", Program: " + program);
                } catch(Exception e) {
                    Log.i("MainActivity", "Error while trying to change program. Program not changed.");
                }
            } else {
                Log.i("MainActivity", "Program not changed");
            }
        }
    }

    /**
     * Set up listeners for all the buttons.
     */
    private void setUpButtonListeners() {
        setUpFileOpenListener();
        setUpBackToStartListener();
        setUpPlayListener();
        setUpPauseListener();
        setUpStopListener();
        setUpForwardToEndListener();
        setUpChangeProgramListener();
    }

    /**
     * Sets up a listener for the "Open" button.
     */
    private void setUpFileOpenListener() {
        Button button = (Button) findViewById(R.id.button_open);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, OPEN_FILE_REQUEST_CODE);
            }
        });
    }

    /**
     * Sets up a listener for the "Skip to Start" button.
     */
    private void setUpBackToStartListener() {
        ImageButton button = (ImageButton) findViewById(R.id.button_to_start);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (mediaPlayer == null)
                        throw new Exception("No file loaded!");
                    mediaPlayer.seekTo(0);
                } catch (Exception e) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /**
     * Sets up a listener for the "Play" button.
     */
    private void setUpPlayListener() {
        ImageButton button = (ImageButton) findViewById(R.id.button_play);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    play();
                } catch (Exception e) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /**
     * Sets up a listener for the "Pause" button.
     */
    private void setUpPauseListener() {
        ImageButton button = (ImageButton) findViewById(R.id.button_pause);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    pause();
                } catch (Exception e) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /**
     * Sets up a listener for the "Stop" button.
     */
    private void setUpStopListener() {
        ImageButton button = (ImageButton) findViewById(R.id.button_stop);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    stop();
                } catch (Exception e) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /**
     * Sets up a listener for the "Skip to End" button.
     */
    private void setUpForwardToEndListener() {
        ImageButton button = (ImageButton) findViewById(R.id.button_to_end);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (mediaPlayer == null)
                        throw new Exception("No file loaded!");
                    mediaPlayer.seekTo(mediaPlayer.getDuration());
                } catch (Exception e) {
                    Snackbar.make(v, e.toString(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    /**
     * Sets up a listener for the "Change Program" button.
     */
    private void setUpChangeProgramListener() {
        Button button = (Button) findViewById(R.id.button_program);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProgramChangeActivity.class);
                startActivityForResult(intent, CHANGE_PROGRAM_REQUEST_CODE);
            }
        });
    }

    /**
     * A helper method that starts the media player if it is not running
     * and pauses otherwise. Essentially the same as pause().
     * @throws Exception
     */
    private void play() throws Exception {
        if (mediaPlayer == null)
            throw new Exception("No file loaded!");
        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();
        else mediaPlayer.pause();
    }

    /**
     * A helper method that pauses the media player if it is running
     * and starts it otherwise. Essentially the same as play().
     * @throws Exception
     */
    private void pause() throws Exception {
        if (mediaPlayer == null)
            throw new Exception("No file loaded!");
        if(mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else mediaPlayer.start();
    }

    /**
     * A helper method that stops playback. It accomplishes this by pausing the
     * audio at the start of the track or whatever.
     * @throws Exception
     */
    private void stop() throws Exception {
        if (mediaPlayer == null)
            throw new Exception("No file loaded!");
        mediaPlayer.seekTo(0);
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }

    /**
     * Changes the instrument of all tracks to that specified by program.
     * @param program The program to change to.
     */
    private void changeProgram(int program) throws Exception {
        if(mediaPlayer == null)
            throw new Exception("No file loaded!");
        boolean wasPlaying = mediaPlayer.isPlaying();
        if(wasPlaying)
            pause();
        int currentPosition = mediaPlayer.getCurrentPosition();
        ArrayList<MidiTrack> tracks = midiFile.getTracks();
        ArrayList<MidiTrack> newTracks = new ArrayList<>();
        for(MidiTrack track : tracks) {
            MidiTrack tempTrack = new MidiTrack();
            tempTrack.insertEvent(new ProgramChange(0, 0, program));
            TreeSet<MidiEvent> eventSet = track.getEvents();
            Iterator iterator = eventSet.iterator();
            while(iterator.hasNext()) {
                MidiEvent event = (MidiEvent) iterator.next();
                if(event.getClass() == ProgramChange.class)
                    continue;
                tempTrack.insertEvent(event);
            }
            newTracks.add(tempTrack);
            System.err.println(tempTrack.getEvents().first().toString());
        }
        midiFile = new MidiFile(midiFile.getResolution(), newTracks);
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        prepareMediaPlayer();
        mediaPlayer.seekTo(currentPosition);
        if(wasPlaying)
            play();
    }

    /**
     * Loads music from midiFile into mediaPlayer.
     * @throws Exception
     */
    private void prepareMediaPlayer() throws Exception {
        File tempFile = File.createTempFile(midiFile.toString(), ".mid");
        midiFile.writeToFile(tempFile);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(new FileInputStream(tempFile).getFD());
        mediaPlayer.prepare();
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * Adapted from: http://stackoverflow.com/a/33292700/1843968
     */
    private void verifyStoragePermissions() {
        if (!permissionsGranted()) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    /**
     * Checks whether the necessary permissions have been granted.
     * @return true if permissions have been granted.
     */
    private boolean permissionsGranted() {
        int permission1 = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permission2 = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

}

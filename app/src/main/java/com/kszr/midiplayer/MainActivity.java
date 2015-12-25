package com.kszr.midiplayer;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import midi.MidiFile;
import midi.MidiTrack;
import midi.event.MidiEvent;
import midi.event.ProgramChange;
import midi.event.meta.EndOfTrack;

/**
 * The main activity for the app.
 */
public class MainActivity extends AppCompatActivity {
    private static final int OPEN_FILE_REQUEST_CODE = 1;
    private static final int CHANGE_PROGRAM_REQUEST_CODE = 2;

    private MediaPlayer mediaPlayer = null;
    private MidiFile midiFile = null;

    private Handler handler;
    private Runnable rUpdate;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions();
        setUpButtonListeners();
        setUpHandler();
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
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(data.getData()));
            if(extension == null) {
                Log.i("MainActivity", "File has no extension or invalid extension");
                Toast.makeText(MainActivity.this, "Not a valid file", Toast.LENGTH_LONG).show();
                return;
            } else {
                Log.i("MainActivity", "File extension: " + extension);
            }
            try {
                if (!extension.equals("mid"))
                    throw new Exception("Not a MIDI file!");
                openMidiFileAsync(data.getData());
                Toast.makeText(MainActivity.this, "Opened file", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                if (e.getMessage().equals("Not a MIDI file!"))
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                else Toast.makeText(MainActivity.this, "Error opening file", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else if(requestCode == CHANGE_PROGRAM_REQUEST_CODE && resultCode == RESULT_OK) {
            String instrument = data.getStringExtra("instrument");
            int program = data.getIntExtra("program", -1);
            if(instrument != null && program > 0) {
                try {
                    changeProgram(program);
                    Toast.makeText(MainActivity.this, "Changed instrument to: " + instrument, Toast.LENGTH_LONG).show();
                    Log.i("MainActivity", "Instrument: " + instrument + ", Program: " + program);
                } catch(Exception e) {
                    if(e.getMessage().equals("No file loaded!"))
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    else Toast.makeText(MainActivity.this, "Error changing program", Toast.LENGTH_LONG).show();
                    Log.i("MainActivity", "Error while trying to change program.");
                }
            } else {
                Log.i("MainActivity", "Program not changed");
            }
        }
    }

    /**
     * Opens the MIDI file asynchronously and prepares the media player.
     * @param uri
     */
    private void openMidiFileAsync(final Uri uri) {
        new AsyncTask<Void, Void, Integer>() {
            private ProgressDialog dialog;

            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Opening file...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    midiFile = new MidiFile(getContentResolver().openInputStream(uri));
                    resetMediaPlayer();
                    prepareMediaPlayer();
                    Log.i("MainActivity", "Opened " + uri.getPath());
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }

            protected void onPostExecute(Integer result) {
                if(dialog != null)
                    dialog.dismiss();

                super.onPostExecute(result);
            }
        }.execute();
    }

    /**
     * Changes the instrument of all tracks to that specified by program.
     * @param program The program to change to.
     */
    private void changeProgram(int program) throws Exception {
        if(mediaPlayer == null)
            throw new Exception("No file loaded!");

        changeProgramAsync(program);
    }

    /**
     * Changes the program asynchronously.
     * @param program
     */
    private void changeProgramAsync(final int program) {
        new AsyncTask<Object, Object, Integer>() {
            private ProgressDialog dialog;

            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Changing program...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Integer doInBackground(Object... params) {
                try {
                    boolean wasPlaying = mediaPlayer.isPlaying();
                    if (wasPlaying)
                        mediaPlayer.pause();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    ArrayList<MidiTrack> tracks = midiFile.getTracks();
                    for (MidiTrack track : tracks) {
                        TreeSet<MidiEvent> eventSet = track.getEvents();
                        MidiEvent putativeEOT = eventSet.last();
                        if (putativeEOT.getClass().equals(EndOfTrack.class)) {
                            track.removeEvent(eventSet.last());
                            eventSet = track.getEvents();
                        }
                        List<MidiEvent> eventsToRemove = new ArrayList<>();
                        for (MidiEvent event : eventSet)
                            if (event.getClass().equals(ProgramChange.class))
                                eventsToRemove.add(event);
                        for (MidiEvent event : eventsToRemove)
                            track.removeEvent(event);
                        track.insertEvent(new ProgramChange(0, 0, program));
                        track.closeTrack();
                    }
                    midiFile = new MidiFile(midiFile.getResolution(), tracks);
                    resetMediaPlayer();
                    prepareMediaPlayer();
                    mediaPlayer.seekTo(currentPosition);
                    if (wasPlaying)
                        mediaPlayer.start();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }

            protected void onPostExecute(Integer result) {
                if(dialog != null)
                    dialog.dismiss();

                super.onPostExecute(result);
            }

        }.execute();
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
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
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
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
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
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
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
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
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
                    Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG)
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
        if(!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
        }
        else {
            mediaPlayer.pause();
            Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A helper method that pauses the media player if it is running
     * and starts it otherwise. Essentially the same as play().
     * @throws Exception
     */
    private void pause() throws Exception {
        if (mediaPlayer == null)
            throw new Exception("No file loaded!");
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(MainActivity.this, "Paused", Toast.LENGTH_SHORT).show();
        }
        else {
            mediaPlayer.start();
            Toast.makeText(MainActivity.this, "Playing", Toast.LENGTH_SHORT).show();
        }
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
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Toast.makeText(MainActivity.this, "Stop", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Resets the media player.
     */
    private void resetMediaPlayer() {
        if(mediaPlayer == null)
            return;
        mediaPlayer.stop();
        mediaPlayer.reset();
        mediaPlayer.release();
        mediaPlayer = null;
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

    private void setUpHandler() {
        handler = new Handler();
        rUpdate = new Runnable() {

            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.playback_time);
                textView.setText(getFormattedPlayBackTime());
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(rUpdate, 100);
    }

    /**
     * Sets the current playback time in the field for playback time.
     */
    private String getFormattedPlayBackTime() {
        if(mediaPlayer == null) {
            return "0:00/0:00";
        }  else try {
            String duration = millisToString(mediaPlayer.getDuration());
            String currentPosition = millisToString(mediaPlayer.getCurrentPosition());
            return currentPosition + "/" + duration;
        } catch(IllegalStateException e) {
            return "0:00/0:00";
        }
    }

    /**
     * Formats time in milliseconds to HH:MM:SS or MM:SS.
     * @param millis Time in milliseconds
     * @return Formatted time
     */
    private String millisToString(int millis) {
        if(millis < 0)
            throw new IllegalArgumentException("Millis cannot be negative!");
        long hour = TimeUnit.MILLISECONDS.toHours(millis);
        long minute = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(hour);
        long second = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.HOURS.toSeconds(hour) - TimeUnit.MINUTES.toSeconds(minute);
        String s;
        if(hour == 0 && minute == 0)
            s = String.format("0:%02d", second);
        else if(hour == 0)
            s = String.format("%d:%02d", minute, second);
        else s = String.format("%d:%02d:%02d", hour, minute, second);
        return s;
    }

    /**
     * Checks whether the necessary permissions have been granted.
     * @return true if permissions have been granted.
     */
    private boolean permissionsGranted() {
        for(String permission : PERMISSIONS_STORAGE)
            if(ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

}

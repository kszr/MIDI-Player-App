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

import com.kszr.midiplayer.util.Time;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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

    //Pseudo locks
    private boolean playerIsPrepared = false;
    private boolean programIsChanging = false;

    // App permissions
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
        verifyPermissions();
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
                    else Toast.makeText(MainActivity.this, "Error changing instrument", Toast.LENGTH_LONG).show();
                    Log.i("MainActivity", "Error while trying to change program.");
                }
            } else {
                Log.i("MainActivity", "Program not changed");
            }
        }
    }

    /**
     * Opens the MIDI file asynchronously and prepares the media player.
     * @param uri The Uri of the file that is to be loaded.
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
     * Changes the instrument of all tracks to that specified by program. "Program" is part of
     * MIDI vocabulary. The word "instrument" will be used exclusively in the front end.
     * @param program The program to change to.
     */
    private void changeProgram(int program) throws Exception {
        if(mediaPlayer == null)
            throw new Exception("No file loaded!");

        changeProgramAsync(program);
    }

    /**
     * Changes the program asynchronously.
     * @param program The program number to change to.
     */
    private void changeProgramAsync(final int program) {
        new AsyncTask<Object, Object, Integer>() {
            private ProgressDialog dialog;

            protected void onPreExecute() {
                super.onPreExecute();
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("Changing instrument...");
                dialog.setCancelable(false);
                dialog.setIndeterminate(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.show();
            }

            @Override
            protected Integer doInBackground(Object... params) {
                try {
                    programIsChanging = true;
                    boolean wasPlaying = mediaPlayer.isPlaying();
                    if (wasPlaying)
                        mediaPlayer.pause();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    ArrayList<MidiTrack> tracks = midiFile.getTracks();
                    for (MidiTrack track : tracks) {
                        TreeSet<MidiEvent> eventSet = track.getEvents();
                        MidiEvent putativeEOT = eventSet.last();

                         // Need to remove EndOfTrack event for now, because a track is not
                         // mutable otherwise.
                        if (putativeEOT.getClass().equals(EndOfTrack.class)) {
                            track.removeEvent(eventSet.last());
                            eventSet = track.getEvents();
                        }
                        List<MidiEvent> eventsToRemove = new ArrayList<>();

                         // Need to remove any ProgramChange events that might conflict
                         // with the new ProgramChange.
                        for (MidiEvent event : eventSet)
                            if (event.getClass().equals(ProgramChange.class))
                                eventsToRemove.add(event);
                        for (MidiEvent event : eventsToRemove)
                            track.removeEvent(event);
                        track.insertEvent(new ProgramChange(0, 0, program));

                        //Adding EndOfTrack.
                        track.closeTrack();
                    }
                    midiFile = new MidiFile(midiFile.getResolution(), tracks);
                    resetMediaPlayer();
                    prepareMediaPlayer();
                    mediaPlayer.seekTo(currentPosition);
                    if (wasPlaying)
                        mediaPlayer.start();
                    programIsChanging = false;
                } catch(Exception e) {
                    programIsChanging = false;
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
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
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
        playerIsPrepared = false;
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
        playerIsPrepared = true;
    }

    /**
     * Sets up the Handler responsible for handling view updates with regard to
     * displaying playback time.
     */
    private void setUpHandler() {
        handler = new Handler();
        Runnable rUpdate = new Runnable() {

            @Override
            public void run() {
                TextView textView = (TextView) findViewById(R.id.playback_time);
                String formattedTime = getFormattedPlayBackTime();
                if (formattedTime != null)
                    textView.setText(formattedTime);
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(rUpdate, 100);
    }

    /**
     * Sets the current playback time in the field for playback time.
     */
    private String getFormattedPlayBackTime() {
        if(!playerIsPrepared && !programIsChanging) {
            return "0:00/0:00";
        } else if(programIsChanging) {
            return null;
        } else {
            String duration = Time.millisToString(mediaPlayer.getDuration());
            String currentPosition = Time.millisToString(Math.min(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration()));
            return currentPosition + "/" + duration;
        }
    }

    /**
     * Checks if the app has the necessary permissions to operate. The user will be prompted
     * to grant these permissions otherwise.
     *
     * Adapted from: http://stackoverflow.com/a/33292700/1843968
     */
    private void verifyPermissions() {
        if (!allPermissionsGranted()) {
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
    private boolean allPermissionsGranted() {
        for(String permission : PERMISSIONS_STORAGE)
            if(!permissionGranted(permission))
                return false;
        return true;
    }

    /**
     * Checks whether a given permission has been granted.
     * @param permission The permission that needs to be verified.
     * @return True if permission has been granted.
     */
    private boolean permissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

}

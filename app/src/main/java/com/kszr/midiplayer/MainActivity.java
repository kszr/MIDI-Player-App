package com.kszr.midiplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageButton;

import midi.MidiFile;

/**
 * The main activity for the app.
 */
public class MainActivity extends AppCompatActivity {
    private static final int PICKFILE_RESULT_CODE = 1;

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
        if(requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            String filepath = data.getData().getPath();
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(getContentResolver().getType(data.getData()));
            Log.i("Filetype", extension);
            try {
                if (!extension.equals("mid"))
                    throw new Exception("Not a MIDI file!");
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getApplicationContext(), data.getData());
                mediaPlayer.prepare();
                Log.i("INFO", "Opened " + filepath);
            } catch (Exception e) {
                e.printStackTrace();
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
                startActivityForResult(intent, PICKFILE_RESULT_CODE);
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
                    Snackbar.make(v, "Back to start", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    Snackbar.make(v, "Playing", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
                    if (mediaPlayer == null)
                        throw new Exception("No file loaded!");
                    mediaPlayer.start();
                    Snackbar.make(v, "Playing", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
                    if (mediaPlayer == null)
                        throw new Exception("No file loaded!");
                    mediaPlayer.pause();
                    Snackbar.make(v, "Paused", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
                    if (mediaPlayer == null)
                        throw new Exception("No file loaded!");
                    mediaPlayer.seekTo(0);
                    if (mediaPlayer.isPlaying())
                        mediaPlayer.pause();
                    Snackbar.make(v, "Stop", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
                    Snackbar.make(v, "Reached end", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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
//                Snackbar.make(v, "Changed program", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent intent = new Intent(MainActivity.this, ProgramChangeActivity.class);
                startActivity(intent);
            }
        });
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

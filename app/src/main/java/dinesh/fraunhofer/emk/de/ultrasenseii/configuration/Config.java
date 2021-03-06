package dinesh.fraunhofer.emk.de.ultrasenseii.configuration;

import android.Manifest;
import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by dinesh on 03.11.16.
 */

public class Config {

    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "UltraSenseIIController";

    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_AUDIO_RECORDING = 2;
    public static final int REQUEST_ENABLE_BT = 3;

    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static String[] PERMISSIONS_AUDIO= {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    public static final String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + File.separator + "UltraSenseII" + File.separator;

    public static final String BROADCAST_COUNTDOWN_TIMER = "broadcastimerforcountdownstart";
    public static final String TIMER = "remainingtime";

    //Broadcast receiver
    public static final String BROADCAST_ACTIVITY = "StartBroadcast";
    public static final int NOT_REMOTE = 1;
    public static final int REMOTE = 2;

    public static final String BLUETOOTH_MSG = "bluetooth%message";
    public static final String BROADCAST_RECEIVE_COMMAND = "remote%command";

    //Remote Commands
    public static final String START_RECORDING = "Start recording";
    public static final String STOP_RECORDING = "Stop recording";
    public static final String GET_TIMESTAMPS = "Pin timestamp";
}

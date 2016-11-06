package dinesh.fraunhofer.emk.de.ultrasenseii.configuration;

import android.Manifest;
import android.os.Environment;

import java.io.File;

/**
 * Created by dinesh on 03.11.16.
 */

public class Config {

    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    public static final int REQUEST_AUDIO_RECORDING = 2;

    public static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static String[] PERMISSIONS_AUDIO= {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    public static final String fileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .getAbsolutePath() + File.separator + "UltraSenseII Timestamps" + File.separator;

    public static final String BROADCAST_COUNTDOWN_TIMER = "broadcastimerforcountdownstart";
    public static final String TIMER = "remainingtime";
}

package ultrasensecontroller.de.emk.fraunhofer.dinesh.ultrasenseiicontroller;

import java.util.UUID;

/**
 * Created by dinesh on 14.12.16.
 */

public class Constants {

    public static final String DOTS = "............................... \n";
    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final String NAME = "UltraSenseIIController";

    //Remote Commands
    public static final String START_RECORDING = "Start recording";
    public static final String STOP_RECORDING = "Stop recording";
    public static final String GET_TIMESTAMPS = "Pin timestamp";

    //Broadcast receivers
    public static final String BROADCAST_RECEIVE_PROGRESS = "broadcast%progress";
    public static final String BLUETOOTH_MSG = "bluetooth%message";
}

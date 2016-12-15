package ultrasensecontroller.de.emk.fraunhofer.dinesh.ultrasenseiicontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by dinesh on 14.12.16.
 */

public class AudioController {

    public static final String TAG = AudioController.class.getSimpleName();

    BluetoothAdapter bluetoothAdapter = null;
    Context context;
    String terminaltext = "";
    TextView terminal;
    String remoteCommand = "";
    BluetoothSocket socket = null;

    public boolean makeRemoteCall(BluetoothAdapter mBluetoothAdapter, TextView terminal,
                                        String terminalText,String remoteCommand,Context ctx) throws IOException {
        this.bluetoothAdapter = mBluetoothAdapter;
        this.terminaltext = terminalText;
        this.terminal = terminal;
        this.remoteCommand = remoteCommand;
        this.context = ctx;

        try {
            new Thread(new BluetoothThread()).start();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private class BluetoothThread extends Thread{

        private final BluetoothServerSocket mmServerSocket;

        public BluetoothThread()  {

            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.NAME, Constants.MY_UUID);
                terminal.append("Started connecting to Ultrasense for starting recording "+Constants.DOTS);
                terminal.setMovementMethod(new ScrollingMovementMethod());
            } catch (IOException e) {
                terminaltext = terminaltext + "Failed to start server "+Constants.DOTS;
                terminal.setText(terminaltext);
                terminal.setMovementMethod(new ScrollingMovementMethod());
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Server running");
            Intent intent = new Intent(Constants.BROADCAST_RECEIVE_PROGRESS);
            try {
                if(socket==null) {
                    Log.d(TAG,"Waiting on accept");
                    socket = mmServerSocket.accept();
                }

            } catch (IOException e) {
                Log.d(TAG,"Failed to accept");
                e.printStackTrace();
            }

            try {
                intent.putExtra(Constants.BLUETOOTH_MSG, "Waiting...");
                context.sendBroadcast(intent);
                //Log.d(TAG,"Waiting...");
                if (socket != null) {
                    String remoteDeviceAddress = "Remote device address: " +
                            socket.getRemoteDevice().getAddress();
                    //For terminal
                    intent.putExtra(Constants.BLUETOOTH_MSG, remoteDeviceAddress);
                    context.sendBroadcast(intent);
                    Log.d(TAG,remoteDeviceAddress);

                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    out.println(remoteCommand);

                    //Command sent to UltrasenseII
                    intent.putExtra(Constants.BLUETOOTH_MSG, remoteCommand);
                    context.sendBroadcast(intent);
                    out.flush();
                } else {
                    Log.d(TAG, "socket is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    //socket.close();
                } catch (Exception e) {
                    Log.d(TAG,"Unable to close socket" + e.getMessage() + Constants.DOTS);
                    e.printStackTrace();
                }
            }
        }
    }
}

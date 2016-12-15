package dinesh.fraunhofer.emk.de.ultrasenseii.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config;

/**
 * Created by dinesh on 15.12.16.
 */

public class RemoteReceivers {

    private static final String TAG = RemoteReceivers.class.getSimpleName();
    BluetoothDevice device;
    BluetoothAdapter adapter;
    Context context;
    private BluetoothSocket socket =null;

    public void startReceivingFromController(BluetoothDevice device, BluetoothAdapter adapter,Context ctx) {

        this.device = device;
        this.adapter = adapter;
        this.context = ctx;
        startClient();
    }

    public void startClient() {
        if (device != null) {
            new Thread(new StartThread()).start();
        }
    }

    private class StartThread extends Thread {

        public StartThread() {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(Config.MY_UUID);
            } catch (IOException e) {
                Log.d(TAG,"Client connection failed: "+e.getMessage()+"\n");
            }
            if(socket==null)
                socket = tmp;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            adapter.cancelDiscovery();
            Intent intent = new Intent(Config.BROADCAST_RECEIVE_COMMAND);
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                if(!socket.isConnected() && socket!=null) {
                    socket.connect();
                    Log.d(TAG,"socket is connected for first time");
                } else if (socket==null) {
                    socket = device.createRfcommSocketToServiceRecord(Config.MY_UUID);
                }

            } catch (IOException e) {
                Log.d(TAG,"Connect failed\n"+e.getMessage());
                e.printStackTrace();
                try {
                    socket.close();
                    socket = null;
                } catch (IOException e2) {
                    Log.d(TAG,"unable to close() socket during connection failure: "+e2.getMessage()+"\n");
                    e2.printStackTrace();
                    socket = null;
                }
                // Start the service over to restart listening mode
            }
            // If a connection was accepted
            if (socket != null) {
                Log.d(TAG,"Connection made\n");
                Log.d(TAG,"Remote device address: "+socket.getRemoteDevice().getAddress()+"\n");
                //Note this is copied from the TCPdemo code.
                try {
                    //TODO : Sent acknowledgement from client.
                    //PrintWriter out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
                    //Log.d(TAG,"Attempting to send message ...\n");
                    //out.println("hello from Bluetooth Demo Client");
                    //out.flush();
                    //Log.d(TAG,"Message sent...\n");

                    Log.d(TAG,"Attempting to receive a message ...\n");
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String receivedCommand = in.readLine();
                    intent.putExtra(Config.BLUETOOTH_MSG, receivedCommand);
                    context.sendBroadcast(intent);
                    Log.d(TAG,"received a message:\n" + receivedCommand+"\n");
                    //TODO : Send info using Broadcast receivers and make button click.
                    Log.d(TAG,"We are done, closing connection\n");
                } catch(Exception e) {
                    Log.d(TAG,"Error happened receiving\n");
                    e.printStackTrace();
                } finally {
                    try {
                        Log.d(TAG,"Restarting client...\n");
                        startClient();
                        //socket.close();
                    } catch (Exception e) {
                        Log.d(TAG,"Unable to close socket"+e.getMessage()+"\n");
                    }
                }
            } else {
                startClient();
                Log.d(TAG,"Made connection, but socket is null\n");
            }
        }
    }

}

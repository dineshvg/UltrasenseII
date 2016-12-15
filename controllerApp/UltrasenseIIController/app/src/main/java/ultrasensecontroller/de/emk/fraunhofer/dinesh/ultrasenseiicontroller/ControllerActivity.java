package ultrasensecontroller.de.emk.fraunhofer.dinesh.ultrasenseiicontroller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

public class ControllerActivity extends AppCompatActivity {

    Button bluetoothConnect;
    Button timeStampCollector;
    Button recordingStarter;
    TextView terminal;
    String terminalText = "\n";
    Boolean start = true;
    BluetoothAdapter mBluetoothAdapter = null;
    AudioController audioController = new AudioController();
    private static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        
        init();
        initListeners();
        setFilters();
        connectToUltrasenseII();
        
    }

    private void init() {
        bluetoothConnect = (Button) findViewById(R.id.connectButton);
        terminal = (TextView) findViewById(R.id.bluetoothInfo);
        recordingStarter = (Button) findViewById(R.id.recordButton);
        timeStampCollector = (Button) findViewById(R.id.timeStampButton);
        terminal.append("Ultrasense Controller"+"\n");

    }

    private void initListeners() {
        bluetoothConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminal.append("Trying to find paired devices"+"\n");
                scrollDown();
                terminal.setMovementMethod(new ScrollingMovementMethod());
                connectToUltrasenseII();
            }
        });

        recordingStarter.setOnClickListener(recordingLister);

        timeStampCollector.setOnClickListener(timeStampListener);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.BROADCAST_RECEIVE_PROGRESS);
        registerReceiver(threadInfoReceiver, filter);
    }

    private void connectToUltrasenseII() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //make sure bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            querypaired();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(threadInfoReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Listener to send the signal to collect timestamps.
     */
    View.OnClickListener timeStampListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                audioController.makeRemoteCall(mBluetoothAdapter,
                        terminal,terminalText,Constants.GET_TIMESTAMPS, getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Listener to send signals to start and stop recording.
     */
    View.OnClickListener recordingLister = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if(start) {
                    recordingStarter.setText(getText(R.string.start_record));
                    // Click on start record in the UltrasenseII app
                    terminal.append("Start recording"+"\n");
                    audioController.makeRemoteCall(mBluetoothAdapter,
                            terminal,terminalText,Constants.START_RECORDING,getApplicationContext());
                    start = false;
                } else {
                    recordingStarter.setText(getText(R.string.stop_record));
                    //Click on stop record in the UltrasenseII app
                    terminal.append("Stop recording"+"\n");
                    audioController.makeRemoteCall(mBluetoothAdapter,
                            terminal,terminalText,Constants.STOP_RECORDING,getApplicationContext());
                    start = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            //bluetooth result code.
            if (resultCode == Activity.RESULT_OK) {
                terminal.append("Trying to connect to Paired devices "+"\n");
                scrollDown();
                terminal.setMovementMethod(new ScrollingMovementMethod());
                querypaired();
            } else {
                Toast.makeText(getApplicationContext(),"Turn on Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void querypaired() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices.size()];
            String item;
            int i = 0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                item = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                terminal.append(item+"\n");
                scrollDown();
                terminal.setMovementMethod(new ScrollingMovementMethod());
                i++;
            }
        } else {
            Toast.makeText(getApplicationContext(),"No paired devices",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Broadcast receiver to update the terminal.
     */

    private final BroadcastReceiver threadInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals(Constants.BROADCAST_RECEIVE_PROGRESS)) {
                Bundle bundle = intent.getExtras();
                String terminalMessage = bundle.getString(Constants.BLUETOOTH_MSG);
                if(terminalMessage!=null) {
                    terminal.append(terminalMessage+"\n");
                    scrollDown();
                    terminal.setMovementMethod(new ScrollingMovementMethod());
                }

            }
        }
    };

    private void scrollDown() {
        while (terminal.canScrollVertically(1)) {
            terminal.scrollBy(0, 10);
        }
    }
}

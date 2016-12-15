package dinesh.fraunhofer.emk.de.ultrasenseii.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dinesh.fraunhofer.emk.de.ultrasenseii.R;
import dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config;
import dinesh.fraunhofer.emk.de.ultrasenseii.util.RemoteReceivers;
import dinesh.fraunhofer.emk.de.ultrasenseii.util.Utility;

public class SettingsActivity extends AppCompatActivity {

    @InjectView(R.id.activity_settings)
    RelativeLayout coordinatorLayout;
    @InjectView(R.id.remoteButton)
    Button remoteAccessButton;
    @InjectView(R.id.terminalView)
    TextView terminal;
    @InjectView(R.id.ownCotrol)
    Button owner;
    @InjectView(R.id.remoteControl)
    Button remote;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    RemoteReceivers receiver = new RemoteReceivers();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        initListeners();
        setFilters();
    }

    private void initListeners() {

        //Don't grant remote access till it has been granted using remoteAccessButton
        remote.setEnabled(false);
        remoteAccessButton.setOnClickListener(remoteAccessListener);

        //Open as owner
        owner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
                intent.putExtra(Config.BROADCAST_ACTIVITY,Config.NOT_REMOTE);
                startActivity(intent);
                finish();
            }
        });

        //open as remote device
        remote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(remote.isEnabled()) {
                    //start threads for remote access.
                    receiver.startReceivingFromController(device,bluetoothAdapter,getApplicationContext());
                    Intent intent = new Intent(SettingsActivity.this, StartActivity.class);
                    intent.putExtra(Config.BROADCAST_ACTIVITY,Config.REMOTE);
                    startActivity(intent);
                    finish();
                } else {
                    Utility.callSnackbar("Obtain remote access first.",coordinatorLayout);
                }
            }
        });
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.BROADCAST_COUNTDOWN_TIMER);
        filter.addAction(Config.BROADCAST_RECEIVE_COMMAND);
        registerReceiver(threadInfoReceiver, filter);
    }

    /**
     * Listener module for granting remote access.
     */
    View.OnClickListener remoteAccessListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                terminal.setText("This device does not support bluetooth. Cannot do remote access");
            }

            if(bluetoothAdapter.isEnabled()) {
                terminal.append("\n Bluetooth is turned off, attempting to turn on.");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Config.REQUEST_ENABLE_BT);
            } else {
                terminal.append("\n Bluetooth is already on for use.");
                queryPaired();
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.REQUEST_ENABLE_BT) {
            //bluetooth result code.
            if (resultCode == Activity.RESULT_OK) {
                terminal.append("\n Bluetooth is on.");
                queryPaired();
            } else {
                terminal.append("\n Please turn the bluetooth on.");
            }
        }
    }

    private void queryPaired() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            terminal.append("\n at least 1 paired device\n");
            final BluetoothDevice blueDev[] = new BluetoothDevice[pairedDevices.size()];
            String[] items = new String[blueDev.length];
            int i =0;
            for (BluetoothDevice devicel : pairedDevices) {
                blueDev[i] = devicel;
                items[i] = blueDev[i].getName() + ": " + blueDev[i].getAddress();
                terminal.append("Device: "+items[i]+"\n");
                i++;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Choose Bluetooth:");
            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    dialog.dismiss();
                    if (item >= 0 && item <blueDev.length) {
                        device = blueDev[item];
                        remoteAccessButton.setText("device: "+blueDev[item].getName());
                        remote.setEnabled(true); //Once the device is chosen , obtain the access.
                        terminal.append("Device to control UltrasenseII remotely chosen."+"\n");
                        scrollDown();
                        terminal.setMovementMethod(new ScrollingMovementMethod());
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private final BroadcastReceiver threadInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Config.BROADCAST_COUNTDOWN_TIMER)) {
                Bundle bundle = intent.getExtras();
                String remainingTime = bundle.getString(Config.TIMER);
                terminal.append("Timer : "+remainingTime+"\n");
                scrollDown();
                terminal.setMovementMethod(new ScrollingMovementMethod());
            } else if(intent.getAction().equals(Config.BROADCAST_RECEIVE_COMMAND)) {
                Bundle bundle = intent.getExtras();
                String remoteCommand = bundle.getString(Config.BLUETOOTH_MSG);
                terminal.append("Command :"+remoteCommand);
                scrollDown();
                terminal.setMovementMethod(new ScrollingMovementMethod());
            }

        }

    };

    private void scrollDown() {
        while (terminal.canScrollVertically(1)) {
            terminal.scrollBy(0, 10);
        }
    }
}

package dinesh.fraunhofer.emk.de.ultrasenseii.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dinesh.fraunhofer.emk.de.ultrasenseii.R;
import dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config;
import dinesh.fraunhofer.emk.de.ultrasenseii.util.Utility;
import dinesh.fraunhofer.emk.de.ultrasenseii.view.UltraSenseModule;

import static dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config.fileDir;

public class StartActivity extends AppCompatActivity{

    private final static String TAG = StartActivity.class.getSimpleName();

    @InjectView(R.id.activity_start)
    RelativeLayout coordinatorLayout;
    @InjectView(R.id.timeStampButton)
    Button timeStampButton;
    @InjectView(R.id.recordButton)
    Button recordButton;
    @InjectView(R.id.filenameEditText)
    EditText filenameEditText;
    @InjectView(R.id.saveExcelButton)
    Button saveExcel;
    int clicks = 0;
    File file;
    File from;
    HSSFWorkbook workbook;
    String time = "";
    private UltraSenseModule ultraSenseModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.inject(this);
        initListeners();
        getPermissionForExcel();
        setFilters();
        this.ultraSenseModule = new UltraSenseModule(StartActivity.this);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Config.BROADCAST_COUNTDOWN_TIMER);
        registerReceiver(threadInfoReceiver, filter);
    }

    private void getPermissionForMicrophone() {
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartActivity.this,
                    Config.PERMISSIONS_AUDIO,Config.REQUEST_AUDIO_RECORDING);
        } else if (permission == PackageManager.PERMISSION_GRANTED) {
            startRecordProcess();
        }
    }

    private void getPermissionForExcel() {

        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(StartActivity.this,
                    Config.PERMISSIONS_STORAGE,Config.REQUEST_EXTERNAL_STORAGE);
        } else if (permission == PackageManager.PERMISSION_GRANTED) {
            initExcelSheet();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode==1) {
            initExcelSheet();
        }
        if(requestCode==2) {
            startRecordProcess();
        }
    }

    private void initExcelSheet() {
        //make directory for file.
        new File(fileDir).mkdirs();
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet timeStampSheet = workbook.createSheet("TimeStamp Sheet");
        HSSFRow row = timeStampSheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString("TimeStamp for current test"));

        FileOutputStream fos = null;
        try {
            file = new File(fileDir + getString(R.string.app_name) + ".xls");
            fos = new FileOutputStream(file);
            workbook.write(fos);
            callSnackbar("Excel file generated to save timestamps");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //Toast.makeText(StartActivity.this, "File Generated", Toast.LENGTH_LONG).show();

        }
    }

    private void initListeners() {
        timeStampButton.setOnClickListener(timeStampListener);
        recordButton.setOnClickListener(recordListener);
        saveExcel.setOnClickListener(excelListener);
    }

    private View.OnClickListener excelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                if(filenameEditText.getText()!=null) {
                    if(!filenameEditText.getText().toString().equals("")) {
                        File directory = new File(fileDir);
                        from = new File(directory, getString(R.string.app_name)+".xls");
                        File to = new File(directory, filenameEditText.getText().toString() + ".xls");
                        from.renameTo(to);
                    } else {
                        callSnackbar("Enter Filename to change");
                    }
                } else {
                    callSnackbar("Enter Filename to change");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void callSnackbar(String text) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, text, Snackbar.LENGTH_SHORT);
        snackbar.show();

    }

    private View.OnClickListener timeStampListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileOutputStream fos = null;
            try {
                clicks = clicks+1;
                Log.d(TAG,"clicks :"+clicks);
                // TODO Store timeStamp in file on every click till the recording is going on.
                String currentTime = Utility.getTimeStamp();
                //File file = new File(fileDir + getString(R.string.app_name) + ".xls");
                if(file.exists()) {
                    FileInputStream fileStream = new FileInputStream(file);
                    workbook = new HSSFWorkbook(fileStream);
                    HSSFSheet sheet = workbook.getSheet("TimeStamp Sheet");
                    //String value = sheet.getRow(0).getCell(0).getStringCellValue();
                    sheet.createRow(clicks).createCell(0).setCellValue(currentTime);
                    fos = new FileOutputStream(file);
                    workbook.write(fos);
                } else {
                    Log.d(TAG,"no file");
                }
                Log.d(TAG,currentTime);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.flush();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private View.OnClickListener recordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getPermissionForMicrophone();
        }
    };

    private void startRecordProcess() {
        try {
            // TODO Start the 20 Khz signal and also start the recording of the ambient signal through a recorder.
            if(recordButton.getText().equals(getResources().getString(R.string.start_record))) {
                //Change name to stop for standby
                recordButton.setText(getResources().getString(R.string.stop_record));
                time = Utility.getWaveFileName();
                ultraSenseModule.createCustomScenario();
                ultraSenseModule.startRecord();
                //Start the 20 Khz signal from the speaker

                //Save the excel sheet for the timestamps

            } else if (recordButton.getText().equals(getResources().getString(R.string.stop_record))) {
                //Change name to start for standby
                recordButton.setText(getResources().getString(R.string.start_record));
                ultraSenseModule.stopRecord();
                callSnackbar("Audio files saved with names: "+time);
                ultraSenseModule.saveRecordedFiles(time);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver threadInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Config.BROADCAST_COUNTDOWN_TIMER)) {
                Bundle bundle = intent.getExtras();
                String remainingTime = bundle.getString(Config.TIMER);
                if (!remainingTime.trim().equals("1")) {
                    recordButton.setText(remainingTime);
                    recordButton.setClickable(false);
                }
                else if (remainingTime.trim().equals("1")) {
                    callSnackbar("Recording process has started. Start Activity");
                    recordButton.setText(getResources().getString(R.string.stop_record));
                    recordButton.setClickable(true);
                }
            }
        }
    };
}

// Example of a call to a native method
//TextView tv = (TextView) findViewById(R.id.sample_text);
//tv.setText(stringFromJNI());

/**
 * A native method that is implemented by the 'native-lib' native library,
 * which is packaged with this application.
 */
    /*public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }*/

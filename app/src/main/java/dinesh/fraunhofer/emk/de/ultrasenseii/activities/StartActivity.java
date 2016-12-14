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
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dinesh.fraunhofer.emk.de.ultrasenseii.R;
import dinesh.fraunhofer.emk.de.ultrasenseii.api.StopWatch;
import dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config;
import dinesh.fraunhofer.emk.de.ultrasenseii.util.Utility;
import dinesh.fraunhofer.emk.de.ultrasenseii.view.UltraSenseModule;

import static dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config.fileDir;

public class StartActivity extends AppCompatActivity{

    private final static String TAG = StartActivity.class.getSimpleName();

    @InjectView(R.id.activity_start)
    RelativeLayout coordinatorLayout;
    @InjectView(R.id.startButton)
    Button startTimeButton;
    /*@InjectView(R.id.stopButton)
    Button stopTimeButton;*/
    @InjectView(R.id.recordButton)
    Button recordButton;
    @InjectView(R.id.filenameEditText)
    EditText filenameEditText;
    @InjectView(R.id.saveExcelButton)
    Button saveExcel;
    int clicks = 0;
    //int startTimeCell = 0;
    int prevAns = 1; int cellValToFill = 1;
    //int stopclicks = 0;
    File file;
    File from;
    HSSFWorkbook workbook;
    String time = "";
    private UltraSenseModule ultraSenseModule;
    StopWatch timer = new StopWatch();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.inject(this);
        initListeners();
        getPermissionForExcel();
        setFilters();

        this.ultraSenseModule = new UltraSenseModule(StartActivity.this);
        //Make excel data collection off until recording starts
        //startTimeButton.setClickable(false);
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
        HSSFSheet timeStampSheet = workbook.createSheet("timestamp sheet");

        //Style for header
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        headerStyle.setFillForegroundColor(HSSFColor.LIME.index);
        headerStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setColor(HSSFColor.BLACK.index);
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setFontHeightInPoints((short) 12);
        headerStyle.setFont(font);
        headerStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
        headerStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
        headerStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
        headerStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);

        //Style for normal cells
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
        Font smallFont = workbook.createFont();
        smallFont.setColor(HSSFColor.DARK_BLUE.index);
        smallFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        smallFont.setFontHeightInPoints((short) 10);
        cellStyle.setFont(smallFont);

        HSSFRow row = timeStampSheet.createRow(0);
        HSSFCell cell = row.createCell(0);
        cell.setCellStyle(headerStyle);
        cell.setCellValue(new HSSFRichTextString("Label start"));
        HSSFCell cell2 = row.createCell(1);
        cell2.setCellStyle(headerStyle);
        cell2.setCellValue(new HSSFRichTextString("label stop"));

        for(int i = 1; i<100; i++) {
            HSSFRow r = timeStampSheet.createRow(i);
            HSSFCell c0 = r.createCell(i);
            c0.setCellStyle(cellStyle);
            HSSFCell c1 = r.createCell(i);
            c1.setCellStyle(cellStyle);
            c0.setCellValue("");
            c1.setCellValue("");
        }

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
        }
    }

    private void initListeners() {
        startTimeButton.setOnClickListener(startTimeStampListener);
        //stopTimeButton.setOnClickListener(stopTimeStampListener);
        recordButton.setOnClickListener(recordListener);
        saveExcel.setOnClickListener(excelListener);
    }

    private View.OnClickListener excelListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            File to = null;
            Utility.renameExcel(filenameEditText,from, to, StartActivity.this, coordinatorLayout);
        }
    };

    public void callSnackbar(String text) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, text, Snackbar.LENGTH_SHORT);
        snackbar.show();

    }

    public void swipeToDissmissSnackbar(String text) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private View.OnClickListener startTimeStampListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileOutputStream fos = null;
            try {
                clicks = clicks+1;

                // TODO Store timeStamp in file on every click till the recording is going on.
                //String currentTime = Utility.getTimeStamp();
                //File file = new File(fileDir + getString(R.string.app_name) + ".xls");
                if(file.exists()) {
                    FileInputStream fileStream = new FileInputStream(file);
                    workbook = new HSSFWorkbook(fileStream);
                    HSSFSheet sheet = workbook.getSheet("timestamp sheet");
                    Log.d(TAG,"clicks "+clicks);
                    Log.d(TAG,"where it goes: "+clicks%2);
                    if(clicks!=0 && clicks%2 != 0) {//start time
                        //startTimeCell = startclicks;
                        if(clicks==1) {
                            sheet.getRow(clicks).createCell(0).setCellValue((float)timer.getElapsedTime()/1000);
                        } else {
                            cellValToFill = clicks - cellValToFill;
                            sheet.getRow(cellValToFill).createCell(0).setCellValue((float)timer.getElapsedTime()/1000);
                        }

                    } else if ( clicks!=0 && clicks%2 == 0) {//end time
                        sheet.getRow(cellValToFill).createCell(1).setCellValue((float)timer.getElapsedTime()/1000);
                    }
                    fos = new FileOutputStream(file);
                    workbook.write(fos);
                } else {
                    Log.d(TAG,"no file");
                }
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
            //
            // TODO Start the 20 Khz signal and also start the recording of the ambient signal through a recorder.
            if(recordButton.getText().equals(getResources().getString(R.string.start_record))) {
                //Change name to stop for standby
                time = Utility.getWaveFileName();
                callSnackbar(time);
                recordButton.setText(getResources().getString(R.string.stop_record));
                ultraSenseModule.createCustomScenario();
                ultraSenseModule.startRecord();
                //Start the 20 Khz signal from the speaker

                //Save the excel sheet for the timestamps

            } else if (recordButton.getText().equals(getResources().getString(R.string.stop_record))) {
                //Change name to start for standby
                recordButton.setText(getResources().getString(R.string.start_record));
                ultraSenseModule.stopRecord();
                filenameEditText.setText(time);
                File directory = new File(fileDir);
                File to = new File(directory, time);
                Utility.renameExcel(filenameEditText,from, to, StartActivity.this, coordinatorLayout);
                swipeToDissmissSnackbar("Audio files saved with names: "+time);
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
                    //Set excel data collection true since recording has started
                    timer.start();
                    startTimeButton.setClickable(true);
                    time = Utility.getWaveFileName();
                    callSnackbar(time);
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


/*private View.OnClickListener stopTimeStampListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileOutputStream fos = null;
            try {
                timer.stop();
                stopclicks = stopclicks+1;
                Log.d(TAG,"stopclicks :"+stopclicks);
                // TODO Store timeStamp in file on every click till the recording is going on.
                String currentTime = Utility.getTimeStamp();
                //File file = new File(fileDir + getString(R.string.app_name) + ".xls");
                if(file.exists()) {
                    FileInputStream fileStream = new FileInputStream(file);
                    workbook = new HSSFWorkbook(fileStream);
                    HSSFSheet sheet = workbook.getSheet("TimeStamps");
                    //String value = sheet.getRow(0).getCell(0).getStringCellValue();
                    if(sheet.getRow(stopclicks)!=null) {
                        sheet.getRow(stopclicks).createCell(1).setCellValue((float)timer.getElapsedTime()/1000);
                        fos = new FileOutputStream(file);
                        workbook.write(fos);
                        Log.d(TAG,"updated value stopclicks :" + sheet.getRow(stopclicks).getCell(1).getStringCellValue());
                    } else {
                        stopclicks = stopclicks-1;
                        callSnackbar("No Start time");
                    }
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
    };*/
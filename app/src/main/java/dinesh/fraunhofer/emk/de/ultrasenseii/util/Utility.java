package dinesh.fraunhofer.emk.de.ultrasenseii.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

import dinesh.fraunhofer.emk.de.ultrasenseii.R;
import dinesh.fraunhofer.emk.de.ultrasenseii.activities.StartActivity;

import static dinesh.fraunhofer.emk.de.ultrasenseii.configuration.Config.fileDir;

/**
 * Created by dinesh on 03.11.16.
 */

public class Utility {


    public static String getTimeStamp(){

        Calendar c = Calendar.getInstance();

        int milliseconds = c.get(Calendar.MILLISECOND);
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        return hours+":"+minutes+":"+seconds+":"+milliseconds;
    }

    public static String getWaveFileName(){

        Calendar c = Calendar.getInstance();

        int milliseconds = c.get(Calendar.MILLISECOND);
        int seconds = c.get(Calendar.SECOND);
        int minutes = c.get(Calendar.MINUTE);
        int hours = c.get(Calendar.HOUR_OF_DAY);
        return hours+"_"+minutes+"_"+seconds+"_"+milliseconds;
    }

    public static boolean renameExcel(EditText filenameEditText, File from, File to, Activity actvty,
                               RelativeLayout coordinatorLayout) {
        try {
            if(filenameEditText.getText()!=null) {
                if(!filenameEditText.getText().toString().equals("")) {
                    File directory = new File(fileDir);
                    from = new File(directory, actvty.getString(R.string.app_name)+".xls");
                    if(null!=from) {
                        to = new File(directory, filenameEditText.getText().toString() + ".xls");
                        from.renameTo(to);
                        swipeToDissmissSnackbar("File renamed to "+filenameEditText.getText().toString(),
                                coordinatorLayout);
                        return  true;
                    } else {
                        callSnackbar("File does not exist",
                                coordinatorLayout);
                        return false;
                    }
                } else {
                    callSnackbar("Enter Filename to change", coordinatorLayout);
                    return false;
                }
            } else {
                callSnackbar("Enter Filename to change", coordinatorLayout);
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static  void callSnackbar(String text, RelativeLayout coordinatorLayout) {

        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, text, Snackbar.LENGTH_SHORT);
        snackbar.show();

    }

    public static void swipeToDissmissSnackbar(String text, RelativeLayout coordinatorLayout) {
        Snackbar snackbar = Snackbar
                .make(coordinatorLayout, text, Snackbar.LENGTH_LONG);
        snackbar.show();
    }
}

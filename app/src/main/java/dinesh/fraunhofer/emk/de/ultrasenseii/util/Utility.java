package dinesh.fraunhofer.emk.de.ultrasenseii.util;

import android.annotation.TargetApi;
import android.os.Build;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void readingExcel(String fileName) {
        try (FileInputStream file = new FileInputStream(new File(fileName))) {
            HSSFWorkbook workbook = new HSSFWorkbook(file);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.cellIterator();
                for (Cell cell : row) {
                    System.out.println(cell.getStringCellValue());
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}

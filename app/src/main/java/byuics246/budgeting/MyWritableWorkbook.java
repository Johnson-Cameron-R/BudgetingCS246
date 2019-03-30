package byuics246.budgeting;


import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import jxl.write.Number;

import android.util.Log;


public class MyWritableWorkbook {
    private static final String TAG = "Writable book";
    String folder;
    File directory;
    String file_name_inp;
    String file_name_out;
    File inp;
    File out;
    public WritableWorkbook wb;
    File sdCard = Environment.getExternalStorageDirectory();


//    FileOutputStream fos = null;//???

    public MyWritableWorkbook(WritableWorkbook workbook){wb = workbook;}//
    public MyWritableWorkbook(){}//


    public int checkDirectory(String folder) {
        this.folder = folder;
        //Finding a directory
        File sdCard = Environment.getExternalStorageDirectory();
//        check permission//////////////////////////////////////////////////////////
        boolean writable = isExternalStorageWritable();
        if (!writable)
            return 2;/////////////////////////////////// add an error message of permission
        directory = new File(sdCard.getAbsolutePath() + folder);
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        if (!directory.isDirectory()) {
            return 1; // an error of creating a directory
        }
        return 0;// no error
    }


    //String inp, String out){file_name_inp = inp; file_name_out = out;

    public int createCopyWorkbook (String templete, String copy)
    {
        file_name_inp = templete;
        file_name_out = copy;
        String path = sdCard.getAbsolutePath()+ folder +file_name_inp;
        inp = new File (path);
        for (int i = 0; i < 30; i++) {
            if (inp.exists()) {
                out = new File(sdCard.getAbsolutePath() + folder + file_name_out);
                try {
                    Workbook existingWorkbook = Workbook.getWorkbook(inp);// This opens up a read-only copy of the workbook
                    wb = Workbook.createWorkbook(out, existingWorkbook); // This opens up a writable workbook so that we can edit the copy
                    existingWorkbook.close();    // Important: Close it before writing the copy with copy.write();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "An exception in createCopyWorkbook");
                    return 1;
                } catch (BiffException e) {
                    e.printStackTrace();
                    Log.d(TAG, "An exception in createCopyWorkbook2");
                    return 1;
                }
                Log.d(TAG, "Success in createCopyWorkbook");
                return 0;
            } else {
                try {
                    Thread.sleep(600);
                    Log.d(TAG, "The templete isn't loaded yet");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.d(TAG, "An exception in createCopyWorkbook3");
                }
            }
        }
        Log.d(TAG, "The templete isn't loaded");
        return 2;
    }



    public WritableWorkbook createWorkbook(String fileName){
        //Creating a file
        File file = new File(directory, fileName);

        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        WritableWorkbook workbook = null;
        try {
            workbook = Workbook.createWorkbook(file, wbSettings);
        } catch (IOException e) {
            e.printStackTrace();
        }
        wb = workbook;
        //////////////////////////////////////////////////////////////////////////////////////try 3
//        if(isExternalStorageWritable()){
//            File txt = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"test.txt");
////            File dir = getPublicAlbumStorageDir("DIRECTORY_EXCEL");
////            File txt = new File(dir, fileName);
//            if (txt.exists()){
//                Log.i(TAG, "createWorkbook: file created");
//            }
//            else
//            {
//                Log.i(TAG, "createWorkbook: file Not created");
//            }
//
//        }

//        return this;
        return workbook;
    }



    /**
     *
     * @param sheetName - name to be given to new sheet
     * @param sheetIndex - position in sheet tabs at bottom of workbook
     * @return - a new WritableSheet in given WritableWorkbook
     */
    public WritableSheet createSheet(String sheetName, int sheetIndex) {
        //create a new WritableSheet and return it
        WritableSheet ws = null;
        if (wb != null) {
            ws = wb.createSheet(sheetName, sheetIndex);
            try {
                wb.write();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            System.out.print("NULL!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return ws;
    }

    public void updateSheet(int sheetNumber, List<CellNumberRecord> cellNumberRecords, List<CellStringRecord> cellStringRecords)
    {
        WritableSheet sheet = wb.getSheet(sheetNumber);
        try {
            for (CellNumberRecord cr : cellNumberRecords) {
                Number number = new Number(cr.getColumn(), cr.getRow(),cr.getValue());
                sheet.addCell(number);
            }
            for (CellStringRecord cr : cellStringRecords) {
                Label label = new Label(cr.getColumn(), cr.getRow(),cr.getValue());
                sheet.addCell(label);
            }
            wb.write();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param columnPosition - column to place new cell in
     * @param rowPosition - row to place new cell in
     * @param contents - string value to place in cell
     * @param headerCell - whether to give this cell special formatting
     * @param sheet - WritableSheet to place cell in
     * @throws RowsExceededException - thrown if adding cell exceeds .xls row limit
     * @throws WriteException - Idunno, might be thrown
     */
    public void writeCell(int columnPosition, int rowPosition, String contents, boolean headerCell,
                          WritableSheet sheet) throws RowsExceededException, WriteException {
        //create a new cell with contents at position

        Label newCell = new Label(columnPosition,rowPosition,contents);

        if (headerCell){
            //give header cells size 10 Arial bolded
            WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
            //center align the cells' contents
            headerFormat.setAlignment(Alignment.CENTRE);
            newCell.setCellFormat(headerFormat);
        }

        sheet.addCell(newCell);

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File root = Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + albumName);
//                Environment.DIRECTORY_PICTURES), albumName);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Directory not created");
            }
        }
        return dir;
    }
}

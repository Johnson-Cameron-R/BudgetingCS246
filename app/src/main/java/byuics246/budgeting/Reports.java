package byuics246.budgeting;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import jxl.write.WritableWorkbook;

public class Reports extends AppCompatActivity {
    String filePath = "";
    String folder = "/Simple_Budgeting_files/";
    String file_name_inp = "ExcelTable.xls";
    String file_name_out = "March.xls";
    File inp;
    File out;
    WritableWorkbook wb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

    }

    public void requestReport(View view){
        //request and check permissions

        //create a directory
        MyWritableWorkbook mywb = new MyWritableWorkbook();
        int directoryStatus = mywb.checkDirectory(folder); // handle error messages // permissions
        if (directoryStatus != 0)
        {
            Toast.makeText(this, "Make sure you add a writing memory permission to the app and try again",
                    Toast.LENGTH_LONG).show();
        }
        else {
           downloadTemplete();
           //make sure the excel is closed
            if (!mywb.createCopyWorkbook(file_name_inp, file_name_out)) {
                Toast.makeText(this, "Make sure that excel reports in the app folder are closed and try again",
                        Toast.LENGTH_LONG).show();
            }
            else {
                mywb.updateSheetNumber(0, 28, 4, (double) 40.05);
                mywb.close();
                Toast.makeText(this, "File is saved!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    //**********************************
    //*
    //**********************************
    public boolean downloadTemplete()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard.getAbsolutePath() + folder +file_name_inp);
        if(!file.exists()){
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse("https://github.com/inessae/Excel-report-tamplete/raw/master/ExcelTable2.xls"))
                    .setTitle(file_name_inp)// Title of the Download Notification
                    .setDescription("Downloading")// Description of the Download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                    .setDestinationInExternalPublicDir("/Simple_Budgeting_files/", file_name_inp)//
//                .setRequiresCharging(false)// Set if charging is required to begin the download
//                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
//                .setVisibleInDownloadsUi(false);
                    .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
            DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        }
        return true;/////////////////////////////
    }



}

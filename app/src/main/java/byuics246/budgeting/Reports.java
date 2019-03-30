package byuics246.budgeting;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.write.WritableWorkbook;

public class Reports extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "Reports";

    String filePath = "";
    String folder = "/Simple_Budgeting_files/";
    String file_name_inp = "Don't Change.xls";
    String file_name_out = "Month.xls";
    File inp;
    File out;
    WritableWorkbook wb;
    String month;
    String year;

    ArrayAdapter <CharSequence> adapterMonths;
    Spinner monthsSpinner;
    ArrayAdapter <CharSequence> adapterYears;
    Spinner yearsSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        monthsSpinner = findViewById(R.id.spinnerReportsMonths);
        adapterMonths = ArrayAdapter.createFromResource(this, R.array.Months, android.R.layout.simple_spinner_item);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthsSpinner.setAdapter(adapterMonths);
        monthsSpinner.setOnItemSelectedListener(this);

        yearsSpinner = findViewById(R.id.spinnerReportsYears);
        adapterYears = ArrayAdapter.createFromResource(this, R.array.Years, android.R.layout.simple_spinner_item);
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearsSpinner.setAdapter(adapterYears);
        yearsSpinner.setOnItemSelectedListener(this);


    }

    public void requestReport(View view){
        file_name_out = month + year + ".xls";
        int monthNumber = new MonthStringToIntConverter(month).convert();
        if (monthNumber != 0) {

            //request and check permissions

            //create a directory
            MyWritableWorkbook mywb = new MyWritableWorkbook();
            int directoryStatus = mywb.checkDirectory(folder); // handle error messages // permissions
            if (directoryStatus != 0) {
                Toast.makeText(this, "Make sure you add a writing memory permission to the app and try again",
                        Toast.LENGTH_LONG).show();
            } else {
                downloadTemplete();
                int creationResult = mywb.createCopyWorkbook(file_name_inp, file_name_out);
                //make sure the excel is closed
                if (creationResult == 1) {
                    Toast.makeText(this, "Make sure that excel reports in the app folder are closed and try again",
                            Toast.LENGTH_LONG).show();
                } else if (creationResult == 2) {
                    Toast.makeText(this, "The templete is having issues downloading. Try again later",
                            Toast.LENGTH_LONG).show();
                } else {
                    populateWorkbook(mywb, monthNumber);
                    //
                    //                mywb.updateSheetNumber(0, 28, 4, (double) 40.05);
                    //                mywb.close();
                    Toast.makeText(this, "File is saved in  internal storage/Simple_Budgeting_files!",
                            Toast.LENGTH_LONG).show();
                }
            }

        }
        else {
            Log.d(TAG, "Wrong month value");
            Toast.makeText(this, "Please select a month",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void populateWorkbook(MyWritableWorkbook mywb, int monthNumber) {
        //Pull goals categories and values
        Map <String, Integer> expensesCategories = new HashMap<>();
        expensesCategories.put("Inactive Savings", 0);
        expensesCategories.put("Education", 1);

        // Pull incomes of the asked month and year

        // Pull expences of the asked month and year
        List<Transaction> expenses =  new ArrayList<Transaction>();
        expenses.add(new Transaction("03/17/2019", "user", "Inactive Savings", "100", "description"));
        expenses.add(new Transaction("03/10/2019", "user", "Education", "200", "description"));
        expenses.add(new Transaction("03/10/2019", "user", "Education", "300", "description"));

        int startExpencesCellsX = 4 - 1;
        int startExpencesCellsY = 33;
        int startIncomeCellsX = 4;
        int startIncomeCellsY = 33;

        // create Cell records
        List <CellRecord> cellRecords = getExpensesRecords(expenses, expensesCategories, startExpencesCellsX, startExpencesCellsY);

        //Update excel
        mywb.updateSheetNumber(0, cellRecords);
        mywb.close();

    }


    //**********************************
    //*
    //**********************************
    public boolean downloadTemplete()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard.getAbsolutePath() + folder +file_name_inp);
        if(!file.exists()){
            DownloadManager.Request request=new DownloadManager.Request(Uri.parse("https://github.com/inessae/Excel-report-tamplete/blob/master/Tamplete.xls?raw=true"))
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.spinnerReportsMonths:
                month = parent.getItemAtPosition(position).toString();
                break;
            case R.id.spinnerReportsYears:
                year = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private List <CellRecord> getExpensesRecords(List <Transaction> expenses, Map <String, Integer> expensesCategories, int startExpencesCellsX, int startExpencesCellsY)
    {
        List <CellRecord> cellRecords = new ArrayList<>();
        for (Transaction ex : expenses) {
            try {
                Date date=new SimpleDateFormat("MM/dd/yyyy").parse(ex.date);
                int day = date.getDate();
                int category = expensesCategories.get(ex.category);
                double amount = Double.parseDouble(ex.getAmount());
                // if there is a record for the same day for the same category, update its value to its original value + amount
                boolean found = false;
                CellRecord crMatching = null;
                for (CellRecord cr : cellRecords){
                    if (cr.getColumn() == day + startExpencesCellsX && cr.getRow() == category + startExpencesCellsY){
                        found = true;
                        cr.setValue(amount + cr.getValue());
                    }
                }
                //if there are no records for the same day for the same category create a new record
                if (!found) {
                    CellRecord cellRecord = new CellRecord(day + startExpencesCellsX, category + startExpencesCellsY, amount);
                    cellRecords.add(cellRecord);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return cellRecords;
    }
}

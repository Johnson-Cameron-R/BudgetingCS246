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
import java.util.List;

import jxl.write.WritableWorkbook;

public class ReportsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "ReportsActivity";

    String folder = "/Simple_Budgeting_files/";
    String file_name_inp = "Don't Change.xls";
    String file_name_out = "Month.xls";

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
        // Pull data from web elements
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
                // download templete
                downloadTemplete();
                // create a copy of the templete
                int creationResult = mywb.createCopyWorkbook(file_name_inp, file_name_out);
                if (creationResult == 1) {
                    Toast.makeText(this, "Make sure that excel reports in the app folder are closed and try again",
                            Toast.LENGTH_LONG).show();
                } else if (creationResult == 2) {
                    Toast.makeText(this, "The templete is having issues downloading. Try again later",
                            Toast.LENGTH_LONG).show();
                } else {
                    // fill in the report
                    populateWorkbook(mywb, monthNumber);
                    Toast.makeText(this, "File is saved in  internal storage/Simple_Budgeting_files/" + file_name_out + "!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void populateWorkbook(MyWritableWorkbook mywb, int monthNumber) {
        //Pull income goals categories and values
        List <Goal> incomesGoals = new ArrayList<>();
        incomesGoals.add(new Goal("job1", "500", "1"));
        incomesGoals.add(new Goal("job2", "600", "2"));


        //Pull expenses goals categories and values
        List <Goal> expensesGoals = new ArrayList<>();
        expensesGoals.add(new Goal("Inactive Savings", "700", "1"));
        expensesGoals.add(new Goal("Education", "800", "2"));

        // Pull incomes of the asked month and year
        List<Transaction> incomes =  new ArrayList<Transaction>();
        incomes.add(new Transaction("03/17/2019", "user", "1", "100", "description"));
        incomes.add(new Transaction("03/10/2019", "user", "2", "200", "description"));
        incomes.add(new Transaction("03/10/2019", "user", "2", "300", "description"));

        // Pull expences of the asked month and year
        List<Transaction> expenses =  new ArrayList<Transaction>();
        expenses.add(new Transaction("03/17/2019", "user", "1", "100", "description"));
        expenses.add(new Transaction("03/10/2019", "user", "2", "200", "description"));
        expenses.add(new Transaction("03/10/2019", "user", "2", "300", "description"));

        int startIncomesCategoriesCellY = 18 - 1;
        int startExpensesCategoriesCellY = 33 - 1;
        int startExpensesIncomesCategoriesCellX = 0 - 1; // names of goals
        int startExpensesIncomesGoalsCellX = 1 - 1; // amount of goals
        int startExpensesIncomesCellsX = 4 - 1;

        // create Cell records
        List <CellNumberRecord> cellNumberRecords = new ArrayList<>();
        cellNumberRecords.addAll(getRecords(incomes, startExpensesIncomesCellsX, startIncomesCategoriesCellY));
        cellNumberRecords.addAll(getRecords(expenses, startExpensesIncomesCellsX, startExpensesCategoriesCellY));
        cellNumberRecords.addAll(getGoalRecordsNumber(expensesGoals, startExpensesIncomesGoalsCellX, startExpensesCategoriesCellY));
        cellNumberRecords.addAll(getGoalRecordsNumber(incomesGoals, startExpensesIncomesGoalsCellX, startIncomesCategoriesCellY));

        List <CellStringRecord> cellStringRecords = new ArrayList<>();
        cellStringRecords.addAll(getGoalRecordsString(expensesGoals, startExpensesIncomesCellsX, startExpensesCategoriesCellY));
        cellStringRecords.addAll(getGoalRecordsString(incomesGoals, startExpensesIncomesCellsX, startIncomesCategoriesCellY));

        //Update excel
        mywb.updateSheet(0, cellNumberRecords, cellStringRecords);
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
        return true;
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

    private List <CellNumberRecord> getRecords(List <Transaction> transactions, int startCellsX, int startCellsY)
    {
        List <CellNumberRecord> cellNumberRecords = new ArrayList<>();
        for (Transaction ex : transactions) {
            try {
                Date date=new SimpleDateFormat("MM/dd/yyyy").parse(ex.date);
                int day = date.getDate();
                int category = Integer.parseInt(ex.getCategory());
                double amount = Double.parseDouble(ex.getAmount());
                // if there is a record for the same day for the same category, update its value to its original value + amount
                boolean found = false;
                CellNumberRecord crMatching = null;
                for (CellNumberRecord cr : cellNumberRecords){
                    if (cr.getColumn() == day + startCellsX && cr.getRow() == category + startCellsY){
                        found = true;
                        cr.setValue(amount + cr.getValue());
                        break;
                    }
                }
                //if there are no records for the same day for the same category create a new record
                if (!found) {
                    CellNumberRecord cellNumberRecord = new CellNumberRecord(day + startCellsX, category + startCellsY, amount);
                    cellNumberRecords.add(cellNumberRecord);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return cellNumberRecords;
    }


    private List <CellStringRecord> getGoalRecordsString(List <Goal> goals, int startCellsX, int startCellsY)
    {
        List <CellStringRecord> cellStringRecords = new ArrayList<>();
        for (Goal g : goals) {
            String value = g.getName();
            int id = Integer.parseInt(g.getId());
            CellStringRecord cellStringRecord = new CellStringRecord(startCellsX + 1, id + startCellsY, value);
            cellStringRecords.add(cellStringRecord);

        }
        return cellStringRecords;
    }

    private List <CellNumberRecord> getGoalRecordsNumber(List <Goal> goals, int startCellsX, int startCellsY) {
        List<CellNumberRecord> cellNumberRecords = new ArrayList<>();
        for (Goal g : goals) {
            Double value = Double.parseDouble(g.getAmount());
            int id = Integer.parseInt(g.getId());
            CellNumberRecord cellNumberRecord = new CellNumberRecord(startCellsX + 1, id + startCellsY, value);
            cellNumberRecords.add(cellNumberRecord);
        }
        return cellNumberRecords;
    }

}

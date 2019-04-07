package byuics246.budgeting;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


public class ReportsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    MyWritableWorkbook wb;

    private FirebaseFirestore db;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;


    private static final String TAG = "ReportsActivity";
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

    String folder = "/Simple_Budgeting_files/";
    String file_name_inp = "Don't Change.xls";
    String file_name_out = "Month.xls";

    String month;
    String monthNumberString;
    String year;

    ArrayAdapter <CharSequence> adapterMonths;
    Spinner monthsSpinner;
    ArrayAdapter <CharSequence> adapterYears;
    Spinner yearsSpinner;


    int startIncomesCategoriesCellY = 18 - 1;
    int startExpensesCategoriesCellY = 33 - 1;
    int startExpensesIncomesCategoriesCellX = 0 - 1; // names of goals
    int startExpensesIncomesGoalsCellX = 1 - 1; // amount of goals
    int startExpensesIncomesCellsX = 4 - 1;
    List <Goal> incomesGoals = new ArrayList<>();
    List <Goal> expensesGoals = new ArrayList<>();
    List <Transaction> expenses = new ArrayList<>();
    List <Transaction> incomes = new ArrayList<>();
    List <CellNumberRecord> cellNumberRecords = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        NavigationView navView = (NavigationView)findViewById(R.id.navigationLayoutReports);
        navView.setNavigationItemSelectedListener(this);

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

        db = FirebaseFirestore.getInstance();

        //activate LoginPrefs
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
    }

    public void requestReport(View view){
        // Pull data from web elements
        file_name_out = month + year + ".xls";
        int monthNumber = new Conversion().convertMonthToInt(month, getResources().getStringArray(R.array.Months));

        if (monthNumber != 0) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Make sure you add a writing memory permission to the app and try again.",
                        Toast.LENGTH_LONG).show();
                requestPermission();
            } else {
                //create a directory
                MyWritableWorkbook mywb = new MyWritableWorkbook();
                int directoryStatus = mywb.checkDirectory(folder); // handle error messages // permissions
                if (directoryStatus != 0) {
                    Toast.makeText(this, "Make sure you add a writing memory permission to the app and try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    // download templete
                    if (downloadTemplete()) {
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
                        }
                    }
                    else{
                        Toast.makeText(this, "Templete hasn't been properly downloaded. Check internet connection and try again" ,
                                Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

    private void populateWorkbook(MyWritableWorkbook mywb, int monthNumber) {
        wb = mywb;
        monthNumberString = "";
        if (monthNumber < 10)
            monthNumberString+="0";
        monthNumberString+=String.valueOf(monthNumber);

        //Pull amount goals categories and values
//        incomesGoals.add(new Goal("Navex", "500", "1"));
//        incomesGoals.add(new Goal("BYUI", "600", "2"));


        //Pull expenses goals categories and values
//        expensesGoals.add(new Goal("Inactive Savings", "700", "1"));
//        expensesGoals.add(new Goal("Education", "800", "2"));

        //Pull incomes of the asked month and year
        db.collection(loginPreferences.getString("email", "") + "/Budget/Income")
                .whereGreaterThanOrEqualTo("date", year + "-" + monthNumberString + "-01")
                .whereLessThan("date", year + "-" + monthNumberString + "-31")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> expenseData;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                expenseData = document.getData();
                                String docID = document.getId();
                                Transaction income = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString(), expenseData.get("id").toString());
                                incomes.add(income);
                            }
//                            for (Transaction b: incomes)
//                            {
//                                b.setCategory(String.valueOf(new Conversion().ConvertCategory(b.getCategory(), getResources().getStringArray(R.array.IncomeCategories))));
//                            }
                            cellNumberRecords.addAll(getRecords(incomes, startExpensesIncomesCellsX, startIncomesCategoriesCellY));
                            db.collection(loginPreferences.getString("email", "") + "/Budget/Expenses")
                                    .whereGreaterThanOrEqualTo("date", year + "-" + monthNumberString + "-01")
                                    .whereLessThan("date", year + "-" + monthNumberString + "-31")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Map<String, Object> expenseData;
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                    expenseData = document.getData();
                                                    Transaction expense = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString(), expenseData.get("id").toString());
                                                    expenses.add(expense);
                                                }
                                                cellNumberRecords.addAll(getRecords(expenses, startExpensesIncomesCellsX, startExpensesCategoriesCellY));
                                                cellNumberRecords.addAll(getGoalRecordsNumber(expensesGoals, startExpensesIncomesGoalsCellX, startExpensesCategoriesCellY));
                                                cellNumberRecords.addAll(getGoalRecordsNumber(incomesGoals, startExpensesIncomesGoalsCellX, startIncomesCategoriesCellY));

                                                List <CellStringRecord> cellStringRecords = new ArrayList<>();
                                                cellStringRecords.addAll(getGoalRecordsString(expensesGoals, startExpensesIncomesCategoriesCellX, startExpensesCategoriesCellY));
                                                cellStringRecords.addAll(getGoalRecordsString(incomesGoals, startExpensesIncomesCategoriesCellX, startIncomesCategoriesCellY));

                                                //Update excel
                                                wb.updateSheet(0, cellNumberRecords, cellStringRecords);
                                                wb.close();
                                                Toast.makeText(getBaseContext(), "File is saved in  internal storage/Simple_Budgeting_files/" + file_name_out + "!",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });


                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    //**********************************
    //*
    //**********************************
    public boolean downloadTemplete()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard.getAbsolutePath() + folder +file_name_inp);
        if(!file.exists()) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://github.com/inessae/Excel-report-tamplete/raw/master/Tamplete.xls"))
                    .setTitle(file_name_inp)// Title of the Download Notification
                    .setDescription("Downloading")// Description of the Download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                    .setDestinationInExternalPublicDir("/Simple_Budgeting_files/", file_name_inp)//
//                .setRequiresCharging(false)// Set if charging is required to begin the download
//                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
//                .setVisibleInDownloadsUi(false);
                    .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);// enqueue puts the download request in the queue.
            }
            catch (Exception e){
                Log.d(TAG, "File wasn't created. Check internet connection");
                return false;
            }
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
                Date date=new SimpleDateFormat("yyyy-MM-dd").parse(ex.date);
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

    public void requestPermission(){
        //request and check permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            }else {

            // No explanation needed; request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//            }
        } else {
            // Permission has already been granted
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutReports);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutReports);
        drawer.closeDrawer(GravityCompat.START);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reports) {
            Intent reportsIntent = new Intent(this, ReportsActivity.class);
            startActivity(reportsIntent);
        } else if (id == R.id.nav_expenses) {
            Intent expensesIntent = new Intent(this, ExpensesActivity.class);
            startActivity(expensesIntent);
        } else if (id == R.id.nav_incomes) {
            Intent incomeIntent = new Intent(this, IncomeActivity.class);
            startActivity(incomeIntent);
        }
        return true;
    }
}

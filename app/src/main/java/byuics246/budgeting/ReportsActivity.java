package byuics246.budgeting;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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


/**
 * Filles in the report activity and deals with its functionality
 *
 * @author Inessa Carroll
 */
public class ReportsActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ReportsActivity";

    /** variables for spinners */
    ArrayAdapter <CharSequence> adapterMonths;
    Spinner monthsSpinner;
    ArrayAdapter <CharSequence> adapterYears;
    Spinner yearsSpinner;

    /** variables for firebase */
    private FirebaseFirestore db;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;


    /** an integer for a permission status */
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;

    /** A workbook object instance */
    MyWritableWorkbook wb;

    /** file path and names */
    String folder = "/Simple_Budgeting_files/";
    String file_name_inp = "Don't Change.xls";
    String file_name_out = "Month.xls";

    /** variables for a month and a year of the report */
    String month;
    String monthNumberString;
    String year;

    /** cell coordinates in the Excel report for different budget data */
    int startIncomesCategoriesCellY = 18 - 1;
    int startExpensesCategoriesCellY = 33 - 1;
    int startExpensesIncomesCategoriesCellX = 0 - 1; // names of goals
    int startExpensesIncomesGoalsCellX = 1 - 1; // amount of goals
    int startExpensesIncomesCellsX = 4 - 1;

    /** lists of budget data */
    List <Goal> incomesGoals = new ArrayList<>();
    List <Goal> expensesGoals = new ArrayList<>();
    List <Transaction> expenses = new ArrayList<>();
    List <Transaction> incomes = new ArrayList<>();
    List <CellRecord <Double>> cellNumberRecords = new ArrayList<>();

    /**
     * Creates a view and initializes variables
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** populate the view */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        /** activate navigation */
        NavigationView navView = (NavigationView)findViewById(R.id.navigationLayoutReports);
        navView.setNavigationItemSelectedListener(this);

        /** activate spinners */
        monthsSpinner = findViewById(R.id.spinnerReportsMonths);
        adapterMonths = ArrayAdapter.createFromResource(this, R.array.Months,
                android.R.layout.simple_spinner_item);
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthsSpinner.setAdapter(adapterMonths);
        monthsSpinner.setOnItemSelectedListener(this);

        yearsSpinner = findViewById(R.id.spinnerReportsYears);
        adapterYears = ArrayAdapter.createFromResource(this, R.array.Years,
                android.R.layout.simple_spinner_item);
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearsSpinner.setAdapter(adapterYears);
        yearsSpinner.setOnItemSelectedListener(this);

        /** activate database */
        db = FirebaseFirestore.getInstance();

        /** activate shared prefferences */
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
    }

    /**
     * Leads the reports creation process
     * <p>
     *  Activated by clicking "Request a Report" button
     *  </p>
     *
     * @param view
     */
    public void requestReport(View view){
        /** create the right report file name*/
        file_name_out = month + year + ".xls";

        /** check if month number is acceptable */
        int monthNumber = new Conversion().convertMonthToInt(month, getResources().
                getStringArray(R.array.Months));
        if (monthNumber != 0) {
            /** check if memory writing permission is granted if not - request*/
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED){
                //memory writing permission hasn't been granted granted - request
                new AlertDialog.Builder(ReportsActivity.this)
                        .setTitle("Permissions error")
                        .setMessage("Please make sure you add a writing memory permission to the " +
                                "app and try again.")
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermission();
                            }
                        }).setNegativeButton("", null).show();
//                requestPermission();
            } else {
                /** check if app directory has been created*/
                wb = new MyWritableWorkbook();
                int directoryStatus = wb.checkDirectory(folder);
                if (directoryStatus != 0) {
                    //directory hasn't been created
                    new AlertDialog.Builder(ReportsActivity.this)
                            .setTitle("Permissions error")
                            .setMessage("An error in creating a directory. Try again later.")
                            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setNegativeButton("", null).show();
                }
                else {
                    /** call the download the report templete function and check success*/
                    if (!downloadTemplete()) {
                        //error downloading the templete
                        new AlertDialog.Builder(ReportsActivity.this)
                                .setTitle("Error")
                                .setMessage("Templete hasn't been properly downloaded. Check " +
                                        "internet connection and try again.")
                                .setPositiveButton("Close", new DialogInterface.
                                        OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).setNegativeButton("", null).show();
                    }
                    else{
                        /** copy the templete*/
                        int creationResult = wb.createCopyWorkbook(file_name_inp, file_name_out);
                        if (creationResult == 1) {
                            // error creating a copy
                            new AlertDialog.Builder(ReportsActivity.this)
                                    .setTitle("Access error")
                                    .setMessage("Make sure that excel reports in the app folder " +
                                            "are closed and try again.")
                                    .setPositiveButton("Close", new DialogInterface.
                                            OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).setNegativeButton("", null).show();
                        } else if (creationResult == 2) {
                            //error accessing the templete
                            new AlertDialog.Builder(ReportsActivity.this)
                                    .setTitle("Downloading error")
                                    .setMessage("The templete is having issues downloading. " +
                                            "Try again later.")
                                    .setPositiveButton("Close", new DialogInterface.
                                            OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).setNegativeButton("", null).show();
                        } else {
                            /** call pupulateWorkbook function*/
                            populateWorkbook(monthNumber);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates cell records according to busget data for the selected month and populates the report
     *
     * @param monthNumber
     */
    private void populateWorkbook(int monthNumber) {
        monthNumberString = new Conversion().convertMonthIntToNumberString(monthNumber);

        //Pull amount goals categories and values
//        incomesGoals.add(new Goal("Navex", "500", "1"));
//        incomesGoals.add(new Goal("BYUI", "600", "2"));


        //Pull expenses goals categories and values
//        expensesGoals.add(new Goal("Inactive Savings", "700", "1"));
//        expensesGoals.add(new Goal("Education", "800", "2"));

        /** Pull incomes of the asked month and year */
        db.collection(loginPreferences.getString("email", "")
                + "/Budget/Income")
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
                                Transaction income = new Transaction(expenseData.get("date").
                                        toString(), expenseData.get("user").toString(), expenseData
                                        .get("category").toString(), expenseData.get("amount")
                                        .toString(), expenseData.get("description").toString(),
                                        expenseData.get("id").toString());
                                incomes.add(income);
                            }
//                            for (Transaction b: incomes)
//                            {
//                                b.setCategory(String.valueOf(new Conversion().ConvertCategory
//                                  (b.getCategory(), getResources().getStringArray
//                                  (R.array.IncomeCategories))));
//                            }
                            /** add all incomes to cell numertic records*/
                            cellNumberRecords.addAll(getTransactionRecords(incomes,
                                    startExpensesIncomesCellsX, startIncomesCategoriesCellY));

                            /** Pull expenses of the asked month and year */
                            db.collection(loginPreferences.getString("email",
                                    "") + "/Budget/Expenses")
                                    .whereGreaterThanOrEqualTo("date", year + "-" +
                                            monthNumberString + "-01")
                                    .whereLessThan("date", year + "-" + monthNumberString
                                            + "-31")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            Map<String, Object> expenseData;
                                            if (task.isSuccessful()) {
                                                for (QueryDocumentSnapshot document :
                                                        task.getResult()) {
                                                    expenseData = document.getData();
                                                    Transaction expense = new Transaction
                                                            (expenseData.get("date").toString(),
                                                                    expenseData.get("user")
                                                                            .toString(), expenseData
                                                                    .get("category").toString(),
                                                                    expenseData.get("amount")
                                                                            .toString(), expenseData
                                                                    .get("description").toString(),
                                                                    expenseData.get("id")
                                                                            .toString());
                                                    expenses.add(expense);
                                                }
                                                /** add all incomes to cell numertic records*/
                                                cellNumberRecords.addAll(getTransactionRecords
                                                        (expenses, startExpensesIncomesCellsX,
                                                                startExpensesCategoriesCellY));
//                                                cellNumberRecords.addAll(getGoalRecordsNumber
//                                                  (expensesGoals, startExpensesIncomesGoalsCellX,
//                                                   startExpensesCategoriesCellY));
//                                                cellNumberRecords.addAll(getGoalRecordsNumber
//                                                  (incomesGoals, startExpensesIncomesGoalsCellX,
//                                                   startIncomesCategoriesCellY));

                                                List <CellRecord <String>> cellStringRecords =
                                                        new ArrayList<>();
//                                                cellStringRecords.addAll(getGoalRecordsString
//                                                  (expensesGoals,
//                                                   startExpensesIncomesCategoriesCellX,
//                                                   startExpensesCategoriesCellY));
//                                                cellStringRecords.addAll(getGoalRecordsString
//                                                  (incomesGoals,
//                                                   startExpensesIncomesCategoriesCellX,
//                                                   startIncomesCategoriesCellY));

                                                /** write all cell records in the excel, close it
                                                 * and show a success message*/
                                                wb.updateSheet(0, cellNumberRecords,
                                                        cellStringRecords);
                                                wb.close();
                                                Toast.makeText(getBaseContext(), "File is " +
                                                                "saved in  internal " +
                                                                "storage/Simple_Budgeting_files/"
                                                                + file_name_out + "!",
                                                        Toast.LENGTH_LONG).show();
                                            } else {
                                                // reading expenses failed
                                                Log.d(TAG, "Error getting documents: ",
                                                        task.getException());
                                            }
                                        }
                                    });


                        } else {
                            //reading incomes failed
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    /**
     * Downloads the report templete if it isn't downloaded yet
     *
     * @return success of downloading
     */
    public boolean downloadTemplete()
    {
        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard.getAbsolutePath() + folder +file_name_inp);
        /** check if the file with the templete name already exist in the app directory*/
        if(!file.exists()) {
            /** download the templete*/
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(
                        "https://github.com/inessae/Excel-report-tamplete/raw/master/Tamplete.xls"))
                    .setTitle(file_name_inp)// Title of the Download Notification
                    .setDescription("Downloading")// Description of the Download Notification
                        // Visibility of the download Notification
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationInExternalPublicDir("/Simple_Budgeting_files/",
                            file_name_inp)//
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

    /**
     * Processes an option chosen from spinners
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.spinnerReportsMonths:
                /** for the month spinner*/
                month = parent.getItemAtPosition(position).toString();
                break;
            case R.id.spinnerReportsYears:
                /** for the year spinner*/
                year = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    /**
     * Processes spinners if no spinner option is selected
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * based on date and category of a transaction, creates a record with the cell
     * coordinates and amount
     *
     * @param transactions
     * @param startCellsX
     * @param startCellsY
     * @return
     */
    private List <CellRecord <Double>> getTransactionRecords(List <Transaction> transactions,
                                                             int startCellsX, int startCellsY)
    {
        List <CellRecord <Double>> cellNumberRecords = new ArrayList<>();
        for (Transaction ex : transactions) {
            try {
                Date date=new SimpleDateFormat("yyyy-MM-dd").parse(ex.date);
                int day = date.getDate();
                int category = Integer.parseInt(ex.getCategory());
                double amount = Double.parseDouble(ex.getAmount());
                // if there is a record for the same day for the same category, update its value
                // to its original value + amount
                boolean found = false;
                for (CellRecord <Double> cr : cellNumberRecords){
                    if (cr.getColumn() == day + startCellsX && cr.getRow() == category +
                            startCellsY){
                        found = true;
                        cr.setValue(amount + cr.getValue());
                        break;
                    }
                }
                //if there are no records for the same day for the same category create a new record
                if (!found) {
                    CellRecord < Double> cellNumberRecord = new CellRecord < Double>(day +
                            startCellsX, category + startCellsY, amount);
                    cellNumberRecords.add(cellNumberRecord);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return cellNumberRecords;
    }

    /**
     * creates a cell string record of a list of goals
     * @param goals
     * @param startCellsX
     * @param startCellsY
     * @return
     */
    private List <CellRecord <String>> getGoalRecordsString(List <Goal> goals, int startCellsX,
                                                            int startCellsY)
    {
        List <CellRecord <String>> cellStringRecords = new ArrayList<>();
        for (Goal g : goals) {
            String value = g.getName();
            int id = Integer.parseInt(g.getId());
            CellRecord <String> cellStringRecord = new CellRecord<String>(startCellsX + 1,
                    id + startCellsY, value);
            cellStringRecords.add(cellStringRecord);

        }
        return cellStringRecords;
    }

    /**
     * created a list of number records of goals
     * @param goals
     * @param startCellsX
     * @param startCellsY
     * @return
     */
    private List <CellRecord <Double>> getGoalRecordsNumber(List <Goal> goals, int startCellsX,
                                                            int startCellsY) {
        List<CellRecord <Double>> cellNumberRecords = new ArrayList<>();
        for (Goal g : goals) {
            Double value = Double.parseDouble(g.getAmount());
            int id = Integer.parseInt(g.getId());
            CellRecord <Double> cellNumberRecord = new CellRecord <Double>(startCellsX + 1,
                    id + startCellsY, value);
            cellNumberRecords.add(cellNumberRecord);
        }
        return cellNumberRecords;
    }

    /**
     * requests memory permission from the user
     */
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


    /**
     * closes navigation panel if user clicks outside of it
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutReports);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * navigates to different pages
     * @param item
     * @return
     */
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

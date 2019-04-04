package byuics246.budgeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class IncomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "Income";

    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    ListView listView;
    ArrayList <Transaction> listIncome;

    //for add new expense
    private EditText newDate;
    private EditText newAmount;
    private EditText newDescription;
    String source;

    //for category spinner
    boolean hasSource = false;
    ArrayAdapter <CharSequence> adapterCategories;
    Spinner categoriesSpinner;
    ThreeColumnsAdapter IncomeHistoryAdapter;

    //Firebase DB
    private FirebaseFirestore db;

    //Login preferences
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Entered Function");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        //activate FireStore
        db = FirebaseFirestore.getInstance();

        //activate LoginPrefs
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        //activate a history list view
        listView = (ListView) findViewById(R.id.listViewIncomeHistory);
        listIncome = new ArrayList<>();

        //get data from DB and add it to the viewer.
        db.collection(loginPreferences.getString("email", "") + "/Budget/Income")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> expenseData;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                expenseData = document.getData();
                                Transaction income = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString());
                                listIncome.add(income);
                            }
                            IncomeHistoryAdapter = new ThreeColumnsAdapter(IncomeActivity.this, R.layout.three_columns_history_layout, listIncome);
                            listView.setAdapter(IncomeHistoryAdapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //activate add new Income fields
        newDate = findViewById(R.id.editTextIncomeNewDate);
        newAmount = findViewById(R.id.editTextIncomeNewAmount);
        newDescription = findViewById(R.id.editTextIncomeNEwDescription);
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
        String today = format1.format(Calendar.getInstance().getTime());
        newDate.setText(today);

        //activate a category spinner
        categoriesSpinner = findViewById(R.id.spinnerIncomeNewCategories);
        adapterCategories = ArrayAdapter.createFromResource(this, R.array.IncomeCategories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapterCategories);
        categoriesSpinner.setOnItemSelectedListener(this);

        //Onclick for Navigation buttons
        //// onclick assignment handled in XML of layout
    }


    //******************************************************************************************************************************
    //UI functions
    //******************************************************************************************************************************
//    public void openIncomePage(View v) {
//        Intent incomeIntent = new Intent(this, IncomeActivity.class);
//        startActivity(incomeIntent);
//    }

//    public void openGoalsPage(View v) {
//        Intent goalsIntent = new Intent(this, GoalsActivity.class);
//        startActivity(goalsIntent);
//    }

    public void openAddNewIncomeWindow(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.AddNewIncomeLayout);
        expandableRelativeLayout.toggle();
    }

    public void showHistory(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.ShowHistoryIncomeLayout);
        expandableRelativeLayout.toggle();
    }

    public void addToList(View view) {

        if (validateForm()) {
            String dateToAdd = new Conversion().reformatDateForDB(newDate.getText().toString());
            Transaction incomeTransaction = new Transaction(dateToAdd, loginPreferences.getString("name", ""), String.valueOf(new Conversion().ConvertCategory(source, getResources().getStringArray(R.array.IncomeCategories))), newAmount.getText().toString(),newDescription.getText().toString());////////////////////
            IncomeHistoryAdapter.add(incomeTransaction);
            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Income").add(incomeTransaction);
            Toast.makeText(this, "The income has been added" ,
                    Toast.LENGTH_LONG).show();
        }
    }


    //***********************************************************************************
    //Spinner functions
    //***********************************************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        source = parent.getItemAtPosition(position).toString();
        if (source.equals("Source")) {
            hasSource = false;
        }
        else {
            hasSource = true;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        hasSource = false;
    }

    //***********************************************************************************
    //Functions
    //***********************************************************************************

    private boolean validateForm() {
        boolean valid = true;

        //Check if Amount is empty
        String amount = newAmount.getText().toString();
        if (TextUtils.isEmpty(amount)) {
            newAmount.setError("Required.");
            valid = false;
        } else {
            newAmount.setError(null);
        }

        //Check if Category is empty
        if (!hasSource) {
            new AlertDialog.Builder(IncomeActivity.this)
                    .setTitle("Source Required")
                    .setMessage("Please choose a source from the list.")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setNegativeButton("", null).show();
            valid = false;
        } else {
        }

        return valid;
    }

    private String generateCurrency(Double number) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        return format.format(number);
    }

    public void reportsPage(View view) {
        Intent reportsIntent = new Intent(this, ReportsActivity.class);
        startActivity(reportsIntent);
    }
}



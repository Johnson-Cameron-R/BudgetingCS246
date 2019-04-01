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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

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

//AdapterView.OnItemSelectedListener
public class ExpensesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "Expenses";

    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    ListView listView;
    ArrayList <Transaction> listExpenses;

    //for add new expense
    private EditText newDate;
    private EditText newAmount;
    private EditText newDescription;
    String category;

    //for category spinner
    boolean hasCategory = false;
    ArrayAdapter <CharSequence> adapterCategories;
    Spinner categoriesSpinner;
    ThreeColumnsAdapter expensesHistoryAdapter;

    //Firebase DB
    private FirebaseFirestore db;

    //Login preferences
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        //activate FireStore
        db = FirebaseFirestore.getInstance();

        //activate LoginPrefs
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        //activate a history list view
        listView = (ListView) findViewById(R.id.listViewExpensesHistory);
        listExpenses = new ArrayList<>();

        //get data from DB and add it to the viewer.
        db.collection(loginPreferences.getString("email", "") + "/Budget/Expenses")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> expenseData;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                expenseData = document.getData();
                                Transaction expense = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString());
                                listExpenses.add(expense);
                            }
                            expensesHistoryAdapter = new ThreeColumnsAdapter(ExpensesActivity.this, R.layout.three_columns_history_layout, listExpenses);
                            listView.setAdapter(expensesHistoryAdapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //activate add new expense fields
        newDate = findViewById(R.id.editTextExpensesNewDate);
        newAmount = findViewById(R.id.editTextExpensesNewAmount);
        newDescription = findViewById(R.id.editTextExpensesNEwDescription);
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
        String today = format1.format(Calendar.getInstance().getTime());
        newDate.setText(today);

        //activate a category spinner
        categoriesSpinner = findViewById(R.id.spinnerExpensesNewCategories);
        adapterCategories = ArrayAdapter.createFromResource(this, R.array.Categories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapterCategories);
        categoriesSpinner.setOnItemSelectedListener(this);

        //Onclick for Navigation buttons
        ////Handled by Layout XML
    }


    //******************************************************************************************************************************
    //UI functions
    //******************************************************************************************************************************
    public void openIncomePage(View view) {
        Log.d(TAG, "openIncomePage: In function");
        Intent incomeIntent = new Intent(getApplicationContext(), IncomeActivity.class);
        startActivity(incomeIntent);
        finish();
    }

    public void openGoalsPage(View view) {
        Intent goalsIntent = new Intent(this, MainActivity.class);
        startActivity(goalsIntent);
        finish();
    }

    public void openAddNewExpenseWindow(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.AddNewExpenseLayout);
        expandableRelativeLayout.toggle();
    }

    public void showHistory(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.ShowHistoryExpenseLayout);
        expandableRelativeLayout.toggle();
    }

    public void addToList(View view) {

        if (validateForm()) {
            Transaction example2 = new Transaction(newDate.getText().toString(), loginPreferences.getString("name", ""), category, generateCurrency(Double.valueOf(newAmount.getText().toString())),newDescription.getText().toString());////////////////////
            expensesHistoryAdapter.add(example2);
            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Expenses").add(example2);
        }
    }


    //***********************************************************************************
    //Spinner functions
    //***********************************************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
        if (category.equals("Category")) {
            hasCategory = false;
            Log.d(TAG, "onItemSelected: hasCategory is False");
        }
        else {
            hasCategory = true;
            Log.d(TAG, "onItemSelected: hasCategory is True");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        hasCategory = false;
        Log.d(TAG, "onNothingSelected: HasCategory is False");
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
        if (!hasCategory) {
            new AlertDialog.Builder(ExpensesActivity.this)
                    .setTitle("Category Required")
                    .setMessage("Please choose a category from the list.")
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



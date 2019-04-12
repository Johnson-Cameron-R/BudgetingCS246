package byuics246.budgeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.util.Attributes;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * Handles all activities on the Income page
 *
 * @author Cameron Johnson and Inessa Carroll
 */
public class IncomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        NavigationView.OnNavigationItemSelectedListener  {
    private static final String TAG = "Income";

    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    SwipeRecyclerViewAdapter mAdapter;
    private TextView tvEmptyTextView;
    private RecyclerView mRecyclerView;
    ListView listView;
    ArrayList <Transaction> listTransactions;
    int indexItemToDelete;
    Transaction toDelete;
    Transaction transactionToAdd;

    //for add new transaction
    private EditText newDate;
    private EditText newAmount;
    private EditText newDescription;
    String category;

    //for category spinner
    ArrayAdapter <CharSequence> adapterCategories;
    Spinner categoriesSpinner;

    //Firebase DB
    private FirebaseFirestore db;

    //Login preferences
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;


    /**
     * Activates main functionality and populates the view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /** creates the view*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        /**creates navigation*/
        NavigationView navView = (NavigationView)findViewById(R.id.navigationLayoutIncomes);
        navView.setNavigationItemSelectedListener(this);

        /**activates FireStore */
        db = FirebaseFirestore.getInstance();

        /**activates LoginPrefs*/
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();


        /**activate add new transaction fields*/
        newDate = findViewById(R.id.editTextIncomesNewDate);
        newAmount = findViewById(R.id.editTextIncomesNewAmount);
        newDescription = findViewById(R.id.editTextIncomesNewDescription);
        SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
        String today = format1.format(Calendar.getInstance().getTime());
        newDate.setText(today);

        /**activate a category spinner*/
        categoriesSpinner = findViewById(R.id.spinnerIncomesNewCategories);
        adapterCategories = ArrayAdapter.createFromResource(this, R.array.IncomeCategories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapterCategories);
        categoriesSpinner.setOnItemSelectedListener(this);

        /**activate and populate the history listView*/
        tvEmptyTextView = (TextView) findViewById(R.id.empty_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        listTransactions = new ArrayList<>();
        loadData();
    }


    /**
     * loads data from db to views
     */
    public void loadData() {

        /**get data from DB and add it to the viewer*/
        db.collection(loginPreferences.getString("email", "")
                + "/Budget/"+TAG)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Object> transactionData;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                transactionData = document.getData();
                                Transaction transaction = new Transaction(transactionData.get("date")
                                        .toString(), transactionData.get("user").toString(), transactionData
                                        .get("category").toString(),transactionData.get("amount")
                                        .toString(), transactionData.get("description").toString(),
                                        transactionData.get("id").toString());
                                listTransactions.add(transaction);
                            }
                            if(listTransactions.isEmpty()){
                                mRecyclerView.setVisibility(View.GONE);
                                tvEmptyTextView.setVisibility(View.VISIBLE);
                            }else{
                                mRecyclerView.setVisibility(View.VISIBLE);
                                tvEmptyTextView.setVisibility(View.GONE);
                            }

                            mAdapter = new SwipeRecyclerViewAdapter(IncomeActivity.this, listTransactions);

                            ((SwipeRecyclerViewAdapter) mAdapter).setMode(Attributes.Mode.Single);

                            mRecyclerView.setAdapter(mAdapter);

                            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    Log.e("RecyclerView", "onScrollStateChanged");
                                }
                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);
                                }
                            });

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }




    /**
     * closes or opens the expandable window for adding transaction
     * @param view
     */
    public void openAddNewIncomeWindow(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.AddNewIncomeLayout);
        expandableRelativeLayout.toggle();
    }

    /** closes or opens the expandable window for viewing history*/
    public void showHistory(View view) {
        expandableRelativeLayout = (ExpandableRelativeLayout) findViewById(R.id.ShowHistoryIncomeLayout);
        expandableRelativeLayout.toggle();
    }


    /**adds an transaction to the histoty list and db*/
    public void addToList(View view) {

        if (validateForm()) {
//            additionCompleted = false;
            String dateToAdd = new Conversion().reformatDateForDB(newDate.getText().toString());
            transactionToAdd = new Transaction(dateToAdd, loginPreferences.getString("name", ""), String.valueOf(new Conversion().ConvertCategory(category, getResources().getStringArray(R.array.IncomeCategories))), newAmount.getText().toString(),newDescription.getText().toString(), "tempString");
            db.collection(loginPreferences.getString("email", "")).document("Budget").collection(TAG)
                    .add(transactionToAdd)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            transactionToAdd.setId(documentReference.getId());
                            Log.d(TAG, "onSuccess: ID = " + transactionToAdd.getId());
                            db.collection(loginPreferences.getString("email", "")).document("Budget").collection(TAG).document(documentReference.getId())
                                    .set(transactionToAdd);
                            listTransactions.add(transactionToAdd);

                            if(listTransactions.isEmpty()){
                                mRecyclerView.setVisibility(View.GONE);
                                tvEmptyTextView.setVisibility(View.VISIBLE);
                            }else{
                                mRecyclerView.setVisibility(View.VISIBLE);
                                tvEmptyTextView.setVisibility(View.GONE);
                            }
                            mAdapter = new SwipeRecyclerViewAdapter(IncomeActivity.this, listTransactions);

                            ((SwipeRecyclerViewAdapter) mAdapter).setMode(Attributes.Mode.Single);

                            mRecyclerView.setAdapter(mAdapter);

                            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    Log.e("RecyclerView", "onScrollStateChanged");
                                }
                                @Override
                                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error writing documents: ", e);
                        }
                    });
            Toast.makeText(this, "The transaction has been added" ,
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * handles a cetegoty spinner if an item is selected
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
    }


    /**
     * handles a category spinner if an item isn't selected
     *
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected: HasCategory is False");
    }


    /**
     * validates the data provided for creating a new transaction
     * @return
     */
    private boolean validateForm() {
        boolean valid = true;
        String date = newDate.getText().toString();
        String amount = newAmount.getText().toString();

        String errorName = "";
        String errorText = "";
        int validationCode = new Utilities().validateNewTransaction(date, category, amount);
        switch (validationCode){
            case 1:
                valid = false;
                errorName = "Wrong month number";
                errorText = "Month must be less than 12";
                break;
            case 2:
                valid = false;
                errorName = "Wrong day number";
                errorText = "This day doesn't exist in the chosen month";
                break;
            case 3:
                valid = false;
                newAmount.setError("Required.");
                break;
            case 4:
                valid = false;
                errorName = "Category required";
                errorText = "Please choose a category from the list.";
                break;
            default:
                break;
        }
        if (!errorName.equals("")){
            new AlertDialog.Builder(IncomeActivity.this)
                    .setTitle(errorName)
                    .setMessage(errorText)
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setNegativeButton("", null).show();
        }
        return valid;
    }



    /**
     * close navigation if clicked outside of it
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutIncomes);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /**
     * handles navigation buttons to direct the user to different pages
     * @param item
     * @return
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutIncomes);
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
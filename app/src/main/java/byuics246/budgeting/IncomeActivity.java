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
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
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
 * Handles all activities on the Expence page
 *
 * @author Cameron Johnson and Inessa Carroll
 */
public class IncomeActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener  {
    private static final String TAG = "Income";

    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    SwipeMenuListView listView;
    ArrayList <Transaction> listIncomes;
    int indexItemToDelete;
    Transaction toDelete;

    //for add new expense
    private EditText newDate;
    private EditText newAmount;
    private EditText newDescription;
    String source;

    //for category spinner
    boolean hasSource = false;
    ArrayAdapter <CharSequence> adapterCategories;
    Spinner categoriesSpinner;
    ThreeColumnsAdapter incomeHistoryAdapter;

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

        NavigationView navView = (NavigationView)findViewById(R.id.navigationLayoutIncomes);
        navView.setNavigationItemSelectedListener(this);

        //activate FireStore
        db = FirebaseFirestore.getInstance();

        //activate LoginPrefs
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        //activate a history list view
        listView = (SwipeMenuListView) findViewById(R.id.listViewIncomeHistory);
        listIncomes = new ArrayList<>();

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
                                Transaction income = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString(), expenseData.get("id").toString());
                                listIncomes.add(income);
                            }
                            incomeHistoryAdapter = new ThreeColumnsAdapter(IncomeActivity.this, R.layout.three_columns_history_layout, listIncomes);
                            listView.setAdapter(incomeHistoryAdapter);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //allow for deletion of list items
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(170);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);

//                // create "open" item
//                SwipeMenuItem openItem = new SwipeMenuItem(
//                        getApplicationContext());
//                // set item background
//                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
//                        0xCE)));
//                // set item width
//                openItem.setWidth(170);
//                // set item title
//                openItem.setTitle("Open");
//                // set item title fontsize
//                openItem.setTitleSize(18);
//                // set item title font color
//                openItem.setTitleColor(Color.WHITE);
//                // add to menu
//                menu.addMenuItem(openItem);
            }
        };
        // set creator
        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                indexItemToDelete = position;
                switch (index) {
                    case 0:
                        // delete
                        toDelete = listIncomes.get(position);
                        db.collection(loginPreferences.getString("email", "") + "/Budget/Income").document(toDelete.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        listIncomes.remove(indexItemToDelete);
                                        incomeHistoryAdapter = new ThreeColumnsAdapter(IncomeActivity.this, R.layout.three_columns_history_layout, listIncomes);
                                        listView.setAdapter(incomeHistoryAdapter);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
                        break;
//                    case 1:
//                        // open
//                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
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
            final Transaction income = new Transaction(dateToAdd, loginPreferences.getString("name", ""), String.valueOf(new Conversion().ConvertCategory(source, getResources().getStringArray(R.array.IncomeCategories))), newAmount.getText().toString(),newDescription.getText().toString(), new Utilities().randomString());////////////////////

            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Income")
                    .add(income)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            income.setId(documentReference.getId());
                            Log.d(TAG, "onSuccess: ID = " + income.getId());
                            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Income").document(documentReference.getId())
                                    .set(income);
                            listIncomes.add(income);
                            incomeHistoryAdapter = new ThreeColumnsAdapter(IncomeActivity.this, R.layout.three_columns_history_layout, listIncomes);
                            listView.setAdapter(incomeHistoryAdapter);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error writing documents: ", e);
                        }
                    });

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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutIncomes);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



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



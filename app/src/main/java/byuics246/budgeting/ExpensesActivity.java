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
 * @author Cody Cornelison and Inessa Carroll
 */
public class ExpensesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, NavigationView.OnNavigationItemSelectedListener  {
    private static final String TAG = "Expenses";

    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    SwipeMenuListView listView;
    ArrayList <Transaction> listExpenses;
    int indexItemToDelete;
    Transaction toDelete;
    Transaction expenseToAdd;

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

        NavigationView navView = (NavigationView)findViewById(R.id.navigationLayoutExpenses);
        navView.setNavigationItemSelectedListener(this);

        //activate FireStore
        db = FirebaseFirestore.getInstance();

        //activate LoginPrefs
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        //activate a history list view
        listView = (SwipeMenuListView) findViewById(R.id.listViewExpensesHistory);
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
                                Transaction expense = new Transaction(expenseData.get("date").toString(), expenseData.get("user").toString(), expenseData.get("category").toString(), expenseData.get("amount").toString(), expenseData.get("description").toString(), expenseData.get("id").toString());
                                listExpenses.add(expense);
                            }
                            expensesHistoryAdapter = new ThreeColumnsAdapter(ExpensesActivity.this, R.layout.three_columns_history_layout, listExpenses);
                            listView.setAdapter(expensesHistoryAdapter);
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
                        toDelete = listExpenses.get(position);
                        db.collection(loginPreferences.getString("email", "") + "/Budget/Expenses")
                                .whereEqualTo("id", toDelete.getId())
                                .orderBy("date", Query.Direction.DESCENDING)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        Map<String, Object> expenseData;
                                        if (task.isSuccessful()) {
                                            int numOfRecords = 0;
                                            String docName = "";
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                numOfRecords++;
                                                expenseData = document.getData();
                                                if (numOfRecords <=1) {
                                                    docName = document.getId();
                                                    String date = expenseData.get("date").toString();
                                                }
                                                else {
                                                    Log.d(TAG, "Multiple similar records: ");
                                                    break;
                                                }
                                            }
                                            if (numOfRecords == 1)
                                            {
                                                db.collection(loginPreferences.getString("email", "") + "/Budget/Expenses")
                                                        .document(docName).delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                                                listExpenses.remove(indexItemToDelete);
                                                                expensesHistoryAdapter = new ThreeColumnsAdapter(ExpensesActivity.this, R.layout.three_columns_history_layout, listExpenses);
                                                                listView.setAdapter(expensesHistoryAdapter);
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "Error deleting document", e);
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                })

                        ;
                        break;
//                    case 1:
//                        // open
//                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
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
        adapterCategories = ArrayAdapter.createFromResource(this, R.array.ExpensesCategories, android.R.layout.simple_spinner_item);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoriesSpinner.setAdapter(adapterCategories);
        categoriesSpinner.setOnItemSelectedListener(this);

        //Onclick for Navigation buttons
        ////Handled by Layout XML
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
//            additionCompleted = false;
            String dateToAdd = new Conversion().reformatDateForDB(newDate.getText().toString());
            expenseToAdd = new Transaction(dateToAdd, loginPreferences.getString("name", ""), String.valueOf(new Conversion().ConvertCategory(category, getResources().getStringArray(R.array.ExpensesCategories))), newAmount.getText().toString(),newDescription.getText().toString(), "tempString");
            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Expenses")
                    .add(expenseToAdd)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            expenseToAdd.setId(documentReference.getId());
                            Log.d(TAG, "onSuccess: ID = " + expenseToAdd.getId());
                            db.collection(loginPreferences.getString("email", "")).document("Budget").collection("Expenses").document(documentReference.getId())
                                    .set(expenseToAdd);
                            listExpenses.add(expenseToAdd);
                            expensesHistoryAdapter = new ThreeColumnsAdapter(ExpensesActivity.this, R.layout.three_columns_history_layout, listExpenses);
                            listView.setAdapter(expensesHistoryAdapter);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error writing documents: ", e);
                        }
                    });
            Toast.makeText(this, "The expense has been added" ,
                    Toast.LENGTH_LONG).show();
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutExpenses);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayoutExpenses);
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



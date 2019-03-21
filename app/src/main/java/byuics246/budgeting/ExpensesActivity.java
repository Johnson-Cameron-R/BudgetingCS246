package byuics246.budgeting;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.lang.reflect.Array;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ExpensesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    ListView listView;
    ArrayList <Expense> listExpenses;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        //activate a history list view
        listView = (ListView) findViewById(R.id.listViewExpensesHistory);
        listExpenses = new ArrayList<>();
        Expense example = new Expense("03/05/2019", "Inessa", "food", "100.00");//////////////////////////////////
//        Expense example2 = new Expense("03/05/2019", "Inessa", "food", "100.00");/////////////////////////////////////////////////////////
//        listExpenses.add(example2);
        listExpenses.add(example);
        expensesHistoryAdapter = new ThreeColumnsAdapter(this, R.layout.three_columns_history_layout, listExpenses);
        listView.setAdapter(expensesHistoryAdapter);
        expensesHistoryAdapter.add(example);//////////////////////////////////////////////////////////////////////////////////////////////

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
    }


    //******************************************************************************************************************************
    //UI functions
    //******************************************************************************************************************************
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
            Expense example2 = new Expense(newDate.getText().toString(), "Inessa", category, generateCurrency(Double.valueOf(newAmount.getText().toString())));////////////////////
            example2.setDescription(newDescription.getText().toString());
            expensesHistoryAdapter.add(example2);
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
        }
        else {
            hasCategory = true;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        hasCategory = false;
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

}



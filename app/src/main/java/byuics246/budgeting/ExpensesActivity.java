package byuics246.budgeting;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

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
//        Expense example2 = new Expense("03/05/2019", "Inessa", "food", "100.00");//////////////////////////////////
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


        Expense example2 = new Expense(newDate.getText().toString(), "Inessa", category, newAmount.getText().toString());
        example2.setDescription(newDescription.getText().toString());
        expensesHistoryAdapter.add(example2);


    }


    //***********************************************************************************
    //Spinner functions
    //***********************************************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        category = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

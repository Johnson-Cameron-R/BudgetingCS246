package byuics246.budgeting;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.util.ArrayList;

public class ExpensesActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ExpandableRelativeLayout expandableRelativeLayout;

    //for history listView
    ListView listView;
    ArrayList <Expense> listExpenses;

    //for category spinner
    ArrayAdapter <CharSequence> adapterCategories;
    Spinner categoriesSpinner;
    ThreeColumnsAdapter expensesHistoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);



        //activate a history list view
        listView = (ListView) findViewById(R.id.listViewExpensesHistory);////////////////
        listExpenses = new ArrayList<>();
        Expense example = new Expense("03/05/2019", "Inessa", "food", 100.00);
        Expense example2 = new Expense("04/05/2019", "Inessa", "food", 100.00);
        listExpenses.add(example);
        listExpenses.add(example2);
        expensesHistoryAdapter = new ThreeColumnsAdapter(this, R.layout.three_columns_history_layout, listExpenses);
        listView.setAdapter(expensesHistoryAdapter);

//        Expense example = new Expense("03/05/2019", "Inessa", "food", 100.00);
//        Expense example2 = new Expense("04/05/2019", "Inessa", "food", 100.00);
//        listExpenses.add(example);
//        listExpenses.add(example2;
//
//        Expense example2 = new Expense("04/05/2019", "Inessa", "food", 100.00);
//        expensesHistoryAdapter.add(example2);

//        Expense example2 = new Expense("04/05/2019", "Inessa", "food", 100.00);
//        listExpenses.add(example2);
//        expensesHistoryAdapter = new ThreeColumnsAdapter(this, R.layout.three_columns_history_layout, listExpenses);
//        listView.setAdapter(expensesHistoryAdapter);

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

        Expense example2 = new Expense("04/05/2019", "Inessa", "food", 100.00);
        listExpenses.add(example2);
//        expensesHistoryAdapter.notifyDataSetChanged();
//        expensesHistoryAdapter = new ThreeColumnsAdapter(this, R.layout.three_columns_history_layout, listExpenses);
//        listView.setAdapter(expensesHistoryAdapter);
//        expensesHistoryAdapter.add(example);

    }


    //***********************************************************************************
    //Spinner functions
    //***********************************************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

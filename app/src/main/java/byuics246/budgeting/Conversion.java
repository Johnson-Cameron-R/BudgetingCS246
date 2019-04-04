package byuics246.budgeting;

import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Conversion {
    private static final String TAG = "Convertion";
    public Conversion() {}


    public int ConvertCategory(String category, String[] categories){
        int categoryInt = 0;
        for (String s : categories){
            if (category.equals(s))
                return categoryInt;
            categoryInt++;
        }
        Log.d(TAG, "Wrong category value");
        return categoryInt;
     }


    public String reformatDateForDB(String oldFormat){
        String newDate = "";
        try {
            SimpleDateFormat dateOldFormat = new SimpleDateFormat("MM-dd-yyyy");
            Date oldDate = new Date();
            oldDate = dateOldFormat.parse(oldFormat);
            SimpleDateFormat dateNewFormat = new SimpleDateFormat("yyyy-MM-dd");
            newDate = dateNewFormat.format(oldDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }


    public int convertMonthToInt(String month, String[] months){
        int monthInt = 1;
        for (String s : months){
            if (month.equals(s))
                return monthInt;
            monthInt++;
        }
        Log.d(TAG, "Wrong month value");
        return monthInt;
    }
    public  String convertExpenseCategoryIDToString(int categoryID, View view){
        String[] categories = view.getResources().getStringArray(R.array.ExpensesCategories);
        return categories[categoryID];
    }

    public  String convertIncomeCategoryIDToString(int categoryID, View view){
        String[] categories = view.getResources().getStringArray(R.array.IncomeCategories);
        return categories[categoryID];
    }

}

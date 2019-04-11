package byuics246.budgeting;

import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * contains all types of conversion functions
 */
public class Conversion {
    private static final String TAG = "Convertion";
    public Conversion() {}

    /**
     * converts a category string into a category index
     *
     * @param category
     * @param categories
     * @return
     */
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


    /**
     * chenges the date format to yyyy-MM-dd
     *
     * @param oldFormat
     * @return
     */
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

    /**
     * converts string month into a number it represents
     * @param month
     * @param months
     * @return
     */
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

    /**
     * converts a number of the month from the int to string format
     *
     * @param monthNumber
     * @return
     */
    public String convertMonthIntToNumberString(int monthNumber) {
        String monthNumberString = "";
        if (monthNumber < 10)
            monthNumberString+="0";
        monthNumberString+=String.valueOf(monthNumber);
        return monthNumberString;
    }

    /**
     * converts an expense category index to an category name
     *
     * @param categoryID
     * @param view
     * @return
     */
    public  String convertExpenseCategoryIDToString(int categoryID, View view){
        String[] categories = view.getResources().getStringArray(R.array.ExpensesCategories);
        return categories[categoryID];
    }

    /**
     * converts an income category index to an category name
     *
     * @param categoryID
     * @param view
     * @return
     */
    public  String convertIncomeCategoryIDToString(int categoryID, View view){
        String[] categories = view.getResources().getStringArray(R.array.IncomeCategories);
        return categories[categoryID];
    }

}

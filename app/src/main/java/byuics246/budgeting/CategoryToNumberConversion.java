package byuics246.budgeting;

import android.util.Log;

import java.util.List;

public class CategoryToNumberConversion {
    private static final String TAG = "CategoryToNumberConvert";
    public CategoryToNumberConversion() {
    }

    public String translateCategory(String category){
         String categoryInt = "0";
         switch(category) {
             case "Grocieries":
                 categoryInt = "1";
                 break;

             case "Gas":
                 categoryInt = "2";
                 break;

             default:
                 Log.d(TAG, "Wrong category value");
         }
         return categoryInt;
     }

}

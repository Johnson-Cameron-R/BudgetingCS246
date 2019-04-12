package byuics246.budgeting;

import java.text.NumberFormat;
import java.util.Random;

/**
 * contains supporting fucntions
 *
 * @author Inessa Carroll
 */
public class Utilities {
    int randomStringLength = 20;

    /**
     * creates a random string of a set length
     * @return
     */
    String randomString() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < randomStringLength; i++) {
            char c = (char)(r.nextInt((int)(Character.MAX_VALUE)));
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * validates the input of the user for adding a Transaction
     *
     * @param date
     * @param category
     * @param amount
     * @return
     */
    public int validateNewTransaction(String date, String category, String amount) {
        /** Check if date is correct*/
        Integer month = Integer.valueOf(Character.toString(date.charAt(0)) + Character.toString(date.charAt(1)));
        Integer day = Integer.valueOf(Character.toString(date.charAt(3)) + Character.toString(date.charAt(4)));
        //if month number is bigger than 12
        if(month > 12){
            return 1;
        }
        //if date is bigger than 31
        else if (day > 31){
            return 2;
        }
        //if months than contain 28 or 29 days contain more
        else if (((month < 8 && month % 2 == 0) || (month >= 8 && month % 2 == 1)) && (day > 29)){
            if (month == 2) {
                return 2;
            }
            else{
                if (day > 30) {
                    return 2;
                }
            }
        }
        /**Check if amount is empty*/
        if (amount.equals("")) {
            return 3;
        }

        /**Check if category is empty*/
        if (category.equals("Category")) {
            return 4;
        } else {
        }

        return 0;
    }


    /**
     * generate currency
     * @param number
     * @return
     */
    private String generateCurrency(Double number) {
        NumberFormat format = NumberFormat.getCurrencyInstance();
        return format.format(number);
    }

}

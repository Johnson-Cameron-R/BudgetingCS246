package byuics246.budgeting;

public class MonthStringToIntConverter {
    String month;

    public MonthStringToIntConverter(String monthName) {
        month = monthName;
    }

    public int convert(){
        int monthInt = 0;
        switch(month) {
            case "January":
                monthInt = 1;
                break;

            case "Febuary":
                monthInt = 2;
                break;

            case "March":
                monthInt = 3;
                break;

            case "April":
                monthInt = 4;
                break;

            case "May":
                monthInt = 5;
                break;

            case "June":
                monthInt = 6;
                break;

            case "July":
                monthInt = 7;
                break;

            case "August":
                monthInt = 8;
                break;

            case "September":
            case "sep":
                monthInt = 9;
                break;

            case "October":
                monthInt = 10;
                break;

            case "November":
                monthInt = 11;
                break;

            case "December":
                monthInt = 12;
                break;
        }
        return monthInt;
    }

}

package byuics246.budgeting;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Transaction {
    String date;/////
    String user;
    String description;
    String category;
    Double amount;//or double

    public boolean equals(Transaction obj) {
        if (obj.getDate().equals(date) && obj.getUser().equals(user) && obj.getDescription().equals(description)
                && obj.getCategory().equals(category) && obj.getAmount().equals(amount)) {
            return true;
        }
        else
            return false;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Transaction(String date, String user, String category, Double amount, String description) {
        this.date = date;
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }
}
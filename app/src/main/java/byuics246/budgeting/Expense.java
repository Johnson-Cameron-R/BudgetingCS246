package byuics246.budgeting;

public class Expense {
    String date;/////
    String user;
    String description;
    String category;
    double amount;

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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Expense(String date, String user, String category, double amount) {
        this.date = date;
        this.user = user;
        this.category = category;
        this.amount = amount;
    }
}
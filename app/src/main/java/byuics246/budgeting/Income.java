package byuics246.budgeting;

public class Income {
    String date;
    String user;
    String description;
    String category;
    String income;

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

    public String getIncome() { return income; }

    public void setTotalIncome(String totalIncome) { this.income = totalIncome; }


    public Income(String date, String user, String description, String category, String income) {
        this.date = date;
        this.user = user;
        this.description = description;
        this.category = category;
        this.income = income;
    }
}

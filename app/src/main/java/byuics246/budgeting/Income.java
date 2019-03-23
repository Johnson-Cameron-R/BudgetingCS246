package byuics246.budgeting;

public class Income {
    String totalIncome;
    String monthlyIncome;
    String assets;

    public String getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(String totalIncome) { this.totalIncome = totalIncome; }

    public String getMonthlyIncome() {
        return monthlyIncome;
    }

    public void setMonthlyIncome(String monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public Income(String totalIncome, String monthlyIncome) {
        this.totalIncome = totalIncome;
        this.monthlyIncome = monthlyIncome;
    }
}

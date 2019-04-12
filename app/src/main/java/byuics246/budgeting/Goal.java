package byuics246.budgeting;

/**
 * represents income and expence goals
 *
 * @author Inessa Carroll
 */
public class Goal {
    String name;
    String amount;
    String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Goal(String name, String amount, String id) {
        this.name = name;
        this.amount = amount;
        this.id = id;
    }
}

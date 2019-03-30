package byuics246.budgeting;

class CellNumberRecord {
    int column;
    int row;
    double value;

    public CellNumberRecord(int column, int row, double value) {
        this.column = column;
        this.row = row;
        this.value = value;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}

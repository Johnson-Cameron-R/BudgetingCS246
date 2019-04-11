package byuics246.budgeting;

/**
 * represents a colection of data to be inserted into the excel cell
 * <p>
 * Contains a column and row number and a value to be inserted.
 * The value can be of any type
 * </p>
 *
 * @param <E>
 */
public class  CellRecord <E>{
    int column;
    int row;
    E value;

    public CellRecord(int column, int row, E value) {
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

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }
}

/**
 * Represent the Player's Position
 */
public class PlayerPosition {
    int row;
    int column;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int hashCode() {
        return (row * 31) + column;
    }
    public boolean equals(Object o) {
        if (o instanceof PlayerPosition) {
            PlayerPosition other = (PlayerPosition) o;
            return (row == other.row && column == other.column);
        }
        return false;
    }
}

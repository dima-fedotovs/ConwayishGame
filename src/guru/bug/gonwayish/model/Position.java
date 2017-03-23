package guru.bug.gonwayish.model;

import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Dimitrijs Fedotovs <a href="http://www.bug.guru">www.bug.guru</a>
 * @version 1.0
 * @since 1.0
 */
public final class Position implements Comparable<Position> {
    public static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    private int column;
    private int row;

    public static Set<Position> all() {
        SortedSet<Position> result = new TreeSet<>();
        for (int r = 0; r < HEIGHT; r++) {
            for (int c = 0; c < WIDTH; c++) {
                result.add(new Position(c, r));
            }
        }
        return result;
    }

    private Position(int column, int row) {
        if (!isValid(column, row)) {
            throw new IllegalArgumentException("Column or row is invalid: " + column + ", " + row);
        }
        this.column = column;
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public Set<Position> around() {
        SortedSet<Position> result = new TreeSet<>();
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = column - 1; c <= column + 1; c++) {
                if (isValid(c, r) && (c != column || r != row)) {
                    result.add(new Position(c, r));
                }
            }
        }
        return result;
    }

    private boolean isValid(int c, int r) {
        return r >= 0 && r < HEIGHT && c >= 0 && c < WIDTH;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return column == position.column &&
                row == position.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(column, row);
    }

    @Override
    public String toString() {
        return "Position{" +
                "column=" + column +
                ", row=" + row +
                '}';
    }

    @Override
    public int compareTo(Position o) {
        int result = this.row - o.row;
        if (result == 0) {
            result = this.column - o.column;
        }
        return result;
    }
}

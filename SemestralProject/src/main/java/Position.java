import java.util.Objects;
import java.util.Random;

/**
 * Immutable class. 2D vector with x and y coordinates. Position in the simulated world.
 * (Commonly used to represent size of an area as well)
 */
public final class Position {
    /** x coordinate */
    private final int x;
    /** y coordinate */
    private final int y;

    /** Constructor
     * @param x x coordinate
     * @param y y coordinate */
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** x coordinate getter
     * @return int x coordinate of the vector */
    public int getX(){
        return x;
    }

    /** y coordinate getter
     * @return int y coordinate of the vector */
    public int getY(){
        return y;
    }

    /** Sums this position with other position and returns result position (vector sum).
     * @param other Position to sum this position with.
     * @return Position vector sum of the two positions. */
    public Position add(Position other){
        return new Position(x + other.getX(), y + other.getY());
    }

    /** Subtracts other Position from this position and return result position (vector subtraction).
     * @param other Position to subtract from this position.
     * @return Position vector sum of this position with inverted other position. */
    public Position subtract(Position other){
        return new Position(x - other.getX(), y - other.getY());
    }

    /** Calculates distance of this position from other position.
     * @param other position, from which to calculate the distance.
     * @return double - Euclidean distance of the two positions. */
    public double distance(Position other){
        final Position sub = this.subtract(other);
        return Math.sqrt(sub.getX()*sub.getX() + sub.getY()* sub.getY());
    }

    /** Calculates if this position is inside the given area.
     * @param topLeft top-left corner of the area.
     * @param size size of the area.
     * @return true if position is inside area, false otherwise.*/
    public boolean isInArea(Position topLeft, Position size){
        Position relative = this.subtract(topLeft);
        return relative.getX() >= 0 && relative.getY() >= 0 &&
                relative.getX() < size.getX() && relative.getY() < size.getY();
    }

    /** Creates a new Position object from formatted string
     * @param positionString formatted string representing position
     * @return Position loaded from the string. */
    public static Position parsePosition(String positionString){
        String[] split = positionString.split(",");
        return new Position(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    /** Creates a random position in the area (0,0) to (max.X, max.Y)
     * @param max limit to the random positions size
     * @param random random instance to use to generate the random move
     * @return random Position in given range. */
    public static Position randomPosition(Position max, Random random){
        if(max.getX() < 0 || max.getY() < 0)
            return new Position(0,0);
        int x = random.nextInt(max.getX());
        int y = random.nextInt(max.getY());
        return new Position(x, y);
    }

    @Override
    public String toString() {
        return String.format("%d,%d", x, y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}

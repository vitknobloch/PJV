import org.junit.jupiter.api.Test;

import java.util.Random;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.*;

public class PositionTest {

    @Test
    public void getX() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x = rand.nextInt();
            int y = rand.nextInt();
            Position p = new Position(x, y);
            assertEquals(x, p.getX());
        }
    }

    @Test
    public void getY() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x = rand.nextInt();
            int y = rand.nextInt();
            Position p = new Position(x, y);
            assertEquals(y, p.getY());
        }
    }

    @Test
    public void add() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x1 = rand.nextInt();
            int y1 = rand.nextInt();
            int x2 = rand.nextInt();
            int y2 = rand.nextInt();
            Position p1 = new Position(x1, y1);
            Position p2 = new Position(x2, y2);
            Position p = p1.add(p2);
            assertEquals(x1 + x2, p.getX());
            assertEquals(y1 + y2, p.getY());
        }
    }

    @Test
    public void subtract() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x1 = rand.nextInt();
            int y1 = rand.nextInt();
            int x2 = rand.nextInt();
            int y2 = rand.nextInt();
            Position p1 = new Position(x1, y1);
            Position p2 = new Position(x2, y2);
            Position p = p1.subtract(p2);
            assertEquals(x1 - x2, p.getX());
            assertEquals(y1 - y2, p.getY());
        }
    }

    @Test
    public void distance() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x1 = rand.nextInt();
            int y1 = rand.nextInt();
            int x2 = rand.nextInt();
            int y2 = rand.nextInt();
            int distX = x1-x2;
            int distY = y1-y2;
            Position p1 = new Position(x1, y1);
            Position p2 = new Position(x2, y2);
            assertEquals(sqrt(distX*distX + distY*distY), p1.distance(p2));
        }
    }

    @Test
    public void isInArea() {
        assertTrue(new Position(10, 10).isInArea(new Position(0,0), new Position(15, 15)));
        assertTrue(new Position(-5, 5).isInArea(new Position(-2564,0), new Position(4000, 300)));
        assertTrue(new Position(-10, 0).isInArea(new Position(-10,0), new Position(1, 1)));
        assertTrue(new Position(9, 9).isInArea(new Position(0,0), new Position(10, 10)));

        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            Position bottomRight = Position.randomPosition(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE), rand);
            Position topLeft = Position.randomPosition(bottomRight, rand);
            Position size = bottomRight.subtract(topLeft);
            Position p = Position.randomPosition(size, rand).add(topLeft);
            assertTrue(p.isInArea(topLeft, size));
        }

        assertFalse(new Position(10, 10).isInArea(new Position(0,0), new Position(9, 9)));
        assertFalse(new Position(10, 10).isInArea(new Position(0,0), new Position(10, 10)));
        assertFalse(new Position(-5, -53).isInArea(new Position(-4,-52), new Position(13, 55)));
        assertFalse(new Position(32, -16).isInArea(new Position(15,-26), new Position(432, 10)));
    }

    @Test
    public void parsePosition() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x = rand.nextInt();
            int y = rand.nextInt();
            Position p = new Position(x, y);
            String positionString = x + "," + y;
            assertEquals(p, Position.parsePosition(positionString));
        }
    }

    @Test
    public void randomPosition() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            Position max = new Position(rand.nextInt(Integer.MAX_VALUE), rand.nextInt(Integer.MAX_VALUE));
            Position p = Position.randomPosition(max, rand);
            assertTrue(p.getX() < max.getX());
            assertTrue(p.getY() < max.getY());
        }
    }

    @Test
    public void testToString() {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            int x = rand.nextInt();
            int y = rand.nextInt();
            Position p = new Position(x,y);
            assertEquals(x + "," + y, p.toString());
        }
    }
}
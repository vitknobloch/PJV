import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TimetableTest {

    private Random random = new Random();
    private Position[] occupations;
    private Timetable timetable;

    @BeforeEach
    void setUp(){
        occupations = new Position[24];
        for(int i = 0; i < 24; i++){
            if(random.nextBoolean()){
                occupations[i] = Position.randomPosition(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE), random);
            }
        }
        timetable = new Timetable();
    }

    @Test
    void setOccupation() {
        for(int i = 0; i < 24; i++){
            timetable.setOccupation(i, occupations[i]);
        }

        for(int i = 0; i < 24; i++){
            assertEquals(occupations[i], timetable.getOccupation(i));
        }
    }

    @Test
    void getOccupation() {
        for(int i = 0; i < 24; i++){
            timetable.setOccupation(i, occupations[i]);
        }

        for(int i = 0; i < 24; i++){
            assertEquals(occupations[i], timetable.getOccupation(i));
        }
    }

    @Test
    void parseTimetable() {
        String[] occupationsStrings = new String[24];
        for(int i = 0; i < 24; i++){
            occupationsStrings[i] = occupations[i] == null ? "" : occupations[i].toString();
        }
        String timetableString = String.join(";", occupationsStrings);

        timetable = Timetable.parseTimetable(timetableString);

        for(int i = 0; i < 24; i++){
            assertEquals(occupations[i], timetable.getOccupation(i));
        }
    }

    @Test
    void testToString() {
        String[] occupationsStrings = new String[24];
        for(int i = 0; i < 24; i++){
            occupationsStrings[i] = occupations[i] == null ? "" : occupations[i].toString();
            timetable.setOccupation(i, occupations[i]);
        }
        String timetableString = String.join(";", occupationsStrings);


        assertEquals(timetableString, timetable.toString());
    }
}
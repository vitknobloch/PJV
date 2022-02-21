import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    private Random random = new Random();
    private Person person;
    private int personalNumber;
    private PersonHealth personHealth;
    private Timetable timetable;
    private Position home;


    @BeforeEach
    void BuildUp(){
        personalNumber = random.nextInt(Integer.MAX_VALUE);
        int personHealthNumber = random.nextInt(PersonHealth.values().length);
        personHealth = PersonHealth.values()[personHealthNumber];
        timetable = new Timetable();
        home = Position.randomPosition(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE), random);

        person = new Person(personalNumber,personHealth, home, timetable);
    }

    @Test
    void getPersonalNumber() {
        assertEquals(personalNumber, person.getPersonalNumber());
    }

    @Test
    void visitLocation() {
        Location location = new Location(new Position(0,0));
        person.visitLocation(location, new ContagionParameters());
        assertTrue(location.visitors.contains(person));
    }

    @Test
    void getOccupation() {
        for(int i = 0; i < 100; i ++){
            Position occupation = Position.randomPosition(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE), random);
            timetable.setOccupation(i % 24, occupation);
            assertEquals(person.getOccupation(i%24), occupation);
        }
    }

    @Test
    void setOccupation() {
        for(int i = 0; i < 100; i ++){
            Position occupation = Position.randomPosition(new Position(Integer.MAX_VALUE, Integer.MAX_VALUE), random);
            person.setOccupation(i % 24, occupation);
            assertEquals(timetable.getOccupation(i%24), occupation);
        }
    }

    @Test
    void getHome() {
        assertEquals(person.getHome(), home);
    }

    @Test
    void leaveCurrentLocation() {
        Location location = new Location(new Position(0,0));
        person.visitLocation(location, new ContagionParameters());
        person.leaveCurrentLocation();
        assertFalse(location.visitors.contains(person));
        assertDoesNotThrow(() -> person.leaveCurrentLocation());
    }

    @Test
    void move() {
        ContagionParameters parameters = new ContagionParameters();
        Location location = new Location(new Position(0,0));
        parameters.freeTimeBan = false;
        person = new Person(personalNumber, PersonHealth.deceased, home, timetable);
        person.visitLocation(location, parameters);
        assertEquals(location.getPosition(), person.move(0, parameters));
        person = new Person(personalNumber, PersonHealth.quarantined, home, timetable);
        person.visitLocation(location, parameters);
        assertEquals(home, person.move(0, parameters));
        person = new Person(personalNumber, PersonHealth.healthy, home, timetable);
        person.visitLocation(location, parameters);
        assertTrue(person.move(0, parameters).isInArea(new Position(-5,-5), new Position(11,11)));
        person = new Person(personalNumber, PersonHealth.healthy, home, timetable);
        timetable.setOccupation(0, new Position(333,666));
        person.visitLocation(location, parameters);
        assertEquals(new Position(333,666), person.move(0, parameters));
        parameters.freeTimeBan = true;
        assertEquals(person.move(1, parameters), home);
    }

    @Test
    void getHealth() {
        assertEquals(person.getHealth(), personHealth);
    }

    @Test
    void tryInfect() {
        for(int i = 0; i < 100; i++){
            person = new Person(personalNumber, PersonHealth.healthy, home, timetable);
            person.tryInfect(0.0);
            assertEquals(PersonHealth.healthy, person.getHealth());
        }
        for(int i = 0; i < 100; i++){
            person = new Person(personalNumber, PersonHealth.healthy, home, timetable);
            person.tryInfect(1);
            assertEquals(PersonHealth.infected, person.getHealth());
        }
    }

    @Test
    void tryChangeHealth() {
        ContagionParameters parameters = new ContagionParameters();
        parameters.quarantineChance = 0.15f;
        parameters.deathChance = 0.15f;
        parameters.recoveryChance = 0.15f;
        int infected = 0;
        int cured = 0;
        int quarantined = 0;
        int deceased = 0;
        for(int i = 0; i < 10000; i++){
            person = new Person(personalNumber, PersonHealth.infected, home, timetable);
            person.tryChangeHealth(parameters);
            switch (person.getHealth()){
                case infected -> infected++;
                case cured -> cured++;
                case quarantined -> quarantined++;
                case deceased -> deceased++;
                default -> fail("Health changed to " + person.getHealth().toString());
            }
        }

        assertTrue(0.52f <= infected / 10000.0 && infected / 10000.0 <= 0.58f);
        assertTrue(0.14f <= cured / 10000.0 && cured / 10000.0 <= 0.16f);
        assertTrue(0.14f <= quarantined / 10000.0 && quarantined / 10000.0 <= 0.16f);
        assertTrue(0.14f <= deceased / 10000.0 && deceased / 10000.0 <= 0.16f);
    }

    @Test
    void parsePerson() {
        String personString = "Person:" + personalNumber + ":0,0:" + personHealth + ":" + timetable.toString() + ":" + home.toString();
        Person p = Person.parsePerson(personString);
        assertEquals(personHealth, p.getHealth());
        assertEquals(personalNumber, p.getPersonalNumber());
        assertEquals(home, p.getHome());

        for(int i = 0; i < 24; i++){
            assertEquals(timetable.getOccupation(i), person.getOccupation(i));
        }
    }

    @Test
    void testToString() {
        String personString = "Person:" + personalNumber + ":33,66:" + personHealth + ":" + timetable.toString() + ":" + home.toString();
        assertEquals(personString, person.toString(new Position(33, 66)));
    }

    @Test
    void testToString1() {
        String personString = "Person:" + personalNumber + ":-1,-1:" + personHealth + ":" + timetable.toString() + ":" + home.toString();
        assertEquals(personString, person.toString());
    }
}
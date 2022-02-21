import java.util.Random;
import java.util.logging.Logger;

/**
 * Class holding one person, it's health status, timetable, position and unique personal number.
 */
public class Person {
    /** Random values generator (for random moves and infection generation) */
    private static final Random rand = new Random();
    /** Logger for Person class */
    private static final Logger log = Logger.getLogger(Person.class.getName());

    /** Current global location of the Person */
    private Location currentLocation;
    /** Health status of the person - healthy, infected, cured, vaccinated, deceased, qurantined */
    private PersonHealth health;
    /** Timetable of the person -> list of positions to "teleport" to at a certime time of day.*/
    private Timetable timetable;
    /** Global unique identifier of the person */
    private int personalNumber;
    /** Position of this persons home - this where he move if quarantined. */
    private Position home;

    /** Person class constructor.
     * <p>Sets up all the properties except current location.
     * Need to call visitLocation afterwards to set up location.</p>
     * @param personalNumber personal number of the person.
     * @param health health status of the person.
     * @param home position of the home of the person.
     * @param timetable timetable of the person. */
    public Person(int personalNumber, PersonHealth health, Position home, Timetable timetable){
        this.personalNumber = personalNumber;
        this.health = health;
        this.home = home;
        this.timetable = timetable;

        currentLocation = null;
    }

    /** Personal number getter
     * @return personal number of this Person */
    public int getPersonalNumber(){
        return personalNumber;
    }

    /** Handles Persons transfer to another location.
     * @param location location to visit.
     * @param parameters Contagion parameters of the infection.
     * @return true if Person relocated to the location, false otherwise. */
    public boolean visitLocation(Location location, ContagionParameters parameters) {
        if(location.visit(this, parameters)){
            leaveCurrentLocation();
            currentLocation = location;
            return true;
        }
        return false;
    }

    /**
     * Returns current occupation from the persons timetable. Null if there is no occupation.
     * @param hour hour of day.
     * @return Position - position the person should be at at the given time.
     */
    public Position getOccupation(int hour){
        return timetable.getOccupation(hour);
    }

    /**
     * Sets the persons occupation in it's timetable to given position at given time of day.
     * @param hour hour of day.
     * @param position position for the person to move on at that time.
     */
    public void setOccupation(int hour, Position position){
        timetable.setOccupation(hour, position);
    }

    /** Home getter.
     * @return Position of this persons home. */
    public Position getHome(){
        return home;
    }

    /**
     * Makes the person to be remove from currently visited location and sets it visited location to null.
     */
    public void leaveCurrentLocation(){
        if(currentLocation != null){
            currentLocation.leave(this);
        }
        currentLocation = null;
    }

    /** Finds assigned position in timetable or generates random move near current position.
     * @param hour current time of day.
     * @param parameters currently active contagion parameters containing restrictions.
     * @return Position - position to move to. */
    public Position move(int hour, ContagionParameters parameters){
        if(currentLocation == null) {
            log.severe("Trying to move person without assigned location");
            return new Position(0, 0);
        }

        if(health == PersonHealth.deceased)
            return currentLocation.getPosition();

        if(health == PersonHealth.quarantined){
            return home;
        }

        if(timetable.getOccupation(hour) != null){
            return timetable.getOccupation(hour);
        }

        if(parameters.freeTimeBan)
            return home;

        Position move = Position.randomPosition(new Position(11, 11), rand).subtract(new Position(5,5));
        Position cur = this.currentLocation.getPosition();
        return cur.add(move);
    }

    /** Person health getter.
     * @return PersonHealth - current health of this person. */
    public PersonHealth getHealth() {
        return health;
    }

    /** Infects healthy person with the given chance
     * @param chance chance that the healthy person gets infected */
    public void tryInfect(double chance){
        if(health == PersonHealth.healthy){
            if(rand.nextFloat() < chance){
                health = PersonHealth.infected;
            }
        }
        else{
            log.info("Trying to infect not-healthy person.");
        }
    }

    /** If person is infected or quarantined this changes it's health according to contagion parameters.
     * @param parameters contagion parameters to use while calculating health changes. */
    public void tryChangeHealth(ContagionParameters parameters){
        float randomFloat = rand.nextFloat();
        if(randomFloat < parameters.quarantineChance + parameters.deathChance + parameters.recoveryChance){
            this.health = PersonHealth.quarantined;
        }
        if(randomFloat < parameters.deathChance + parameters.recoveryChance){
            this.health = PersonHealth.deceased;
        }
        if(randomFloat < parameters.recoveryChance){
            this.health = PersonHealth.cured;
        }
    }

    /**
     * Parses person from the given formatted string and returns it.
     * @param personString formatted text representation of person class.
     * @return Person - new person parsed from the string.
     */
    public static Person parsePerson(String personString){
        String[] split = personString.split(":",-1);
        int personalNumber = Integer.parseInt(split[1]);
        PersonHealth health = PersonHealth.valueOf(split[3]);
        Timetable timetable = Timetable.parseTimetable(split[4]);
        Position home = Position.parsePosition(split[5]);
        return new Person(personalNumber, health, home, timetable);
    }

    /** Get inline text representation of the Person.
     * @param position desired position of the person to end at
     * @return Text representation of person to send via network. */
    public String toString(Position position){
        return  String.format("Person:%d:%d,%d:%s:%s:%d,%d",
                personalNumber,
                position.getX(),
                position.getY(),
                health.toString(),
                timetable.toString(),
                home.getX(),
                home.getY());
    }

    /** Get inline text representation of the Person with the current position as position.
     * @return Text representation of person to send via network.
     */
    @Override
    public String toString(){
        if(currentLocation == null)
            return toString(new Position(-1,-1));
        return toString(currentLocation.getPosition());
    }
}

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Main class of simulator slave app, holds the grid for this simulators respective area.
 * Handles commands from master, generates acknowledgements.
 */
public class Simulator {
    private static Logger log = Logger.getLogger(Simulator.class.getName());
    /** Top-left corner of this simulator's active area. */
    private Position topLeft;
    /** Size of this simulator's active area. */
    private Position size;
    /** Current hour of day of the simulation */
    private int hour;
    /** 2D array of all positions in this simulator's active area. */
    private Location[][] tiles;
    /** List of all persons in this simulator's active area. */
    private List<Person> people;
    /** List of people awaiting confirmation. */
    private List<Person> awaitingConfirmation;
    /** Currently active contagion spread parameters. */
    private ContagionParameters contagionParameters;

    /** Sets up the simulator to the given world size, prepares grid for locations.
     * @param topLeft most top-left position in this simulator's area.
     * @param size size of this simulator's area */
    public void setSimulator(Position topLeft, Position size){
        this.topLeft = topLeft;
        this.size = size;
        tiles = new Location[size.getX()][size.getY()];
        for(int i = 0; i < size.getX(); i++){
            for (int j = 0; j < size.getY(); j++) {
                tiles[i][j] = new Location(topLeft.add(new Position(i, j)));
            }
        }
        hour = 0;
        people = new ArrayList<>();
        awaitingConfirmation = new ArrayList<>();
    }

    /** Moves all the people to their new location (move part of the round.)
     * This function sends confirmation to the master at the end.
     * @param connection connection to use to send people who want to leave this simulators area. */
    public void movePeople(MasterConnection connection){
        hour++;
        for(Person p : people){
            //don't move dead people
            if(p.getHealth() == PersonHealth.deceased)
                continue;
            //get position to move to from alive people
            Position pos = p.move(hour % 24, contagionParameters);

            //move person if target position is inside this simulator
            if(pos.isInArea(topLeft, size)){
                Position index = pos.subtract(topLeft);
                p.visitLocation(tiles[index.getX()][index.getY()], contagionParameters);
            }
            //send person to master if target position is outside this simulator
            else {
                connection.sendPerson(p, pos);
                awaitingConfirmation.add(p);
            }
        }
    }

    /** Updates health status of people based on spread of the desease. (spread part of the round.)
     * This function sends confirmation to the master at the end.*/
    public void calculateHealth(){
        for(int x = 0; x < size.getX(); x++){
            for(int y = 0; y < size.getY(); y++){
                tiles[x][y].calculateVisitorsHealth(contagionParameters);
            }
        }
    }

    /** Tries to add new person to target location.
     *  Return positive or negative confirmation of accepting the Person on the new position.
     *  @param person person to add.
     *  @param targetPosition position the person want's to visit.
     *  @return boolean - true if person was accepted, false otherwise. */
    public boolean addPerson(Person person, Position targetPosition){
        //check that target position is inside this simulator
        if(!targetPosition.isInArea(topLeft, size)){
            log.severe("Person " + person.getPersonalNumber() + " target position out of this simulators area.");
            return false;
        }
        //get location index of target position
        Position index = targetPosition.subtract(topLeft);
        //try to visit the location and add the person this simulators people on success
        if(person.visitLocation(tiles[index.getX()][index.getY()], contagionParameters)){
            people.add(person);
            return true;
        }
        return false;
    }

    /** Removes person with passed personalNumber from people waiting for confirmation.
     * removes person from the simulator if confirmation is true.
     * @param personalNumber personal number of confirmed person.
     * @param confirmation true if person was accepted by other simulator, false otherwise.
     */
    public void confirmPerson(int personalNumber, boolean confirmation){
        for(Person p : awaitingConfirmation){
            if(p.getPersonalNumber() == personalNumber){
                if(confirmation){
                    p.leaveCurrentLocation();
                    people.remove(p);
                }
                awaitingConfirmation.remove(p);
                return;
            }
        }
    }

    /** Adds the given location to the grid on it's position.
     * @param location location to add.*/
    public void addLocation(Location location){
        Position index = location.getPosition().subtract(topLeft);
        tiles[index.getX()][index.getY()] = location;
    }

    /** Updates this simulators contagion parameters to the passed ones.
     * @param contagionParameters parameters to set to the simulator. */
    public void setContagionParameters(ContagionParameters contagionParameters){
        this.contagionParameters = contagionParameters;
    }

    /** Calculates the counts people with different health statuses.
     * @return Stats - counts of people of a certain health status (order: healthy, infected, cured, quarantined, deceased, vaccinated) */
    public Stats getStats(){
        Stats ret = new Stats();
        for(Person p : people){
            ret.countPerson(p);
        }
        return ret;
    }

    /** Returns array of all locations in the given area's intersection with this simulator's active area.
     * @param topLeft position of the top-left corner of requested area.
     * @param size Position(vector) of the x and y coordinate sizes of the requested area.
     * @return array of locations in the given area. */
    public Location[] getArea(Position topLeft, Position size){
        //find intersection
        log.fine("Get area - topLeft: " + topLeft.toString() + "; size: " + size.toString());
        Position intersectSize = calculateInnerAreaSize(topLeft, size);
        log.finer("Intersection size: " + intersectSize);

        if(intersectSize.getX() == 0 && intersectSize.getY() == 0)
            return null; //if the intersection is empty

        //extract locations to 1D array
        Location[] ret = new Location[intersectSize.getX() * intersectSize.getY()];
        Position index = topLeft.subtract(this.topLeft);
        index = new Position(Math.max(index.getX(), 0), Math.max(index.getY(), 0));
        for(int x = 0; x < intersectSize.getX(); x++){
            for(int y = 0; y < intersectSize.getY(); y++){
                ret[x * intersectSize.getY() + y] = tiles[index.getX() + x][index.getY() + y];
            }
        }
        return ret;
    }

    /** Calculates the size of given area's intersection with this simulators area
     * @param topLeft top-left corner of the area.
     * @param size size of the area.
     * @return Position - width and height of the intersection. */
    private Position calculateInnerAreaSize(Position topLeft, Position size){
        Position thisBottomRight = this.topLeft.add(this.size);
        Position bottomRight = topLeft.add(size);

        int startX = trimInt(this.topLeft.getX(), thisBottomRight.getX(), topLeft.getX());
        int endX = trimInt(this.topLeft.getX(), thisBottomRight.getX(), bottomRight.getX());

        int startY = trimInt(this.topLeft.getY(), thisBottomRight.getY(), topLeft.getY());
        int endY = trimInt(this.topLeft.getY(), thisBottomRight.getY(), bottomRight.getY());

        return new Position(endX - startX, endY - startY);
    }

    /** Returns value closest to the given value that is from interval <min;max>
     * @param min smallest value to return
     * @param max largest value to return
     * @param value value to trim
     * @return trimmed value. */
    private int trimInt(int min, int max, int value){
        if(value < min)
            return min;
        else if(value > max)
            return max;
        else
            return value;
    }

}

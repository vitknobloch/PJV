import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Base location class, contains information about a single tile in the simulated world.
 * Knows which persons are present, person class instance can visit or leave a location.
 */
public class Location {
    /** Position of the location in the whole simulated world */
    protected Position position;
    /** List of persons currently present at the locations position */
    protected List<Person> visitors;

    /** Location constructor
     * @param position position of the Location. */
    public Location(Position position){
        this.position = position;
        visitors = new ArrayList<>();
    }

    /** Accepts the person, adds it to the visitors list.
     * @param person person to visit this location.
     * @param parameters currently active contagion parameters
     * @return true if the person can visit this location, false if the person isn't allowed there.
     * */
    public synchronized boolean visit(Person person, ContagionParameters parameters){
        if(visitors.contains(person))
            return false;

        visitors.add(person);
        return true;
    }

    /** Removes the person from visitors list.
     * @param person person that is leaving this location. */
    public synchronized void leave(Person person){
        visitors.remove(person);
    }

    /** Position getter.
     * @return position of this location */
    public Position getPosition(){
        return this.position;
    }

    /** Calculates stats for this location only and returns it.
     * @return Stats of this location. (Counts of different health states.) */
    public Stats getStats(){
        Stats ret = new Stats();
        for(Person p: visitors){
            ret.countPerson(p);
        }
        return ret;
    }

    /** Runs simulation of decease spread and health status changes among it's visitors.
     * @param parameters infection parameters - settings of contagion.*/
    public synchronized void calculateVisitorsHealth(ContagionParameters parameters){
        tryInfectVisitors(parameters, parameters.defaultSpreadChance);
    }

    /**
     * For every infected person in the locations tries to infect all healthy people in the location.
     * @param parameters Contagion parameters of the contagion.
     * @param spreadChance Chance with which one person infects another in this location.
     */
    protected void tryInfectVisitors(ContagionParameters parameters, double spreadChance){
        for(Person p : visitors){
            if(p.getHealth() == PersonHealth.infected || p.getHealth() == PersonHealth.quarantined){
                //for every infectious person
                for(Person contact: visitors){
                    if(contact.getHealth() == PersonHealth.healthy){
                        //for every person that can be infected
                        contact.tryInfect(spreadChance);
                    }
                }
                p.tryChangeHealth(parameters);
            }
        }
    }

    /** Returns text with name of the locations type
     * @return String - name of the locations type */
    public String getTypeSting(){
        return "default";
    }

    /**
     * Parses Whitelist from given formatted string.
     * @param whitelistString formatted string representing whitelist.
     * @return List<Integer> List containing PersonalNumbers of whitelisted people.
     */
    private static List<Integer> parseWhitelist(String whitelistString){
        String[] split = whitelistString.split(",");
        ArrayList<Integer> whitelist = new ArrayList<>(split.length);
        if(split.length == 1 && split[0].equals("")){
            return whitelist;
        }

        for(String s: split){
            whitelist.add(Integer.parseInt(s));
        }
        return  whitelist;
    }

    /**
     * Parses location from given formatted string
     * @param locationString formatted string representing the location.
     * @return Location parsed location of the correct type parsed from the string.
     */
    public static Location parseLocation(String locationString) {
        String[] split = locationString.split(":", -1);
        if (!split[0].equals("Location")) {
            return null;
        }
        Position pos = Position.parsePosition(split[2]);
        switch (split[1]) {
            case "default":
                return new Location(pos);
            case "home":
                return new LocationHome(pos, parseWhitelist(split[3]));
            case "school":
                return new LocationSchool(pos, parseWhitelist(split[3]));
            case "workplace":
                return new LocationWorkplace(pos, parseWhitelist(split[3]));
            case "restaurant":
                return new LocationRestaurant(pos);
            default:
                Logger.getLogger(Location.class.getName()).severe("Unknown location type " + split[2]);
                return new Location(pos);
        }
    }

    /** Returns text representation of location to send via network.
     * @return string - text representation of location. */
    @Override
    public String toString(){
        return "Location:default:" + position.toString();
    }
}

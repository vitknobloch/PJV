import java.util.logging.Logger;

/**
 * Class holding positions a person should move to in certain hours of day.
 */
public class Timetable {
    /** Timetable class logger */
    private Logger log = Logger.getLogger(Timetable.class.getName());

    /** Array of 24 positions (or nulls), one for each our of day. Positions to move to at certain times. */
    private Position[] timetable;

    /** Timetable constructor. Creates new empty timetable. */
    public Timetable() {
        this.timetable = new Position[24];
    }

    /** Sets position to timetable at given hour of day
     * @param hour hour of day (0-23)
     * @param position position the Person should move to at the given time of day. */
    public void setOccupation(int hour, Position position){
        if(hour >= 0 && hour < 24){
            timetable[hour] = position;
        }
    }

    /** Returns position the Person should now move to.
     * @param hour hour of day (0-23)
     * @return Position to be at given hour of day, or null if there's no position. */
    public Position getOccupation(int hour){
        return timetable[hour];
    }

    /**
     * Parses timetable from given formatted string and returns it.
     * @param timetableString formatted string representation of timetable
     * @return Timetable - new timetable instance parsed from the string
     */
    public static Timetable parseTimetable(String timetableString){
        String[] split = timetableString.split(";", -1);
        Timetable timetable = new Timetable();
        for(int i = 0; i < 24; i++){
            if(!split[i].equals("")){
                Position pos = Position.parsePosition(split[i]);
                timetable.setOccupation(i, pos);
            }
        }
        return  timetable;
    }

    /** Get inline text representation of the Timetable.
     * @return Text representation of timetable to send via network.
     */
    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i < 24; i++){
            if(timetable[i] != null){
                ret.append(timetable[i].toString());
            }
            if(i != 23)
                ret.append(";");
        }
        return ret.toString();
    }
}

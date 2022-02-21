import java.util.List;
/**
 * Class of school location. Extends LocationWhitelisted.
 */
public class LocationSchool extends LocationWhitelisted{

    /** Location school constructor
     * @param position position of the location.
     * @param whitelist List of personal numbers of persons that are allowed here. */
    public LocationSchool(Position position, List<Integer> whitelist){
        super(position, whitelist);
    }

    @Override
    public boolean visit(Person person, ContagionParameters parameters) {
        if(!parameters.schoolsOpen){
            return  false;
        }
        return super.visit(person, parameters);
    }

    @Override
    public void calculateVisitorsHealth(ContagionParameters parameters) {
        double spreadChance = parameters.defaultSpreadChance * parameters.schoolSpreadMultiplier;
        if(parameters.masks)
            spreadChance *= parameters.masksMultiplier;
        tryInfectVisitors(parameters, spreadChance);
    }

    /** Returns text with name of the locations type
     * @return String - name of the locations type */
    @Override
    public String getTypeSting(){
        return "school";
    }

    /** Returns text representation of location to send via network.
     * @return string - text representation of location. */
    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder("Location:school:" + position.toString() + ":");
        for(int i = 0; i < whiteList.size(); i++){
            if(i > 0){
                ret.append(",");
            }

            ret.append(whiteList.get(i).toString());
        }

        return ret.toString();
    }
}

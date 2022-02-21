import java.util.List;
/**
 * Class of home location. Extends LocationWhitelisted.
 */
public class LocationHome extends LocationWhitelisted{

    /** Location home constructor.
     * @param position position of the location.
     * @param whitelist List of personal numbers of persons that are allowed here. */
    public LocationHome(Position position, List<Integer> whitelist){
        super(position, whitelist);
    }

    @Override
    public void calculateVisitorsHealth(ContagionParameters parameters) {
        tryInfectVisitors(parameters, parameters.defaultSpreadChance * parameters.homeSpreadMultiplier);
    }

    /** Returns text with name of the locations type
     * @return String - name of the locations type */
    @Override
    public String getTypeSting(){
        return "home";
    }

    /** Returns text representation of location to send via network.
     * @return string - text representation of location. */
    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder("Location:home:" + position.toString() + ":");
        for(int i = 0; i < whiteList.size(); i++){
            if(i > 0){
                ret.append(",");
            }

            ret.append(whiteList.get(i).toString());
        }

        return ret.toString();
    }
}

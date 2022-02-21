import java.util.List;
/**
 * Class of workplace location. Extends LocationWhitelisted.
 */
public class LocationWorkplace extends LocationWhitelisted{
    /** Location workplace constructor
     * @param position position of the location.
     * @param whiteList List of personal numbers of persons that are allowed here. */
    public LocationWorkplace(Position position, List<Integer> whiteList){
        super(position, whiteList);
    }

    @Override
    public boolean visit(Person person, ContagionParameters parameters) {
        if(!parameters.workOnSite){
            return false;
        }
        return super.visit(person, parameters);
    }

    @Override
    public void calculateVisitorsHealth(ContagionParameters parameters) {
        double spreadChance = parameters.defaultSpreadChance * parameters.workplaceSpreadMultiplier;
        if(parameters.masks)
            spreadChance *= parameters.masksMultiplier;
        tryInfectVisitors(parameters, spreadChance);
    }

    /** Returns text with name of the locations type
     * @return String - name of the locations type */
    @Override
    public String getTypeSting(){
        return "workplace";
    }

    /** Returns text representation of location to send via network.
     * @return string - text representation of location. */
    @Override
    public String toString(){
        StringBuilder ret = new StringBuilder("Location:workplace:" + position.toString() + ":");
        for(int i = 0; i < whiteList.size(); i++){
            if(i > 0){
                ret.append(",");
            }
            ret.append(whiteList.get(i).toString());
        }

        return ret.toString();
    }
}

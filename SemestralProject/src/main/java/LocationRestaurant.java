/**
 * Class of restaurant location. Extends Location.
 */
public class LocationRestaurant extends Location{

    /** Location restaurant class constructor.
     * @param position position of the restaurant */
    public LocationRestaurant(Position position){
        super(position);
    }

    @Override
    public boolean visit(Person person, ContagionParameters parameters) {
        if(!parameters.restaurantsOpen)
            return false;

        return super.visit(person, parameters);
    }

    @Override
    public void calculateVisitorsHealth(ContagionParameters parameters) {
        double spreadChance =  parameters.defaultSpreadChance * parameters.restaurantSpreadMultiplier;
        if(parameters.masks)
            spreadChance *= parameters.masksMultiplier;
        tryInfectVisitors(parameters, spreadChance);
    }

    /** Returns text with name of the locations type
     * @return String - name of the locations type */
    @Override
    public String getTypeSting(){
        return "restaurant";
    }

    /** Returns text representation of location to send via network.
     * @return string - text representation of location. */
    @Override
    public String toString(){
        return "Location:restaurant:" + position.toString();
    }
}

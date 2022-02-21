import java.util.ArrayList;
import java.util.List;
/** Abstract class of location that only certain people can visit. Extends Location. */
public abstract class LocationWhitelisted extends Location {
    /** List of persons that can visit this location */
    List<Integer> whiteList;

    /** Location whitelisted constructor
     * @param position this locations position
     * @param whiteList list of personal numbers of persons that can visit this location*/
    public LocationWhitelisted(Position position, List<Integer> whiteList){
        super(position);
        this.whiteList = whiteList;
        if(whiteList == null){
            this.whiteList = new ArrayList<>();
        }
    }

    /** Adds new person to whitelisted persons list
     * @param personalNumber The newly whitelisted persons personal number. */
    public void addToWhiteList(Integer personalNumber){
        whiteList.add(personalNumber);
    }

    /** Returns true if person is on the allowed persons list of this location.
     * @param person person to look up in whitelist
     * @return true if person is whitelisted here, false otherwise. */
    public boolean isWhitelisted(Person person){
        for(int pn: whiteList){
            if(pn == person.getPersonalNumber()){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean visit(Person person, ContagionParameters parameters) {
        if(isWhitelisted(person)){
            return super.visit(person, parameters);
        }
        return false;
    }
}

/**
 * Container class to hold information about decease spread settings.
 */
public class ContagionParameters {
    /** Chance that an infected person passes decease on healthy person in one hour. */
    public float defaultSpreadChance;
    /** Chance that an infected person becomes cured in one hour. */
    public float recoveryChance;
    /** Chance that an infected person dies in one hour. */
    public float deathChance;
    /** Chance that an infected person is uncovered and quarantined in one hour. */
    public float quarantineChance;


    /** Boolean representing whether or not restaurants are open according to current restriction settings of simulation. */
    public boolean restaurantsOpen;
    /** Boolean representing whether or not free time activities are allowed according to current restriction settings of simulation. */
    public boolean freeTimeBan;
    /** Boolean that represents whether or not people visit work according to current restriction settings. */
    public boolean workOnSite;
    /** Boolean that represents whether or not schools are open according to current restriction settings. */
    public boolean schoolsOpen;
    /** Whether or not masks are mandatory in locations restaurant, school and workplace */
    public boolean masks;


    /** Factor by which it's more/less likely that the contagion spreads in this LocationHome compared to default. */
    public double homeSpreadMultiplier;
    /** Factor by which it's more/less likely that the contagion spreads in this LocationWorkplace compared to default. */
    public double workplaceSpreadMultiplier;
    /** Factor by which it's more/less likely that the contagion spreads in this LocationSchool compared to default. */
    public double schoolSpreadMultiplier;
    /** Factor by which it's more/less likely that the contagion spreads in this LocationRestaurant compared to default. */
    public double restaurantSpreadMultiplier;
    /** Factor by witch to multiply spread probability when masks are mandatory */
    public double masksMultiplier;

    /**
     * Parses contagion parameters from predefined text format.
     * @param contagionParametersString formatted string with contagion parameters.
     * @return ContagionParameters - parsed contagion parameters.
     */
    public static ContagionParameters parseContagionParameters(String contagionParametersString){
        String[] split = contagionParametersString.split(":");
        if(!split[0].equals("ContagionParameters"))
            return null;

        ContagionParameters parameters = new ContagionParameters();

        parameters.defaultSpreadChance = Float.parseFloat(split[1]);
        parameters.recoveryChance = Float.parseFloat(split[2]);
        parameters.deathChance = Float.parseFloat(split[3]);
        parameters.quarantineChance = Float.parseFloat(split[4]);
        parameters.restaurantsOpen = Boolean.parseBoolean(split[5]);
        parameters.freeTimeBan = Boolean.parseBoolean(split[6]);
        parameters.workOnSite = Boolean.parseBoolean(split[7]);
        parameters.schoolsOpen = Boolean.parseBoolean(split[8]);
        parameters.homeSpreadMultiplier = Float.parseFloat(split[9]);
        parameters.workplaceSpreadMultiplier = Float.parseFloat(split[10]);
        parameters.schoolSpreadMultiplier = Float.parseFloat(split[11]);
        parameters.restaurantSpreadMultiplier = Float.parseFloat(split[12]);
        parameters.masks = Boolean.parseBoolean(split[13]);
        parameters.masksMultiplier = Float.parseFloat(split[14]);

        return parameters;
    }

    /** Returns formatted string representation of the contagion parameters instance.
     * @return String - formatted contagion parameters string representation. */
    @Override
    public String toString() {
        return String.format("ContagionParameters:%f:%f:%f:%f:%b:%b:%b:%b:%f:%f:%f:%f:%b:%f",
                defaultSpreadChance, recoveryChance, deathChance, quarantineChance,
                restaurantsOpen, freeTimeBan, workOnSite, schoolsOpen,
                homeSpreadMultiplier, workplaceSpreadMultiplier, schoolSpreadMultiplier, restaurantSpreadMultiplier,
                masks, masksMultiplier);
    }
}

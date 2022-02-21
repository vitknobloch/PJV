/** Class holding basic statistical information.*/
public class Stats {
    /** Number of not yet infected people.*/
    public int healthy;
    /** Number of infected non-quarantined people.*/
    public int infected;
    /** Number of cured people, they can no longer get infected.*/
    public int cured;
    /** Number of quarantined people, note that these are also infected
     * and can spread the infection. */
    public int quarantined;
    /** Number of people that died as result of the infection. */
    public int deceased;
    /** Number of people that were vaccinated and cannot be infected. */
    public int vaccinated;

    /** Constructor that zeroes out all the properties. */
    public Stats(){
        healthy = 0;
        infected = 0;
        cured = 0;
        quarantined = 0;
        deceased = 0;
        vaccinated = 0;
    }

    /** Constructor that sets the properties to the parameters values.
     * @param healthy number of healthy people.
     * @param infected number of infected people.
     * @param cured number of cured people.
     * @param quarantined number of quarantined people.
     * @param deceased number of deceased people.
     * @param vaccinated number of vaccinated people. */
    public Stats(int healthy, int infected, int cured, int quarantined, int deceased, int vaccinated) {
        this.healthy = healthy;
        this.infected = infected;
        this.cured = cured;
        this.quarantined = quarantined;
        this.deceased = deceased;
        this.vaccinated = vaccinated;
    }

    /** Adds the passed parameter values to the already stored values. (This method is synchronized)
     * @param healthy number of healthy people to add.
     * @param infected number of infected people to add.
     * @param cured number of cured people to add.
     * @param quarantined number of quarantined people to add.
     * @param deceased number of deceased people to add.
     * @param vaccinated number of vaccinated people to add. */
    public synchronized void update(int healthy, int infected, int cured, int quarantined, int deceased, int vaccinated){
        this.healthy += healthy;
        this.infected += infected;
        this.cured += cured;
        this.quarantined += quarantined;
        this.deceased += deceased;
        this.vaccinated += vaccinated;
    }

    /** Adds the passed stats values to the already stored values. (The update is synchronized)
     * @param stats stats to add to the stored values. */
    public void update(Stats stats){
        update(stats.healthy, stats.infected, stats.cured, stats.quarantined, stats.deceased, stats.vaccinated);
    }

    /**
     * Parses stats from the given formatted string and returns them
     * @param statsString formatted string representation of stats class.
     * @return Stats - stats parsed from the string.
     */
    public static Stats parseStats(String statsString){
        String[] statsSplit = statsString.split(",");
        Stats stats = new Stats();
        int healthy = Integer.parseInt(statsSplit[0]);
        int infected = Integer.parseInt(statsSplit[1]);
        int cured = Integer.parseInt(statsSplit[2]);
        int quarantined = Integer.parseInt(statsSplit[3]);
        int deceased = Integer.parseInt(statsSplit[4]);
        int vaccinated = Integer.parseInt(statsSplit[5]);
        stats.update(healthy, infected, cured, quarantined, deceased, vaccinated);
        return stats;
    }

    /** Adds this persons health status to the statistics.
     * @param person person to count. */
    public void countPerson(Person person){
        switch (person.getHealth()){
            case healthy -> healthy++;
            case cured -> cured++;
            case infected -> infected++;
            case deceased -> deceased++;
            case vaccinated -> vaccinated++;
            case quarantined -> quarantined++;
        }
    }

    @Override
    public String toString() {
        return String.format("%d,%d,%d,%d,%d,%d", healthy, infected, cured, quarantined, deceased, vaccinated);
    }
}

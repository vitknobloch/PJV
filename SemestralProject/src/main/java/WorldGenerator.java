import javax.naming.CommunicationException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Logger;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;

/** Class responsible for generating world based on given parameters and sending it to the simulators. */
public class WorldGenerator implements Flow.Publisher{
    private static final Logger log = Logger.getLogger(WorldGenerator.class.getName());
    /** Flag indicating that an error occurred. */
    private volatile boolean error;
    /** Random instance used to randomize generated world. */
    private final Random random;
    /** Simulators connected to the simulation */
    private final SimulatorConnection[] sims;
    /** Subscribers register to receive progress updates. */
    private final LinkedList<Subscription> subscriptions;
    private Position worldSize;
    private ContagionParameters parameters;
    /** number of homes in the generated world */
    private int homeCount;
    /** number of restaurants in the generated world */
    private int restaurantCount;
    /** number of schools in the generated world */
    private int schoolCount;
    /** number of workplaces in the generated world */
    private int workplaceCount;
    /** number of people in the generated world */
    private int population;
    /** Chance a person will be infected when it is generated */
    private float initialInfectedRate;
    /** Chance a person will be cured when it is generated */
    private float initialCuredRate;
    /** Chance a person will be vaccinated when it is generated */
    private float initialVaccinatedRate;

    /**
     * World generator constructor, initializes flags and inner properties,
     * but not the world generating parameters, they have to be specified before starting the generation.
     * @param sims simulators to run the simulation
     */
    public WorldGenerator(SimulatorConnection[] sims){
        this.sims = sims;
        random = new Random();
        worldSize = null;
        error = false;
        subscriptions = new LinkedList<>();
    }

    /**
     * Whether or no the world generating parameters have been set.
     * @return boolean - true if generator was set up, false otherwise.
     */
    public boolean isSetUp(){
        return worldSize != null;
    }

    /**
     * Simulators getter.
     * @return array of simulators used to run the simulation.
     */
    public SimulatorConnection[] getSimulators() {
        return sims;
    }

    /**
     * Contagion parameters getter.
     * @return contagion parameters that are set in the simulators after world generation.
     */
    public ContagionParameters getContagionParameters(){
        return parameters;
    }

    /**
     * Sets up generators world generating properties
     * @param worldSize size of the generated world
     * @param parameters contagion parameters to be set initially in the simulators
     * @param homeCount number of homes in the world
     * @param restaurantCount number of restaurants in the world
     * @param schoolCount number of schools in the world
     * @param workplaceCount number of workplaces in the world
     * @param population number of people in the world
     * @param initialInfectedRate chance a person is infected when generated
     * @param initialCuredRate chance a person is cured when generated
     * @param initialVaccinatedRate chance a person is vaccinated when generated
     */
    public void setUpGenerator(Position worldSize, ContagionParameters parameters,
                               int homeCount, int restaurantCount, int schoolCount, int workplaceCount,
                               int population, float initialInfectedRate, float initialCuredRate, float initialVaccinatedRate){
        this.worldSize = worldSize;
        this.parameters = parameters;
        this.homeCount = homeCount;
        this.restaurantCount = restaurantCount;
        this.schoolCount = schoolCount;
        this.workplaceCount = workplaceCount;
        this.population = population;
        this.initialInfectedRate = initialInfectedRate;
        this.initialCuredRate = initialCuredRate;
        this.initialVaccinatedRate = initialVaccinatedRate;
    }

    /**
     * World size getter.
     * @return Position size of the world
     */
    public Position getWorldSize(){
        return worldSize;
    }

    /**
     * Generates the world according to set properties,
     * sends status updates to subscribers.
     */
    public void generateWorld(){
        if(worldSize == null){
            return;
        }

        //Set up simulators and contagion parameters
        setUpSims();
        if(error) return;
        publishToAll(1);
        setUpContagionParameters();
        if(error) return;
        publishToAll(2);

        //field to quickly find if a location is still empty
        boolean[][] freePositions = new boolean[worldSize.getX()][worldSize.getY()];

        //generate homes and people
        LocationHome[] homes = generateHomes(freePositions);
        publishToAll(3);
        Person[] people = generatePeople(homes);
        publishToAll(4);

        //send homes to sims
        sendLocations(homes);
        homes = null;
        if(error) return;
        publishToAll(5);

        //generate schools and send them to simulators
        LocationSchool[] schools = generateSchools(freePositions, people);
        sendLocations(schools);
        schools = null;
        if(error) return;
        publishToAll(6);

        //generate schools and send them to simulators
        LocationWorkplace[] workplaces = generateWorkplaces(freePositions, people);
        sendLocations(workplaces);
        workplaces = null;
        if(error) return;
        publishToAll(7);

        //generate schools and send them to simulators
        LocationRestaurant[] restaurants = generateRestaurants(freePositions, people);
        sendLocations(restaurants);
        restaurants = null;
        if(error) return;
        publishToAll(8);

        //send people to simulators
        sendPeople(people);
        completeToAll();
    }

    /**
     * error flag getter
     * @return boolean - true if an error occurred while generating the world, false otherwise.
     */
    public boolean raisedError(){
        return error;
    }

    /**
     * Sends locations which are in the simulators area to the simulator.
     * @param sim simulator to which to send the locations
     * @param locations array of locations to send
     * @throws SimulatorConnectionException in case of an error between the master and simulator.
     */
    private void sendLocationsSim(SimulatorConnection sim, Location[] locations) throws SimulatorConnectionException {
        for (Location location : locations) {
            if (sim.containsPosition(location.getPosition())) {
                sim.sendLocation(location);
            }
        }

    }

    /**
     * Sends people whose homes are in the simulators area to the simulator.
     * @param sim simulator to which to send the locations
     * @param people array of people to send
     * @throws SimulatorConnectionException in case of an error between the master and simulator.
     */
    private void sendPeopleSim(SimulatorConnection sim, Person[] people) throws SimulatorConnectionException {
        for(Person p: people){
            if(sim.containsPosition(p.getHome())){
                sim.sendPerson(p, p.getHome(), null);
            }
        }
    }

    /**
     * Sends people to their respective simulators.
     * @param people array of people to be sent.
     */
    private void sendPeople(Person[] people){
        Thread[] senders = new Thread[sims.length];
        for(int i = 0; i < sims.length; i++){
            int finalI = i;
            senders[i] = new Thread(() -> {
                try {
                    sendPeopleSim(sims[finalI], people);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            senders[i].start();
        }
        waitForThreads(senders);
    }

    /**
     * Sends locations to their respective simulators.
     * @param locations array of locations to be sent
     */
    private void sendLocations(Location[] locations){
        Thread[] senders = new Thread[sims.length];
        for(int i = 0; i < sims.length; i++){
            int finalI = i;
            senders[i] = new Thread(() -> {
                try {
                    sendLocationsSim(sims[finalI], locations);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            senders[i].start();
        }
        waitForThreads(senders);
    }

    /**
     * Returns a random position that is free according to passed free positions array, and marks that position as full.
     * @param freePositions Array of free positions
     * @return Position a random free position.
     */
    private Position getRandomFreePosition(boolean[][] freePositions){
        Position pos = Position.randomPosition(worldSize, random);
        while(freePositions[pos.getX()][pos.getY()]){
            pos = Position.randomPosition(worldSize, random);
        }
        freePositions[pos.getX()][pos.getY()] = true;
        return pos;
    }

    /**
     * Returns a random person from the array of people,
     * that has no occupation in their timetable at given time
     * @param people array of people to choose from
     * @param time time of day that the chosen person should be free at
     * @return Person - a random person that meets the requirements
     */
    private Person getRandomPersonFreeAtTime(Person[] people, int time){
        int personIndex = random.nextInt(population);
        while(people[personIndex].getOccupation(time) != null){
            personIndex = random.nextInt(population);
        }
        return people[personIndex];
    }

    /**
     * Generates restaurants.
     * @param freePositions array of free and full positions in the world.
     * @param people array of people in the world.
     * @return Array of restaurants
     */
    private LocationRestaurant[] generateRestaurants(boolean[][] freePositions, Person[] people){
        LocationRestaurant[] restaurants = new LocationRestaurant[restaurantCount];
        if(restaurantCount == 0)
            return restaurants;
        int peoplePerRestaurant = (int)((population * 0.1) / restaurantCount);
        peoplePerRestaurant = min(peoplePerRestaurant, 100);
        for(int i = 0; i < restaurantCount; i++){
            Position pos = getRandomFreePosition(freePositions);
            LocationRestaurant restaurant = new LocationRestaurant(pos);
            for(int j = 0; j < peoplePerRestaurant; j++){
                Person p = getRandomPersonFreeAtTime(people, 19);
                for(int h = 19; h < 24; h++){
                    p.setOccupation(h, pos);
                }
            }
            restaurants[i] = restaurant;
        }
        return restaurants;
    }

    /**
     * Generates workplaces.
     * @param freePositions array of free and full positions in the world.
     * @param people array of people in the world.
     * @return Array of workplaces
     */
    private LocationWorkplace[] generateWorkplaces(boolean[][] freePositions, Person[] people){
        LocationWorkplace[] workplaces = new LocationWorkplace[workplaceCount];
        if(workplaceCount == 0)
            return workplaces;
        int peoplePerWorkplace = (int)((population * 0.5) / workplaceCount);
        peoplePerWorkplace = min(peoplePerWorkplace, 100);
        for(int i = 0; i < workplaceCount; i++){
            List<Integer> whitelist = new ArrayList<>(peoplePerWorkplace);
            Position pos = getRandomFreePosition(freePositions);
            LocationWorkplace workplace = new LocationWorkplace(pos, whitelist);
            for(int j = 0; j < peoplePerWorkplace; j++){
                Person p = getRandomPersonFreeAtTime(people, 8);
                for(int h = 7; h < 15; h++){
                    p.setOccupation(h, pos);
                }
                workplace.addToWhiteList(p.getPersonalNumber());
            }
            workplaces[i] = workplace;
        }
        return workplaces;
    }

    /**
     * Generates schools.
     * @param freePositions array of free and full positions in the world.
     * @param people array of people in the world.
     * @return Array of schools
     */
    private LocationSchool[] generateSchools(boolean[][] freePositions, Person[] people){
        LocationSchool[] schools = new LocationSchool[schoolCount];
        if(schoolCount == 0)
            return schools;

        int peoplePerSchool = (int)((population * 0.2) / schoolCount);
        peoplePerSchool = min(peoplePerSchool, 50);
        for(int i = 0; i < schoolCount; i++){
            List<Integer> whitelist = new ArrayList<>(peoplePerSchool);
            Position pos = getRandomFreePosition(freePositions);
            LocationSchool school = new LocationSchool(pos, whitelist);
            for(int j = 0; j < peoplePerSchool; j++){
                Person p = getRandomPersonFreeAtTime(people, 8);
                for(int h = 8; h < 13; h++){
                    p.setOccupation(h, pos);
                }
                school.addToWhiteList(p.getPersonalNumber());
            }
            schools[i] = school;
        }
        return schools;
    }

    /**
     * Generates homes.
     * @param freePositions array of free and full positions in the world.
     * @return Array of homes
     */
    private LocationHome[] generateHomes(boolean[][] freePositions){
        LocationHome[] ret = new LocationHome[homeCount];
        for(int i = 0; i < homeCount; i++){
            Position pos = getRandomFreePosition(freePositions);
            ret[i] = new LocationHome(pos, new LinkedList<>());
        }
        return ret;
    }

    /**
     * Generates people.
     * @param homes array of homes in the world.
     * @return Array of people.
     */
    private Person[] generatePeople(LocationHome[] homes){
        Person[] people = new Person[population];
        for(int i = 0; i < population; i++){
            int homeIndex = random.nextInt(homeCount);
            Position homePosition = homes[homeIndex].getPosition();
            homes[homeIndex].addToWhiteList(i);
            Timetable timetable = new Timetable();
            for(int h = 0; h < 6; h++){
                timetable.setOccupation(h, homePosition);
            }
            people[i] = new Person(i, getRandomHealth(), homePosition, timetable);
        }
        return people;
    }

    /**
     * Generates a random health for new person status based on world generating properties
     * @return PersonHealth - random person health.
     */
    private PersonHealth getRandomHealth(){
        float healthFloat = random.nextFloat();
        if(healthFloat < initialVaccinatedRate){
            return PersonHealth.vaccinated;
        }
        if(healthFloat < initialVaccinatedRate + initialCuredRate){
            return PersonHealth.cured;
        }
        if(healthFloat < initialVaccinatedRate + initialCuredRate + initialInfectedRate){
            return PersonHealth.infected;
        }
        return PersonHealth.healthy;
    }

    /**
     * Calculates how to divide world to individual simulators and sets them up accordingly.
     */
    private void setUpSims(){
        //calculate how to divide the world
        Position div = divideWorld();
        Position blockSize = new Position(worldSize.getX() / div.getX(), worldSize.getY() / div.getY());
        Thread[] threads = new Thread[sims.length];
        for(int i = 0; i < div.getX(); i++){
            for(int j = 0; j < div.getY(); j++){
                //calculate the simulators topLeft and size
                Position topLeft = new Position(i * blockSize.getX(), j * blockSize.getY());
                Position size = blockSize;
                if(i == div.getX() - 1){
                    size = size.add(new Position(worldSize.getX() - div.getX() * blockSize.getX(), 0));
                }
                if(i == div.getY() - 1){
                    size = size.add(new Position(0, worldSize.getY() - div.getY() * blockSize.getY()));
                }

                //set up the simulator in separate thread
                Position finalSize = size;
                int simIndex = i * div.getY() + j;
                threads[simIndex] = new Thread(() -> {
                    try {
                        sims[simIndex].initSimulator(topLeft, finalSize);
                    } catch (SimulatorConnectionException e) {
                        handleCommunicationError();
                    }
                });
                threads[simIndex].start();
            }
        }
        waitForThreads(threads);
    }

    /**
     * Sets contagion parameters to the simulators
     */
    private void setUpContagionParameters(){
        Thread[] threads = new Thread[sims.length];
        for(int i = 0; i < sims.length; i++){
            int finalI = i;
            threads[i] = new Thread(() -> {
                try {
                    sims[finalI].sendContagionParameters(parameters);
                } catch (SimulatorConnectionException e) {
                    handleCommunicationError();
                }
            });
            threads[i].start();
        }
        waitForThreads(threads);
    }

    /**
     * Handles communication error between the generator and the simulators.
     */
    private synchronized void handleCommunicationError(){
        if(!error){
            errorToAll(new CommunicationException());
            error = true;
            exitSims();
        }
    }

    /**
     * Ends all connected simulators
     */
    private void exitSims(){
        for( SimulatorConnection sim : sims){
            sim.sendExitSim();
        }
    }

    /**
     * Joins all thread in the passed array.
     * @param threads array with thread to join.
     */
    private void waitForThreads(Thread[] threads){
        for(Thread t: threads){
            try {
                t.join();
            } catch (InterruptedException e) {
                log.severe("Threads interrupted");
            }
        }
    }

    /**
     * Calculates how to optimally divide the simulated world into connected simulators
     * @return Position - recommended number of blocks on x and y axis
     */
    private Position divideWorld(){
        int bestDiv = 1;
        for(int i = 2; i <= sqrt(sims.length); i++){
            if (sims.length % i == 0){
                bestDiv= i;
            }
        }

        int shortEdgeDiv = bestDiv;
        int longEdgeDiv = sims.length / bestDiv;

        return worldSize.getX() < worldSize.getY() ?
                new Position(shortEdgeDiv, longEdgeDiv) : new Position(longEdgeDiv, shortEdgeDiv);
    }

    private void publishToAll(int segment){
        for(Subscription s: subscriptions){
            s.publish(segment);
        }
    }

    private void errorToAll(Throwable t){
        for(Subscription s: subscriptions){
            s.sendError(t);
        }
    }

    private void completeToAll(){
        for(Subscription s: subscriptions){
            s.onComplete();
        }
    }

    @Override
    public void subscribe(Subscriber subscriber) {
        new Subscription(subscriber);
    }

    /** Inner class handling communication between world generator and it's subscribers. */
    public class Subscription implements Flow.Subscription{
        private Subscriber subscriber;
        private int requested;

        private Subscription(Subscriber subscriber){
            this.subscriber = subscriber;
            requested = 0;
            subscriptions.add(this);
            subscriber.onSubscribe(this);
        }

        private synchronized void publish(int part){
            if(requested > 0){
                subscriber.onNext(part);
            }
        }

        private void sendError(Throwable t){
            subscriber.onError(t);
        }

        private void onComplete(){
            subscriber.onComplete();
        }

        @Override
        public void request(long n) {
            requested = (int) (requested + n);
        }

        @Override
        public void cancel() {
            subscriptions.remove(this);
        }
    }


}

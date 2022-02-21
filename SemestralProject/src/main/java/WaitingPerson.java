/**
 * Class encapsulating information about person waiting to be moved to another simulator
 * Contains the person, it's targetPosition and connection to original simulator.
 */
public class WaitingPerson {
    private final Person person;
    private final Position targetPosition;
    private final SimulatorConnection origin;

    /**
     * Waiting person constructor.
     * @param person the waiting person.
     * @param targetPosition position the person wants to move to.
     * @param origin the SimulatorConnection to send confirmation to
     */
    public WaitingPerson(Person person, Position targetPosition, SimulatorConnection origin) {
        this.person = person;
        this.targetPosition = targetPosition;
        this.origin = origin;
    }

    /**
     * Person getter.
     * @return waiting person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * TargetPosition getter
     * @return position the person wants to move to.
     */
    public Position getTargetPosition() {
        return targetPosition;
    }

    /**
     * Simulator of origin getter.
     * @return simulator connection to add the confirmation to.
     */
    public SimulatorConnection getOrigin() {
        return origin;
    }
}

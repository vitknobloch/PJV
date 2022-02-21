/**
 * Class encapsulating information about a person confirmation to exchange between simulators.
 */
public class WaitingPersonConfirmation {
    private final Integer personalNumber;
    private final Boolean confirmation;

    /**
     * WaitingPersonConfirmation constructor.
     * @param personalNumber personal number of confirmed person
     * @param confirmation confirmation - true if person was accepted, false otherwise.
     */
    public WaitingPersonConfirmation(Integer personalNumber, Boolean confirmation) {
        this.personalNumber = personalNumber;
        this.confirmation = confirmation;
    }

    /**
     * Personal number getter.
     * @return personal number of the confirmed person
     */
    public Integer getPersonalNumber() {
        return personalNumber;
    }

    /**
     * Confirmation getter
     * @return the confirmation value - true if person was accepted, false otherwise.
     */
    public Boolean getConfirmation() {
        return confirmation;
    }
}

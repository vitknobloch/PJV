/** Enum with various states of a persons health status. */
public enum PersonHealth {
    /** Person that still can be infected. */
    healthy,
    /** Person that is infected with the decease and can infect others. */
    infected,
    /** Person that already had the infection and cannot be infected again. */
    cured,
    /** Person that died as a result of the infection. */
    deceased,
    /** Person that is infected, but it's movement is handled in different way. */
    quarantined,
    /** Person that cannot be infected. */
    vaccinated
}

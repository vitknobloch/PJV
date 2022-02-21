import java.util.logging.Logger;

/**
 * Class representing statistical data about one location.
 * Contains that location person health statistics and the locations type.
 */
public class ExtractLocation {
    private Stats stats;
    private Type type;

    /**
     * Constructor of ExtractLocation class.
     * @param type Location type of the Extracted location.
     * @param stats Statistical data about person health of locations visitors.
     */
    public ExtractLocation(Type type, Stats stats){
        this.type = type;
        this.stats = stats;
    }

    /**
     * Stats getter.
     * @return Stats - statistical data about person health of locations visitors.
     */
    public Stats getStats() {
        return stats;
    }

    /**
     * Returns type of the extracted location.
     * @return ExtractLocation.Type object representing the locations type.
     */
    public Type getType() {
        return type;
    }

    /** Enum type representing type of extracted location. */
    public enum Type{
        /** Loaction class extract */
        defaultL("default"),
        /** LocationHome class extract */
        homeL("home"),
        /** LocationRestaurant class extract */
        restaurantL("restaurant"),
        /** LocationSchool class extract */
        schoolL("school"),
        /** LocationWorkplace class extract */
        workplaceL("workplace");

        private String text;

        Type(String text) {
            this.text = text;
        }

        /**
         * Returns enum object according to text passed.
         * @param text String to parse
         * @return Enum object representing location of given type or defaultL if passed location type isn't recognised.
         */
        public static Type fromString(String text) {
            for (Type t : Type.values()) {
                if (t.text.equalsIgnoreCase(text)) {
                    return t;
                }
            }
            Logger.getLogger(ExtractLocation.class.getName()).severe("Unrecognized location type.");
            return defaultL;
        }
    }
}

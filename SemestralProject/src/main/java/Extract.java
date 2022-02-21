/** Class representing simple extract of an area, contains ExtractLocations */
public class Extract {
    private Position topLeft;
    private Position size;

    private ExtractLocation[][] extracts;

    /** Creates a new empty extract of given size
     * @param topLeft topLeft corner of extracted location
     * @param size size of the extracted area */
    public Extract(Position topLeft, Position size){
        this.topLeft = topLeft;
        this.size = size;

        extracts = new ExtractLocation[size.getX()][size.getY()];
    }

    /** Creates a new extract of given size and filles intersecting areas from previous extract.
     * @param topLeft topLeft corner of extracted area.
     * @param size size of the extracted area.
     * @param previous Extract to fill intersectiong positions from.*/
    public Extract(Position topLeft, Position size, Extract previous){
        this(topLeft, size);
        //find bottom right corners of extracts
        Position previousBottomRight = previous.topLeft.add(previous.size);
        Position bottomRight = topLeft.add(size);
        //find top left corner of intersection
        int intersectionTopLeftX = Math.max(topLeft.getX(), previous.topLeft.getX());
        int intersectionTopLeftY = Math.max(topLeft.getY(), previous.topLeft.getY());
        //find bottom right corner of intersection
        int intersectionBottomRightX = Math.min(bottomRight.getX(), previousBottomRight.getX());
        int intersectionBottomRightY = Math.min(bottomRight.getY(), previousBottomRight.getY());
        //find size of intersection (can be of negative values if there is no intersection)
        int intersectionSizeX = intersectionBottomRightX - intersectionTopLeftX;
        int intersectionSizeY = intersectionBottomRightY - intersectionTopLeftY;

        //copy intersecting locations from previous extract to the new extract
        for(int x = 0; x < intersectionSizeX; x++){
            for(int y = 0; y < intersectionSizeY; y++){
                ExtractLocation el = previous.getExtractedLocation(
                        intersectionTopLeftX - previous.topLeft.getX() + x,
                        intersectionTopLeftY - previous.topLeft.getY() + y
                );
                addExtractedLocation(
                        intersectionTopLeftX - topLeft.getX() + x,
                        intersectionTopLeftY - topLeft.getY() + y,
                        el
                );
            }
        }
    }

    /** Parses the extracted location and adds it to the extract if it is in the area.
     * @param locationExtractString formatted text representation of the extracted location. */
    public void addExtractedLocation(String locationExtractString){
        String[] split = locationExtractString.split(";");
        Position pos = Position.parsePosition(split[0]);
        if(pos.isInArea(topLeft, size)){
            ExtractLocation.Type type = ExtractLocation.Type.fromString(split[1]);
            Stats stats = Stats.parseStats(split[2]);
            Position index = pos.subtract(topLeft);
            addExtractedLocation(index.getX(), index.getY(), new ExtractLocation(type, stats));
        }
    }

    private synchronized void addExtractedLocation(int x, int y, ExtractLocation extractLocation){
        extracts[x][y] = extractLocation;
    }

    /** Returns the locationExtract of location on the given position, if it is in the area.
     * @param position position of the requested position in the whole simulated world.
     * @return ExtractLocation - extract of location on given position or null. */
    public ExtractLocation getExtractedLocation(Position position){
        if(position.isInArea(topLeft, size)){
            Position index = position.subtract(topLeft);
            return getExtractedLocation(index.getX(),index.getY());
        }
        return  null;
    }

    /** Returns locationExtract of location on given index in this Extract (position relative to this Extract topLeft corner).
     * Can cause index out of bounds exception
     * @param x - x index of requested location.
     * @param y - y index of requested location.
     * @return ExtractLocation - extract location on given index in this extract*/
    public ExtractLocation getExtractedLocation(int x, int y){
        return extracts[x][y];
    }
}

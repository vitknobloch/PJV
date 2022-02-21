import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import static java.lang.Math.max;

/** Class extending JPanel for graphical output of simulated world. */
public class ExtractDrawer extends JPanel {

    private Position worldSize;
    private Position zeroPosition;
    private int locationsPerDot;
    private Extract  extract;

    private SimulationDialog parentDialog;
    private Position requestedExtractTopLeft;
    private Position requestedExtractSize;

    private Point lastMousePos;

    private static final int DOTSIZE = 25;

    /**
     * Constructor of Extract drawer class.
     * @param worldSize size of the simulated world.
     * @param dialog parent window component of this drawer.
     */
    public ExtractDrawer(Position worldSize, SimulationDialog dialog) {
        this.parentDialog = dialog;
        this.worldSize = worldSize;
        zeroPosition = new Position(0,0);
        locationsPerDot = 1;
        updateExtractPositionAndSize();
        extract = null;
        this.setBackground(Color.LIGHT_GRAY);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos == null)
                    return;

                Position move = new Position(lastMousePos.x - e.getX(), lastMousePos.y - e.getY());
                lastMousePos = e.getPoint();
                Thread t = new Thread(() -> moveController(move));
                t.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                Thread t = new Thread(() -> zoomController(e.getPreciseWheelRotation()));
                t.start();
            }
        };

        this.addMouseWheelListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        drawExtract(g2d);
    }

    /**
     * Recalculates the needed extraction area to display. Updates requested extract properties.
     * Helps save some time and memory that would be needed to send extract of the whole simulated world.
     */
    public void updateExtractPositionAndSize(){
        requestedExtractTopLeft = new Position(
                (int)(-zeroPosition.getX() * locationsPerDot / (double)DOTSIZE),
                (int)(-zeroPosition.getY() * locationsPerDot / (double)DOTSIZE));

        requestedExtractSize = new Position(
                (this.getWidth()  / DOTSIZE + 2) * locationsPerDot,
                (this.getHeight() / DOTSIZE + 2) * locationsPerDot);
    }

    /**
     * RequestedExtractTopLeft getter.
     * @return Position - coordinates of topLeft corner of needed extract.
     */
    public Position getRequestedExtractTopLeft(){
        return requestedExtractTopLeft;
    }

    /**
     * RequestedExtractSize getter.
     * @return Position - size of the needed extract.
     */
    public Position getRequestedExtractSize(){
        return requestedExtractSize;
    }

    /**
     * Sets the extract to display to passed extract.
     * @param extract extract to draw on component
     */
    public void setExtract(Extract extract){
        this.extract = extract;
    }

    private void drawExtract(Graphics2D g2d){
        if(extract == null)
            return;

        //Draw outline around simulated world
        g2d.setColor(Color.BLACK);
        g2d.drawRect(zeroPosition.getX()-1, zeroPosition.getY() - 1,
                (int)(DOTSIZE * (worldSize.getX()/(double)locationsPerDot + 1)) + 1,
                (int)(DOTSIZE * (worldSize.getY()/(double)locationsPerDot + 1)) + 1);

        //Find topLeft corner of needed Extract
        Position extractTopLeft = new Position(
                (-zeroPosition.getX() / DOTSIZE) * locationsPerDot,
                (-zeroPosition.getY() / DOTSIZE) * locationsPerDot);

        //Find number of aggregated location extract to cover the component
        int partCountX = this.getWidth() / DOTSIZE + 2;
        int partCountY = this.getHeight() / DOTSIZE + 2;
        //Find coordinates of component where first locationExtracted aggregate will be painted
        int startX = zeroPosition.getX() + (extractTopLeft.getX() / locationsPerDot) * DOTSIZE;
        int startY = zeroPosition.getY() + (extractTopLeft.getY() / locationsPerDot) * DOTSIZE;

        //Get aggregated location extracts and draw them
        for(int i = 0; i < partCountX; i++){
            for(int j = 0; j < partCountY; j++){
                Stats stats = getStats(extractTopLeft.add(new Position(i * locationsPerDot, j * locationsPerDot)), locationsPerDot);
                drawPart(g2d, startX + i * DOTSIZE, startY + j * DOTSIZE, stats);
            }
        }
    }

    private Stats getStats(Position topLeft, int locationsPerPart){
        Stats stats = new Stats();
        for(int x = 0; x < locationsPerPart; x++){
            for(int y = 0; y < locationsPerPart; y++){
                ExtractLocation loc = extract.getExtractedLocation(topLeft.add(new Position(x, y)));
                if(loc != null) stats.update(loc.getStats());
            }
        }
        return stats;
    }

    /** Draws a circle sized and colored accordingly to health of people in that circles aggregated location.
     * Can represent one or more (neighbouring) locations */
    private void drawPart(Graphics2D g2d, int x, int y, Stats stats){
        double radiusPerPerson = DOTSIZE/(double)(locationsPerDot * 5);
        x += DOTSIZE / 2;
        y += DOTSIZE / 2;

        //vaccinated
        int peopleCount = stats.deceased + stats.cured + stats.quarantined + stats.infected + stats.healthy + stats.vaccinated;
        int radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.CYAN);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        peopleCount -= stats.vaccinated;
        //healthy
        radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.MAGENTA);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        peopleCount -= stats.healthy;
        //infected
        radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        peopleCount -= stats.infected;
        //quarantined
        radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.GREEN);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        peopleCount -= stats.quarantined;
        //cured
        radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.BLUE);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        peopleCount -= stats.cured;
        //deceased
        radius = (int)(radiusPerPerson * peopleCount);
        g2d.setColor(Color.RED);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

    }

    private void zoomController(double clicksRotated){
        //change number of locations aggregated in one dot
        this.locationsPerDot += (int) clicksRotated;
        int maxLocationsPerDot = max(worldSize.getX() / (getWidth() / DOTSIZE), worldSize.getY() / (getHeight() / DOTSIZE));
        if(locationsPerDot > maxLocationsPerDot){
            locationsPerDot = maxLocationsPerDot;
        }
        if(locationsPerDot < 1) locationsPerDot = 1;

        //update displayed area and display it
        updateExtractPositionAndSize();
        parentDialog.extractChanged();
        repaint();
    }

    private void moveController(Position move){
        //move displayed area and display it
        zeroPosition = zeroPosition.subtract(move);
        updateExtractPositionAndSize();
        parentDialog.extractChanged();
        repaint();
    }

}

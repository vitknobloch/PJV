import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


/** Class handling outputting stats to the stats-chart */
public class StatsDrawer {
    private JFreeChart chart;
    private XYSeries deceasedSeries;
    private XYSeries curedSeries;
    private XYSeries quarantinedSeries;
    private XYSeries infectedSeries;
    private XYSeries healthySeries;
    private XYSeries vaccinatedSeries;

    /** Stats drawer constructor, initializes the chart, creates the value series. */
    public StatsDrawer() {
        deceasedSeries = new XYSeries("Deceased");
        curedSeries = new XYSeries("Cured");
        quarantinedSeries = new XYSeries("Quarantined");
        infectedSeries = new XYSeries("Infected");
        healthySeries = new XYSeries("Healthy");
        vaccinatedSeries = new XYSeries("Vaccinated");

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        seriesCollection.addSeries(deceasedSeries);
        seriesCollection.addSeries(curedSeries);
        seriesCollection.addSeries(quarantinedSeries);
        seriesCollection.addSeries(infectedSeries);
        seriesCollection.addSeries(healthySeries);
        seriesCollection.addSeries(vaccinatedSeries);

        chart = ChartFactory.createXYLineChart("", "", "",
                seriesCollection, PlotOrientation.VERTICAL, true, false, false);
    }

    /** Returns the chart updated by this instance.
     * @return JFreeChart this components connected chart*/
    public JFreeChart getChart(){
        return chart;
    }

    /**
     * Adds passed stats to the chart.
     * @param stats this round's stats.
     * @param roundNumber this rounds number.
     */
    public void drawStats(Stats stats, int roundNumber){
        int height = stats.deceased;
        deceasedSeries.addOrUpdate(roundNumber, height);
        height += stats.cured;
        curedSeries.addOrUpdate(roundNumber, height);
        height += stats.quarantined;
        quarantinedSeries.addOrUpdate(roundNumber, height);
        height += stats.infected;
        infectedSeries.addOrUpdate(roundNumber, height);
        height += stats.healthy;
        healthySeries.addOrUpdate(roundNumber, height);
        height += stats.vaccinated;
        vaccinatedSeries.addOrUpdate(roundNumber, height);
    }
}

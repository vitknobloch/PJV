package cz.cvut.fel.pjv.logging;

import java.util.logging.*;

public class BasicLoggingUsage {

    private static final Logger LOG = Logger.getLogger(BasicLoggingUsage.class.getName());

    public static void main(String[] args) {
        LOG.setLevel(Level.FINEST);
        LOG.setUseParentHandlers(false);
        Handler handler = new StreamHandler(System.err, new SimpleFormatter());
        handler.setLevel(Level.FINEST);
        LOG.addHandler(handler);

        LOG.severe("Severe message");
        LOG.info("Info message");
        LOG.finest("Finest message");
    }

}

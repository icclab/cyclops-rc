package ch.icclab.cyclops.tnova;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by skoviera on 03/11/15.
 */
public class TNovaListener implements ServletContextListener {
    final static Logger logger = LogManager.getLogger(TNovaListener.class.getName());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("TNova Listener - successfully loaded");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.trace("TNova Listener - we are shutting down");
        TNovaScheduler.getInstance().stop();
    }
}

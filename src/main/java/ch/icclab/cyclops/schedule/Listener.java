package ch.icclab.cyclops.schedule;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by skoviera on 03/11/15.
 */
public class Listener implements ServletContextListener {
    final static Logger logger = LogManager.getLogger(Listener.class.getName());

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("TNova Listener - successfully loaded");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.trace("TNova Listener - we are shutting down");
        Scheduler.getInstance().stop();
    }
}

package ch.icclab.cyclops.schedule;

import ch.icclab.cyclops.tnova.RateRunner;
import ch.icclab.cyclops.tnova.TNovaRunner;
import ch.icclab.cyclops.util.Load;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Author: Martin Skoviera
 * Created on: 03-Nov-15
 * Description: Implementation of scheduler for requesting UDR from
 */
public class Scheduler {

    final static Logger logger = LogManager.getLogger(Scheduler.class.getName());

    // this class has to be a singleton
    private static Scheduler singleton = new Scheduler();

    // executor service (we only need one thread)
    private ScheduledExecutorService executor;

    /**
     * We need to hide constructor from public
     */
    private Scheduler() {
        this.executor = null;
    }

    /**
     * Simple implementation of Singleton class
     * @return instance of scheduler object
     */
    public static Scheduler getInstance() {
        return singleton;
    }

    /**
     * Starts execution run for every hour
     */
    public void start() {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();

            // start Usage records collection every full hour plus five minutes (13:05, 14:05, etc)
            executor.scheduleAtFixedRate(new RateRunner(), 0, Integer.parseInt(Load.configuration.get("schedulerFrequency")), TimeUnit.SECONDS);
            executor.scheduleAtFixedRate(new TNovaRunner(), 0, Integer.parseInt(Load.configuration.get("schedulerFrequency")), TimeUnit.SECONDS);
        }
    }

    /**
     * Stops execution run
     */
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Returns whether scheduler is running or not
     * @return
     */
    public Boolean isRunning() {
        return (executor != null);
    }

    /**
     * Manually (on top of scheduler) update Usage records from CloudStack
     */
    public void force() {
        Thread tnova = new Thread(new TNovaRunner());
        tnova.start();
        Thread rate = new Thread(new RateRunner());
        rate.start();
        //TODO:Manu i have to use executorService.invokeAll because of listener so we are able to kill it
//        ExecutorService executorService = Executors.newFixedThreadPool(2);
//        executorService.invoke

    }
}

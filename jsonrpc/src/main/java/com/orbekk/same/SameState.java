package com.orbekk.same;

import java.util.List;
import java.util.LinkedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of a 'Same' state.
 *
 * This class manages the current state of the Same protocol.
 */
public class SameState extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<String> participants = new LinkedList<String>();
    private String currentState = "";
    private String networkName;
    private boolean stopped = false;

    /**
     * Queue for pending participants.
     */
    private List<String> pendingParticipants = new LinkedList<String>();

    public SameState(String networkName) {
        this.networkName = networkName;
    }

    public synchronized List<String> getParticipants() {
        return participants;
    }

    public String getNetworkName() {
        return networkName;
    }

    public String getCurrentState() {
        return currentState;
    }

    public synchronized void addParticipant(String url) {
        synchronized(this) {
            logger.info("Add pending participant: {}", url);
            pendingParticipants.add(url);
            notifyAll();
        }
    }

    private synchronized void handleNewParticipants() {
        for (String url : pendingParticipants) {
            logger.info("New participant: {}", url);
            participants.add(url);
        }
        pendingParticipants.clear();
    }

    public synchronized void run() {
        while (!stopped) {
            handleNewParticipants();
            try {
                wait(1000);
            } catch (InterruptedException e) {
                // Ignore interrupt in wait loop.
            }
        }
    }

    public synchronized void stopSame() {
        try {
            stopped = true;
            notifyAll();
            this.join();
        } catch (InterruptedException e) {
            logger.warn("Got InterruptedException while waiting for SameState " +
                    "to finish. Ignoring.");
        }
    }
}

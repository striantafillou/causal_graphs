package edu.cmu.tetrad.util;

import java.util.List;

/**
 * Represents the configuration for the logger.  The idea is that each model has its own logger
 * configuration, which is merely the events that the model supports.
 *
 * @author Tyler Gibson
 */
public interface TetradLoggerConfig {


    /**
     * States whether the event associated with the given id is active, that is whether
     * it should be logged or not.
     *
     * @param id
     * @return
     */
    public boolean isEventActive(String id);


    /**
     * States whether the config is active or not. THe config is considered active if there is
     * at least one active event.
     *
     * @return - true iff its active.
     */
    public boolean isActive();


    /**
     * Returns all the events that are supported.
     *
     * @return - events
     */
    public List<Event> getSupportedEvents();


    /**
     * Sets whether the event associated with the given id is active or not.
     *
     * @param id
     * @param active
     */
    public void setEventActive(String id, boolean active);


    /**
     * Represents an event which is just an id and a description.
     */
    public interface Event {


        /**
         * Returns the id of the event (should be unique).
         *
         * @return
         */
        public String getId();


        /**
         * Returns a brief description for the event.
         *
         * @return
         */
        public String getDescription();


    }


}

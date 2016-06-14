package edu.cmu.tetrad.util;


import java.util.*;

/**
 * Logger configuration.
 *
 * @author Tyler Gibson
 */
@SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
public class DefaultTetradLoggerConfig implements TetradLoggerConfig {


    /**
     * The events that are supported.
     */
    private List<Event> events;


    /**
     * The event ids that are currently active.
     */
    private final Set<String> active = new HashSet<String>();


    /**
     * Constructs the config given the events in it.
     *
     * @param events The events that the logger reports.
     */
    public DefaultTetradLoggerConfig(List<Event> events) {
        if (events == null) {
            throw new NullPointerException("The given list of events must not be null");
        }
        this.events = new ArrayList<Event>(events);
    }


    /**
     * Constructs the config for the given event ids. This will create <code>Event</code>s with
     * no descriptions.
     *
     * @param events The events that the logger reports.
     */
    public DefaultTetradLoggerConfig(String... events) {
        this.events = new ArrayList<Event>(events.length);
        for (String event : events) {
            this.events.add(new DefaultEvent(event, "No Description"));
        }
    }

    //=========================== public methods ================================//

    public boolean isEventActive(String id) {
        return this.active.contains(id);
    }

    public boolean isActive() {
        return !this.active.isEmpty();
    }

    public List<Event> getSupportedEvents() {
        return Collections.unmodifiableList(this.events);
    }

    public void setEventActive(String id, boolean active) {
        if (!contains(id)) {
            throw new IllegalArgumentException("There is no event known under the given id: " + id);
        }
        if (active) {
            this.active.add(id);
        } else {
            this.active.remove(id);
        }
    }

    //======================= Private Methods ==================================//

    private boolean contains(String id) {
        for (Event event : this.events) {
            if (id.equals(event.getId())) {
                return true;
            }
        }
        return false;
    }

    //================================= Inner class ==================================//

    public static class DefaultEvent implements TetradLoggerConfig.Event {


        private String id;
        private String description;


        public DefaultEvent(String id, String description) {
            if (id == null) {
                throw new NullPointerException("The given id must not be null");
            }
            if (description == null) {
                throw new NullPointerException("The given description must not be null");
            }
            this.id = id;
            this.description = description;
        }


        public String getId() {
            return this.id;
        }

        public String getDescription() {
            return this.description;
        }
    }

}

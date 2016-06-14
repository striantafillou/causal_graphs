package edu.cmu.tetrad.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a logging utility used throughout tetrad.  Unlike a typical logger this does not
 * work on levels, instead one can set events need to be logged. This is done by giving
 * the logger a <code>TetradLoggerConfig</code> which will be used to determine whether
 * some event should be logged.
 * <p/>
 * Although one can explicity construct instance of <code>TetradLoggerConfig</code> and
 * set them in the logger, the configuration details for most models is defined in the
 * <code>configuration.xml</code> file and added to the logger at startup.  A pre-configured
 * <code>TetradLoggerConfig</code> for some model can be found by calling
 * <code>getTetradLoggerConfigForModel(Class)</code>
 * <p/>
 * Furthermore the logger supports logging to a sequence of files in some directory. To start logging
 * to a new file in the logging directory (assuming it has been set) call <code>setNextOutputStream</code>
 * to remove this stream from the logger call <code>removeNextOutputStream</code>. In addiong to the feature
 * arbitrary streams can be add and removed from the logger by calling <code>addOutputStream</code> and
 * <code>removeOutputStream</code>.
 *
 * @author Tyler Gibson
 */
@SuppressWarnings({"MethodMayBeStatic"})
public class TetradLogger {


    /**
     * A mapping between output streams and writers used to wrap them.
     */
    private final Map<OutputStream, Writer> writers = new LinkedHashMap<OutputStream, Writer>();


    /**
     * States whether events should be logged, this allows one to turn off all loggers at once.
     * (Note, a field is used, since fast lookups are important)
     */
    private boolean logging = Preferences.userRoot().getBoolean("loggingActivated", false);


    /**
     * A mapping from model classes to their configured loggers.
     */
    private final Map<Class, TetradLoggerConfig> configMap = new ConcurrentHashMap<Class, TetradLoggerConfig>();


    /**
     * The configuration to use to determine which events to log.
     */
    private TetradLoggerConfig config;


    /**
     * The listeners.
     */
    private final List<TetradLoggerListener> listeners = new ArrayList<TetradLoggerListener>();


    /**
     * The current file stream that is being written to, this is set in "setNextOutputStream()".s
     */
    private OutputStream stream;


    /**
     * Forces the logger to log all output.
     */
    private boolean forceLog = false;


    /**
     * The singleton instance of the logger.
     */
    private static final TetradLogger INSTANCE = new TetradLogger();


    /**
     * Private constructor, this is a singleton.
     */
    private TetradLogger() {

    }

    //=============================== Public methods ===================================//


    /**
     * Returns the singleton instance of the <code>TetradLogger</code>.
     *
     * @return - instance
     */
    public static TetradLogger getInstance() {
        return INSTANCE;
    }


    /**
     * Adds the given listener to the logger.
     *
     * @param l
     */
    public void addTetradLoggerListener(TetradLoggerListener l) {
        this.listeners.add(l);
    }


    /**
     * Removes the given listener from the logger.
     *
     * @param l
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void removeTetradLoggerListener(TetradLoggerListener l) {
        this.listeners.remove(l);
    }


    /**
     * Sets what configuration should be used to determine which events to log. Null can be
     * given to remove a previously set configuration from the logger.
     *
     * @param config
     */
    public void setTetradLoggerConfig(TetradLoggerConfig config) {
        TetradLoggerConfig previous = this.config;
        if (config == null) {
            this.config = null;
            if (previous != null) {
                this.fireDeactived();
            }
        } else {
            this.config = config;
            this.fireActived(this.config);
        }
    }


    /**
     * This can be used to tell the logger which events to log without having
     * to first define a <code>TetradLoggerConfig</code>.
     *
     * @param events
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void setEventsToLog(String ... events){
        setTetradLoggerConfig(new DefaultTetradLoggerConfig(events));
    }


    /**
     * Forces the logger to log all events, useful for testing.
     */
    public void setForceLog(boolean force) {
        this.forceLog = force;
        if(!force){
            flush();
        }
    }


    /**
     * If there is a pre-defined configuration for the given model it is set, otherwise
     * an exception is thrown.
     *
     * @param model
     */
    public void setTetradLoggerConfigForModel(Class model) {
        TetradLoggerConfig config = this.configMap.get(model);
        if (config == null) {
            System.out.println("There is no pre-defined logger config for the model " + model);
        }
        setTetradLoggerConfig(config);
    }


    /**
     * Adds the given <code>TetradLoggerConfig</code>  to the logger, so that it can be used
     * throughout the live of the application.
     *
     * @param model
     */
    public void addTetradLoggerConfig(Class model, TetradLoggerConfig config) {
        this.configMap.put(model, config);
    }


    /**
     * Returns the <code>TetradLoggerConfig</code> associated with the given model.
     *
     * @param model
     * @return - config
     */
    public TetradLoggerConfig getTetradLoggerConfigForModel(Class model) {
        return this.configMap.get(model);
    }


    /**
     * Resets the logger by removing any configuration info set with <code>setTetradLoggerConfig</code>.
     */
    public void reset() {
        this.config = null;
        flush();
    }


    /**
     * States whether the logger is turned on or not.
     *
     * @return true iff the logger is logging.
     */
    public boolean isLogging() {
        return this.logging;
    }


    /**
     * Sets whether the logger is on or not.
     *
     * @param logging
     */
    public void setLogging(boolean logging) {
        Preferences.userRoot().putBoolean("loggingActivated", logging);
        this.logging = logging;
    }


    /**
     * Flushes the writers.
     */
    public void flush() {
        if (this.logging) {
            try {
                for (Writer writer : this.writers.values()) {
                    writer.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for(OutputStream stream : this.writers.keySet()){
            if(stream instanceof LogDisplayOutputStream){
                LogDisplayOutputStream logStream = (LogDisplayOutputStream)stream;
                logStream.moveToEnd();
            }
        }
    }

    /**
     * Logs the given message with a newline associated with the given event.
     *
     * @param event   - The name of the log event.
     * @param message - The messag eto be logged.
     */
    public void log(String event, String message) {
        if (this.logging && isEventActive(event) && !writers.isEmpty()) {
            try {
                for (Writer writer : writers.values()) {
                    writer.write(message);
                    writer.write("\n");
                    writer.flush();
                }
            } catch (IOException e) {
                // should be an error stream too?
                e.printStackTrace();
            }
        }
    }


    /**
     * Logs the "info" event for the given message, equivalent to <code>log("info", message)</code>.
     *
     * @param message
     */
    public void info(String message) {
        log("info", message);
    }


    /**
     * Convenience method for <code>log("edgeOriented", message)</code>.
     *
     * @param message
     */
    public void edgeOriented(String message) {
        log("edgeOriented", message);
    }


    /**
     * Convenience method for <code>log("independenceDetails", message)</code>.
     *
     * @param message
     */
    public void independenceDetails(String message) {
        log("independenceDetails", message);
    }


    /**
     * Convenience method for <code>log("colliderOriented", message)</code>.
     *
     * @param message
     */
    public void colliderOriented(String message) {
        log("edgeOriented", message);
    }

    /**
     * Convenience method for <code>log("details", message)</code>.
     *
     * @param message
     */
    public void details(String message) {
        log("details", message);
    }

    /**
     * Logs an error, this will log the message regardless of any configuration information.
     * Although it won't be logged if the logger is off and of course if there are no streams
     * attached.
     *
     * @param message
     */
    public void error(String message) {
        if (this.logging) {
            if (this.config == null) {

            }
            try {
                for (Writer writer : writers.values()) {
                    writer.write(message);
                    writer.write("\n");
                }
            } catch (IOException e) {
                // should be an error stream too?
                e.printStackTrace();
            }
        }
    }


    /**
     * Logs the given message regardless of the logger's current settings. Although nothing
     * will be logged if the logger has been turned off.
     *
     * @param message
     */
    public void forceLogMessage(String message) {
        if (this.logging) {
            if (this.config == null) {
                this.fireActived(new EmptyConfig(true));
            }
            try {
                for (Writer writer : writers.values()) {
                    writer.write(message);
                    writer.write("\n");
                }
            } catch (IOException e) {
                // should be an error stream too?
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets the <code>OutputStream</code> that is used to log matters out to.
     *
     * @param stream
     */
    public void addOutputStream(OutputStream stream) {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
        this.writers.put(stream, writer);
    }


    /**
     * Removes the given stream from the logger.
     *
     * @param stream
     */
    public void removeOutputStream(OutputStream stream) {
        this.writers.remove(stream);
    }


    /**
     * Removes all streams from the logger.
     */
    public void clear() {
        for (OutputStream stream : this.writers.keySet()) {
            if (stream != System.out) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        this.writers.clear();
        this.stream = null;
    }


    /**
     * Sets the next output stream to use for logging, call <code>removeNextOutputStream</code> to remove it.
     * This will create the next output file in the output directory and form a stream from it and add it
     * to the logger.
     *
     * @throws IllegalStateException - Thrown if there is an error setting the stream, the message will state
     *                               the nature of the error.
     */
    public void setNextOutputStream() {
        if (logging && this.isFileLoggingEnabled()) {
            File dir = new File(getLoggingDirectory());
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new IllegalStateException("Could not create the output directory "
                            + dir.getAbsolutePath() + ".");
                }
            }
            if (!dir.canWrite()) {
                throw new IllegalStateException("Cannot write to the directory choosen for saving output " +
                        " logs (" + dir.getAbsolutePath() + "). Please pick another directory.");
            }
            // get the next file name to use.
            String prefix = getLoggingFilePrefix();
            List<String> files = Arrays.asList(dir.list());
            int index = 1;
            String name = prefix + (index++) + ".txt";
            while (files.contains(name)) {
                name = prefix + (index++) + ".txt";
            }
            // finally create log file and add a stream to the logger
            File logFile = new File(dir.getAbsolutePath() + "/" + name);
            OutputStream old = this.stream;
            try {
                this.stream = new FileOutputStream(logFile);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("Could not create file in output directory ("
                        + dir.getAbsolutePath() + ").");
            }
            if (old != null) {
                removeOutputStream(old);
            }
            addOutputStream(this.stream);
        }
    }


    public void removeNextOutputStream() {
        flush();
        if (this.stream != null) {
            removeOutputStream(this.stream);
            try {
                this.stream.close();
            } catch (IOException e) {
                // do nothing.
            }
            this.stream = null;
        }
    }

    /**
     * Returns the logging prefix.
     *
     * @return - prefix
     */
    public String getLoggingFilePrefix() {
        return Preferences.userRoot().get("loggingPrefix", "output");
    }


    /**
     * Sets whether the display log should be used or not.
     */
    public void setDisplayLogEnabled(boolean enabled) {
        Preferences.userRoot().putBoolean("enableDisplayLogging", enabled);
    }


    /**
     * States whether to display the log display.
     */
    public boolean isDisplayLogEnabled() {
        return Preferences.userRoot().getBoolean("enableDisplayLogging", true);
    }


    /**
     * Sets the logging prefix.
     *
     * @param loggingFilePrefix
     */
    public void setLoggingFilePrefix(String loggingFilePrefix) {
        if (loggingFilePrefix == null) {
            throw new NullPointerException();
        }

        if ("".equals(loggingFilePrefix)) {
            throw new IllegalArgumentException("Empty prefix name; ignored.");
        }

        Preferences.userRoot().put("loggingPrefix", normalize(loggingFilePrefix));
    }


    /**
     * States whether file logging is enabled or not.
     */
    public boolean isFileLoggingEnabled() {
        return Preferences.userRoot().getBoolean("enableFileLogging", false);
    }


    /**
     * Sets whether "file logging" is enabled or not, that is whether
     * calls to <code>setNextOutputStream</code> will be respected.
     *
     * @param enabled
     */
    public void setFileLoggingEnabled(boolean enabled) {
        Preferences.userRoot().putBoolean("enableFileLogging", enabled);
    }


    /**
     * States whether the automatic log display is enabled or not, or returns null if
     * there is no value stored in the user's prefs.
     */
    public Boolean isAutomaticLogDisplayEnabled(){
        String s = Preferences.userRoot().get("allowAutomaticLogDisplay", "unknown");
        if(s.equals("unknown")){
            return null;
        }
        return s.equals("allow");
    }

    /**
     * States whether log displays should be automatically displayed or not.
     */
    public void setAutomaticLogDisplayEnabled(boolean enable){
        Preferences.userRoot().put("allowAutomaticLogDisplay", enable ? "allow" : "disallow");
    }



    /**
     * Returns the the default logging directory.
     *
     * @return - logging directory.
     */
    public String getLoggingDirectory() {
        return Preferences.userRoot().get("loggingDirectory",
                Preferences.userRoot().absolutePath());
    }

    /**
     * Sets the logging directory, but first checks whether we can write to it etc.
     *
     * @param directory - The directory to set.
     * @throws IllegalStateException if there is a problem with the directory.
     */
    public void setLoggingDirectory(String directory) {
        File selectedFile = new File(directory);

        if (selectedFile.exists() && !selectedFile.isDirectory()) {
            throw new IllegalStateException("That 'output directory' is actually a file, not a directory");
        }

        if (selectedFile.exists() && selectedFile.isDirectory() & !selectedFile.canWrite()) {
            throw new IllegalStateException("The output directory cannot be written to.");
        }

        if (!selectedFile.exists()) {
            boolean created = selectedFile.mkdir();

            if (!created) {
                throw new IllegalStateException("The output directory cannot be created. ");
            }

            if (!selectedFile.canWrite()) {
                throw new IllegalStateException("That output directory cannot be written to. " +
                        "Keeping the old one.");
            }
            selectedFile.delete();
        }

        Preferences.userRoot().put("loggingDirectory", selectedFile.getAbsolutePath());
    }

    //========================================= Private Method ============================//


    /**
     * States whether the given event is active or not.
     */
    private boolean isEventActive(String id) {
        return this.forceLog || (this.config != null && this.config.isEventActive(id));
    }


    /**
     * Normalizes the prefix.
     */
    private String normalize(String prefix) {
        StringBuffer buf = new StringBuffer();
        Pattern pattern = Pattern.compile("[a-zA-Z_]");
        for (int i = 0; i < prefix.length(); i++) {
            String s = prefix.substring(i, i + 1);
            Matcher matcher = pattern.matcher(s);
            if (matcher.matches()) {
                buf.append(s);
            }
        }
        return buf.toString();
    }


    private void fireActived(TetradLoggerConfig config) {
        if (this.logging && !this.listeners.isEmpty()) {
            TetradLoggerEvent evt = new TetradLoggerEvent(this, config);
            for (TetradLoggerListener l : this.listeners) {
                l.configurationActived(evt);
            }
        }
    }


    private void fireDeactived() {
        if (this.logging && !this.listeners.isEmpty() && this.config == null) {
            TetradLoggerEvent evt = new TetradLoggerEvent(this, null);
            for (TetradLoggerListener l : this.listeners) {
                l.configurationDeactived(evt);
            }
        }
    }

    //================================ Inner classes ====================================//

    /**
     * A empty config, where no event is active.
     */
    private static class EmptyConfig implements TetradLoggerConfig {

        private final boolean active;


        @SuppressWarnings({"SameParameterValue"})
        public EmptyConfig(boolean active) {
            this.active = active;
        }


        public boolean isEventActive(String id) {
            return this.active;
        }

        public boolean isActive() {
            return this.active;
        }

        public List<Event> getSupportedEvents() {
            if (!this.active) {
                return Collections.emptyList();
            }
            throw new UnsupportedOperationException("Not supported if active is true");
        }

        public void setEventActive(String id, boolean active) {
            throw new UnsupportedOperationException("Can't modify the logger config");
        }
    }


    /**
     * Represents an output stream that can get its own length.
     */
    public static interface LogDisplayOutputStream {


        /**
         * The total string length written to the text area.
         *
         * @return The total string length written to the text area.
         */
        @SuppressWarnings({"UnusedDeclaration"})
        public int getLengthWritten();


        /**
         * Should move the log to the end of the stream.
         */
        public void moveToEnd();

    }





}

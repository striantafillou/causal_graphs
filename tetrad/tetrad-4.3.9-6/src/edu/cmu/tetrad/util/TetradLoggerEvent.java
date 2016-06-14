package edu.cmu.tetrad.util;

import java.util.EventObject;

/**
 * An event associated with the <code>TetradLoggerListener</code>.
 *
 * @author Tyler Gibson
 */
public class TetradLoggerEvent extends EventObject {


    private final TetradLoggerConfig config;


    /**
     * Constructs the event given the source and the <code>TetradLoggerConfig</code>
     * associated with the event if there is one
     *
     * @param source - The source
     * @param config - The config, may be null.
     */
    public TetradLoggerEvent(Object source, TetradLoggerConfig config){
        super(source);
        this.config = config;
    }



    public TetradLoggerConfig getTetradLoggerConfig(){
        return this.config;
    }




}

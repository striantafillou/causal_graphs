package edu.cmu.tetrad.manip.experimental_setup;


/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Oct 7, 2004
 * Time: 5:03:44 PM
 * To change this template use File | Settings | File Templates.
 *
 * This class describes each variable in an experimental setup. It keeps track
 * of its individual manipulation status and whether it's studied (or ignored)
 * in the experiment.
 */
public class ExperimentalSetupVariable  {

    private String name;
    //private Manipulation manipulation;
    private VariableManipulation manipulation;
    private boolean isStudied;

    private double mean;
    private double stdDev;
    private String lockedAt;


    public ExperimentalSetupVariable(String name){
        this(name, VariableManipulation.NONE, true, 0, 1);
    }

    public ExperimentalSetupVariable(String name,
                                     VariableManipulation manip,
                                     boolean isStudied){
        this(name, manip, isStudied, 0, 1);
    }

    /**
     * Copy Constructor.
     * @param esv the variable to copy.
     */
    public ExperimentalSetupVariable(ExperimentalSetupVariable esv, double mean, double stdDev){
        this(esv.getName(), esv.getManipulation(), esv.isStudied(), mean, stdDev);
    }

    /**
     * Constructor.
     * @param name the name of the variable.
     */
    public ExperimentalSetupVariable (String name,
                                      VariableManipulation manip,
                                      boolean isStudied,
                                      double mean,
                                      double stdDev){
        if(name == null){ throw new NullPointerException("Name cannot be null");}
        if(name.length() == 0){ throw new IllegalArgumentException("Name cannot be empty");}

        this.name = name;
        manipulation = manip;
        this.isStudied = isStudied;
        this.mean = mean;
        this.stdDev = stdDev;
    }

    //======================================================================
    //
    //  GETTERS and SETTERS
    //
    //======================================================================

    /**
     * @return the variable name.
     */
    public String getName() {
        return name;
    }

    public VariableManipulation getManipulation(){
        return manipulation;
    }


//    /**
//     * @return the variable manipulation status.
//     */
//    public Manipulation getManipulation() {
//        return manipulation;
//    }


    /**
     * @return if the variable is studied in the experimental setup.
     */
    public boolean isStudied() throws IllegalArgumentException{
        return isStudied;
    }

    /**
     * Set this variable to be randomized.
     */
    public void setRandomized() {
        //manipulation = new Randomized();
        manipulation = VariableManipulation.RANDOMIZED;
    }


    public double getMean() {
        return this.mean;
    }
    public double getStandardDeviation(){
        return this.stdDev;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setStandardDeviation(double stdDev){
        this.stdDev = stdDev;
    }


    /**
     * Set this variable to be locked.
     */
    public void setLocked(String lockedValue) throws IllegalArgumentException {
//        Locked l = new Locked();
//        l.setLockedAt(lockedValue);
//        manipulation = l;
        lockedAt = lockedValue;
        manipulation = VariableManipulation.LOCKED;
    }

    public String getLockedAtValue(){
        if(manipulation == VariableManipulation.LOCKED){
            return lockedAt;
        }else{
            return null;
        }
    }

    /**
     * Remove all manipulation on this variable.
     */
    public void setUnmanipulated() throws IllegalArgumentException {
        //manipulation = new None();
        manipulation = VariableManipulation.NONE;
    }

    /**
     * Set this variable to be studied or ignored in the experiment.
     */
    public void setStudied(boolean isStudied) throws IllegalArgumentException{
        this.isStudied = isStudied;
    }


}

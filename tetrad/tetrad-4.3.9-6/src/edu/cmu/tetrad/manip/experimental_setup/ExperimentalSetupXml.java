package edu.cmu.tetrad.manip.experimental_setup;

import nu.xom.Attribute;
import nu.xom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: mattheweasterday
 * Date: Oct 7, 2004
 * Time: 10:54:14 PM
 *
 * Translates between experimental setup and xml.
 *
 */
public class ExperimentalSetupXml {

    public final static String EXPSETUP        = "expSetup";
    public final static String NAME            = "name";
    public final static String IGNORED         = "ignored";
    public final static String EXPVARIABLE     = "expVariable";
    public final static String MANIPULTATION   = "manipulation";
    public final static String LOCKEDAT        = "lockedAt";


    /**
     * Convert an experimental setup to xml.
     * @param esvs the experimental setup.
     * @return xml representation of the experimental setup.
     */
    public static Element renderStudiedVariables(ExperimentalSetup esvs){
        Element esElement = new Element(EXPSETUP);
        esElement.addAttribute(new Attribute(NAME, esvs.getName()));
        Element var;

        String [] names = esvs.getVariableNames();
        for(int i = 0; i < names.length; i++){
            var = new Element(EXPVARIABLE);
            var.addAttribute(new Attribute(NAME, names[i]));
            var.addAttribute(new Attribute(IGNORED, esvs.getVariable(names[i]).isStudied() ? "no" : "yes")); //$NON-NLS-1$ //$NON-NLS-2$
            var.addAttribute(new Attribute(MANIPULTATION, esvs.getVariable(names[i]).getManipulation().toString()));
            if(esvs.getVariable(names[i]).getManipulation() == VariableManipulation.LOCKED){
                var.addAttribute(new Attribute(LOCKEDAT, esvs.getVariable(names[i]).getLockedAtValue()));
            }
            esElement.appendChild(var);
        }
        return esElement;
    }

}

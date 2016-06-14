package edu.cmu.tetradapp.model;

/**
 * Search params for algorithms that use the <code>MeekRules</code>.
 *
 * @author Tyler Gibson
 */
public interface MeekSearchParams extends SearchParams{

    public boolean isAggressivelyPreventCycles();

    public void setAggressivelyPreventCycles(boolean aggressivelyPreventCycles);

}

package edu.cmu.tetrad.search;

import cern.jet.random.Normal;

public class GaussianConfidenceInterval {
    private double alpha; //but is it a significance test?

	public GaussianConfidenceInterval(double alpha){
		this.alpha = alpha;
	}
	
	/**
	 * two-sided confidence interval
	 * @param mean
	 * @param sampleSd
	 * @return whether we reject the hypothesis that 'value' was sampled from this normal distribution
	 * with confidence alpha
	 */
	public boolean test(double value, double mean, double sampleSd, int sampleSize){
        double score = new Normal(mean, sampleSd, null).cdf(value);
		return (score < alpha / 2.0) || (score > 1.0 - (alpha) / 2.0);
	}
}

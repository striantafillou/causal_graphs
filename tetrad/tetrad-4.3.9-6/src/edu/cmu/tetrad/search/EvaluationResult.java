package edu.cmu.tetrad.search;

/**
 * The purpose of this class is to store evaluation results.
 * 
 * @author Gustavo
 *
 */
public class EvaluationResult {

    public interface PartialEvaluationResult {
        public double[] values();
    }

    static class AdjacencyEvaluationResult implements PartialEvaluationResult {
		Integer errorsOfOmission;
		Integer errorsOfCommission;
		
		public AdjacencyEvaluationResult(Integer errorsOfOmission, Integer errorsOfCommission) {
			super();
			this.errorsOfOmission = errorsOfOmission;
			this.errorsOfCommission = errorsOfCommission;
		}

		public double loss() {
			return errorsOfOmission + errorsOfCommission;
		}
	
		public double[] values(){
			return new double[]{errorsOfOmission,errorsOfCommission,loss()};
		}
	}
	
	static class OrientationEvaluationResult implements PartialEvaluationResult {
		Integer nCorrect;
		Integer directedWrongWay;
		Integer undirectedWhenShouldBeDirected;
		Integer directedWhenShouldBeUndirected;

		
		public OrientationEvaluationResult(Integer correct, Integer directedWrongWay,
				Integer undirectedWhenShouldBeDirected, Integer directedWhenShouldBeUndirected) {
			super();
			this.nCorrect = correct;
			this.directedWrongWay = directedWrongWay;
			this.undirectedWhenShouldBeDirected = undirectedWhenShouldBeDirected;
			this.directedWhenShouldBeUndirected = directedWhenShouldBeUndirected;
		}

		public double[] values(){
			return new double[]{nCorrect,directedWrongWay,undirectedWhenShouldBeDirected,directedWhenShouldBeUndirected};
		}
		
//		public double loss(){
//			
//		}
	
	}

	static class CoefficientEvaluationResult implements PartialEvaluationResult {
		Double totalCoeffErrorSq;
		Integer nEdgesEvaluated;

		public CoefficientEvaluationResult(Double totalCoeffErrorSq, Integer edgesEvaluated) {
			super();
			this.totalCoeffErrorSq = totalCoeffErrorSq;
			this.nEdgesEvaluated = edgesEvaluated;
		}
		
		double loss(){
			return totalCoeffErrorSq;
		}
		
		public double[] values(){
			return new double[]{totalCoeffErrorSq,nEdgesEvaluated,loss()};
		}
		
	}


	AdjacencyEvaluationResult adj;
	OrientationEvaluationResult ori;
	CoefficientEvaluationResult coeffAll;
	CoefficientEvaluationResult coeffSome;
	
	PatternEvaluationResult pat;
	
	String name = null;

	/**
	 * Loss function for PC:
	 * * for adjacency errors, 1 pt (i.e. 1 for omission, 1 for commission)
	 * for orientation errors:
	 * * undirected when it should be directed: 0.5
	 * * directed when it should be undirected: 0.5
	 * * directed the wrong way: 1.0
     * (in other words, 0.5 for each arrow-head difference, for orientation errors)
	 */
	static class PatternEvaluationResult {

		AdjacencyEvaluationResult adj;
		OrientationEvaluationResult ori;

		public PatternEvaluationResult(AdjacencyEvaluationResult adj, OrientationEvaluationResult ori) {
			this.adj = adj;
			this.ori = ori;
		}

		public double loss() {
			double oriLoss = ori.directedWrongWay + 0.5 * ori.undirectedWhenShouldBeDirected +
			0.5 * ori.directedWhenShouldBeUndirected;

			double adjLoss = 1.5 * adj.errorsOfOmission + 1.0 * adj.errorsOfCommission;			

			//			System.out.println("adjLoss = " + adjLoss);
//			System.out.println("oriLoss = " + oriLoss);
			double loss = adjLoss + oriLoss;
//			System.out.println("returning loss = " + loss);			
			return loss;
		}
		
	}

	
	/**
	 * constructor for evaluations where the method evaluated purports to give us the entire structure.
	 * @param methodName
	 * @param adj
	 * @param ori
	 * @param coeffAll
	 * @param coeffSome
	 */
	public EvaluationResult(String methodName, AdjacencyEvaluationResult adj, OrientationEvaluationResult ori,
			CoefficientEvaluationResult coeffAll, CoefficientEvaluationResult coeffSome) {
		super();
		this.name = methodName;
		this.adj = adj;
		this.ori = ori;
		this.coeffAll = coeffAll;
		this.coeffSome = coeffSome;

	}		
	
	/**
	 * constructor for evaluations where the method evaluated purports to give us the Markov-equivalence class,
	 * represented by a pattern.
	 * 
	 * 	 * @param methodName
	 * @param pat
	 */
	public EvaluationResult(String methodName, PatternEvaluationResult pat) {
		super();
		this.name = methodName;
		this.pat = pat;
	}		
	

}

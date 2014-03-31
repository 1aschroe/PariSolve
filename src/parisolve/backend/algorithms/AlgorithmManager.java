package parisolve.backend.algorithms;

import java.util.Arrays;
import java.util.List;

public class AlgorithmManager {

	/**
	 * returns a list of currently implemented algorithms to solve parity games.
	 * 
	 * @return algorithms, able to solve parity games
	 */
	public static final List<Solver> getAlgorithms() {
	    return Arrays.asList(new PrimitiveAlgorithm(), new SimpleAlgorithm(), new RecursiveAlgorithm());
	}

}

package parisolve.backend.algorithms;

import java.util.Arrays;
import java.util.List;

/**
 * central class to get instances of parity-solving algorithms from.
 * 
 * @author Arne Schr�der
 */
public final class AlgorithmManager {
    private AlgorithmManager() {
        // empty and private as this is a utility class with static methods
        // only.
    }

    /**
     * returns a list of currently implemented algorithms to solve parity games.
     * 
     * @return algorithms, able to solve parity games
     */
    public static List<Solver> getAlgorithms() {
        return Arrays.asList(new RecursiveAlgorithm(), new BetterAlgorithm(),
                new MemoizedRecursiveAlgorithm(),
                new AttractiveBetterAlgorithm(),
                new MediumSizedStepAlgorithm(), new BigStepAlgorithm(),
                new StrategyImprovementAlgorithm());
    }

}

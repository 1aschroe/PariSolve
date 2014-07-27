package parisolve.backend.algorithms.helper;


/**
 * helper class to return both a solution and the time it took to calculate
 * this solution.
 * 
 * @author Arne Schröder
 */
public class SolutionWithTime {
    private final Solution solution;
    private final long time;

    public SolutionWithTime(final Solution solution, final long time) {
        this.solution = solution;
        this.time = time;
    }

    public Solution getSolution() {
        return solution;
    }

    public long getTime() {
        return time;
    }
}
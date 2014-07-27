package parisolve;

import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

/**
 * provides methods to measure and benchmark the performance of algorithms for
 * solving parity games.
 * 
 * @author Arne Schröder
 */
public final class BenchmarkingManager {
    /**
     * private constructor to disable instantiation.
     */
    private BenchmarkingManager() {
        // disabling instantiation of this utility class
    }

    /**
     * determines how many random arenas are generated for one set of
     * parameters.
     */
    static final int SAMPLE_SIZE = 5;
    /**
     * determines, how long measurements may become before the iteration is
     * stopped.
     */
    static final long MAX_TIME_TO_SAMPLE = 10000;
    /**
     * determines the minimal size to try when benchmarking. Note that too small
     * sizes lead to uncertain measurements as environmental factors become
     * dominant. Therefore, <code>measure()</code> may take quite some time to
     * find a suitable sample.
     */
    static final int MIN_N = 10000;
    /**
     * determines the minimal maximal priority to try when benchmarking.
     */
    static final int MIN_C = 2;
    /**
     * determines the maximal maximal priority to try when benchmarking. Setting
     * this value makes sense despite <code>MAX_TIME_TO_SAMPLE</code> provides a
     * criterion for termination, because <code>RecursiveAlgorithm</code> seems
     * to not be very sensitive to the maximal priority so an increase can be
     * done indefinitely.
     */
    static final int MAX_C = 8;

    /**
     * benchmarks the algorithms. It does so by generating different arenas for
     * increasing parameters until the measured time reaches the value of
     * <code>MAX_TIME_TO_SAMPLE</code>. Measurements are printed in a tabular
     * layout. In order to be able to inspect arenas with unusual results, all
     * areas generated are stored in the folder <code>last_arenas</code> with a
     * descriptive file name.
     */
    protected static void doBenchmarking() {
        for (final Solver solver : AlgorithmManager.getAlgorithms()) {
            System.out.println(solver.getClass().getSimpleName());
            double lastTime = 0;
            double firstTime = 0;
            int n = MIN_N;
            int c = MIN_C;
            while (firstTime < MAX_TIME_TO_SAMPLE) {
                while (lastTime < MAX_TIME_TO_SAMPLE && c <= MAX_C) {
                    DescriptiveStatistics statistics = new DescriptiveStatistics();
                    for (int i = 0; i < SAMPLE_SIZE; i++) {
                        final Arena arena = ArenaManager.generateRandomArena(n,
                                4, c);

                        try {
                            String path = "last_arena/"
                                    + solver.getClass().getSimpleName() + "-n"
                                    + n + "-c" + c + "-" + i;
                            ArenaManager.saveArena(arena, path + ".txt");
                            long time = measure(solver, arena);
                            System.out.print(time + ",");
                            statistics.addValue(time);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.print(statistics.getMean() + "\t");
                    lastTime = statistics.getMean();
                    if (firstTime == 0) {
                        firstTime = lastTime;
                    }
                    c++;
                }
                System.out.println();
                firstTime = 0;
                c = MIN_C;
                n *= 2;
            }
        }
    }

    /**
     * number of times to repeat to solve an arena before the measurements are
     * analysed.
     */
    static final int NO_OF_REPETITIONS = 5;
    /**
     * must I say more? Standard deviation is determined on one sample. It then
     * is divided by the samples median and compared to this value.
     */
    static final double MAXIMAL_ACCEPTED_RELATIVE_STANDARD_DEVIATION = 0.05;

    /**
     * measure how long the given <code>solver</code> usually takes to solve the
     * given <code>arena</code>. To receive representative values, the arena is
     * solve <code>NO_OF_REPETITIONS</code> times and the standard deviation is
     * used to determine whether these measurements agree closely enough. If the
     * standard deviation relative to the measurement's size exceeds
     * <code>MAXIMAL_ACCEPTED_RELATIVE_STANDARD_DEVIATION</code>, the samples
     * are neglected and <code>NO_OF_REPETITIONS</code> new samples are taken.
     * Once the samples agree sufficiently enough, the median is determined and
     * returned as a representative time.
     * 
     * @param solver
     *            algorithm to solve the arena with
     * @param arena
     *            arena to solve
     * @return median of timings which were similar enough
     */
    static long measure(final Solver solver, final Arena arena) {
        DescriptiveStatistics statistics = null;
        double relativeStandardDeviation = 1.0;
        while (relativeStandardDeviation > MAXIMAL_ACCEPTED_RELATIVE_STANDARD_DEVIATION) {
            statistics = new DescriptiveStatistics();
            for (int i = 0; i < NO_OF_REPETITIONS; i++) {
                final long time = solver.solveAndTime(arena).getTime();
                statistics.addValue(time);
                System.out.print(time + "|");
            }
            relativeStandardDeviation = statistics.getStandardDeviation()
                    / statistics.getPercentile(50);
            System.out.print(relativeStandardDeviation + "|");
        }
        return (long) statistics.getPercentile(50);
    }

}

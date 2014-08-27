package parisolve;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.AttractiveSmallMeasureAlgorithm;
import parisolve.backend.algorithms.SmallMeasureAlgorithm;
import parisolve.backend.algorithms.BigStepAlgorithm;
import parisolve.backend.algorithms.MemoizedRecursiveAlgorithm;
import parisolve.backend.algorithms.RecursiveAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.backend.algorithms.helper.SolutionWithTime;
import parisolve.backend.algorithms.preprocessor.GraphPreprocessor;
import parisolve.backend.algorithms.preprocessor.GraphPreprocessor.Optimization;
import parisolve.io.ArenaManager;
import parisolve.io.LinearArenaGenerator;
import parisolve.io.LinearArenaGenerator.GeneratorType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

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
     * entry-point for all benchmarkings.
     */
    protected static void doBenchmarking() {
        doLinearBenchmarking();
        doRandomBenchmarking();
    }

    /**
     * defines the subcollection of algorithms to benchmark.
     */
    private static List<Class<? extends Solver>> getAlgorithms() {
        return new ImmutableList.Builder<Class<? extends Solver>>()
                .add(MemoizedRecursiveAlgorithm.class)
                .add(RecursiveAlgorithm.class).add(SmallMeasureAlgorithm.class)
                .add(AttractiveSmallMeasureAlgorithm.class)
                .add(BigStepAlgorithm.class).build();
    }

    /**
     * determines, how long measurements may become before the iteration is
     * stopped.
     */
    static final long MAX_TIME_TO_SAMPLE = 10000;

    /**
     * benchmarks the algorithms. It does so by generating different arenas for
     * increasing parameters until the measured time reaches the value of
     * <code>MAX_TIME_TO_SAMPLE</code>. Measurements are printed in a tabular
     * layout. In order to be able to inspect arenas with unusual results, all
     * areas generated are stored in the folder <code>last_arenas</code> with a
     * descriptive file name.
     */
    protected static void doLinearBenchmarking() {
        for (final GeneratorType type : new GeneratorType[] {
                GeneratorType.RESILIENT, GeneratorType.SOLITAIRE }) {
            // System.out.println(type);
            for (final Class<? extends Solver> solverClass : getAlgorithms()) {
                // System.out.println(solverClass.getSimpleName());
                doLinearBenchmarking(type, solverClass);
            }
        }
    }

    /**
     * time spent solving arenas to warm up the JVM. Value determined in
     * Schröder (2014), 5.1.1.1.
     */
    static final long WARM_UP_TIME = 2000;

    protected static void warmUp(final Arena arena,
            final Class<? extends Solver> solverClass) {
        int time = 0;
        while (time < WARM_UP_TIME) {
            try {
                final Solver solver = solverClass.newInstance();
                SolutionWithTime solutionWithTime = solver.solveAndTime(arena);
                time += solutionWithTime.getTime();
            } catch (IllegalAccessException | InstantiationException e) {
                // should not happen
                System.err.println("Solver could not be instatiated.");
                e.printStackTrace();
            }
        }
    }

    protected static void doLinearBenchmarking(GeneratorType type,
            final Class<? extends Solver> solverClass) {
        Set<Set<Optimization>> optimizationPowerSet = Sets
                .powerSet(new HashSet<>(Arrays.asList(Optimization.values())));
        for (final Set<Optimization> optimizations : optimizationPowerSet) {
            doLinearBenchmarking(type, solverClass, optimizations);
        }
    }

    static final long MAX_LINEAR_N = 1500;

    protected static void doLinearBenchmarking(GeneratorType type,
            final Class<? extends Solver> solverClass,
            final Set<Optimization> optimizations) {
        double lastTime = 0;
        int n = 1;
        System.out.println("data_" + type + "_" + solverClass.getSimpleName()
                + (optimizations.isEmpty() ? "" : optimizations) + " = [");
        warmUp(LinearArenaGenerator.generateArena(type, 10), solverClass);
        while (lastTime < MAX_TIME_TO_SAMPLE) {
            // System.out.print("n=" + n + "\t");
            final Arena arena = LinearArenaGenerator.generateArena(type, n);

            long time = (long) measure(solverClass, arena, optimizations)
                    .getPercentile(50);

            // System.out.print(time);
            lastTime = time;
            if (lastTime < MAX_TIME_TO_SAMPLE) {
                System.out.println(";");
            }
            if (n < MAX_LINEAR_N) {
                n++;
            } else {
                n *= 2;
            }
        }
        System.out.println("]';");
    }

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
     * determines how many random arenas are generated for one set of
     * parameters.
     */
    static final int SAMPLE_SIZE = 5;

    /**
     * benchmarks the algorithms. It does so by generating different arenas for
     * increasing parameters until the measured time reaches the value of
     * <code>MAX_TIME_TO_SAMPLE</code>. Measurements are printed in a tabular
     * layout. In order to be able to inspect arenas with unusual results, all
     * areas generated are stored in the folder <code>last_arenas</code> with a
     * descriptive file name.
     */
    protected static void doRandomBenchmarking() {
        for (final Class<? extends Solver> solverClass : getAlgorithms()) {
            System.out.println(solverClass.getSimpleName());
            double lastTime = 0;
            double firstTime = 0;
            int n = MIN_N;
            int c = MIN_C;
            warmUp(ArenaManager.generateRandomArena(1000, 4, 5), solverClass);
            while (firstTime < MAX_TIME_TO_SAMPLE) {
                while (lastTime < MAX_TIME_TO_SAMPLE && c <= MAX_C) {
                    DescriptiveStatistics statistics = new DescriptiveStatistics();
                    for (int i = 0; i < SAMPLE_SIZE; i++) {
                        final Arena arena = ArenaManager.generateRandomArena(n,
                                4, c);

                        try {
                            String path = "last_arena/"
                                    + solverClass.getSimpleName() + "-n" + n
                                    + "-c" + c + "-" + i;
                            ArenaManager.saveArena(arena, path + ".txt");
                            long time = (long) measure(solverClass, arena)
                                    .getPercentile(50);
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
     * convenience-method
     */
    static DescriptiveStatistics measure(
            final Class<? extends Solver> solverClass, final Arena arena) {
        return measure(solverClass, arena, new HashSet<>());
    }

    /**
     * number of times to repeat to solve an arena before the measurements are
     * analysed. Determined by calculating the minimal sample size for several
     * solver-arena-pairs and maximal relative error. For details, see the
     * thesis Schröder (2014), Section 5.1.1.2.
     */
    static final int NO_OF_REPETITIONS = 25;

    /**
     * measure how long the given <code>solver</code> usually takes to solve the
     * given <code>arena</code>. To receive representative values, the arena is
     * solve <code>NO_OF_REPETITIONS</code> times. the median is determined and
     * returned as a representative time.
     * 
     * @param solverClass
     *            algorithm to solve the arena with
     * @param arena
     *            arena to solve
     * @param optimizations
     *            optimizations to apply to the arena before running the
     *            algorithm
     * @return median of timings which were similar enough
     */
    static DescriptiveStatistics measure(
            final Class<? extends Solver> solverClass, final Arena arena,
            Set<Optimization> optimizations) {
        DescriptiveStatistics statistics = new DescriptiveStatistics();
        for (int i = 0; i < NO_OF_REPETITIONS; i++) {
            try {
                final Solver solver = solverClass.newInstance();
                final long time;
                if (optimizations.isEmpty()) {
                    time = solver.solveAndTime(arena).getTime();
                } else {
                    GraphPreprocessor preprocessor = new GraphPreprocessor(
                            solver);
                    for (final Optimization optimization : optimizations) {
                        preprocessor.addOptimization(optimization);
                    }
                    time = preprocessor.solveAndTime(arena).getTime();
                }
                System.out.print(time + " ");
                statistics.addValue(time);
            } catch (IllegalAccessException | InstantiationException e) {
                // should not happen
                System.err.println("Solver could not be instatiated.");
                e.printStackTrace();
            }
        }
        return statistics;
    }
}

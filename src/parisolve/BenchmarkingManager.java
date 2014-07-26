package parisolve;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.BetterAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

public class BenchmarkingManager {
    static final int sampleSize = 5;
    static final long maxTimeToSample = 10000;
    static final int maxC = 6;

    protected static void doBenchmarking() {
        final Solver solver = new BetterAlgorithm();
        System.out.println(solver.getClass().getSimpleName());
        double lastTime = 0;
        double firstTime = 0;
        int n = 20000;
        int c = 4;
        while (firstTime < maxTimeToSample) {
            while (lastTime < maxTimeToSample && c <= maxC) {
                DescriptiveStatistics statistics = new DescriptiveStatistics();
                for (int i = 0; i < sampleSize; i++) {
                    final Arena arena = ArenaManager.generateRandomArena(n, 4,
                            c);
    
                    try {
                        String path = "last_arena/"
                                + solver.getClass().getSimpleName() + "-n" + n
                                + "-c" + c + "-" + i;
                        ArenaManager.saveArena(arena, path + ".txt");
                        BufferedWriter writer = Files.newBufferedWriter(
                                Paths.get(path + ".sdlog"),
                                StandardOpenOption.CREATE);
                        long time = measure(solver, arena, writer);
                        writer.close();
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
            c = 2;
            n *= 2;
        }
    }

    static final int noOfRepetitions = 5;

    static long measure(final Solver solver, final Arena arena,
                final BufferedWriter writer) throws IOException {
            DescriptiveStatistics statistics = null;
            double relativeStandardDeviation = 1.0;
            while (relativeStandardDeviation > 0.05) {
                statistics = new DescriptiveStatistics();
                for (int i = 0; i < noOfRepetitions; i++) {
                    final long time = solver.solveAndTime(arena).getTime();
                    statistics.addValue(time);
                    System.out.print(time + "|");
                }
                relativeStandardDeviation = statistics.getStandardDeviation()
                        / statistics.getPercentile(50);
                System.out.print(relativeStandardDeviation + "|");
    //            writer.write(relativeStandardDeviation + "\n");
    //            writer.flush();
            }
            return (long) statistics.getPercentile(50);
        }

}

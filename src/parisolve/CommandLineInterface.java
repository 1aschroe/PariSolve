package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import parisolve.backend.Arena;
import parisolve.backend.Player;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.BigStepAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

public class CommandLineInterface extends AbstractUI {

    @Override
    public void displayInfo(final String message) {
        System.out.println(message);
    }

    @Override
    public void displayError(final String message) {
        System.err.println(message);
    }

    @Override
    public void populateGraphWithArena(Arena currentArena) {
        displayInfo(currentArena.getStatistics());
    }

    @Override
    public void run() {
        displayInfo("To get help for the commands possible, type '?' or 'help'");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (!"exit".equals(line)) {
            System.out.print("> ");
            try {
                line = br.readLine();
                if (line == null) {
                    break;
                }
                // TODO perfect place for a string-switch or a map
                if ("help".equals(line) || "?".equals(line)) {
                    displayInfo("You can either\n - open [FILENAME] an arena-file,\n - generate [NUMBER_VERTICES AVERAGE_DEGREE MAX_PRIORITY] a arena or\n - solve [ALGORITHM] a loaded arena using the given algorithm");
                } else if (line.startsWith("open")) {
                    String[] parts = line.split(" ");
                    String file;
                    if (parts.length == 1) {
                        System.out.print("open> ");
                        file = br.readLine();
                    } else {
                        file = parts[1];
                    }
                    loadArenaFromFile(file);
                } else if (line.startsWith("generate")) {
                    String[] parts = line.split(" ");
                    int numberOfVertices;
                    double averageDegree;
                    int maxPriority;
                    if (parts.length == 1) {
                        displayInfo("Expected parameters are: [number of vertices] [average degree] [maximal priority]");
                        System.out.print("generate> ");
                        parts = br.readLine().split(" ");

                        numberOfVertices = Integer.parseInt(parts[0]);
                        averageDegree = Double.parseDouble(parts[1]);
                        maxPriority = Integer.parseInt(parts[2]);
                    } else {
                        numberOfVertices = Integer.parseInt(parts[1]);
                        averageDegree = Double.parseDouble(parts[2]);
                        maxPriority = Integer.parseInt(parts[3]);
                    }
                    generateArena(numberOfVertices, averageDegree, maxPriority);
                } else if (line.startsWith("solve")) {
                    String[] parts = line.split(" ");
                    String algorithm;
                    if (parts.length == 1) {
                        displayInfo("Possible Solvers are: "
                                + AlgorithmManager.getAlgorithms());
                        System.out.print("solve> ");
                        algorithm = br.readLine();
                    } else {
                        algorithm = parts[1];
                    }
                    try {
                        @SuppressWarnings("unchecked")
                        Class<? extends Solver> algorithmClass = (Class<? extends Solver>) Class
                                .forName("parisolve.backend.algorithms."
                                        + algorithm);
                        Constructor<? extends Solver> constructor = algorithmClass
                                .getConstructor();
                        Solver instance = constructor.newInstance();
                        fireSolve(instance);
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (SecurityException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayError("Error when reading a line.");
            }
        }
    }

    @Override
    public final void handleArguments(final String[] arenas) {
        final Solver solver = new BigStepAlgorithm();
        for (final String arenaFile : arenas) {
            loadArenaFromFile(arenaFile);
            displayInfo(String.format(StartUp.SOLVE_MSG, arenaFile, solver
                    .getClass().getSimpleName()));
            fireSolve(solver);
        }
    }
}

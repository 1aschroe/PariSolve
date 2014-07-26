package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.BigStepAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.LinearArenaGenerator.GeneratorType;

/**
 * User interface implementation to interact with the program via a command-line
 * interpreter, also known as terminal, console or command line.
 * 
 * Runs in an endless loop, waiting for the user to issue commands until the
 * user uses the exit command.
 * 
 * @author Arne Schröder
 */
public class CommandLineInterface extends AbstractUI {

    @Override
    public final void displayInfo(final String message) {
        System.out.println(message);
    }

    @Override
    public final void displayError(final String message) {
        System.err.println(message);
    }

    @Override
    public final void populateGraphWithArena(final Arena currentArena) {
        displayInfo(currentArena.getStatistics());
    }

    @Override
    public void highlightSolution(
            Collection<? extends ParityVertex> winningRegion,
            Map<ParityVertex, ParityVertex> strategy) {
        displayInfo("Player A wins " + winningRegion.size() + " vertices.");
    }

    /**
     * string to indicate that the ui is listening on the user's action.
     */
    final static String PROMPT = "> ";
    /**
     * string to separate the parts of the command line. That is, the command
     * and its parameters from each other.
     */
    final static String COMMAND_SEPARATOR = " ";

    @Override
    public final void run() {
        displayInfo("To get help for the commands possible, type '?' or 'help'");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        executeRequests(br, false);
    }

    /**
     * Successively executes the lines given from br. If the commands should be
     * incomplete, it is tried to obtain the missing ones.
     * 
     * @param br
     *            reader to get the lines from
     * @param batch
     *            whether these requests are part of a batch-file
     */
    protected final void executeRequests(final BufferedReader br,
            final boolean batch) {
        while (true) {
            System.out.print(PROMPT);
            try {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (batch) {
                    System.out.println(line);
                }
                final String[] parts = line.split(COMMAND_SEPARATOR);
                switch (parts[0]) {
                case "help":
                case "?":
                    displayInfo("You can either\n"
                            + " - open [FILENAME] an arena-file,\n"
                            + " - generate-random [NUMBER_VERTICES AVERAGE_DEGREE MAX_PRIORITY] an arena,\n"
                            + " - generate-hlb [NUMBER_LEVELS NUMBER_BLOCKS] an arena,\n"
                            + " - generate-linear [ARENA_TYPE] [NUMBER_LEVELS] an arena,\n"
                            + " - save [FILENAME] the current arena or\n"
                            + " - solve [ALGORITHMS] a loaded arena using the given algorithms (comma-separated)");
                    break;
                case "open":
                    doOpen(getParts(br, parts, 1, null));
                    break;
                case "generate-random":
                    doGenerateRandom(getParts(
                            br,
                            parts,
                            3,
                            "Expected parameters are: [number of vertices] [average degree] [maximal priority]"));
                    break;
                case "generate-hlb":
                    doGenerateHlb(getParts(br, parts, 2,
                            "Expected parameters are: [number of levels] [number of blocks]"));
                    break;
                case "generate-linear":
                    doGenerateLinear(getParts(br, parts, 2,
                            "Expected parameters are: [type of arena] [number of levels]"));
                    break;
                case "save":
                    doSave(getParts(br, parts, 1, null));
                    break;
                case "solve":
                    doSolve(getParts(br, parts, 1, "Possible Solvers are: "
                            + AlgorithmManager.getAlgorithms()));
                    break;
                case "exit":
                    // TODO: is this too harsh, as it cannot be stopped? Maybe
                    // an exception instead?
                    System.exit(0);
                case "":
                    continue;
                default:
                    displayInfo("Unknown command " + parts[0]);
                }
            } catch (IOException e) {
                e.printStackTrace();
                displayError("Error when reading a line.");
            }
        }
    }

    /**
     * retrieves parameters from the user if they have only given the command.
     * 
     * @param br
     *            buffered reader to read from the command line
     * @param parts
     *            the parts originally entered by the user
     * @param noOfExpectedParameters
     *            the number of parameters expected for this command
     * @param info
     *            line to display to assist the user in entering the parameters.
     *            <code>null</code> if not needed.
     * @return the parts of the command line ready to be executed
     * @throws IOException
     *             occurs if there has been an IO error on the command line
     */
    private String[] getParts(final BufferedReader br, final String[] parts,
            final int noOfExpectedParameters, final String info)
            throws IOException {
        // TODO: encounter for parameter with spaces
        final String command = parts[0];
        int noOfEnteredParts = parts.length;
        final String[] returnParts = new String[noOfExpectedParameters + 1];
        // TODO: give feedback about parameters being ignored
        System.arraycopy(parts, 0, returnParts, 0,
                Math.min(noOfEnteredParts, noOfExpectedParameters + 1));
        while (noOfEnteredParts < noOfExpectedParameters + 1) {
            final int parametersMissing = noOfExpectedParameters + 1
                    - noOfEnteredParts;
            displayInfo(parametersMissing
                    + " parameter(s) missing. You entered: "
                    + Arrays.toString(returnParts));
            if (info != null) {
                displayInfo(info);
            }
            System.out.print(command + PROMPT);
            final String[] split = br.readLine().split(COMMAND_SEPARATOR);
            // TODO: give feedback about parameters being ignored
            System.arraycopy(split, 0, returnParts, noOfEnteredParts,
                    Math.min(parametersMissing, split.length));
            noOfEnteredParts += split.length;
        }
        return returnParts;
    }

    /**
     * executes the open-action.
     * 
     * @param parts
     *            parts of the command line
     */
    protected final void doOpen(final String[] parts) {
        loadArenaFromFile(parts[1]);
    }

    /**
     * executes the generate-action for random arenas.
     * 
     * @param parts
     *            parts of the command line
     */
    protected final void doGenerateRandom(final String[] parts) {
        generateRandomArena(Integer.parseInt(parts[1]),
                Double.parseDouble(parts[2]), Integer.parseInt(parts[3]));
    }

    /**
     * executes the generate-action for H_l,b arenas.
     * 
     * @param parts
     */
    protected final void doGenerateHlb(final String[] parts) {
        generateHlbArena(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    /**
     * executes the generate-action for the respective family of arenas as
     * specified in "Zielonka’s Recursive Algorithm: dull, weak and solitaire
     * games and tighter bounds".
     * 
     * @param parts
     *            first the command, second the type, third the size
     */
    protected final void doGenerateLinear(final String[] parts) {
        final GeneratorType generatorType;
        // TODO: introduce mapping (String -> type) in GeneratorType for this
        // and the corresponding code in GenerateButtonListener
        switch (parts[1].toLowerCase()) {
        case "weak":
            generatorType = GeneratorType.WEAK;
            break;
        case "solitaire":
            generatorType = GeneratorType.SOLITAIRE;
            break;
        case "resilient":
            generatorType = GeneratorType.RESILIENT;
            break;
        case "hard":
            generatorType = GeneratorType.HARD;
            break;
        case "two-ring":
            generatorType = GeneratorType.TWO_RING;
            break;
        case "chain":
            generatorType = GeneratorType.CHAIN;
            break;
        default:
            displayError("Unknown generator type: " + parts[1]);
            return;
        }
        generateLinearArena(generatorType, Integer.parseInt(parts[2]));
    }

    /**
     * executes the save-action.
     * 
     * @param parts
     *            parts of the command line
     */
    protected final void doSave(final String[] parts) {
        fireSave(parts[1]);
    }

    /**
     * executes the solve-action.
     * 
     * @param parts
     *            parts of the command line
     */
    protected final void doSolve(final String[] parts) {
        final String[] algorithms = parts[1].split(",");
        for (final String algorithm : algorithms) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends Solver> algorithmClass = (Class<? extends Solver>) Class
                        .forName("parisolve.backend.algorithms." + algorithm);
                Constructor<? extends Solver> constructor = algorithmClass
                        .getConstructor();
                Solver instance = constructor.newInstance();
                fireSolve(instance);
            } catch (ClassNotFoundException | NoSuchMethodException
                    | SecurityException | InstantiationException
                    | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public final void handleArguments(final String[] arenas) {
        final Solver solver = new BigStepAlgorithm();
        for (final String arenaFile : arenas) {
            loadArenaFromFile(arenaFile);
            displayInfo(String.format(UserInterface.SOLVE_MSG, arenaFile,
                    solver.getClass().getSimpleName()));
            fireSolve(solver);
        }
    }
}

package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import parisolve.backend.Arena;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.BigStepAlgorithm;
import parisolve.backend.algorithms.Solver;

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
        String[] parts = new String[1];
        while (!"exit".equals(parts[0])) {
            System.out.print(PROMPT);
            try {
                final String line = br.readLine();
                if (line == null) {
                    break;
                }
                parts = line.split(COMMAND_SEPARATOR);
                switch (parts[0]) {
                case "help":
                case "?":
                    displayInfo("You can either\n"
                            + " - open [FILENAME] an arena-file,\n"
                            + " - generate [NUMBER_VERTICES AVERAGE_DEGREE MAX_PRIORITY] a arena,\n"
                            + " - save [FILENAME] the current arena or\n"
                            + " - solve [ALGORITHM] a loaded arena using the given algorithm");
                    break;
                case "open":
                    doOpen(getParts(br, parts, 1, null));
                    break;
                case "generate":
                    doGenerate(getParts(
                            br,
                            parts,
                            3,
                            "Expected parameters are: [number of vertices] [average degree] [maximal priority]"));
                    break;
                case "save":
                    doSave(getParts(br, parts, 1, null));
                    break;
                case "solve":
                    doSolve(getParts(br, parts, 1, "Possible Solvers are: "
                            + AlgorithmManager.getAlgorithms()));
                    break;
                case "exit":
                    break;
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
    private void doOpen(final String[] parts) {
        loadArenaFromFile(parts[1]);
    }

    /**
     * executes the generate-action.
     * 
     * @param parts
     *            parts of the command line
     */
    private void doGenerate(final String[] parts) {
        generateArena(Integer.parseInt(parts[1]), Double.parseDouble(parts[2]),
                Integer.parseInt(parts[3]));
    }

    /**
     * executes the save-action.
     * 
     * @param parts
     *            parts of the command line
     */
    private void doSave(final String[] parts) {
        fireSave(parts[1]);
    }

    /**
     * executes the solve-action.
     * 
     * @param parts
     *            parts of the command line
     */
    private void doSolve(final String[] parts) {
        final String algorithm = parts[1];
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

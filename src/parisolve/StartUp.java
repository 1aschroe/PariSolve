package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

/**
 * Entry point for PariSolve.
 * 
 * This class's main-method controls PariSolve's flow. It parses the input
 * arguments sets up the ui and runs it with listeners attached, which call
 * backend functionality.
 */
public final class StartUp {
    /**
     * the arena, loaded or generated, to be worked with in the next steps
     * (usually to be solved).
     */
    private static Arena currentArena;
    /**
     * display-string when an arenas file could not be loaded.
     */
    static final String ARENA_NOT_READ_MSG = "Arena %s could not be read.";
    static final String SOLVE_MSG = "Solving %s using %s";
    static final String TIME_MSG = "Solving took %d milliseconds";
    static final String NON_UI_OPTION = "non-ui";
    static final String TIME_OPTION = "time";
    static final String HELP_OPTION = "help";
    static final String BATCH_OPTION = "batch";

    /**
     * private constructor to prevent instantiation.
     */
    private StartUp() {
        // private constructor to prevent instantiation.
    }

    /**
     * Entry point for PariSolve.
     * 
     * @param args
     *            command line arguments
     */
    public static void main(final String[] args) {

        final Options options = new Options();
        options.addOption("n", NON_UI_OPTION, false, "use UI");
        options.addOption("t", TIME_OPTION, false, "time solving");
        options.addOption("?", HELP_OPTION, false, "display help");
        options.addOption("b", BATCH_OPTION, true, "run a batch-file");

        try {
            final CommandLine line = new BasicParser().parse(options, args);

            if (line.hasOption(HELP_OPTION)) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter
                        .printHelp(
                                "PariSolve [OPTIONS] [ARENAFILES] (order is not important)",
                                "Options:", options, "");
                return;
            }

            final UserInterface ui;
            if (line.hasOption(NON_UI_OPTION)) {
                // command line mode
                ui = new CommandLineInterface();
            } else {
                // GUI mode
                ui = new GraphicalUI();
            }
            ui.addUserListener(new UserListener() {
                @Override
                public void openedArena(final Arena arena) {
                    currentArena = arena;
                    ui.populateGraphWithArena(currentArena);
                }

                @Override
                public void save(final String path) throws IOException {
                    ArenaManager.saveArena(currentArena, path);
                }

                @Override
                public void solve(final Solver solver) {
                    if (currentArena == null) {
                        ui.displayError("No arena loaded");
                        return;
                    }
                    final long start = System.currentTimeMillis();
                    final Collection<? extends ParityVertex> winningRegion = solver
                            .getWinningRegionForPlayer(currentArena, Player.A);
                    final long stop = System.currentTimeMillis();
                    ui.highlightRegion(winningRegion);
                    if (line.hasOption(TIME_OPTION)) {
                        ui.displayInfo(String.format(TIME_MSG, stop - start));
                    }
                }
            });
            if (line.hasOption(NON_UI_OPTION) && line.hasOption(BATCH_OPTION)) {
                try {
                    final String batchFile = line.getOptionValue(BATCH_OPTION);
                    final BufferedReader br = Files.newBufferedReader(
                            Paths.get(batchFile), Charset.defaultCharset());
                    ((CommandLineInterface) ui).batchReader(br);
                    ui.displayInfo("Batching finished");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ui.handleArguments(line.getArgs());
            ui.run();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

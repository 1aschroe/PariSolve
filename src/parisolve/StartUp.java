package parisolve;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.RecursiveAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

/**
 * Entry point for PariSolve.
 */
public class StartUp {

    static Arena currentArena;
    /**
     * display-string when an arenas file could not be loaded.
     */
    static final String ARENA_NOT_READ_MSG = "Arena %s could not be read.";
    static final String TIME_MSG = "Solving took %d milliseconds";
    static final String NON_UI_OPTION = "non-ui";
    static final String TIME_OPTION = "time";
    static final String HELP_OPTION = "help";

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

        try {
            final CommandLine line = new BasicParser().parse(options, args);
            final String[] arenas = line.getArgs();

            if (line.hasOption(HELP_OPTION)) {
                final HelpFormatter formatter = new HelpFormatter();
                formatter
                        .printHelp(
                                "PariSolve [OPTIONS] [ARENAFILES] (order is not important)",
                                "Options:", options, "");
                return;
            }

            if (line.hasOption(NON_UI_OPTION)) {
                // command line mode
                if (arenas.length == 0) {
                    System.out.println("No arenas specified.");
                    // TODO: enter interactive mode
                }

                final Solver solver = new RecursiveAlgorithm();
                for (final String arenaFile : arenas) {
                    try {
                        final Arena arena = ArenaManager.loadArena(arenaFile);
                        final long start = System.currentTimeMillis();
                        solver.getWinningRegionForPlayer(arena, 0);
                        final long stop = System.currentTimeMillis();
                        if (line.hasOption("time")) {
                            System.out.println(String.format(TIME_MSG, stop
                                    - start));
                        }
                    } catch (IOException e) {
                        System.err.println(String.format(ARENA_NOT_READ_MSG,
                                arenaFile));
                    }
                }
            } else {
                // GUI mode
                final GraphicalUI gui = new GraphicalUI();
                gui.addOpenListener(new OpenListener() {
                    @Override
                    public void openedArena(final Arena arena) {
                        currentArena = arena;
                        gui.populateGraphWithArena(currentArena);
                    }
                });
                gui.addSolveListener(new SolveListener() {
                    @Override
                    public void solve() {
                        if (currentArena == null) {
                            gui.displayError("No arena loaded");
                            return;
                        }
                        final Solver solver = new RecursiveAlgorithm();
                        final long start = System.currentTimeMillis();
                        final Collection<? extends ParityVertex> winningRegion = solver
                                .getWinningRegionForPlayer(currentArena, 0);
                        final long stop = System.currentTimeMillis();
                        gui.highlightWinningRegion(winningRegion);
                        if (line.hasOption(TIME_OPTION)) {
                            gui.displayInfo(String.format(TIME_MSG, stop
                                    - start));
                        }
                    }
                });
                if (arenas.length > 0) {
                    try {
                        currentArena = ArenaManager.loadArena(arenas[0]);
                        gui.populateGraphWithArena(currentArena);
                    } catch (IOException e) {
                        gui.displayError(String.format(ARENA_NOT_READ_MSG,
                                arenas[0]));
                    }
                }
                gui.run();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

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
import parisolve.backend.algorithms.PrimitiveAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

public class StartUp {

    static Arena currentArena;
    final static String ARENA_NOT_READ_MSG = "Arena %s could not be read.";
    final static String TIME_MSG = "Solving took %d milliseconds";

    public static void main(String[] args) {

        Options options = new Options();
        options.addOption("n", "non-ui", false, "use UI");
        options.addOption("t", "time", false, "time solving");
        options.addOption("?", "help", false, "display help");

        try {
            final CommandLine line = new BasicParser().parse(options, args);
            String[] arenas = line.getArgs();

            if (line.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("PariSolve [OPTIONS] [ARENAFILES] (order is not important)", "Options:", options, "");
                return;
            }

            if (!line.hasOption("non-ui")) {
                final UI ui = new UI();
                ui.addOpenListener(new OpenListener() {
                    @Override
                    public void openedArena(Arena arena) {
                        currentArena = arena;
                        ui.populateGraphWithArena(currentArena);
                    }
                });
                ui.addSolveListener(new SolveListener() {
                    @Override
                    public void solve() {
                        if (currentArena != null) {
                            Solver solver = new PrimitiveAlgorithm();
                            long start = System.currentTimeMillis();
                            Collection<? extends ParityVertex> winningRegionForPlayer = solver.getWinningRegionForPlayer(currentArena, 0);
                            long stop = System.currentTimeMillis();
                            ui.highlightWinningRegion(winningRegionForPlayer);
                            if (line.hasOption("time")) {
                                ui.displayMessage(String.format(TIME_MSG, stop - start));
                            }
                        } else {
                            ui.displayError("No arena loaded");
                        }
                    }
                });
                if (arenas.length > 0) {
                    try {
                        currentArena = ArenaManager.loadArena(arenas[0]);
                        ui.populateGraphWithArena(currentArena);
                    } catch (IOException e) {
                        ui.displayError(String.format(ARENA_NOT_READ_MSG, arenas[0]));
                    }
                }
                ui.run();
            } else {
                if (arenas.length == 0) {
                    System.out.println("No arenas specified.");
                    // TODO: enter interactive mode
                }

                Solver solver = new PrimitiveAlgorithm();
                for (String arenaFile : arenas) {
                    try {
                        Arena arena = ArenaManager.loadArena(arenaFile);
                        long start = System.currentTimeMillis();
                        solver.getWinningRegionForPlayer(arena, 0);
                        long stop = System.currentTimeMillis();
                        if (line.hasOption("time")) {
                            System.out.println(String.format(TIME_MSG, stop - start));
                        }
                    } catch (IOException e) {
                        System.err.println(String.format(ARENA_NOT_READ_MSG, arenaFile));
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}

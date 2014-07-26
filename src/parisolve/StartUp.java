package parisolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Entry point for PariSolve.
 * 
 * This class's main-method sets up all components for PariSolve to run. It
 * parses the input arguments sets up the ui and runs it with listeners
 * attached, which call backend functionality.
 */
public final class StartUp {
    static final String NON_UI_OPTION = "non-ui";
    static final String TIME_OPTION = "time";
    static final String HELP_OPTION = "help";
    static final String BATCH_OPTION = "batch";
    static final String BENCHMARK_OPTION = "benchmark";

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
        options.addOption("bench", BENCHMARK_OPTION, false,
                "run the benchmarking");

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
            UserListener listener = new InteractionListener(
                    line.hasOption(TIME_OPTION), ui);
            ui.addUserListener(listener);
            if (line.hasOption(BENCHMARK_OPTION)) {
                BenchmarkingManager.doBenchmarking();
            }
            if (line.hasOption(NON_UI_OPTION) && line.hasOption(BATCH_OPTION)) {
                try {
                    final String batchFile = line.getOptionValue(BATCH_OPTION);
                    final BufferedReader br = Files.newBufferedReader(
                            Paths.get(batchFile), Charset.defaultCharset());
                    ((CommandLineInterface) ui).executeRequests(br, true);
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

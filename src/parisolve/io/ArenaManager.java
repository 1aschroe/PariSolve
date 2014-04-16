package parisolve.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * The <code>ArenaManager</code> loads and stores arenas.
 */
public final class ArenaManager {
    private ArenaManager() {
        // to prevent instantiation of this utility class.
    };

    /**
     * opens file specified by <code>fileName</code> and returns the arena
     * specified within.
     * 
     * @param fileName
     *            arenas file's filename
     * @return arena stored in file specified by <code>fileName</code>
     * @throws IOException
     *             the file specified by <code>fileName</code> could not be read
     *             or the content did not conform to the default charset
     */
    public static Arena loadArena(final String fileName) throws IOException {
        final List<String> lines = Files.readAllLines(Paths.get(fileName),
                Charset.defaultCharset());
        final LinkedArena arena = new LinkedArena();
        if (fileName.endsWith(".arena")) {
            fillArenaFromLinesInDotFormat(lines, arena);
        } else {
            fillArenaFromLinesInTxtFormat(lines, arena);
        }
        return arena;
    }

    /**
     * creates an arena, populates it with <code>numberOfVertices</code>
     * vertices, randomly assigns priorities up to <code>maxPriority</code> and
     * randomly connects these vertices so that approximately an average of
     * <code>averageDegree</code> is achieved.
     * 
     * @param numberOfVertices
     *            the number of vertices in the generated arena
     * @param averageDegree
     *            the average degree aimed for
     * @param maxPriority
     *            the maximal priority to assign to a vertex
     * @return a newly generated arena
     */
    public static Arena generateRandomArena(final int numberOfVertices,
            final double averageDegree, final int maxPriority) {
        final LinkedArena arena = new LinkedArena();
        final Random random = new Random(System.currentTimeMillis());
        final int[] numberOfEdges = new int[numberOfVertices];
        for (int i = 0; i < numberOfVertices; i++) {
            final int priority = random.nextInt(maxPriority) + 1;
            final Player player = Player
                    .getPlayerForPriority(random.nextInt(2));
            arena.addVertex("v" + i, priority, player);
            numberOfEdges[i] = Math.max(
                    (int) Math.round(random.nextDouble() * 2.0
                            * (averageDegree - 1)), 0) + 1;
        }

        for (int i = 0; i < numberOfVertices; i++) {
            for (int edge = 0; edge < numberOfEdges[i]; edge++) {
                final int toVertex = random.nextInt(numberOfVertices);
                arena.addEdge("v" + i, "v" + toVertex);
            }
        }

        return arena;
    }

    public static Pattern VERTEX_PATTERN = Pattern
            .compile("\\s*(\\w+)\\[shape=(box|oval),\\s*label=\"(\\d+)\"];");
    public static Pattern EDGE_PATTERN = Pattern
            .compile("\\s*(\\w+)\\s*->\\s*(\\w+);");

    /**
     * fills <code>arena</code> with vertices and edges defined in
     * <code>lines</code>. This is done in the arena-format which is compatible
     * with GraphViz/DOT.
     * 
     * @param lines
     *            lines from file, containing the information
     * @param arena
     *            arena to fill with vertices and edges
     */
    private static void fillArenaFromLinesInDotFormat(final List<String> lines,
            final LinkedArena arena) {
        for (final String line : lines) {
            final Matcher vertexMatcher = VERTEX_PATTERN.matcher(line);
            if (vertexMatcher.find()) {
                arena.addVertex(vertexMatcher.group(1),
                        Integer.parseInt(vertexMatcher.group(3)),
                        Player.getPlayerForShapeString(vertexMatcher.group(2)));
            }
        }
        for (final String line : lines) {
            final Matcher edgeMatcher = EDGE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                arena.addEdge(edgeMatcher.group(1), edgeMatcher.group(2));
            }
        }
    }

    /**
     * pattern describing the syntax of a line in the txt-format for arenas,
     * grouping the relevant elements.
     */
    public static final Pattern LINE_PATTERN = Pattern
            .compile("(\\d+) (\\d+) (\\d+) (\\d+(,\\d+)*)( \"([^\"]+)\")?;");

    /**
     * fills <code>arena</code> with vertices and edges defined in
     * <code>lines</code>. This is done in the txt/fg-format used by the
     * benchmark arenas provided at
     * https://www7.in.tum.de/tools/gpupg/index.php.
     * 
     * @param lines
     *            lines from file, containing the information
     * @param arena
     *            arena to fill with vertices and edges
     */
    private static void fillArenaFromLinesInTxtFormat(final List<String> lines,
            final LinkedArena arena) {
        for (final String line : lines) {
            final Matcher vertexMatcher = LINE_PATTERN.matcher(line);
            if (vertexMatcher.find()) {
                arena.addVertex(vertexMatcher.group(1), Integer
                        .parseInt(vertexMatcher.group(2)), Player
                        .getPlayerForPriority(Integer.parseInt(vertexMatcher
                                .group(3))));
            }
        }
        for (final String line : lines) {
            final Matcher edgeMatcher = LINE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                for (final String target : edgeMatcher.group(4).split(",")) {
                    arena.addEdge(edgeMatcher.group(1), target);
                }
            }
        }
    }

    /**
     * utility method for printing GraphViz-representation to inspect strategy.
     * That is, for every vertex only the outgoing edge, chosen by the strategy,
     * is shown.
     * 
     * @param strategy
     *            the strategy to convert
     * @return String in dot-format to be understood by GraphViz
     */
    public static String getGraphVizFromStrategy(
            final Map<ParityVertex, ParityVertex> strategy) {
        final StringBuilder resultBuilder = new StringBuilder(25);
        resultBuilder.append("digraph strategy {\n");

        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder
                    .append(String.format("  %s[shape=%s,label=\"%d\"];\n",
                            vertex.getName(), vertex.getPlayer()
                                    .getShapeString(), vertex.getPriority()));
        }

        resultBuilder.append('\n');

        // print edges
        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder.append(String.format("  %s->%s;\n", vertex.getName(),
                    strategy.get(vertex).getName()));
        }
        resultBuilder.append('}');
        return resultBuilder.toString();
    }
}

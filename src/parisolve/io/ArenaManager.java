package parisolve.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
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
    /**
     * private constructor to prevent instantiation of this utility class.
     */
    private ArenaManager() {
        // private constructor to prevent instantiation of this utility class.
    };

    /**
     * tests whether this is a file containing an arena in the arena-format.
     * 
     * @param filename
     *            the filename to test
     * @return whether this is an arena-file
     */
    private static boolean isArenaFile(final String filename) {
        return filename.endsWith(".arena");
    }

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
        if (isArenaFile(fileName)) {
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
        for (int i = 0; i < numberOfVertices; i++) {
            final int priority = random.nextInt(maxPriority) + 1;
            final Player player = Player
                    .getPlayerForPriority(random.nextInt(2));
            arena.addVertex("v" + i, priority, player);
        }

        for (int i = 0; i < numberOfVertices; i++) {
            final int numberOfEdges = Math.max(
                    (int) Math.round(random.nextDouble() * 2.0
                            * (averageDegree - 1)), 0) + 1;
            for (int edge = 0; edge < numberOfEdges; edge++) {
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
            .compile("(\\w+) +(\\d+) +(\\d+) +(\\w+(,\\w+)*)( +\"([^\"]+)\")?;");

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
     * method for printing GraphViz-representation of a given arena. The
     * returned list of strings represents the lines of the graph-viz-file.
     * 
     * @param arena
     *            the arena to print
     * @return lines in dot-format to be understood by GraphViz
     */
    public static List<String> getGraphVizFromArena(final Arena arena) {
        List<String> lines = new ArrayList<String>();
        lines.add("digraph arena {");

        // print vertices
        for (final ParityVertex vertex : arena.getVertices()) {
            lines.add(String.format("  %s[shape=%s,label=\"%d\"];",
                    vertex.getName(), vertex.getPlayer().getShapeString(),
                    vertex.getPriority()));
        }

        lines.add("");

        // print edges
        for (final ParityVertex vertex : arena.getVertices()) {
            for (final ParityVertex successor : vertex.getSuccessors()) {
                lines.add(String.format("  %s->%s;", vertex.getName(),
                        successor.getName()));
            }
        }
        lines.add("}");
        return lines;
    }

    /**
     * returns a line-by-line representation of the specified arena using the
     * txt-format.
     * 
     * @param currentArena
     *            the arena to print
     * @return lines of the txt-file
     */
    public static List<String> getTxtFromArena(final Arena currentArena) {
        List<String> lines = new ArrayList<String>();
        Collection<? extends ParityVertex> vertices = currentArena
                .getVertices();
        lines.add("parity " + (vertices.size() - 1) + ";");
        final Map<ParityVertex, Integer> numbersOfVertices = new ConcurrentHashMap<>();
        final Iterator<? extends ParityVertex> iterator = vertices.iterator();
        for (int i = 0; i < vertices.size(); i++) {
            final ParityVertex vertex = iterator.next();
            numbersOfVertices.put(vertex, i);
        }
        for (final ParityVertex vertex : vertices) {
            final StringBuilder successors = new StringBuilder();
            for (final ParityVertex successor : vertex.getSuccessors()) {
                successors.append("," + numbersOfVertices.get(successor));
            }
            lines.add(String.format("%d %d %d %s;", numbersOfVertices
                    .get(vertex), vertex.getPriority(), vertex.getPlayer()
                    .getNumber(), successors.substring(1)));
        }
        return lines;
    }

    /**
     * saves the arena given under the path specified.
     * 
     * @param currentArena
     *            the arena to store
     * @param path
     *            the path to store the arena by
     * @throws IOException
     *             if an IO error occurs while writing the arena
     */
    public static void saveArena(final Arena currentArena, final String path)
            throws IOException {
        final List<String> lines;
        if (isArenaFile(path)) {
            lines = getGraphVizFromArena(currentArena);
        } else {
            lines = getTxtFromArena(currentArena);
        }

        Files.write(Paths.get(path), lines, Charset.defaultCharset(),
                StandardOpenOption.CREATE);
    }

    /**
     * tests whether the arena given is weak as in Definition 3 of Gazda,
     * Willemse (2013)
     * "Zielonka’s Recursive Algorithm: dull, weak and solitaire games and tighter bounds"
     */
    public static boolean isWeak(final Arena arena) {
        for (final ParityVertex vertex : arena.getVertices()) {
            for (final ParityVertex successor : vertex.getSuccessors()) {
                if (successor.getPriority() > vertex.getPriority()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * tests whether the arena given is weak as in Definition 5 of Gazda,
     * Willemse (2013)
     * "Zielonka’s Recursive Algorithm: dull, weak and solitaire games and tighter bounds"
     */
    public static boolean isSolitaire(final Arena arena) {
        boolean mightBeSolitaireForA = true;
        boolean mightBeSolitaireForB = true;
        for (final ParityVertex vertex : arena.getVertices()) {
            if (vertex.getPlayer() == Player.A) {
                mightBeSolitaireForA &= (vertex.getSuccessors().size() == 1);
            } else {
                mightBeSolitaireForB &= (vertex.getSuccessors().size() == 1);
            }
        }
        return mightBeSolitaireForA || mightBeSolitaireForB;
    }
}

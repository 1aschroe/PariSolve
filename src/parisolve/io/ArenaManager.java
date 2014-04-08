package parisolve.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * The ArenaManager loads and stores arenas. It does so in a DOT-compatible way
 * 
 */
public class ArenaManager {

    public static Arena loadArena(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName),
                Charset.defaultCharset());
        LinkedArena arena = new LinkedArena();
        if (fileName.endsWith(".arena")) {
            fillArenaFromLinesInDotFormat(lines, arena);
        } else {
            fillArenaFromLinesInTxtFormat(lines, arena);
        }
        return arena;
    }

    public static Pattern VERTEX_PATTERN = Pattern
            .compile("\\s*(\\w+)\\[shape=(box|oval),\\s*label=\"(\\d+)\"];");
    public static Pattern EDGE_PATTERN = Pattern
            .compile("\\s*(\\w+)\\s*->\\s*(\\w+);");

    private static void fillArenaFromLinesInDotFormat(List<String> lines,
            LinkedArena arena) {
        for (String line : lines) {
            Matcher vertexMatcher = VERTEX_PATTERN.matcher(line);
            if (vertexMatcher.find()) {
                arena.addVertex(vertexMatcher.group(1),
                        Integer.parseInt(vertexMatcher.group(3)),
                        Player.getPlayerForShapeString(vertexMatcher.group(2)));
            }
        }
        for (String line : lines) {
            Matcher edgeMatcher = EDGE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                arena.addEdge(edgeMatcher.group(1), edgeMatcher.group(2));
            }
        }
    }

    public static Pattern LINE_PATTERN = Pattern
            .compile("(\\d+) (\\d+) (\\d+) (\\d+(,\\d+)*) \"([^\"]+)\";");

    private static void fillArenaFromLinesInTxtFormat(List<String> lines,
            LinkedArena arena) {
        for (String line : lines) {
            Matcher vertexMatcher = LINE_PATTERN.matcher(line);
            if (vertexMatcher.find()) {
                arena.addVertex(vertexMatcher.group(1), Integer
                        .parseInt(vertexMatcher.group(2)), Player
                        .getPlayerForPriority(Integer.parseInt(vertexMatcher
                                .group(3))));
            }
        }
        for (String line : lines) {
            Matcher edgeMatcher = LINE_PATTERN.matcher(line);
            if (edgeMatcher.find()) {
                for (String target : edgeMatcher.group(4).split(",")) {
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

        // print vertices and store their numbers
        final Map<ParityVertex, Integer> numbersOfVertices = new ConcurrentHashMap<>();
        int numberOfVertex = 0;
        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder
                    .append(String.format("  z%d[shape=%s,label=\"%d\"];\n",
                            numberOfVertex,
                            vertex.getPlayer().getShapeString(),
                            vertex.getPriority()));
            numbersOfVertices.put(vertex, numberOfVertex);
            numberOfVertex++;
        }

        resultBuilder.append('\n');

        // print edges
        for (final ParityVertex vertex : strategy.keySet()) {
            resultBuilder.append(String.format("  z%d->z%d;\n",
                    numbersOfVertices.get(vertex),
                    numbersOfVertices.get(strategy.get(vertex))));
        }
        resultBuilder.append('}');
        return resultBuilder.toString();
    }
}

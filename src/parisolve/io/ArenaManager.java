package parisolve.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;

/**
 * The ArenaManager loads and stores arenas. It does so in a DOT-compatible way
 *
 */
public class ArenaManager {
	public static Pattern VERTEX_PATTERN = Pattern.compile("\\s*(\\w+)\\[shape=(box|oval),\\s*label=\"(\\d+)\"];");
	public static Pattern EDGE_PATTERN = Pattern.compile("\\s*(\\w+)\\s*->\\s*(\\w+);");
	
	public static Arena loadArena(String fileName) throws IOException {
		List<String> lines = Files.readAllLines(Paths.get(fileName), Charset.defaultCharset());
		LinkedArena arena = new LinkedArena();
		//TODO encapsulate this and parse properly
		for (String line : lines) {
			Matcher vertexMatcher = VERTEX_PATTERN.matcher(line);
			if (vertexMatcher.find()) {
				arena.addVertex(vertexMatcher.group(1), Integer.parseInt(vertexMatcher.group(3)), "box".equals(vertexMatcher.group(2)) ? 1 : 0);
			}
		}
		for (String line : lines) {
			Matcher edgeMatcher = EDGE_PATTERN.matcher(line);
			if (edgeMatcher.find()) {
				arena.addEdge(edgeMatcher.group(1), edgeMatcher.group(2));
			}
		}
		return arena;
	}
}

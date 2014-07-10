package parisolve.backend;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parisolve.io.ArenaManager;

public class LinkedArena implements Arena {
    public static class LinkedParityVertex implements ParityVertex {
        final private int priority;
        final private Player player;
        final private Set<LinkedParityVertex> successors = new HashSet<>();
        final private String name;

        public LinkedParityVertex(final String name, final int priority,
                final Player player) {
            this.name = name;
            this.priority = priority;
            this.player = player;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public String getName() {
            return name;
        }

        public Set<? extends ParityVertex> getSuccessors() {
            return successors;
        }

        public void addSuccessor(LinkedParityVertex v) {
            successors.add(v);
        }

        public void removeSuccessor(LinkedParityVertex v) {
            successors.remove(v);
        }

        public ParityVertex getSuccessorFromSet(
                final Set<ParityVertex> verticesToConsider) {
            for (final ParityVertex successor : getSuccessors()) {
                if (verticesToConsider.contains(successor)) {
                    return successor;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return getName() + ": [player=" + getPlayer() + ",label=\""
                    + getPriority() + "\"]";
        }
    }

    Map<String, LinkedParityVertex> vertices = new HashMap<>();

    public void addVertex(String name, int priority, Player player) {
        vertices.put(name, new LinkedParityVertex(name, priority, player));
    }

    public void addEdge(String from, String to) {
        vertices.get(from).addSuccessor(vertices.get(to));
    }

    @Override
    public final Set<ParityVertex> getVertices() {
        return new HashSet<ParityVertex>(vertices.values());
    }

    @Override
    public final String getStatistics() {
        int maxDegree = 0;
        int numberEdges = 0;
        int numberOfSelfloops = 0;
        for (final ParityVertex vertex : getVertices()) {
            final int degree = vertex.getSuccessors().size();
            if (degree > maxDegree) {
                maxDegree = degree;
            }
            numberEdges += degree;
            if (vertex.getSuccessors().contains(vertex)) {
                numberOfSelfloops++;
            }
        }
        final int numberVertices = vertices.size();

        final boolean solitaire = ArenaManager.isSolitaire(this);
        final boolean weak = ArenaManager.isWeak(this);

        return "Number of vertices:\t" + numberVertices + "\nNumber of edges\t"
                + numberEdges + "\nAverage degree\t" + ((double) numberEdges)
                / numberVertices + "\nMaximal degree\t" + maxDegree
                + "\nMaximal priority\t" + getMaxPriority()
                + "\nNumber of selfloops\t" + numberOfSelfloops + "\n\n"
                + (!solitaire ? "not " : "") + "solitaire\n"
                + (!weak ? "not " : "") + "weak";
    }

    public ParityVertex getVertex(String string) {
        // for testing purpose
        return vertices.get(string);
    }
}

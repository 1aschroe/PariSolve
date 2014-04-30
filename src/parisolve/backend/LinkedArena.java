package parisolve.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkedArena implements Arena {
    class LinkedParityVertex implements ParityVertex {
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
    public final Set<? extends ParityVertex> getVertices() {
        return new HashSet<>(vertices.values());
    }

    @Override
    public final int getMaxPriority() {
        return getMaxPriority(getVertices());
    }

    public final static int getMaxPriority(
            final Collection<? extends ParityVertex> vertices) {
        // TODO introduce AbstractArena in case there should be more than one
        // implementation of the arena interface as this can be implemented upon
        // the original arena interface
        int maxPriority = Integer.MIN_VALUE;
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPriority() > maxPriority) {
                maxPriority = vertex.getPriority();
            }
        }
        return maxPriority;
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

        return "Number of vertices\tNumber of edges\tAverage degree\tMaximal degree\tMaximal priority\tNumber of selfloops\n"
                + numberVertices
                + "\t"
                + numberEdges
                + "\t"
                + ((double) numberEdges)
                / numberVertices
                + "\t"
                + maxDegree
                + "\t" + getMaxPriority() + "\t" + numberOfSelfloops;
    }
}

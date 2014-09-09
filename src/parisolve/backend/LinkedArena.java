package parisolve.backend;

import java.util.AbstractCollection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import parisolve.io.ArenaManager;

public class LinkedArena extends AbstractCollection<ParityVertex> implements
        Arena {
    public static class LinkedParityVertex implements ParityVertex {
        private final int priority;
        private final Player player;
        private final Set<ParityVertex> successors = new HashSet<>();
        private final String name;

        public LinkedParityVertex(final String name, final int priority,
                final Player player) {
            this.name = name;
            this.priority = priority;
            this.player = player;
        }

        @Override
        public final int getPriority() {
            return priority;
        }

        @Override
        public final Player getPlayer() {
            return player;
        }

        @Override
        public final String getName() {
            return name;
        }

        public final Set<ParityVertex> getSuccessors() {
            return successors;
        }

        public final void addSuccessor(final LinkedParityVertex v) {
            successors.add(v);
        }

        public final void removeSuccessor(final LinkedParityVertex v) {
            successors.remove(v);
        }

        public final ParityVertex getSuccessorFromSet(
                final Set<ParityVertex> verticesToConsider) {
            for (final ParityVertex successor : getSuccessors()) {
                if (verticesToConsider.contains(successor)) {
                    return successor;
                }
            }
            return null;
        }

        @Override
        public final String toString() {
            return getName() + ": [player=" + getPlayer() + ",label=\""
                    + getPriority() + "\"]";
        }
    }

    private final Map<String, LinkedParityVertex> vertices = new HashMap<>();

    public final void addVertex(final String name, final int priority,
            final Player player) {
        vertices.put(name, new LinkedParityVertex(name, priority, player));
    }

    public final void addEdge(final String from, final String to) {
        vertices.get(from).addSuccessor(vertices.get(to));
    }

    @Override
    public final Set<ParityVertex> getVertices() {
        return new HashSet<ParityVertex>(vertices.values());
    }

    @Override
    public final String getStatistics() {

        final boolean solitaire = ArenaManager.isSolitaire(this);
        final boolean weak = ArenaManager.isWeak(this);

        return getStatistics("Number of vertices:\t%d\n"
                + "Number of edges\t%d\n" + "Average degree\t%f\n"
                + "Maximal degree\t%d\n" + "Maximal priority\t%d\n"
                + "Number of selfloops\t%d")
                + "\n\n"
                + (solitaire ? "" : "not ")
                + "solitaire\n"
                + (weak ? "" : "not ") + "weak";
    }

    @Override
    public final String getStatistics(final String formatString) {
        int maxDegree = 0;
        int numberEdges = 0;
        int numberOfSelfloops = 0;
        for (final ParityVertex vertex : vertices.values()) {
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

        return String.format(formatString, numberVertices, numberEdges,
                ((double) numberEdges) / numberVertices, maxDegree,
                getMaxPriority(), numberOfSelfloops);
    }

    /**
     * returns the vertex with the given <code>name</code>.
     * 
     * @param name
     * @return
     */
    public final ParityVertex getVertex(final String name) {
        // for testing purpose
        return vertices.get(name);
    }

    @Override
    public Iterator<ParityVertex> iterator() {
        final Iterator<LinkedParityVertex> iterator = vertices.values()
                .iterator();
        return new Iterator<ParityVertex>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public ParityVertex next() {
                return iterator.next();
            }
        };
    }

    @Override
    public int size() {
        return vertices.size();
    }
}

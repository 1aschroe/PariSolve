package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;

/**
 * structure which helps <code>BetterAlgorithm</code> to efficiently lift those
 * vertices which need lifting. This is done on the basis of the idea expressed
 * in LNCS 2500 p. 123 to implement a queue which hold the liftable vertices.
 * The idea for this structures implementation bases on the fact that a lift is
 * recalculating the prog-values of vertices, which change iff the successors
 * value changes. Therefore, once a vertex has been lifted, its predecessors are
 * checked, whether these can also be lifted.
 * 
 * As it implements the <code>Iterable</code> interface, one can use instances
 * of this class in for-each-loops.
 * 
 * @author Arne Schröder
 */
public abstract class Liftable implements Iterable<ParityVertex>,
        Iterator<ParityVertex> {
    /**
     * maps each vertex to the set of its predecessors.
     */
    private final Map<ParityVertex, Set<ParityVertex>> predecessors = new ConcurrentHashMap<>();

    /**
     * the given vertices are preprocessed to the map of predecessors.
     * 
     * @param vertices
     *            vertices of the arena to consider
     */
    public Liftable(final Collection<? extends ParityVertex> vertices) {
        for (final ParityVertex vertex : vertices) {
            final Set<ParityVertex> successorsInSubGame = new HashSet<>(
                    vertex.getSuccessors());
            successorsInSubGame.retainAll(vertices);
            for (final ParityVertex successor : successorsInSubGame) {
                if (!predecessors.containsKey(successor)) {
                    predecessors.put(successor, new HashSet<ParityVertex>());
                }
                predecessors.get(successor).add(vertex);
            }
        }
    }

    /**
     * gives the set of predecessors of a given vertex. This set should not be
     * modified.
     * 
     * @param vertex
     *            the vertex to return the predecessors for
     * @return the predecessors of <code>vertex</code>
     */
    protected final Collection<ParityVertex> getPredecessorsOf(
            final ParityVertex vertex) {
        if (!predecessors.containsKey(vertex)) {
            return new HashSet<>();
        }
        return predecessors.get(vertex);
    }

    /**
     * @return the number of vertices considered liftable
     */
    public abstract int verticesSize();

    /**
     * tells the <code>Liftable</code>, whether the given vertex was lifted
     * successfully.
     * 
     * @param vertex
     *            this vertex was lifted successfully.
     */
    public abstract void liftWasSuccessful(final ParityVertex vertex);

    @Override
    public final Iterator<ParityVertex> iterator() {
        return this;
    }

    @Override
    public abstract boolean hasNext();

    @Override
    public abstract ParityVertex next();

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
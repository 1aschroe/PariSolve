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
     * if we are to use (lift) a vertex only once, this set keeps track of what
     * vertices have been used.
     */
    protected final Set<ParityVertex> liftedVertices = new HashSet<>();
    /**
     * stores whether we are to use (lift) a vertex only once.
     */
    private boolean liftOnce;

    /**
     * the given vertices are preprocessed to the map of predecessors.
     * 
     * @param vertices
     *            vertices of the arena to consider
     * @param useOnce
     *            whether a vertex should only be iterated through once
     */
    public Liftable(final Collection<? extends ParityVertex> vertices,
            final boolean useOnce) {
        this.liftOnce = useOnce;
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
     * accessor for the collection of vertices which can be lifted.
     * 
     * @return collection of vertices waiting to be lifted
     */
    protected abstract Collection<ParityVertex> getVerticesCollection();

    /**
     * @return the number of vertices considered liftable
     */
    public final int verticesSize() {
        return getVerticesCollection().size();
    }

    /**
     * tells the <code>Liftable</code>, whether the given vertex was lifted
     * successfully.
     * 
     * @param vertex
     *            this vertex was lifted successfully.
     */
    public final void liftWasSuccessful(final ParityVertex vertex) {
        if (liftOnce) {
            liftedVertices.add(vertex);
        }
        addPredecessors(getPredecessorsOf(vertex));
    }

    /**
     * tells the implementation to add these predecessors to the vertices
     * collection once a lift was successful.
     * 
     * @param predecessors
     *            the predecessors to add
     */
    protected abstract void addPredecessors(
            final Collection<ParityVertex> predecessors);

    @Override
    public final Iterator<ParityVertex> iterator() {
        return this;
    }

    @Override
    public final boolean hasNext() {
        return !getVerticesCollection().isEmpty();
    }

    @Override
    public abstract ParityVertex next();

    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
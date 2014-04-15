package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.ParityVertex;

/**
 * implements a <code>Liftable</code> structure using a <code>HashSet</code>.
 * This results in <code>liftWasSuccessful(ParityVertex)</code> being in O(1).
 * However, <code>next()</code> has a running time of O(k) for k being the
 * number of times, elements of the set have been removed.
 * 
 * @author Arne Schröder
 */
public class SetLiftable extends Liftable {
    /**
     * set of liftable vertices.
     */
    private final Set<ParityVertex> verticesSet;

    /**
     * adds all vertices to the set.
     * 
     * @param vertices
     *            vertices of the arena to consider
     * @param liftableFactory
     *            the factory this liftable came from, which can provide it with
     *            the predecessor-relation
     * @param useOnce
     *            whether a vertex should only be iterated through once
     */
    public SetLiftable(final Collection<? extends ParityVertex> vertices,
            final LiftableFactory liftableFactory, final boolean useOnce) {
        super(vertices, liftableFactory, useOnce);
        this.verticesSet = new HashSet<>(vertices);
    }

    @Override
    public final Collection<ParityVertex> getVerticesCollection() {
        return verticesSet;
    }

    @Override
    protected final void addPredecessors(
            final Collection<ParityVertex> predecessors) {
        verticesSet.addAll(predecessors);
        verticesSet.removeAll(liftedVertices);
    }

    @Override
    public final ParityVertex next() {
        final ParityVertex next = verticesSet.iterator().next();
        verticesSet.remove(next);
        return next;
    }

}

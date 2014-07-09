package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import parisolve.backend.ParityVertex;

/**
 * implements a <code>Liftable</code> structure using both a <code>Stack</code>
 * and a <code>HashSet</code>. The <code>Stack</code> is used for retrieving
 * elements in <code>next()</code> resulting in a constant running time of this
 * method. The set is used to keep track of which elements are in the stack to
 * be able to have a constant running time of <code>contains()</code> resulting
 * in a fast method <code>liftWasSuccessful()</code>. The overhead of keeping
 * both data structures in sync is constant, too.
 * 
 * @author Arne Schröder
 */
public class SetStackLiftable extends Liftable {
    /**
     * set for keeping track of which vertices are considered to be liftable.
     */
    private final Set<ParityVertex> verticesSet;
    /**
     * stack to quickly retrieve liftable vertices.
     */
    private final Stack<ParityVertex> verticesStack = new Stack<ParityVertex>();

    /**
     * adds all vertices to both the set and the stack.
     * 
     * @param vertices
     *            vertices of the arena to consider
     * @param verticesToStartWith 
     * @param liftableFactory
     *            the factory this liftable came from, which can provide it with
     *            the predecessor-relation
     * @param useOnce
     *            whether a vertex should only be iterated through once
     */
    protected SetStackLiftable(
            final Set<? extends ParityVertex> vertices,
            Set<ParityVertex> verticesToStartWith, final LiftableFactory liftableFactory, final boolean useOnce) {
        super(vertices, liftableFactory, useOnce);
        this.verticesSet = new HashSet<>(verticesToStartWith);
        this.verticesStack.addAll(verticesToStartWith);
    }

    @Override
    public final Collection<ParityVertex> getVerticesCollection() {
        return verticesStack;
    }

    @Override
    public final void addPredecessors(
            final Collection<? extends ParityVertex> predecessors) {
        for (final ParityVertex predecessor : predecessors) {
            if (!verticesSet.contains(predecessor)
                    && !liftedVertices.contains(predecessor)) {
                verticesStack.add(predecessor);
            }
        }
        // we do not need to remove the lifted vertices from verticesSet, as
        // verticesSet is never used for the retrieval of vertices.
        verticesSet.addAll(predecessors);
    }

    @Override
    public final ParityVertex next() {
        final ParityVertex next = verticesStack.pop();
        verticesSet.remove(next);
        return next;
    }

}
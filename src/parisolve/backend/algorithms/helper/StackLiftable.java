package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.Stack;

import parisolve.backend.ParityVertex;

/**
 * implements a <code>Liftable</code> structure using a <code>Stack</code>. This
 * results in <code>next()</code> being in O(1). However,
 * <code>liftWasSuccessful(ParityVertex)</code> has a running time of O(n), as
 * for every predecessor of the given vertex it has to be checked, whether the
 * predecessor already is in the stack.
 * 
 * @author Arne Schröder
 */
public class StackLiftable extends Liftable {
    /**
     * stack of liftable vertices.
     */
    private final Stack<ParityVertex> verticesStack = new Stack<ParityVertex>();

    /**
     * adds all vertices to the stack.
     * 
     * @param vertices
     *            the vertices to consider
     * @param useOnce
     *            whether a vertex should only be iterated through once
     */
    public StackLiftable(final Collection<? extends ParityVertex> vertices,
            final boolean useOnce) {
        super(vertices, useOnce);
        this.verticesStack.addAll(vertices);
    }

    @Override
    public final Collection<ParityVertex> getVerticesCollection() {
        return verticesStack;
    }

    @Override
    public final void addPredecessors(final Collection<ParityVertex> predecessors) {
        for (final ParityVertex predecessor : predecessors) {
            if (!verticesStack.contains(predecessor) && !liftedVertices.contains(predecessor)) {
                verticesStack.add(predecessor);
            }
        }
    }

    @Override
    public final ParityVertex next() {
        return verticesStack.pop();
    }
}

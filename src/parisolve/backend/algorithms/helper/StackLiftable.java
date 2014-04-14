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
     */
    public StackLiftable(final Collection<? extends ParityVertex> vertices) {
        super(vertices);
        this.verticesStack.addAll(vertices);
    }

    @Override
    public final int verticesSize() {
        return verticesStack.size();
    };

    @Override
    public final void liftWasSuccessful(final ParityVertex vertex) {
        for (final ParityVertex successor : getPredecessorsOf(vertex)) {
            if (!verticesStack.contains(successor)) {
                verticesStack.add(successor);
            }
        }
    }

    @Override
    public final boolean hasNext() {
        return !verticesStack.isEmpty();
    }

    @Override
    public final ParityVertex next() {
        return verticesStack.pop();
    }
}

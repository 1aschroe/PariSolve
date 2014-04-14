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
     *            the vertices to consider
     */
    public SetStackLiftable(final Collection<? extends ParityVertex> vertices) {
        super(vertices);
        this.verticesSet = new HashSet<>(vertices);
        this.verticesStack.addAll(vertices);
    }

    @Override
    public final int verticesSize() {
        return verticesStack.size();
    }

    @Override
    public final void liftWasSuccessful(final ParityVertex vertex) {
        // TODO: it might even be faster to do verticesStack.addAll(new
        // HashSet<>(getPredecessorsOf(vertex)).removeAll(verticesSet));
        for (final ParityVertex successor : getPredecessorsOf(vertex)) {
            if (!verticesSet.contains(successor)) {
                verticesStack.add(successor);
            }
        }
        verticesSet.addAll(getPredecessorsOf(vertex));
    }

    @Override
    public final boolean hasNext() {
        return !verticesStack.isEmpty();
    }

    @Override
    public final ParityVertex next() {
        final ParityVertex next = verticesStack.pop();
        verticesSet.remove(next);
        return next;
    }

}
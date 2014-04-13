package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import parisolve.backend.ParityVertex;

public class SetStackLiftable extends Liftable {
    final Set<ParityVertex> verticesSet;
    final Stack<ParityVertex> verticesStack = new Stack<ParityVertex>();

    public SetStackLiftable(Collection<? extends ParityVertex> vertices) {
        super(vertices);
        this.verticesSet = new HashSet<>(vertices);
        this.verticesStack.addAll(vertices);
    }

    @Override
    public int verticesSize() {
        return verticesStack.size();
    }

    @Override
    public void liftWasSuccessful(ParityVertex vertex) {
        // TODO: it might even be faster to do verticesStack.addAll(new
        // HashSet<>(getPredecessorsOf(vertex)).removeAll(verticesSet));
        for (ParityVertex successor : getPredecessorsOf(vertex)) {
            if (!verticesSet.contains(successor)) {
                verticesStack.add(successor);
            }
        }
        verticesSet.addAll(getPredecessorsOf(vertex));
    }

    @Override
    public boolean hasNext() {
        return !verticesStack.isEmpty();
    }

    @Override
    public ParityVertex next() {
        final ParityVertex next = verticesStack.pop();
        verticesSet.remove(next);
        return next;
    }

}
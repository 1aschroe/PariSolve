package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.Stack;

import parisolve.backend.ParityVertex;

public class StackLiftable extends Liftable {
    final private Stack<ParityVertex> verticesStack = new Stack<ParityVertex>();

    public StackLiftable(Collection<? extends ParityVertex> vertices) {
        super(vertices);
        this.verticesStack.addAll(vertices);
    }
    
    public int verticesSize() {
        return verticesStack.size();
    };

    @Override
    public void liftWasSuccessful(final ParityVertex vertex) {
        for (ParityVertex successor : getPredecessorsOf(vertex)) {
            if (!verticesStack.contains(successor)) {
                verticesStack.add(successor);
            }
        }
    }

    @Override
    public boolean hasNext() {
        return !verticesStack.isEmpty();
    }

    @Override
    public ParityVertex next() {
        return verticesStack.pop();
    }
}

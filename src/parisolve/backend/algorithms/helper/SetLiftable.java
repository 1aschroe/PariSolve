package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import parisolve.backend.ParityVertex;

public class SetLiftable extends Liftable {
    final Set<ParityVertex> verticesSet;

    public SetLiftable(Collection<? extends ParityVertex> vertices) {
        super(vertices);
        this.verticesSet = new HashSet<>(vertices);
    }

    @Override
    public int verticesSize() {
        return verticesSet.size();
    }

    @Override
    public void liftWasSuccessful(ParityVertex vertex) {
        verticesSet.addAll(getPredecessorsOf(vertex));
    }

    @Override
    public boolean hasNext() {
        return !verticesSet.isEmpty();
    }

    @Override
    public ParityVertex next() {
        final ParityVertex next = verticesSet.iterator().next();
        verticesSet.remove(next);
        return next;
    }

}

package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;

public abstract class Liftable implements Iterable<ParityVertex>,
        Iterator<ParityVertex> {
    final private Map<ParityVertex, Set<ParityVertex>> predecessors = new ConcurrentHashMap<>();

    public Liftable(final Collection<? extends ParityVertex> vertices) {
        for (ParityVertex vertex : vertices) {
            Set<ParityVertex> successorsInSubGame = new HashSet<>(
                    vertex.getSuccessors());
            successorsInSubGame.retainAll(vertices);
            for (ParityVertex successor : successorsInSubGame) {
                if (!predecessors.containsKey(successor)) {
                    predecessors.put(successor, new HashSet<ParityVertex>());
                }
                predecessors.get(successor).add(vertex);
            }
        }
    }

    protected Collection<ParityVertex> getPredecessorsOf(
            final ParityVertex vertex) {
        if (!predecessors.containsKey(vertex)) {
            return new HashSet<>();
        }
        return predecessors.get(vertex);
    }
    
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
    public Iterator<ParityVertex> iterator() {
        return this;
    }

    @Override
    public abstract boolean hasNext();
    @Override
    public abstract ParityVertex next();

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
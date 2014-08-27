package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import parisolve.backend.ParityVertex;

/**
 * structure which helps to efficiently iterate over vertices, which tries to
 * suggest a vertex as few times as possible. It does that by suggesting a
 * vertex's predecessors, if the vertex itself could be used.
 * 
 * One application of this class is the iteration of liftable vertices in
 * <code>SmallMeasureAlgorithm</code>. This is done on the basis of the idea
 * expressed in LNCS 2500 p. 123 to implement a queue which hold the liftable
 * vertices. The idea for this structures implementation bases on the fact that
 * a lift is recalculating the prog-values of vertices, which change iff the
 * successors value changes. Therefore, once a vertex has been lifted, its
 * predecessors are checked, whether these can also be lifted.
 * 
 * Another application is that of finding vertices which might belong to a
 * attractor as used in <code>RecursiveAlgorithm.getAttractor()</code>.
 * 
 * As it implements the <code>Iterable</code> interface, one can use instances
 * of this class in for-each-loops. If a vertex could be used and the
 * predecessors of this vertex should be iterated as well, the method
 * <code>liftWasSuccessful()</code> has to be called with this vertex.
 * 
 * Instances can be obtained from a <code>LiftableFactory</code> which for
 * performance reasons stores the predecessor-structure to eliminate the need to
 * recalculate it.
 * 
 * @author Arne Schröder
 */
public abstract class Liftable implements Iterable<ParityVertex>,
        Iterator<ParityVertex> {
    /**
     * if we are to use (lift) a vertex only once, this set keeps track of what
     * vertices have been used.
     */
    protected final Set<ParityVertex> liftedVertices = new HashSet<>();
    /**
     * stores whether we are to use (lift) a vertex only once.
     */
    private final boolean liftOnce;

    /**
     * can be queried what the predecessors of a vertex are. However, this might
     * return vertices which are not included in this subgame. Therefore, one
     * has to remove everything which does not belong to the subgame.
     * 
     * May be replaced with a suitable interface if needed.
     */
    private final LiftableFactory predecessorProvider;

    /**
     * the vertices of the subgame considered. Used to remove the predecessors
     * from <code>predecessorProvider.getPredecessors()</code> which do not
     * belong to the subgame considered.
     */
    private Set<? extends ParityVertex> verticesOfSubgame;

    /**
     * @param vertices
     *            vertices of the arena to consider
     * @param liftableFactory
     *            the factory this liftable came from, which can provide it with
     *            the predecessor-relation
     * @param useOnce
     *            whether a vertex should only be iterated through once
     */
    public Liftable(final Set<? extends ParityVertex> vertices,
            final LiftableFactory liftableFactory, final boolean useOnce) {
        verticesOfSubgame = vertices;
        predecessorProvider = liftableFactory;
        this.liftOnce = useOnce;
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

        SetView<? extends ParityVertex> predecessors = Sets.intersection(
                predecessorProvider.getPredecessorsOf(vertex),
                verticesOfSubgame);
        addPredecessors(predecessors);
    }

    /**
     * tells the implementation to add these predecessors to the vertices
     * collection once a lift was successful.
     * 
     * @param predecessors
     *            the predecessors to add
     */
    protected abstract void addPredecessors(
            final Collection<? extends ParityVertex> predecessors);

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
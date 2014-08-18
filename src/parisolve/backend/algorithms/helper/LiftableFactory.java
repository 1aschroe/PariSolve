package parisolve.backend.algorithms.helper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * factory which returns instances of <code>Liftable</code> objects which can be
 * used to iterate over vertices of a graph in a manner so that propagation from
 * a vertex to its predecessor is respected.
 * 
 * Usage: When initialising an algorithm and instance of this with all vertices
 * of the arena should be created. This class then preprocesses the vertices
 * into a map of predecessors which can be used subsequently. With
 * <code>getLiftableInstance()</code> an instance of the <code>Liftable</code>
 * -type can be retrieved, specifying the vertices which to consider for lifting
 * and whether or not a vertex is supposed to be considered once or more times.
 * 
 * @author Arne Schröder
 */
public class LiftableFactory {
    /**
     * the implementations available of the liftable type.
     * 
     * @author Arne Schröder
     */
    public enum LiftableImplementationType {
        /**
         * corresponds to <code>SetLiftable</code>.
         */
        SET,
        /**
         * corresponds to <code>StackLiftable</code>.
         */
        STACK,
        /**
         * corresponds to <code>SetStackLiftable</code>.
         */
        SET_STACK;
    }

    /**
     * maps each vertex to the set of its predecessors.
     */
    private final Map<ParityVertex, Set<ParityVertex>> predecessors;

    /**
     * the given vertices are preprocessed to the map of predecessors.
     * 
     * @param vertices
     *            vertices of the arena to consider
     */
    public LiftableFactory(final Set<? extends ParityVertex> vertices) {
        predecessors = getPredecessorMap(vertices);
    }

    public static Map<ParityVertex, Set<ParityVertex>> getPredecessorMap(
            final Set<? extends ParityVertex> vertices) {
        Map<ParityVertex, Set<ParityVertex>> predecessors = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : vertices) {
            final SetView<? extends ParityVertex> successorsInSubGame = Sets
                    .intersection(vertex.getSuccessors(), vertices);
            for (final ParityVertex successor : successorsInSubGame) {
                if (!predecessors.containsKey(successor)) {
                    predecessors.put(successor, new HashSet<ParityVertex>());
                }
                predecessors.get(successor).add(vertex);
            }
        }
        return predecessors;
    }

    /**
     * gives the set of predecessors of a given vertex. This set should not be
     * modified.
     * 
     * @param vertex
     *            the vertex to return the predecessors for
     * @return the predecessors of <code>vertex</code>
     */
    protected final Set<ParityVertex> getPredecessorsOf(
            final ParityVertex vertex) {
        if (!predecessors.containsKey(vertex)) {
            return new HashSet<>();
        }
        return predecessors.get(vertex);
    }

    /**
     * returns an instance of the <code>Liftable</code>-type, which can be used
     * to iterate over the vertices specified, using the default implementation.
     * 
     * @param vertices
     *            the vertices to iterate
     * @param useOnce
     *            whether to one vertex can be used twice
     * @return a liftable instance
     */
    public final Liftable getLiftableInstance(final Set<ParityVertex> vertices,
            final boolean useOnce) {
        return getLiftableInstance(vertices, vertices, useOnce);
    }

    public Liftable getLiftableInstance(Set<? extends ParityVertex> subGame,
            Set<ParityVertex> verticesToStartWith, boolean useOnce) {
        return getLiftableInstance(subGame, verticesToStartWith,
                LiftableImplementationType.SET_STACK, useOnce);
    }

    /**
     * returns an instance of the <code>Liftable</code>-type, which can be used
     * to iterate over the vertices specified, allowing to specify the
     * implementation.
     * 
     * @param subGame
     *            the vertices to iterate
     * @param verticesToStartWith
     * @param type
     *            the type of implementation of the returned liftable instance
     * @param useOnce
     *            whether to one vertex can be used twice
     * @return a liftable instance
     */
    public final Liftable getLiftableInstance(
            final Set<? extends ParityVertex> subGame,
            Set<ParityVertex> verticesToStartWith,
            final LiftableImplementationType type, final boolean useOnce) {
        switch (type) {
        case SET:
            return new SetLiftable(subGame, verticesToStartWith, this, useOnce);
        case STACK:
            return new StackLiftable(subGame, verticesToStartWith, this,
                    useOnce);
        case SET_STACK:
            return new SetStackLiftable(subGame, verticesToStartWith, this,
                    useOnce);
        default:
            throw new IllegalArgumentException("Type " + type
                    + " is not supported for liftables.");
        }
    }
}
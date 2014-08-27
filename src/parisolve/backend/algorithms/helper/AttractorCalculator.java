package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

import com.google.common.collect.Sets;

public class AttractorCalculator {
    private AttractorCalculator() {
        // disable
    }

    /**
     * calculates the attractor of a given set of vertices with respect to
     * player <code>sigma</code>. That is, from the collection of vertices
     * returned, sigma can force every path into <code>vertices</code>.
     * 
     * @param vertices
     *            the vertices to attract to
     * @param sigma
     *            the player to force
     * @param allVertices
     *            the vertices to consider
     * @return the attractor of <code>sigma</code> to <code>vertices</code> with
     *         respect to the subgame on <code>allVertices</code>
     */
    public static final AttractorStrategyPair getAttractor(
            final Collection<? extends ParityVertex> vertices,
            final Player sigma, final Set<ParityVertex> allVertices,
            final LiftableFactory liftable) {
        final Set<ParityVertex> attractor = new HashSet<>(vertices);
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        // the vertices which might get into the attractor eventually
        final Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
        otherVertices.removeAll(attractor);
        Liftable iterator = liftable.getLiftableInstance(otherVertices, true);

        for (final ParityVertex vertex : iterator) {
            // getSuccessors returns all successor in the original graph.
            // Therefore, we must remove everything outside of allVertices.
            final Set<ParityVertex> successorsInSubGame = Sets.intersection(
                    vertex.getSuccessors(), allVertices);
            if (vertex.getPlayer() == sigma.getOponent()
                    && attractor.containsAll(successorsInSubGame)) {
                attractor.add(vertex);
                iterator.liftWasSuccessful(vertex);
            } else if (vertex.getPlayer() == sigma) {
                for (final ParityVertex successor : successorsInSubGame) {
                    if (attractor.contains(successor)) {
                        attractor.add(vertex);
                        iterator.liftWasSuccessful(vertex);
                        strategy.put(vertex, successor);
                        break;
                    }
                }
            }
        }

        return new AttractorStrategyPair(attractor, strategy);
    }

    /**
     * convenience method to not have to specify allVertices as a set
     * 
     * @deprecated as this is not performant
     */
    public static final AttractorStrategyPair getAttractor(
            final Collection<? extends ParityVertex> vertices,
            final Player sigma,
            final Collection<? extends ParityVertex> allVertices,
            final LiftableFactory liftable) {
        // TODO: one may want to revive the "getVertices()"-Method from arena.
        return getAttractor(allVertices, sigma, Sets.newHashSet(allVertices),
                liftable);
    }
}

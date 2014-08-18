package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableSet;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.RecursiveAlgorithm.AttractorStrategyPair;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.ProgressMeasure;

public class AttractiveBetterAlgorithm extends BetterAlgorithm {
    protected static void doLift(final ProgressMeasure measure,
            final Liftable iterator, final ParityVertex vertex,
            final LiftableFactory liftable) {
        final boolean lifted = measure.lift(vertex);
        if (lifted) {
            iterator.liftWasSuccessful(vertex);
            if (measure.get(vertex).isTop()) {
                AttractorStrategyPair attractor = getAttractor(
                        new ImmutableSet.Builder<ParityVertex>().add(vertex)
                                .build(), measure.getPlayer().getOponent(),
                        measure.getVertices(), liftable);
                for (final ParityVertex attractedVertex : attractor
                        .getAttractor()) {
                    if (!measure.get(attractedVertex).isTop()) {
                        measure.setToTop(attractedVertex);
                        iterator.liftWasSuccessful(attractedVertex);
                    }
                }
            }
        }
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
            final Player sigma,
            final Collection<? extends ParityVertex> allVertices,
            final LiftableFactory liftable) {
        final Set<ParityVertex> attractor = new HashSet<>(vertices);
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        // the vertices which might get into the attractor eventually
        final Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
        otherVertices.removeAll(attractor);
        Liftable iterator = liftable.getLiftableInstance(otherVertices, true);

        for (final ParityVertex vertex : iterator) {
            final Set<ParityVertex> successorsInSubGame = new HashSet<>(
                    vertex.getSuccessors());
            // getSuccessors returns all successor in the original graph.
            // Therefore, we must remove everything outside of allVertices.
            successorsInSubGame.retainAll(allVertices);
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
}

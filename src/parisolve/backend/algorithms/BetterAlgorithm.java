package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.ProgressMeasure;

/**
 * implementation of the algorithm of Jurdzinski - Small Progress Measures for
 * Solving Parity Games (2000), as described in section 7.5 in LNCS 2500. It
 * also stretches to the classes <code>ProgressMeasure</code> and
 * <code>MeasureValue</code> in the <code>helper</code>-package.
 * 
 * @author Arne Schröder
 */
public class BetterAlgorithm implements Solver {
    @Override
    public final Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        int maxPriority = getMaxPriority(vertices);
        final ProgressMeasure measure = new ProgressMeasure(maxPriority,
                getSizeOfMG(vertices, maxPriority));

        // TODO: implement liftable construct
        boolean didChange = true;
        while (didChange) {
            didChange = false;
            for (final ParityVertex vertex : vertices) {
                didChange |= measure.lift(vertex);
            }
        }

        return getWinningRegion(player, vertices, measure);
    }

    /**
     * determines the winning region from the measure given with respect to the
     * player specified. Iff <code>player</code> is player B, then these are all
     * the vertices with measure top. Iff <code>player</code> is player A, then
     * these are all the vertices with a measure not being top. This corresponds
     * to ||rho|| from Definition 7.19 and its complement.
     * 
     * @param player
     *            whose winning region to determine
     * @param vertices
     *            the vertices to consider for the winning region
     * @param measure
     *            the measure from which to derive the winning region
     * @return <code>player</code>'s winning region on <code>vertices</code>
     *         according to <code>measure</code>
     */
    protected Set<ParityVertex> getWinningRegion(final Player player,
            final Collection<? extends ParityVertex> vertices,
            final ProgressMeasure measure) {
        final Set<ParityVertex> winningRegion = new HashSet<>();
        // TODO: the measure also knows all vertices. Therefore, the
        // parameter vertices is redundant
        for (final ParityVertex vertex : vertices) {
            if ((player == Player.A) == (!measure.get(vertex).isTop())) {
                winningRegion.add(vertex);
            }
        }
        return winningRegion;
    }

    /**
     * determines the size of M_G.
     * 
     * @param vertices
     * @param maxPriority
     * @return
     */
    protected int[] getSizeOfMG(
            final Collection<? extends ParityVertex> vertices,
            final int maxPriority) {
        final int[] counts = new int[maxPriority + 1];
        for (final ParityVertex vertex : vertices) {
            counts[vertex.getPriority()]++;
        }
        return counts;
    }

    // TODO: move this to arena
    protected int getMaxPriority(
            final Collection<? extends ParityVertex> vertices) {
        int maxPriority = Integer.MIN_VALUE;
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPriority() > maxPriority) {
                maxPriority = vertex.getPriority();
            }
        }
        return maxPriority;
    }
}

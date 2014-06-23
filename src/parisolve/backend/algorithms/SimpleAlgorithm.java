package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * Idea given in theorem 7.6 LNCS 2500 p. 113 and respectively Zwick, Paterson
 * (1996): The complexity of mean payoff games on graphs.
 * 
 * The main idea is that parity games can be transformed into mean payoff games
 * by using an exponential formula, implemented in
 * <code>calculateValueFromPriority</code>. Then a function nu is used to give
 * the expected payoff from a given vertex. Vertices with positive nu are
 * winning vertices for player A and vice versa.
 * 
 * The description of the algorithm is given in a recursive form. As we only
 * need the values of nu_k-1 to determine the values of nu_k, calculation can be
 * done iteratively, by incrementing k and only storing the values of nu_k-1.
 * 
 * @author Arne SchrÃ¶der
 */
public class SimpleAlgorithm implements Solver {

    @Override
    public final Solution getSolution(final Arena arena) {
        final Collection<? extends ParityVertex> vertices = arena.getVertices();
        final int n = vertices.size();
        final long maxK = calculateMaxK(vertices);

        final Map<ParityVertex, Long> nuForLastK = runAlgorithm(arena,
                vertices, n, maxK);

        return determineSolution(nuForLastK);
    }

    /**
     * runs the algorithm to determine nu.
     * 
     * 
     * 
     * @return a map mapping each vertex to its value, assigned by nu.
     */
    private Map<ParityVertex, Long> runAlgorithm(final Arena arena,
            final Collection<? extends ParityVertex> vertices, final int n,
            final long maxK) {
        // stores nu_k-1
        Map<ParityVertex, Long> nuForLastK = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : vertices) {
            nuForLastK.put(vertex, 0L);
        }
        for (long k = 0; k <= maxK; k++) {
            // stores nu_k
            final Map<ParityVertex, Long> nuForCurrentK = new ConcurrentHashMap<>();

            for (final ParityVertex v : vertices) {
                nuForCurrentK.put(v,
                        getBestSuccessorsValue(arena, nuForLastK, n, v));
            }
            nuForLastK = nuForCurrentK;
        }
        return nuForLastK;
    }

    /**
     * determining winning region of the player specified. In case of player A
     * these are all vertices with positive nu-value. For player B those are the
     * vertices with negative nu-value.
     * 
     * @param player
     *            player to calculate the winning region for
     * @param nuForLastK
     *            the last nu_k calculated
     * @return
     */
    private Solution determineSolution(final Map<ParityVertex, Long> nuForLastK) {
        final Set<ParityVertex> winningRegionForA = new HashSet<>();
        final Set<ParityVertex> winningRegionForB = new HashSet<>();
        final Map<ParityVertex, ParityVertex> strategy = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : nuForLastK.keySet()) {
            if (nuForLastK.get(vertex) > 0) {
                winningRegionForA.add(vertex);
                ParityVertex maxSuccessor = null;
                long maxValue = -1;
                for (final ParityVertex successor : vertex.getSuccessors()) {
                    if (nuForLastK.get(successor) > maxValue) {
                        maxValue = nuForLastK.get(successor);
                        maxSuccessor = successor;
                    }
                }
                strategy.put(vertex, maxSuccessor);
            } else {
                winningRegionForB.add(vertex);
                ParityVertex minSuccessor = null;
                long minValue = 1;
                for (final ParityVertex successor : vertex.getSuccessors()) {
                    if (nuForLastK.get(successor) < minValue) {
                        minValue = nuForLastK.get(successor);
                        minSuccessor = successor;
                    }
                }
                strategy.put(vertex, minSuccessor);
            }
        }
        return new Solution(winningRegionForA, winningRegionForB, Player.A,
                strategy);
    }

    /**
     * returns the maximal value of a successor u as in Zwick, Paterson (1996),
     * Theorem 2.1.
     */
    private long getBestSuccessorsValue(final Arena arena,
            final Map<ParityVertex, Long> nuForLastK,
            final int numberOfVerticesInArena, final ParityVertex v) {
        final Collection<? extends ParityVertex> successors = v.getSuccessors();
        final Player playerOfV = v.getPlayer();
        long bestValue = 0;
        for (final ParityVertex successor : successors) {
            final long successorsValue = getValue(v, successor, nuForLastK,
                    numberOfVerticesInArena);
            if (bestValue == 0) {
                bestValue = successorsValue;
            } else {
                if ((playerOfV == Player.A && successorsValue > bestValue)
                        || (playerOfV == Player.B && successorsValue < bestValue)) {
                    bestValue = successorsValue;
                }
            }
        }
        return bestValue;
    }

    /**
     * calculates the maximal k according to Zwick, Paterson (1996), Theorem
     * 2.4.
     */
    private long calculateMaxK(final Collection<? extends ParityVertex> vertices) {
        final int n = vertices.size();
        int maxPriority = LinkedArena.getMaxPriority(vertices);
        // it is important to have 4 as long to not have an overflow when
        // calculating 4*n^2 as int.
        return (long) (4L * n * n * Math.pow(n, maxPriority));
    }

    /**
     * calculates an edge's value (term in curly braces) in Zwick, Paterson
     * (1996), Theorem 2.1 and LNCS 2500, Equation 7.1 respectively. Variable
     * names are taken from LNCS 2500
     * 
     * @param v
     *            vertex from which the edge starts
     * @param u
     *            vertex to which the edge points
     * @param nuForLastK
     *            stores the values of \nu_k-1
     * @param n
     *            number of vertices in arena
     * @return edge's value
     */
    public static long getValue(final ParityVertex v, final ParityVertex u,
            final Map<ParityVertex, Long> nuForLastK, final int n) {
        return getValueFromPriority(v.getPriority(), n) + nuForLastK.get(u);
    }

    /**
     * used for memoization to not always calculate Math.pow(-n, i). As n
     * usually does not change, values are only stored for one n, which is
     * stored in currentN.
     */
    private static int currentN;
    /**
     * stores the d+1 values (-n)^0 ... (-n)^d
     */
    private static List<Long> valuesForPriority = new ArrayList<>();

    /**
     * returns the value of <code>calculateValueFromPriority</code> but
     * remembers values calculated once.
     * 
     * @param i
     *            vertex' priority
     * @param n
     *            number of vertices in arena
     * @return weight of priority
     */
    public static long getValueFromPriority(final int i, final int n) {
        if (currentN != n) {
            // we assume n to not be changed often therefore we only store
            // values for one n and have to reset those if there is a new value
            valuesForPriority = new ArrayList<>();
            currentN = n;
        }
        if (valuesForPriority.size() <= i) {
            // we insert all values so i is the highes value
            for (int newI = valuesForPriority.size(); newI <= i; newI++) {
                valuesForPriority.add(calculateValueFromPriority(newI, n));
            }
        }
        return valuesForPriority.get(i);
    }

    /**
     * transforms the priority value of a parity game to the weights of a mean
     * payoff game, using formula w(v,u) = (-n)^i from LNCS 2500 proof of Lemma
     * 7.5 p. 112 and Jurdzinski (1998), 'Deciding the winner in parity games is
     * in UP \cap co-UP' from proof of Theorem 4 p. 4 respectively.
     * 
     * @param i
     *            vertex' priority
     * @param n
     *            number of vertices in arena
     * @return weight of priority
     */
    public static long calculateValueFromPriority(final int i, final int n) {
        return (long) Math.pow(-n, i);
    }
}

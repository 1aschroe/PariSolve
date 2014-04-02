package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;

/**
 * implementation of the recursive algorithm given in Automatentheorie und Logik
 * - Hofmann and Lange (2011) 15.1.3 and Jurdzinski (2000) respectively. It is
 * similar to the algorithm given in LNCS 2500 Figure 6.3 and Figure 6.4.
 */
public class RecursiveAlgorithm implements Solver {
	/**
	 * Helper class to return a partition of winning regions. It helps to
	 * enforce that winning regions are assigned to the right player. This
	 * brings clarification to the informal notion of a pair, as used in
	 */
	class WinningRegionPartition {
		Map<Player, Collection<? extends ParityVertex>> partition = new ConcurrentHashMap<>();

		/**
		 * create a partition of a set of vertices, which represent the winning
		 * regions of a player and its opponent. They should be disjoint.
		 * 
		 * @param winningRegionForSigma
		 *            winning region of player <code>sigma</code>
		 * @param winningRegionForOponent
		 *            winning region of player <code>sigma</code>'s opponent
		 * @param sigma
		 *            the player of the first winning region
		 */
		WinningRegionPartition(
				final Collection<? extends ParityVertex> winningRegionForSigma,
				final Collection<? extends ParityVertex> winningRegionForOponent,
				final Player sigma) {
			// TODO: test, whether the two sets are disjoint
			partition.put(sigma, winningRegionForSigma);
			partition.put(sigma.getOponent(), winningRegionForOponent);
		}

		/**
		 * accessor method for the winning region of <code>player</code>
		 * 
		 * @param player
		 *            the player to return the winning region for
		 * @return <code>player</code>'s winning region
		 */
		Collection<? extends ParityVertex> getWinningRegionFor(
				final Player player) {
			return partition.get(player);
		}
	}

	@Override
	public Collection<? extends ParityVertex> getWinningRegionForPlayer(
			Arena arena, Player player) {
		final Collection<? extends ParityVertex> vertices = arena.getVertices();
		WinningRegionPartition partition = solveGame(vertices);
		return partition.getWinningRegionFor(player);
	}

	/**
	 * singleton for empty set to not create an empty set for every instance
	 * when one is needed.
	 */
	private final static Set<ParityVertex> EMPTY_SET = new HashSet<ParityVertex>();

	/**
	 * solves an arena specified by the vertices given by returning the
	 * partition of the given vertices into the winning regions. This
	 * corresponds to Abbildung 15.5 in Hofmann and Lange (2011).
	 * 
	 * @param vertices
	 *            the vertices, to solve the parity game on. This is G in
	 *            Abbildung 15.5
	 * @return a partition with a set of vertices for each player to win upon.
	 */
	private WinningRegionPartition solveGame(
			final Collection<? extends ParityVertex> vertices) {
		// in Abbildung 15.5 this is n
		int maxPriority = LinkedArena.getMaxPriority(vertices);
		if (maxPriority <= 0) {
			return new WinningRegionPartition(vertices, EMPTY_SET, Player.A);
		}
		final Player sigma = Player.getPlayerForInt(maxPriority % 2);

		// in Abbildung 15.5 this is N
		final Set<ParityVertex> verticesWithMaxPriority = new HashSet<>();
		// TODO: this can be included into the previous loop
		for (final ParityVertex vertex : vertices) {
			if (vertex.getPriority() == maxPriority) {
				verticesWithMaxPriority.add(vertex);
			}
		}

		// in Abbildung 15.5 this is N'
		final Collection<ParityVertex> attractor = getAttractor(
				verticesWithMaxPriority, sigma, vertices);

		final WinningRegionPartition partition = getPartitionForUnsolvedVertices(
				vertices, attractor);

		if (partition.getWinningRegionFor(sigma.getOponent()).isEmpty()) {
			// this means player sigma wins all vertices in G\N' and therefore
			// in G
			return new WinningRegionPartition(vertices, EMPTY_SET, sigma);
		}

		// in Abbildung 15.5 this is N''
		Collection<ParityVertex> attractor2 = getAttractor(
				partition.getWinningRegionFor(sigma.getOponent()),
				sigma.getOponent(), vertices);

		final WinningRegionPartition partition2 = getPartitionForUnsolvedVertices(
				vertices, attractor2);
		final Set<ParityVertex> winningRegion2 = new HashSet<>(
				partition2.getWinningRegionFor(sigma.getOponent()));
		winningRegion2.addAll(attractor2);
		return new WinningRegionPartition(
				partition2.getWinningRegionFor(sigma), winningRegion2, sigma);
	}

	private WinningRegionPartition getPartitionForUnsolvedVertices(
			final Collection<? extends ParityVertex> vertices,
			final Collection<ParityVertex> attractor) {
		final Set<ParityVertex> unsolvedVertices = new HashSet<>(vertices);
		unsolvedVertices.removeAll(attractor);
		return solveGame(unsolvedVertices);
	}

	private Collection<ParityVertex> getAttractor(
			final Collection<? extends ParityVertex> vertices,
			final Player sigma,
			final Collection<? extends ParityVertex> allVertices) {
		Set<ParityVertex> attractor = new HashSet<>(vertices);
		Set<ParityVertex> otherVertices = new HashSet<>(allVertices);
		int otherSize = otherVertices.size();
		otherVertices.removeAll(attractor);

		while (otherSize > otherVertices.size()) {
			otherSize = otherVertices.size();
			for (ParityVertex vertex : otherVertices) {
				Set<ParityVertex> successorsInSubGame = new HashSet<>(
						vertex.getSuccessors());
				successorsInSubGame.retainAll(allVertices);
				if ((vertex.getPlayer() == sigma && !Collections.disjoint(
						attractor, successorsInSubGame))
						|| (vertex.getPlayer() == sigma.getOponent() && attractor
								.containsAll(successorsInSubGame))) {
					attractor.add(vertex);
				}
			}
			// TODO: only remove those which are new
			otherVertices.removeAll(attractor);
		}

		return attractor;
	}
}

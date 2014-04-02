package parisolve.backend;

import java.util.Collection;

/**
 * abstracts the notion of an arena in which a parity game can be played.
 * 
 * @author Arne Schröder
 */
public interface Arena {
	/**
	 * accessor to the entirety of vertices in this arena
	 * 
	 * @return the vertices in this arena
	 */
	Collection<? extends ParityVertex> getVertices();

	/**
	 * for a given vertex, returns the vertices, this vertex points to, aka its
	 * successors.
	 * 
	 * @param vertex
	 *            the vertex for which to return the successors
	 * @return the vertex' successors
	 */
	Collection<? extends ParityVertex> getSuccessors(ParityVertex vertex);

	/**
	 * the maximal priority of the vertices in this arena. Convenience method,
	 * as this can be calculated from the vertices, obtained by
	 * <code>getVertices()</code>
	 * 
	 * @return the maximal priority
	 */
	int getMaxPriority();
}

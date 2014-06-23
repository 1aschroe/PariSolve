package parisolve.backend;

import java.util.Set;

/**
 * abstracts the notion of an arena in which a parity game can be played.
 * 
 * @author Arne Schröder
 */
public interface Arena {
    /**
     * accessor to the entirety of vertices in this arena.
     * 
     * @return the vertices in this arena
     */
    Set<ParityVertex> getVertices();

    /**
     * the maximal priority of the vertices in this arena. Convenience method,
     * as this can be calculated from the vertices, obtained by
     * <code>getVertices()</code>
     * 
     * @return the maximal priority
     */
    int getMaxPriority();

    String getStatistics();
}

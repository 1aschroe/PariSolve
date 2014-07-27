package parisolve.backend;

import java.util.Collection;
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
    default public int getMaxPriority() {
        return getMaxPriority(getVertices());
    }

    /**
     * @return a textual description of the features of this arena.
     */
    String getStatistics();

    /**
     * @return the maximal priority of the <code>vertices</code> given.
     */
    public static int getMaxPriority(
            final Collection<? extends ParityVertex> vertices) {
        return vertices.parallelStream().mapToInt(ParityVertex::getPriority)
                .max().orElse(Integer.MIN_VALUE);
    }
}

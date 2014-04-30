package parisolve.backend;

import java.util.Set;

public interface ParityVertex {
    String getName();

    int getPriority();

    /**
     * @return the player whose turn it is to move from this vertex
     */
    Player getPlayer();

    /**
     * returns the vertices, this vertex points to, aka its successors.
     * 
     * @return the vertex' successors
     */
    Set<? extends ParityVertex> getSuccessors();
}

package parisolve;

import parisolve.backend.Arena;

/**
 * to be implemented to be informed when an open-action takes place.
 */
public interface OpenListener {

    /**
     * informs that an arena has been opened.
     * 
     * @param arena
     *            the opened arena
     */
    void openedArena(Arena arena);

}

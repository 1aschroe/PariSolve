package parisolve;

/**
 * to be implemented to be informed when a solve-action is requested.
 */
public interface SolveListener {

    /**
     * informs that the arena is supposed to be solved.
     */
    void solve();
}

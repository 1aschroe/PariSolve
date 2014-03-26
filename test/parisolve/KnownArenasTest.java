package parisolve;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.PrimitiveAlgorithm;
import parisolve.backend.algorithms.SimpleAlgorithm;
import parisolve.backend.algorithms.Solver;
import parisolve.io.ArenaManager;

/**
 * tests whether for the smallest arenas, all implemented algorithms return the
 * same result.
 */
public class KnownArenasTest {

    @Test
    public final void testAUL() throws IOException, URISyntaxException {
        doTestArena("aul.arena");
    }

    @Test
    public final void testDetermacy() throws IOException, URISyntaxException {
        doTestArena("determacy.arena");
    }

    @Test
    public final void testExample() throws IOException, URISyntaxException {
        doTestArena("example.arena");
    }

    // @Test
    // public final void testWikipedia() throws IOException, URISyntaxException
    // {
    // doTestArena("wikipedia.arena");
    // }

    /**
     * given a specific arena file, tests whether all algorithms implemented
     * give the same winning region on this arena.
     * 
     * @param filename
     *            filename of the arena-file
     * @throws IOException
     *             if the file does not exist
     * @throws URISyntaxException
     *             ignore this...
     */
    public final void doTestArena(final String filename) throws IOException,
            URISyntaxException {
        final Arena aulArena = loadArenaFromResources(filename);
        // TODO come up with a solution which is implementation independent
        final PrimitiveAlgorithm primitiveAlgorithm = new PrimitiveAlgorithm();
        final Collection<? extends ParityVertex> referenceWinningRegion = primitiveAlgorithm
                .getWinningRegionForPlayer(aulArena, 0);

        for (final Solver algorithm : getAlgorithms()) {
            final Collection<? extends ParityVertex> winningRegion = algorithm
                    .getWinningRegionForPlayer(aulArena, 0);
            Assert.assertEquals("Algorithm did not solve correctly.",
                    referenceWinningRegion, winningRegion);
        }
    }

    /**
     * loads an arena stored in the <code>resource</code> source-folder.
     * 
     * @param filename
     *            filename of the arena-file
     * @return the arena to be loaded
     * @throws IOException
     *             if the file does not exist
     * @throws URISyntaxException
     *             ignore this...
     */
    private Arena loadArenaFromResources(final String filename)
            throws IOException, URISyntaxException {
        // TODO i cannot even describe how ugly this is...
        // but it works and awaits a better solution
        return ArenaManager.loadArena(Thread.currentThread()
                .getContextClassLoader().getResource(filename).toURI()
                .getPath().substring(1));
    }

    /**
     * returns a list of currently implemented algorithms to solve parity games.
     * 
     * @return algorithms, able to solve parity games
     */
    public static final List<Solver> getAlgorithms() {
        return Arrays.asList(new PrimitiveAlgorithm(), new SimpleAlgorithm());
    }
}

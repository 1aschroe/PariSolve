package parisolve;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.RecursiveAlgorithm;
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

    @Test
    public final void testTrivial() throws IOException, URISyntaxException {
        doTestArena("trivial.arena");
    }

    @Test
    public final void testWikipedia() throws IOException, URISyntaxException {
        doTestArena("wikipedia.arena");
    }

    @Test
    public final void testH43() throws IOException, URISyntaxException {
        doTestArena("H43.txt");
    }

    @Test
    public final void testSchewe() throws IOException, URISyntaxException {
        doTestArena("Schewe-example.txt");
    }

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
        final Solver referenceAlgorithm = new RecursiveAlgorithm();
        final Collection<? extends ParityVertex> referenceWinningRegion = referenceAlgorithm
                .getSolution(aulArena).getWinningRegionFor(Player.A);

        for (final Solver algorithm : AlgorithmManager.getAlgorithms()) {
            final Collection<? extends ParityVertex> winningRegion = algorithm
                    .getSolution(aulArena).getWinningRegionFor(Player.A);
            Assert.assertEquals("Algorithm "
                    + algorithm.getClass().getSimpleName()
                    + " did not solve correctly.", referenceWinningRegion,
                    winningRegion);
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
    public static Arena loadArenaFromResources(final String filename)
            throws IOException, URISyntaxException {
        // TODO I cannot even describe how ugly this is...
        // but it works and awaits a better solution
        return ArenaManager.loadArena(Thread.currentThread()
                .getContextClassLoader().getResource(filename).toURI()
                .getPath().substring(1));
    }
}

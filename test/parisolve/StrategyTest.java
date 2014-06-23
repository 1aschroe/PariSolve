package parisolve;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import parisolve.backend.LinkedArena;
import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.AlgorithmManager;
import parisolve.backend.algorithms.RecursiveAlgorithm;
import parisolve.backend.algorithms.Solution;
import parisolve.backend.algorithms.Solver;

public class StrategyTest {

    @Test
    public final void testAUL() throws IOException, URISyntaxException {
        LinkedArena aulArena = (LinkedArena) KnownArenasTest
                .loadArenaFromResources("aul.arena");

        /* for ( */final Solver algorithm = new RecursiveAlgorithm();
        /* : AlgorithmManager . getAlgorithms ()) { */
        Solution solution = algorithm.getSolution(aulArena);
        Map<ParityVertex, ParityVertex> strategy = solution.getStrategy();

        final ParityVertex a = aulArena.getVertex("a");
        final ParityVertex b = aulArena.getVertex("b");
        final ParityVertex c = aulArena.getVertex("c");
        final ParityVertex d = aulArena.getVertex("d");
        final ParityVertex e = aulArena.getVertex("e");
        final ParityVertex f = aulArena.getVertex("f");
        final ParityVertex g = aulArena.getVertex("g");
        final ParityVertex h = aulArena.getVertex("h");
        final ParityVertex i = aulArena.getVertex("i");

        Assert.assertEquals("Algorithm " + algorithm.getClass().getSimpleName()
                + " did not solve correctly: Strategy for e was not c.", c,
                strategy.get(e));
        Assert.assertEquals("Algorithm " + algorithm.getClass().getSimpleName()
                + " did not solve correctly: Strategy for b was not f.", f,
                strategy.get(b));
        Assert.assertEquals("Algorithm " + algorithm.getClass().getSimpleName()
                + " did not solve correctly: Strategy for h was not g.", g,
                strategy.get(h));
        Assert.assertEquals("Algorithm " + algorithm.getClass().getSimpleName()
                + " did not solve correctly: Strategy for c was not d.", d,
                strategy.get(c));
        // }
    }
}

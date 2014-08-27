package parisolve.backend.algorithms.helper;

import java.util.Map;
import java.util.Set;

import parisolve.backend.ParityVertex;

/**
 * represents an attractor of a set together with the strategy to get to
 * that set.
 * 
 * @author Arne Schröder
 */
public class AttractorStrategyPair {
    private final Set<ParityVertex> attractor;
    private final Map<ParityVertex, ParityVertex> strategy;

    public AttractorStrategyPair(final Set<ParityVertex> attractor,
            final Map<ParityVertex, ParityVertex> strategy) {
        this.attractor = attractor;
        this.strategy = strategy;
    }

    public Set<ParityVertex> getAttractor() {
        return attractor;
    }

    public Map<ParityVertex, ParityVertex> getStrategy() {
        return strategy;
    }
}
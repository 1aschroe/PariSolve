package parisolve.backend.algorithms;

import com.google.common.collect.ImmutableSet;

import parisolve.backend.ParityVertex;
import parisolve.backend.algorithms.helper.AttractorCalculator;
import parisolve.backend.algorithms.helper.AttractorStrategyPair;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.ProgressMeasure;

public class AttractiveSmallMeasureAlgorithm extends SmallMeasureAlgorithm {
    protected static void doLift(final ProgressMeasure measure,
            final Liftable iterator, final ParityVertex vertex,
            final LiftableFactory liftable) {
        final boolean lifted = measure.lift(vertex);
        if (lifted) {
            iterator.liftWasSuccessful(vertex);
            if (measure.get(vertex).isTop()) {
                AttractorStrategyPair attractor = AttractorCalculator.getAttractor(
                        new ImmutableSet.Builder<ParityVertex>().add(vertex)
                                .build(), measure.getPlayer().getOponent(),
                        measure.getVertices(), liftable);
                for (final ParityVertex attractedVertex : attractor
                        .getAttractor()) {
                    if (!measure.get(attractedVertex).isTop()) {
                        measure.setToTop(attractedVertex);
                        iterator.liftWasSuccessful(attractedVertex);
                    }
                }
            }
        }
    }
}

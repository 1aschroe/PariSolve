package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.MeasureValue;
import parisolve.backend.algorithms.helper.ProgressMeasure;

public class BetterAlgorithm implements Solver {
    @Override
    public Collection<? extends ParityVertex> getWinningRegionForPlayer(
            final Arena arena, final Player player) {
        Collection<? extends ParityVertex> vertices = arena.getVertices();
        int maxPriority = Integer.MIN_VALUE;
        List<Integer> counts = new ArrayList<>();
        for (ParityVertex vertex : vertices) {
            if (counts.size() <= vertex.getPriority()) {
                // we insert all values so i is the highes value
                for (int newPriority = counts.size(); newPriority <= vertex
                        .getPriority(); newPriority++) {
                    counts.add(0);
                }
            }
            int currentValue = counts.get(vertex.getPriority());
            counts.set(vertex.getPriority(), currentValue + 1);
            if (vertex.getPriority() > maxPriority) {
                maxPriority = vertex.getPriority();
            }
        }
        long product = 1;
        for (int count : counts) {
            product *= count + 1;
        }
        ProgressMeasure measure = new ProgressMeasure(maxPriority, product);
        boolean didChange = true;
        int loops = 0;
        while (didChange) {
            didChange = false;
            for (ParityVertex vertex : vertices) {
                // TODO: does this skip the right part if didChange is true?
                didChange |= measure.lift(vertex);
            }
            loops++;
        }

        Set<ParityVertex> winningRegion = new HashSet<>();
        for (ParityVertex vertex : vertices) {
            if ((player == Player.A) == (!measure.get(vertex).isT())) {
                winningRegion.add(vertex);
            }
        }
        return winningRegion;
    }

}

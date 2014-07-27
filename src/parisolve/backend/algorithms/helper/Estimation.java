package parisolve.backend.algorithms.helper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.LinkedArena.LinkedParityVertex;

import com.google.common.base.Functions;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * v \in (V' -> R)
 * 
 * Corresponds to <code>ProgressMeasure</code>, so implementations might be
 * merged.
 * 
 * @author Arne Schröder
 * 
 * @see ProgressMeasure
 */
public class Estimation {
    private static final ParityVertex BOTTOM = new LinkedParityVertex("Bottom",
            0, Player.B);

    final Map<ParityVertex, Evaluation> estimation;

    public Estimation(Map<ParityVertex, Evaluation> estimationMap) {
        estimation = estimationMap;
    }

    public Evaluation get(final ParityVertex vertex) {
        if (estimation.containsKey(vertex)) {
            return estimation.get(vertex);
        }
        if (vertex.getPlayer() == Player.A) {
            return Evaluation.ZERO_EVALUATION;
        } else {
            Evaluation minEvaluation = Evaluation.ZERO_EVALUATION;
            for (final ParityVertex succesor : vertex.getSuccessors()) {
                Evaluation compareTo = Evaluation.ZERO_EVALUATION.plus(succesor
                        .getPriority());
                if (minEvaluation.compareTo(compareTo) > 0) {
                    minEvaluation = compareTo;
                }
            }
            return minEvaluation;
        }
    }

    public boolean hasEvaluatedVertex(final ParityVertex vertex) {
        return estimation.containsKey(vertex);
    }

    public boolean hasEvaluatedAllVertices(
            final Collection<? extends ParityVertex> vertices) {
        return estimation.keySet().containsAll(vertices);
    }

    public Set<ParityVertex> getEvaluatedVertices() {
        return estimation.keySet();
    }

    public static Estimation plus(final Estimation estimate1,
            final Estimation estimate2) {
        final Map<ParityVertex, Evaluation> sumEstimation = new ConcurrentHashMap<>(
                estimate1.estimation);
        for (final ParityVertex key : estimate2.estimation.keySet()) {
            if (sumEstimation.containsKey(key)) {
                sumEstimation.put(
                        key,
                        sumEstimation.get(key).plus(
                                estimate2.estimation.get(key)));
            } else {
                sumEstimation.put(key, estimate2.estimation.get(key));
            }
        }
        return new Estimation(sumEstimation);
    }

    public Estimation plus(final Estimation other) {
        return plus(this, other);
    }

    @Override
    public String toString() {
        return estimation.toString();
    }

    public Set<ParityVertex> getNonInfiniteVertices() {
        return estimation
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != Evaluation.INFINITY_EVALUTION)
                .map(entry -> entry.getKey())
                .filter(vertex -> vertex != BOTTOM).collect(Collectors.toSet());
    }

    public Set<ParityVertex> getInfiniteVertices() {
        return estimation
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == Evaluation.INFINITY_EVALUTION)
                .map(entry -> entry.getKey()).collect(Collectors.toSet());
    }

    public static Set<ParityVertex> getOriginalRegion(
            Set<ParityVertex> artificialVertices,
            Map<ParityVertex, ParityVertex> mapping) {
        return new HashSet<>(Collections2.transform(artificialVertices,
                Functions.forMap(mapping)));
    }

    public Solution getSolution(final Map<ParityVertex, ParityVertex> mapping) {
        return new Solution(getOriginalRegion(getInfiniteVertices(), mapping),
                getOriginalRegion(getNonInfiniteVertices(), mapping), Player.A,
                new ConcurrentHashMap<>());
    }

    public boolean isLargerThan(Estimation other) {
        boolean hasLarger = false;
        SetView<ParityVertex> allKeys = Sets.union(estimation.keySet(),
                other.estimation.keySet());
        for (final ParityVertex vertex : allKeys) {
            final Evaluation ourEvaluation = get(vertex);
            final Evaluation theirEvaluation = other.get(vertex);
            if (ourEvaluation.compareTo(theirEvaluation) > 0) {
                hasLarger = true;
            } else if (ourEvaluation.compareTo(theirEvaluation) < 0) {
                return false;
            }
        }
        return hasLarger;
    }

    public static ModifyableEstimation getInitialUpdate() {
        final ModifyableEstimation optimalUpdate = new ModifyableEstimation();
        optimalUpdate.put(BOTTOM, Evaluation.ZERO_EVALUATION);
        return optimalUpdate;
    }
}
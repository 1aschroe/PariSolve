package parisolve.backend.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena.LinkedParityVertex;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Estimation;
import parisolve.backend.algorithms.helper.Evaluation;
import parisolve.backend.algorithms.helper.ImprovementPotential;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;
import parisolve.backend.algorithms.helper.ModifyableEstimation;
import parisolve.backend.algorithms.helper.Solution;

import com.google.common.collect.Sets;

/**
 * implementation of the algorithm sketched in Schewe (2008), "An Optimal
 * Strategy Improvement Algorithm for Solving Parity and Payoff Games".
 * 
 * @author Arne Schröder
 * 
 */
public class StrategyImprovementAlgorithm implements Solver {

    @Override
    public final Solution getSolution(final Arena arena) {
        // TODO: it might cost time to convert the graph to bipartite. Maybe
        // there is a way to not do this?
        Map<ParityVertex, ParityVertex> mapping = getBipartiteArena(arena);
        final Set<ParityVertex> vertices = mapping.keySet();
        Estimation estimation = getDefaultEstimation(vertices);
        ImprovementPotential improvementPotential = getImprovementPotential(
                vertices, estimation);

        long otherTime = 0;

        boolean reachedFixPoint = false;
        while (!reachedFixPoint) {
            final ModifyableEstimation optimalUpdate = Estimation
                    .getInitialUpdate();

            loopBasicUpdateStep(vertices, improvementPotential, optimalUpdate);

            long otherStart = System.currentTimeMillis();
            final Estimation newEstimation = estimation.plus(optimalUpdate);
            reachedFixPoint = !newEstimation.isLargerThan(estimation);
            estimation = newEstimation;
            long otherEnd = System.currentTimeMillis();
            otherTime += otherEnd - otherStart;

            System.out.println("Calculating improvement took "
                    + timeImprovementPotential + " ms.");
            System.out.println("Update took " + timeUpdate + " ms.");
            System.out.println("Update time:\t1\t" + timeUpdate1 + "\t2\t"
                    + timeUpdate2 + "\t3\t" + timeUpdate3 + "\t3a\t"
                    + timeUpdate3a + "\t3b\t" + timeUpdate3b + "\t3c\t"
                    + timeUpdate3c + "\t4\t" + timeUpdate4 + "\t5\t"
                    + timeUpdate5);
            System.out.println("Update times:\t1\t" + timesUpdate1 + "\t2\t"
                    + timesUpdate2 + "\t3\t" + timesUpdate3 + "\t3a\t"
                    + timesUpdate3 + "\t3b\t" + timesUpdate3b + "\t3c\t"
                    + timesUpdate3c + "\t4\t" + timesUpdate4 + "\t5\t"
                    + timesUpdate5);
            System.out.println("Plus took\t" + Evaluation.timePlus);
            System.out.println("Min\t" + minTime + "\tminIter\t" + minIterTime
                    + "\tminHasNext\t" + minHasNextTime + "\tminNext\t"
                    + minNextTime + "\tminMap\t" + minMapTime + "\tminMin\t"
                    + minMinTime);
            System.out.println("Compare\t" + Evaluation.timeCompare + "\t"
                    + Evaluation.timeCompare);
            System.out.println("Other took " + otherTime + " ms.");

            improvementPotential = getImprovementPotential(vertices, estimation);
        }
        return estimation.getSolution(mapping);
    }

    private static Map<ParityVertex, ParityVertex> getBipartiteArena(Arena arena) {
        final long startConvert = System.currentTimeMillis();
        Set<ParityVertex> originalVertices = arena.getVertices();
        Map<String, LinkedParityVertex> newVertices = new ConcurrentHashMap<>();
        Map<ParityVertex, ParityVertex> mapping = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : originalVertices) {
            LinkedParityVertex newVertex = new LinkedParityVertex(
                    vertex.getName(), vertex.getPriority(), vertex.getPlayer());
            newVertices.put(newVertex.getName(), newVertex);
            mapping.put(newVertex, vertex);
        }
        for (final ParityVertex vertex : originalVertices) {
            for (final ParityVertex successor : vertex.getSuccessors()) {
                if (vertex.getPlayer() != successor.getPlayer()) {
                    newVertices.get(vertex.getName()).addSuccessor(
                            newVertices.get(successor.getName()));
                } else {
                    LinkedParityVertex newSuccessor = new LinkedParityVertex(
                            vertex.getName() + "->" + successor.getName(),
                            vertex.getPriority(), vertex.getPlayer()
                                    .getOponent());
                    newSuccessor.addSuccessor(newVertices.get(successor
                            .getName()));
                    newVertices.get(vertex.getName())
                            .addSuccessor(newSuccessor);
                    newVertices.put(newSuccessor.getName(), newSuccessor);
                    // FIXME: should newSuccessor be mapped to successor or
                    // vertex?
                    mapping.put(newSuccessor, successor);
                }
            }
        }
        final long endConvert = System.currentTimeMillis();
        System.out.println("Converting to bipartite took "
                + (endConvert - startConvert) + " ms.");
        System.out.println("Resulted in " + newVertices.size() + " instead of "
                + originalVertices.size() + " vertices.");
        return mapping;
    }

    static long timeImprovementPotential = 0;

    private static ImprovementPotential getImprovementPotential(
            final Set<? extends ParityVertex> vertices,
            final Estimation estimation) {
        long potentialStart = System.currentTimeMillis();
        // improvementPotential = P
        // only includes edges in the improvement arena
        final ImprovementPotential improvementPotential = new ImprovementPotential();
        for (final ParityVertex vertex : vertices) {
            for (final ParityVertex successor : vertex.getSuccessors()) {
                if (estimation.get(vertex)
                        .compareTo(
                                estimation.get(successor).plus(
                                        successor.getPriority())) <= 0) {
                    final Evaluation potential = estimation.get(successor)
                            .plus(successor.getPriority())
                            .minus(estimation.get(vertex));
                    improvementPotential.put(vertex, successor, potential);
                }
            }
        }
        long potentialEnd = System.currentTimeMillis();
        timeImprovementPotential += potentialEnd - potentialStart;
        return improvementPotential;
    }

    protected static Estimation getDefaultEstimation(
            final Set<? extends ParityVertex> vertices) {
        final Map<ParityVertex, Evaluation> estimationMap = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPlayer() == Player.A) {
                estimationMap.put(vertex, Evaluation.ZERO_EVALUATION);
            } else {
                final Evaluation minEvaluation = vertex
                        .getSuccessors()
                        .stream()
                        .map(successor -> Evaluation.ZERO_EVALUATION
                                .plus(successor.getPriority()))
                        .min((a, b) -> a.compareTo(b))
                        .orElse(Evaluation.ZERO_EVALUATION);
                estimationMap.put(vertex, minEvaluation);
            }
        }
        final Estimation estimation = new Estimation(estimationMap);
        return estimation;
    }

    static long timeUpdate = 0;
    static long timeUpdate1 = 0;
    static long timeUpdate2 = 0;
    static long timeUpdate3 = 0;
    static long timeUpdate3a = 0;
    static long timeUpdate3b = 0;
    static long timeUpdate3c = 0;
    static long timeUpdate4 = 0;
    static long timeUpdate5 = 0;
    static int timesUpdate1 = 0;
    static int timesUpdate2 = 0;
    static int timesUpdate3 = 0;
    static int timesUpdate3b = 0;
    static int timesUpdate3c = 0;
    static int timesUpdate4 = 0;
    static int timesUpdate5 = 0;

    static class VertexEvaluationPair {
        final ParityVertex vertex;
        final Evaluation evaluation;

        public VertexEvaluationPair(final ParityVertex vertex,
                final Evaluation evaluation) {
            this.vertex = vertex;
            this.evaluation = evaluation;
        }
    }

    private static void loopBasicUpdateStep(final Set<ParityVertex> vertices,
            final ImprovementPotential improvementPotential,
            final ModifyableEstimation optimalUpdate) {
        final long updateStart = System.currentTimeMillis();
        LiftableFactory liftableFactory = new LiftableFactory(vertices);
        int whileLoopCount = 0;
        int forLoopCount = 0;
        while (!optimalUpdate.hasEvaluatedAllVertices(vertices)) {
            whileLoopCount++;
            timesUpdate1++;
            final long update1Start = System.currentTimeMillis();
            Set<ParityVertex> nonEvaluatedVertices = Sets.difference(vertices,
                    optimalUpdate.getEvaluatedVertices());
            Set<ParityVertex> liftable = nonEvaluatedVertices
                    .stream()
                    .filter(vertex -> optimalUpdate
                            .hasEvaluatedAllVertices(improvementPotential
                                    .getSuccessorsOf(vertex)))
                    .collect(Collectors.toSet());
            Liftable iterator = liftableFactory.getLiftableInstance(
                    nonEvaluatedVertices, liftable, true);
            if (liftable.isEmpty()) {
                Optional<VertexEvaluationPair> min = nonEvaluatedVertices
                        .stream()
                        .filter(vertex -> vertex.getPlayer() == Player.B)
                        .map(vertex -> new VertexEvaluationPair(vertex,
                                getMinSuccessorEvaluationOfVertex(
                                        improvementPotential, optimalUpdate,
                                        vertex)))
                        .min((a, b) -> a.evaluation.compareTo(b.evaluation));
                min.ifPresent(pair -> {
                    timesUpdate5++;
                    final long update5Start = System.currentTimeMillis();
                    doCase4(optimalUpdate, pair.vertex, pair.evaluation,
                            iterator);
                    timeUpdate5 += System.currentTimeMillis() - update5Start;
                });
            } else {
                timeUpdate1 += System.currentTimeMillis() - update1Start;
                System.out.println(Sets.difference(vertices,
                        optimalUpdate.getEvaluatedVertices()).size());
                outerForLoop: for (final ParityVertex vertex : iterator) {
                    if (optimalUpdate.hasEvaluatedVertex(vertex)) {
                        continue;
                    }
                    if (vertex.getPlayer() == Player.B) {
                        timesUpdate2++;
                        final long update2Start = System.currentTimeMillis();
                        if (optimalUpdate
                                .hasEvaluatedAllVertices(improvementPotential
                                        .getSuccessorsOf(vertex))
                                && doUpdateCase1(improvementPotential,
                                        optimalUpdate, vertex)) {
                            iterator.liftWasSuccessful(vertex);
                            timeUpdate2 += System.currentTimeMillis()
                                    - update2Start;
                            continue;
                        } else {
                            // case 2:
                            for (final ParityVertex successor : improvementPotential
                                    .getSuccessorsOf(vertex)) {
                                if (optimalUpdate.hasEvaluatedVertex(successor)
                                        && improvementPotential.get(vertex,
                                                successor).compareTo(
                                                Evaluation.ZERO_EVALUATION) == 0
                                        && optimalUpdate
                                                .get(successor)
                                                .compareTo(
                                                        Evaluation.ZERO_EVALUATION) == 0) {
                                    optimalUpdate.put(vertex,
                                            Evaluation.ZERO_EVALUATION);
                                    iterator.liftWasSuccessful(vertex);
                                    timeUpdate2 += System.currentTimeMillis()
                                            - update2Start;
                                    continue outerForLoop;
                                }
                            }
                        }
                        timeUpdate2 += System.currentTimeMillis()
                                - update2Start;
                    } else {
                        // vertex.getPlayer() == Player.A
                        timesUpdate4++;
                        final long update4Start = System.currentTimeMillis();
                        if (optimalUpdate
                                .hasEvaluatedAllVertices(improvementPotential
                                        .getSuccessorsOf(vertex))
                                && doUpdateCase3(improvementPotential,
                                        optimalUpdate, vertex)) {
                            iterator.liftWasSuccessful(vertex);
                        }
                        timeUpdate4 += System.currentTimeMillis()
                                - update4Start;
                    }
                }
            }
        }
        final long updateEnd = System.currentTimeMillis();
        timeUpdate += updateEnd - updateStart;

        System.out.println(whileLoopCount + "\t" + forLoopCount);
    }

    protected static Evaluation getMinSuccessorEvaluationOfVertex(
            final ImprovementPotential improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        timesUpdate3++;
        final long update3Start = System.currentTimeMillis();
        final long update3aStart = System.currentTimeMillis();
        final List<ParityVertex> evaluatedSuccessors = new ArrayList<>(
                improvementPotential.getSuccessorsOf(vertex));
        evaluatedSuccessors.retainAll(optimalUpdate.getEvaluatedVertices());
        timeUpdate3a += System.currentTimeMillis() - update3aStart;
        timesUpdate3b++;
        final long update3bStart = System.currentTimeMillis();
        timesUpdate3c++;
        final long update3cStart = System.currentTimeMillis();
        final Evaluation minIntermediateImprovement = getMinEvaluation(
                improvementPotential, optimalUpdate, vertex,
                evaluatedSuccessors);
        timeUpdate3b += System.currentTimeMillis() - update3bStart;
        timeUpdate3c += System.currentTimeMillis() - update3cStart;
        timeUpdate3 += System.currentTimeMillis() - update3Start;
        return minIntermediateImprovement;
    }

    protected static void doCase4(final ModifyableEstimation optimalUpdate,
            ParityVertex minForCase4, Evaluation minIntermediateImprovement,
            Liftable iterator) {
        if (minForCase4 == null
                || (optimalUpdate.hasEvaluatedVertex(minForCase4) && optimalUpdate
                        .get(minForCase4).compareTo(minIntermediateImprovement) == 0)) {
            System.out.println("Now we would have broken.");
        } else {
            optimalUpdate.put(minForCase4, minIntermediateImprovement);
            // TODO: this does not seem clean.
            iterator.liftWasSuccessful(minForCase4);
            for (final ParityVertex vertex : iterator) {
                if (!optimalUpdate.hasEvaluatedVertex(vertex)) {
                    optimalUpdate.put(vertex, minIntermediateImprovement);
                    iterator.liftWasSuccessful(vertex);
                }
            }
        }
    }

    protected static boolean doUpdateCase1(
            final ImprovementPotential improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        final List<? extends ParityVertex> successors = improvementPotential
                .getSuccessorsOf(vertex);
        final Evaluation minEvaluation = getMinEvaluation(improvementPotential,
                optimalUpdate, vertex, successors);
        if (optimalUpdate.hasEvaluatedVertex(vertex)
                && optimalUpdate.get(vertex).compareTo(minEvaluation) == 0) {
            return false;
        }
        optimalUpdate.put(vertex, minEvaluation);
        return true;
    }

    private static long minTime = 0;
    private static long minIterTime = 0;
    private static long minHasNextTime = 0;
    private static long minNextTime = 0;
    private static long minMapTime = 0;
    private static long minMinTime = 0;

    // TODO: duplicate code in getMinEvaluation and doUpdateCase1 and
    // doUpdateCase3
    protected static Evaluation getMinEvaluation(
            final ImprovementPotential improvementPotential,
            final Estimation optimalUpdate, final ParityVertex vertex,
            Collection<? extends ParityVertex> successors) {
        Evaluation minEvaluation = successors
                .stream()
                .map(successor -> optimalUpdate.get(successor).plus(
                        improvementPotential.get(vertex, successor)))
                .min((a, b) -> a.compareTo(b))
                .orElse(Evaluation.INFINITY_EVALUTION);
        return minEvaluation;
    }

    protected static boolean doUpdateCase3(
            final ImprovementPotential improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        Evaluation maxEvaluation = improvementPotential
                .getSuccessorsOf(vertex)
                .stream()
                .map(successor -> optimalUpdate.get(successor).plus(
                        improvementPotential.get(vertex, successor)))
                .max((a, b) -> a.compareTo(b))
                .orElse(Evaluation.ZERO_EVALUATION);
        if (optimalUpdate.hasEvaluatedVertex(vertex)
                && optimalUpdate.get(vertex).compareTo(maxEvaluation) == 0) {
            return false;
        }
        optimalUpdate.put(vertex, maxEvaluation);
        return true;
    }
}

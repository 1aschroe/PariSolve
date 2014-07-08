package parisolve.backend.algorithms;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena.LinkedParityVertex;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;

import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Table;

/**
 * implementation of the algorithm sketched in Schewe (2008), "An Optimal
 * Strategy Improvement Algorithm for Solving Parity and Payoff Games".
 * 
 * @author Arne Schröder
 * 
 */
public class StrategyImprovementAlgorithm implements Solver {
    class ParityEdge {
        public final ParityVertex from;
        public final ParityVertex to;

        public ParityEdge(final ParityVertex from, final ParityVertex to) {
            if (from.getSuccessors().contains(to)) {
                this.from = from;
                this.to = to;
            } else {
                throw new IllegalArgumentException();
            }
        }

        @Override
        public final boolean equals(final Object obj) {
            if (!(obj instanceof ParityEdge)) {
                return false;
            }
            final ParityEdge edge = (ParityEdge) obj;
            return Objects.equal(from, edge.from) && Objects.equal(to, edge.to);
        }

        @Override
        public final int hashCode() {
            return Objects.hashCode(from, to);
        }

        @Override
        public String toString() {
            return "(" + from.toString() + "->" + to.toString() + ")";
        }
    }

    /**
     * class representing the type R = (C_0 → Z) ∪ ∞ which is defined in Schewe
     * (2008) p. 374 "Escape Games".
     * 
     * @author Arne Schröder
     */
    static class Evaluation {
        final int[] map;
        final int maxColour;

        Evaluation(final int[] map) {
            maxColour = map.length;
            this.map = map.clone();
        }

        private int get(final int colour) {
            if (colour > 0 && colour <= maxColour) {
                return map[colour - 1];
            }
            return 0;
        }

        static Evaluation combine(final Evaluation eva1, final Evaluation eva2,
                final BinaryOperator<Integer> combinator) {
            // TODO: subtraction with eva2 as infinityEvaluation?
            if (eva1 == infinityEvaluation || eva2 == infinityEvaluation) {
                return infinityEvaluation;
            }
            int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
            int[] combinedMap = new int[maxColour];
            for (int colour = 1; colour <= maxColour; colour++) {
                combinedMap[colour - 1] = combinator.apply(eva1.get(colour),
                        eva2.get(colour));
            }
            return new Evaluation(combinedMap);
        }

        static Table<Evaluation, Evaluation, Evaluation> plusMemo = HashBasedTable
                .create();

        static long timePlus = 0;

        static Evaluation plus(final Evaluation eva1, final Evaluation eva2) {
            long plusStart = System.currentTimeMillis();
            Evaluation sum;
            if (!plusMemo.contains(eva1, eva2)) {
                sum = combine(eva1, eva2, (a, b) -> a + b);
                plusMemo.put(eva1, eva2, sum);
            } else {
                sum = plusMemo.get(eva1, eva2);
            }
            timePlus += System.currentTimeMillis() - plusStart;
            return sum;
        }

        Evaluation plus(final Evaluation summand) {
            return plus(this, summand);
        }

        static Evaluation minus(final Evaluation eva1, final Evaluation eva2) {
            return combine(eva1, eva2, (a, b) -> a - b);
        }

        Evaluation minus(final Evaluation subtrahent) {
            return minus(this, subtrahent);
        }

        /**
         * oplus-operator as defined in Schewe (2008) p. 374, last paragraph of
         * "Escape Games".
         * 
         * @param eva
         *            \rho
         * @param colour
         *            c'
         * @return \rho'
         */
        static Evaluation plus(final Evaluation eva, final int colour) {
            if (eva == infinityEvaluation || colour == 0) {
                return eva;
            }
            final int[] nextMap;
            if (colour <= eva.maxColour) {
                nextMap = eva.map.clone();
            } else {
                nextMap = Arrays.copyOf(eva.map, colour);
            }
            nextMap[colour - 1]++;
            return new Evaluation(nextMap);
        }

        /**
         * syntactic sugar of oplus-operator to use infix-notation.
         * 
         * @param colourToAdd
         * @return
         */
        Evaluation plus(final int colourToAdd) {
            return plus(this, colourToAdd);
        }

        static long timeCompare = 0;

        static int compare(final Evaluation eva1, final Evaluation eva2) {
            final long compareStart = System.currentTimeMillis();
            if (eva1 == eva2) {
                timeCompare += System.currentTimeMillis() - compareStart;
                return 0;
            }
            if (eva1 == infinityEvaluation) {
                timeCompare += System.currentTimeMillis() - compareStart;
                return 1;
            }
            if (eva2 == infinityEvaluation) {
                timeCompare += System.currentTimeMillis() - compareStart;
                return -1;
            }
            int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
            for (int colour = maxColour; colour > 0; colour--) {
                // maybe this can be sped up by testing equality first
                if (eva1.get(colour) > eva2.get(colour)) {
                    if (colour % 2 == 0) {
                        timeCompare += System.currentTimeMillis()
                                - compareStart;
                        return 1;
                    } else {
                        timeCompare += System.currentTimeMillis()
                                - compareStart;
                        return -1;
                    }
                } else if (eva1.get(colour) < eva2.get(colour)) {
                    if (colour % 2 == 0) {
                        timeCompare += System.currentTimeMillis()
                                - compareStart;
                        return -1;
                    } else {
                        timeCompare += System.currentTimeMillis()
                                - compareStart;
                        return 1;
                    }
                }
            }
            timeCompare += System.currentTimeMillis() - compareStart;
            return 0;
        }

        int compareTo(final Evaluation eva) {
            return compare(this, eva);
        }

        @Override
        public String toString() {
            if (maxColour <= 0) {
                return "0";
            }
            String returnString = "";
            for (int i = maxColour; i > 0; i--) {
                returnString += ", " + get(i);
            }
            return "(" + returnString.substring(2) + ")";
        }
    }

    final static Evaluation zeroEvaluation = new Evaluation(new int[0]);

    final static Evaluation infinityEvaluation = new Evaluation(new int[0]) {
        public String toString() {
            return "∞";
        };
    };

    private static final ParityVertex BOTTOM = new LinkedParityVertex("Bottom",
            0, Player.B);

    /**
     * v \in (V' -> R)
     * 
     * @author Arne Schröder
     * 
     */
    static class Estimation {
        final Map<ParityVertex, Evaluation> estimation;

        public Estimation(Map<ParityVertex, Evaluation> estimationMap) {
            estimation = estimationMap;
        }

        Evaluation get(final ParityVertex vertex) {
            if (estimation.containsKey(vertex)) {
                return estimation.get(vertex);
            }
            if (vertex.getPlayer() == Player.A) {
                return zeroEvaluation;
            } else {
                Evaluation minEvaluation = zeroEvaluation;
                for (final ParityVertex succesor : vertex.getSuccessors()) {
                    Evaluation compareTo = zeroEvaluation.plus(succesor
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
            return estimation.entrySet().stream()
                    .filter(entry -> entry.getValue() != infinityEvaluation)
                    .map(entry -> entry.getKey())
                    .filter(vertex -> vertex != BOTTOM)
                    .collect(Collectors.toSet());
        }

        public Set<ParityVertex> getInfiniteVertices() {
            return estimation.entrySet().stream()
                    .filter(entry -> entry.getValue() == infinityEvaluation)
                    .map(entry -> entry.getKey()).collect(Collectors.toSet());
        }

        public static Set<ParityVertex> getOriginalRegion(
                Set<ParityVertex> artificialVertices,
                Map<ParityVertex, ParityVertex> mapping) {
            return new HashSet<>(Collections2.transform(artificialVertices,
                    Functions.forMap(mapping)));
        }

        public Solution getSolution(
                final Map<ParityVertex, ParityVertex> mapping) {
            return new Solution(getOriginalRegion(getInfiniteVertices(),
                    mapping), getOriginalRegion(getNonInfiniteVertices(),
                    mapping), Player.A, new ConcurrentHashMap<>());
        }

        public boolean isLarger(Estimation other) {
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
    }

    class ModifyableEstimation extends Estimation {
        public ModifyableEstimation() {
            super(new ConcurrentHashMap<ParityVertex, Evaluation>());
        }

        public void put(final ParityVertex vertex, final Evaluation eva) {
            estimation.put(vertex, eva);
        }
    }

    @Override
    public final Solution getSolution(final Arena arena) {
        // TODO: this might cost time. Maybe it is possible to not do this?
        Map<ParityVertex, ParityVertex> mapping = getBipartiteArena(arena);
        final Set<ParityVertex> vertices = mapping.keySet();
        Estimation estimation = getDefaultEstimation(vertices);
        Table<ParityVertex, ParityVertex, Evaluation> improvementPotential = getImprovementPotential(
                vertices, estimation);

        long otherTime = 0;

        boolean reachedFixPoint = false;
        while (!reachedFixPoint) {
            final ModifyableEstimation optimalUpdate = getInitialUpdate();

            loopBasicUpdateStep(vertices, improvementPotential, optimalUpdate);

            long otherStart = System.currentTimeMillis();
            final Estimation newEstimation = estimation.plus(optimalUpdate);
            reachedFixPoint = !newEstimation.isLarger(estimation);
            estimation = newEstimation;
            long otherEnd = System.currentTimeMillis();
            otherTime += otherEnd - otherStart;

            System.out.println("Calculating improvement took "
                    + timeImprovementPotential + " ms.");
            System.out.println("Update took " + timeUpdate + " ms.");
            System.out.println("Update time:\t" + timeUpdate1 + "\t"
                    + timeUpdate2 + "\t" + timeUpdate3 + "\t" + timeUpdate3a
                    + "\t" + timeUpdate3b + "\t" + timeUpdate3c + "\t"
                    + timeUpdate4 + "\t" + timeUpdate5);
            System.out.println("Update times:\t" + timesUpdate1 + "\t"
                    + timesUpdate2 + "\t" + timesUpdate3 + "\t" + timesUpdate3
                    + "\t" + timesUpdate3b + "\t" + timesUpdate3c + "\t"
                    + timesUpdate4 + "\t" + timesUpdate5);
            System.out.println("Plus took " + Evaluation.timePlus);
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

    private Map<ParityVertex, ParityVertex> getBipartiteArena(Arena arena) {
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

    private Table<ParityVertex, ParityVertex, Evaluation> getImprovementPotential(
            final Set<? extends ParityVertex> vertices,
            final Estimation estimation) {
        long potentialStart = System.currentTimeMillis();
        // improvementPotential = P
        // only includes edges in the improvement arena
        final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential = HashBasedTable
                .create();
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

    protected Estimation getDefaultEstimation(
            final Set<? extends ParityVertex> vertices) {
        final Map<ParityVertex, Evaluation> estimationMap = new ConcurrentHashMap<>();
        for (final ParityVertex vertex : vertices) {
            if (vertex.getPlayer() == Player.A) {
                estimationMap.put(vertex, zeroEvaluation);
            } else {
                final Evaluation minEvaluation = vertex
                        .getSuccessors()
                        .stream()
                        .map(successor -> zeroEvaluation.plus(successor
                                .getPriority())).min((a, b) -> a.compareTo(b))
                        .orElse(zeroEvaluation);
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

    private void loopBasicUpdateStep(
            final Set<? extends ParityVertex> vertices,
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final ModifyableEstimation optimalUpdate) {
        final long updateStart = System.currentTimeMillis();
        LiftableFactory liftableFactory = new LiftableFactory(vertices);
        int whileLoopCount = 0;
        int forLoopCount = 0;
        while (!optimalUpdate.hasEvaluatedAllVertices(vertices)) {
            whileLoopCount++;
            timesUpdate1++;
            final long update1Start = System.currentTimeMillis();
            boolean changed = false;
            ParityVertex minForCase4 = null;
            Evaluation minIntermediateImprovement = null;
            Liftable iterator = liftableFactory.getLiftableInstance(
                    Sets.difference(vertices,
                            optimalUpdate.getEvaluatedVertices()), true);
            timeUpdate1 += System.currentTimeMillis() - update1Start;
            System.out.println(Sets.difference(vertices,
                    optimalUpdate.getEvaluatedVertices()).size());
            for (final ParityVertex vertex : iterator) {
                if (optimalUpdate.hasEvaluatedVertex(vertex)) {
                    continue;
                }
                if (vertex.getPlayer() == Player.B) {
                    timesUpdate2++;
                    final long update2Start = System.currentTimeMillis();
                    if (optimalUpdate
                            .hasEvaluatedAllVertices(improvementPotential.row(
                                    vertex).keySet())
                            && doUpdateCase1(improvementPotential,
                                    optimalUpdate, vertex)) {
                        changed = true;
                        iterator.liftWasSuccessful(vertex);
                        continue;
                    } else {
                        // case 2:
                        for (final ParityVertex successor : improvementPotential
                                .row(vertex).keySet()) {
                            if (optimalUpdate.hasEvaluatedVertex(successor)
                                    && improvementPotential.get(vertex,
                                            successor)
                                            .compareTo(zeroEvaluation) == 0
                                    && optimalUpdate.get(successor).compareTo(
                                            zeroEvaluation) == 0) {
                                optimalUpdate.put(vertex, zeroEvaluation);
                                changed = true;
                                iterator.liftWasSuccessful(vertex);
                                continue;
                            }
                        }
                    }
                    timeUpdate2 += System.currentTimeMillis() - update2Start;
                    timesUpdate3++;
                    final long update3Start = System.currentTimeMillis();
                    final long update3aStart = System.currentTimeMillis();
                    final Set<ParityVertex> evaluatedSuccessors = Sets
                            .intersection(optimalUpdate.getEvaluatedVertices(),
                                    improvementPotential.row(vertex).keySet());
                    timeUpdate3a += System.currentTimeMillis() - update3aStart;
                    if (minForCase4 == null
                            && optimalUpdate.get(vertex) != infinityEvaluation) {
                        timesUpdate3b++;
                        final long update3bStart = System.currentTimeMillis();
                        minForCase4 = vertex;
                        minIntermediateImprovement = getMinEvaluation(
                                improvementPotential, optimalUpdate, vertex,
                                evaluatedSuccessors);
                        timeUpdate3b += System.currentTimeMillis()
                                - update3bStart;
                    } else {
                        timesUpdate3c++;
                        final long update3cStart = System.currentTimeMillis();
                        Evaluation evaToCompare = getMinEvaluation(
                                improvementPotential, optimalUpdate, vertex,
                                evaluatedSuccessors);
                        if (evaToCompare.compareTo(minIntermediateImprovement) < 0) {
                            minForCase4 = vertex;
                            minIntermediateImprovement = evaToCompare;
                        }
                        timeUpdate3c += System.currentTimeMillis()
                                - update3cStart;
                    }
                    timeUpdate3 += System.currentTimeMillis() - update3Start;
                } else {
                    // vertex.getPlayer() == Player.A
                    timesUpdate4++;
                    final long update4Start = System.currentTimeMillis();
                    if (optimalUpdate
                            .hasEvaluatedAllVertices(improvementPotential.row(
                                    vertex).keySet())) {
                        if (doUpdateCase3(improvementPotential, optimalUpdate,
                                vertex)) {
                            changed = true;
                            iterator.liftWasSuccessful(vertex);
                        }
                    }
                    timeUpdate4 += System.currentTimeMillis() - update4Start;
                }
            }
            timesUpdate5++;
            final long update5Start = System.currentTimeMillis();
            if (!changed) {
                forLoopCount++;
                if (minForCase4 == null
                        || (optimalUpdate.hasEvaluatedVertex(minForCase4) && optimalUpdate
                                .get(minForCase4).compareTo(
                                        minIntermediateImprovement) == 0)) {
                    System.out.println("Now we would have broken.");
                } else {
                    optimalUpdate.put(minForCase4, minIntermediateImprovement);
                    iterator.liftWasSuccessful(minForCase4);
                    for (final ParityVertex vertex : iterator) {
                        if (!optimalUpdate.hasEvaluatedVertex(vertex)) {
                            optimalUpdate.put(vertex,
                                    minIntermediateImprovement);
                            iterator.liftWasSuccessful(vertex);
                        }
                    }
                }
            }
            timeUpdate5 += System.currentTimeMillis() - update5Start;
        }
        final long updateEnd = System.currentTimeMillis();
        timeUpdate += updateEnd - updateStart;

        System.out.println(whileLoopCount + "\t" + forLoopCount);
    }

    private ModifyableEstimation getInitialUpdate() {
        final ModifyableEstimation optimalUpdate = new ModifyableEstimation();
        optimalUpdate.put(BOTTOM, zeroEvaluation);
        return optimalUpdate;
    }

    protected boolean doUpdateCase1(
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        final Set<? extends ParityVertex> successors = improvementPotential
                .row(vertex).keySet();
        final Evaluation minEvaluation = getMinEvaluation(improvementPotential,
                optimalUpdate, vertex, successors);
        if (optimalUpdate.hasEvaluatedVertex(vertex)
                && optimalUpdate.get(vertex).compareTo(minEvaluation) == 0) {
            return false;
        }
        optimalUpdate.put(vertex, minEvaluation);
        return true;
    }

    long minTime = 0;
    long minIterTime = 0;
    long minHasNextTime = 0;
    long minNextTime = 0;
    long minMapTime = 0;
    long minMinTime = 0;

    // TODO: duplicate code in getMinEvaluation and doUpdateCase1 and
    // doUpdateCase3
    protected Evaluation getMinEvaluation(
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final Estimation optimalUpdate, final ParityVertex vertex,
            Set<? extends ParityVertex> successors) {
        Evaluation minEvaluation = infinityEvaluation;
        final long minStartTime = System.currentTimeMillis();
        final long minIterStartTime = System.currentTimeMillis();
        Iterator<? extends ParityVertex> iterator = successors.iterator();
        minIterTime += System.currentTimeMillis() - minIterStartTime;
        long minHasNextStartTime = System.currentTimeMillis();
        boolean hasNext = iterator.hasNext();
        minHasNextTime += System.currentTimeMillis() - minHasNextStartTime;
        while (hasNext) {
            final long minNextStartTime = System.currentTimeMillis();
            final ParityVertex successor = iterator.next();
            minNextTime += System.currentTimeMillis() - minNextStartTime;
            final long minMapTimeStart = System.currentTimeMillis();
            Evaluation evaluation = optimalUpdate.get(successor).plus(
                    improvementPotential.get(vertex, successor));
            minMapTime += System.currentTimeMillis() - minMapTimeStart;
            final long minMinTimeStart = System.currentTimeMillis();
            minEvaluation = minEvaluation.compareTo(evaluation) > 0 ? evaluation
                    : minEvaluation;
            minMinTime += System.currentTimeMillis() - minMinTimeStart;
            minHasNextStartTime = System.currentTimeMillis();
            hasNext = iterator.hasNext();
            minHasNextTime += System.currentTimeMillis() - minHasNextStartTime;
        }
        minTime += System.currentTimeMillis() - minStartTime;
        return minEvaluation;
    }

    protected boolean doUpdateCase3(
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        Evaluation maxEvaluation = improvementPotential
                .row(vertex)
                .keySet()
                .stream()
                .map(successor -> optimalUpdate.get(successor).plus(
                        improvementPotential.get(vertex, successor)))
                .max((a, b) -> a.compareTo(b)).orElse(zeroEvaluation);
        if (optimalUpdate.hasEvaluatedVertex(vertex)
                && optimalUpdate.get(vertex).compareTo(maxEvaluation) == 0) {
            return false;
        }
        optimalUpdate.put(vertex, maxEvaluation);
        return true;
    }
}

package parisolve.backend.algorithms;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import parisolve.backend.Arena;
import parisolve.backend.LinkedArena.LinkedParityVertex;
import parisolve.backend.ParityVertex;
import parisolve.backend.Player;
import parisolve.backend.algorithms.helper.Liftable;
import parisolve.backend.algorithms.helper.LiftableFactory;

import com.google.common.base.Objects;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Sets;
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
        final Map<Integer, Integer> map;
        final int maxColour;

        Evaluation(final Map<Integer, Integer> map) {
            this.map = map;
            int maxColour = 0;
            for (int colour : map.keySet()) {
                if (colour > maxColour) {
                    maxColour = colour;
                }
            }
            this.maxColour = maxColour;
        }

        int get(final int colour) {
            if (map.containsKey(colour)) {
                return map.get(colour);
            }
            return 0;
        }

        static Evaluation plus(final Evaluation eva1, final Evaluation eva2) {
            if (eva1 == infinityEvaluation || eva2 == infinityEvaluation) {
                return infinityEvaluation;
            }
            final Map<Integer, Integer> sumMap = new ConcurrentHashMap<>();
            int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
            for (int colour = 0; colour <= maxColour; colour++) {
                sumMap.put(colour, eva1.get(colour) + eva2.get(colour));
            }
            return new Evaluation(sumMap);
        }

        Evaluation plus(final Evaluation summand) {
            return plus(this, summand);
        }

        static Evaluation minus(final Evaluation eva1, final Evaluation eva2) {
            final Map<Integer, Integer> subMap = new ConcurrentHashMap<>();
            int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
            for (int colour = 0; colour <= maxColour; colour++) {
                subMap.put(colour, eva1.get(colour) - eva2.get(colour));
            }
            return new Evaluation(subMap);
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
            final Map<Integer, Integer> nextMap = new ConcurrentHashMap<>();
            nextMap.putAll(eva.map);
            nextMap.put(colour, eva.get(colour) + 1);
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

        static int compare(final Evaluation eva1, final Evaluation eva2) {
            if (eva1 == eva2) {
                return 0;
            }
            if (eva1 == infinityEvaluation) {
                return 1;
            }
            if (eva2 == infinityEvaluation) {
                return -1;
            }
            int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
            for (int colour = maxColour; colour > 0; colour--) {
                // maybe this can be sped up by testing equality first
                if (eva1.get(colour) > eva2.get(colour)) {
                    if (colour % 2 == 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (eva1.get(colour) < eva2.get(colour)) {
                    if (colour % 2 == 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
            return 0;
        }

        int compareTo(final Evaluation eva) {
            return compare(this, eva);
        }

        static Evaluation getEvaluationForPath(
                final Iterable<? extends ParityVertex> path) {
            final Map<Integer, Integer> evaluationMap = new ConcurrentHashMap<>();
            boolean first = true;
            for (final ParityVertex vertex : path) {
                if (first) {
                    first = false;
                } else {
                    int priority = vertex.getPriority();
                    if (priority > 0) {
                        if (evaluationMap.containsKey(priority)) {
                            evaluationMap.put(priority,
                                    evaluationMap.get(priority) + 1);
                        } else {
                            evaluationMap.put(priority, 1);
                        }
                    }
                }
            }
            return new Evaluation(evaluationMap);
        }

        @Override
        public String toString() {
            if (maxColour <= 0) {
                return "0";
            }
            String returnString = "";
            for (int i = maxColour; i > 0; i--) {
                returnString += ", " + map.get(i);
            }
            return "(" + returnString.substring(2) + ")";
        }
    }

    final static Evaluation zeroEvaluation = new Evaluation(
            new ConcurrentHashMap<Integer, Integer>());

    final static Evaluation infinityEvaluation = new Evaluation(
            new ConcurrentHashMap<Integer, Integer>()) {
        int get(int colour) {
            return Integer.MAX_VALUE;
        };

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
            return new HashSet<>(estimation.keySet());
        }

        public static Estimation plus(final Estimation a, final Estimation b) {
            final Map<ParityVertex, Evaluation> sumEstimation = new ConcurrentHashMap<>(
                    a.estimation);
            for (final ParityVertex key : b.estimation.keySet()) {
                if (sumEstimation.containsKey(key)) {
                    sumEstimation.put(key,
                            sumEstimation.get(key).plus(b.estimation.get(key)));
                } else {
                    sumEstimation.put(key, b.estimation.get(key));
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
                    .map(entry -> entry.getKey()).collect(Collectors.toSet());
        }

        public Set<ParityVertex> getInfiniteVertices() {
            return estimation.entrySet().stream()
                    .filter(entry -> entry.getValue() == infinityEvaluation)
                    .map(entry -> entry.getKey()).collect(Collectors.toSet());
        }

        public Solution getSolution() {
            return new Solution(getInfiniteVertices(),
                    getNonInfiniteVertices(), Player.A,
                    new ConcurrentHashMap<>());
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
        final Set<ParityVertex> vertices = arena.getVertices();
        Estimation estimation = getDefaultEstimation(vertices);
        Table<ParityVertex, ParityVertex, Evaluation> improvementPotential = getImprovementPotential(
                vertices, estimation);

        // this cryptic condition ensures that there are non-zero
        // improvement-potentials
        while (containsNonNull(improvementPotential)) {
            final ModifyableEstimation optimalUpdate = getInitialUpdate();

            loopBasicUpdateStep(vertices, improvementPotential, optimalUpdate);

            estimation = estimation.plus(optimalUpdate);
            improvementPotential = getImprovementPotential(vertices, estimation);
        }
        return estimation.getSolution();
    }

    private boolean containsNonNull(
            Table<ParityVertex, ParityVertex, Evaluation> improvementPotential) {
        for (final Evaluation eva : improvementPotential.values()) {
            if (eva.compareTo(zeroEvaluation) != 0) {
                return true;
            }
        }
        return false;
    }

    private void loopBasicUpdateStep(
            final Set<? extends ParityVertex> vertices,
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final ModifyableEstimation optimalUpdate) {
        LiftableFactory liftableFactory = new LiftableFactory(vertices);
        while (!optimalUpdate.hasEvaluatedAllVertices(vertices)) {
            boolean changed = false;
            ParityVertex minForCase4 = null;
            Evaluation minIntermediateImprovement = null;
            Liftable iterator = liftableFactory.getLiftableInstance(
                    Sets.difference(vertices,
                            optimalUpdate.getEvaluatedVertices()), true);
            for (final ParityVertex vertex : iterator) {
                if (optimalUpdate.hasEvaluatedVertex(vertex)) {
                    continue;
                }
                if (vertex.getPlayer() == Player.B) {
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
                        for (final ParityVertex successor : vertex
                                .getSuccessors()) {
                            if (improvementPotential
                                    .contains(vertex, successor)
                                    && optimalUpdate
                                            .hasEvaluatedVertex(successor)
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
                    if (minForCase4 == null
                            && optimalUpdate.get(vertex) != infinityEvaluation) {
                        minForCase4 = vertex;
                        final Set<ParityVertex> evaluatedSuccessors = optimalUpdate
                                .getEvaluatedVertices();
                        evaluatedSuccessors.retainAll(vertex.getSuccessors());
                        minIntermediateImprovement = getMinEvaluation(
                                improvementPotential, optimalUpdate, vertex,
                                evaluatedSuccessors);
                    } else {
                        final Set<ParityVertex> evaluatedSuccessors = optimalUpdate
                                .getEvaluatedVertices();
                        evaluatedSuccessors.retainAll(vertex.getSuccessors());
                        Evaluation evaToCompare = getMinEvaluation(
                                improvementPotential, optimalUpdate, vertex,
                                evaluatedSuccessors);
                        if (evaToCompare.compareTo(minIntermediateImprovement) < 0) {
                            minForCase4 = vertex;
                            minIntermediateImprovement = evaToCompare;
                        }
                    }
                } else {
                    // vertex.getPlayer() == Player.A
                    if (optimalUpdate
                            .hasEvaluatedAllVertices(improvementPotential.row(
                                    vertex).keySet())) {
                        if (doUpdateCase3(improvementPotential, optimalUpdate,
                                vertex)) {
                            changed = true;
                            iterator.liftWasSuccessful(vertex);
                        }
                    }
                }
            }
            if (!changed) {
                if (minForCase4 == null
                        || (optimalUpdate.hasEvaluatedVertex(minForCase4) && optimalUpdate
                                .get(minForCase4).compareTo(
                                        minIntermediateImprovement) == 0)) {
                    System.out.println("Now we would have broken.");
                } else {
                    optimalUpdate.put(minForCase4, minIntermediateImprovement);
                }
            }
        }
    }

    private ModifyableEstimation getInitialUpdate() {
        final ModifyableEstimation optimalUpdate = new ModifyableEstimation();
        optimalUpdate.put(BOTTOM, zeroEvaluation);
        return optimalUpdate;
    }

    private Table<ParityVertex, ParityVertex, Evaluation> getImprovementPotential(
            final Set<? extends ParityVertex> vertices,
            final Estimation estimation) {
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

    protected boolean doUpdateCase1(
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final ModifyableEstimation optimalUpdate, final ParityVertex vertex) {
        final Set<? extends ParityVertex> successors = vertex.getSuccessors();
        final Evaluation minEvaluation = getMinEvaluation(improvementPotential,
                optimalUpdate, vertex, successors);
        if (optimalUpdate.hasEvaluatedVertex(vertex)
                && optimalUpdate.get(vertex).compareTo(minEvaluation) == 0) {
            return false;
        }
        optimalUpdate.put(vertex, minEvaluation);
        return true;
    }

    protected Evaluation getMinEvaluation(
            final Table<ParityVertex, ParityVertex, Evaluation> improvementPotential,
            final Estimation optimalUpdate, final ParityVertex vertex,
            Set<? extends ParityVertex> successors) {
        Evaluation minEvaluation = infinityEvaluation;
        for (final ParityVertex successor : successors) {
            final Evaluation evaToCompareWith = optimalUpdate.get(successor)
                    .plus(improvementPotential.get(vertex, successor));
            if (evaToCompareWith.compareTo(minEvaluation) < 0) {
                minEvaluation = evaToCompareWith;
            }
        }
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

package parisolve.backend.algorithms.helper;

import java.util.Arrays;
import java.util.function.BinaryOperator;

/**
 * class representing the type R = (C_0 → Z) ∪ ∞ which is defined in Schewe
 * (2008) p. 374 "Escape Games".
 * 
 * Corresponds to <code>MeasureValue</code>, so implementations might be merged.
 * 
 * @author Arne Schröder
 * 
 * @see MeasureValue
 */
public class Evaluation {
    final int[] map;
    final int maxColour;
    /**
     * corresponds to Top.
     */
    public final static Evaluation INFINITY_EVALUTION = new Evaluation(
            new int[0]) {
        public String toString() {
            return "∞";
        };
    };
    public final static Evaluation ZERO_EVALUATION = new Evaluation(new int[0]);

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
        int maxColour = Math.max(eva1.maxColour, eva2.maxColour);
        int[] combinedMap = new int[maxColour];
        for (int colour = 1; colour <= maxColour; colour++) {
            combinedMap[colour - 1] = combinator.apply(eva1.get(colour),
                    eva2.get(colour));
        }
        return new Evaluation(combinedMap);
    }

    public static long timePlus = 0;

    static Evaluation plus(final Evaluation eva1, final Evaluation eva2) {
        if (eva1 == Evaluation.INFINITY_EVALUTION
                || eva2 == Evaluation.INFINITY_EVALUTION) {
            return Evaluation.INFINITY_EVALUTION;
        }
        Evaluation sum = combine(eva1, eva2, (a, b) -> a + b);
        return sum;
    }

    public Evaluation plus(final Evaluation summand) {
        return plus(this, summand);
    }

    static Evaluation minus(final Evaluation eva1, final Evaluation eva2) {
        if (eva1 == Evaluation.INFINITY_EVALUTION) {
            return Evaluation.INFINITY_EVALUTION;
        }
        return combine(eva1, eva2, (a, b) -> a - b);
    }

    public Evaluation minus(final Evaluation subtrahent) {
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
        if (eva == Evaluation.INFINITY_EVALUTION || colour == 0) {
            return eva;
        }
        final int[] nextMap;
        if (colour <= eva.maxColour) {
            nextMap = Arrays.copyOf(eva.map, eva.maxColour);
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
    public Evaluation plus(final int colourToAdd) {
        return plus(this, colourToAdd);
    }

    public static long timeCompare = 0;

    static int compare(final Evaluation eva1, final Evaluation eva2) {
        if (eva1 == eva2) {
            return 0;
        }
        if (eva1 == Evaluation.INFINITY_EVALUTION) {
            return 1;
        }
        if (eva2 == Evaluation.INFINITY_EVALUTION) {
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

    public int compareTo(final Evaluation eva) {
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
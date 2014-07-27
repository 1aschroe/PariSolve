package parisolve.backend.algorithms.helper;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PrimeGenerator {
    private PrimeGenerator() {
        // this utility class is not meant for instantiation.
    }

    private static List<Integer> primes = new ArrayList<>();
    static {
        primes.add(2);
        primes.add(3);
    }

    /**
     * this gives to what number (exclusively) primes have been searched for.
     */
    private static int formerUpperBound = 4;

    /**
     * @return the ith prime
     */
    public static int getPrime(int i) {
        while (primes.size() <= i) {
            final int newUpperBound = 2 * formerUpperBound;
            final List<Boolean> isPrime = new ArrayList<>(newUpperBound - formerUpperBound);
            for (int m = formerUpperBound; m < newUpperBound; m++) {
                isPrime.add(true);
            }
            for (final int prime : primes) {
                // with (int)Math.ceil((double)formerUpperBound / prime) we
                // ensure that m >= formerUpperBound
                for (int m = prime
                        * (int) Math.ceil((double) formerUpperBound / prime); m < newUpperBound; m += prime) {
                    isPrime.set(m - formerUpperBound, false);
                }
            }
            for (int m = formerUpperBound; m < newUpperBound; m++) {
                if (isPrime.get(m - formerUpperBound)) {
                    primes.add(m);
                }
            }
            formerUpperBound = newUpperBound;
        }
        return primes.get(i);
    }

    public static void main(String[] args) {
        final int i = Integer.parseInt(args[0]);
        System.out.println(getPrime(i));
    }

    @Test
    public final void testNumbers() {
        Assert.assertEquals(getPrime(0), 2);
        Assert.assertEquals(getPrime(1), 3);
        Assert.assertEquals(getPrime(2), 5);
        Assert.assertEquals(getPrime(3), 7);
        Assert.assertEquals(getPrime(4), 11);
        Assert.assertEquals(getPrime(5), 13);
        Assert.assertEquals(getPrime(6), 17);
        Assert.assertEquals(getPrime(7), 19);
        Assert.assertEquals(getPrime(8), 23);
        Assert.assertEquals(getPrime(9), 29);
        Assert.assertEquals(getPrime(10), 31);
        Assert.assertEquals(getPrime(11), 37);
        Assert.assertEquals(getPrime(12), 41);
        Assert.assertEquals(getPrime(13), 43);
        Assert.assertEquals(getPrime(14), 47);
        Assert.assertEquals(getPrime(15), 53);
        Assert.assertEquals(getPrime(16), 59);
        Assert.assertEquals(getPrime(17), 61);
        Assert.assertEquals(getPrime(18), 67);
        Assert.assertEquals(getPrime(19), 71);
        Assert.assertEquals(getPrime(20), 73);
        Assert.assertEquals(getPrime(21), 79);
        Assert.assertEquals(getPrime(22), 83);
        Assert.assertEquals(getPrime(23), 89);
        Assert.assertEquals(getPrime(24), 97);
        Assert.assertEquals(getPrime(25), 101);
        Assert.assertEquals(getPrime(26), 103);
        Assert.assertEquals(getPrime(27), 107);
        Assert.assertEquals(getPrime(28), 109);
        Assert.assertEquals(getPrime(29), 113);
        Assert.assertEquals(getPrime(30), 127);
        Assert.assertEquals(getPrime(31), 131);
        Assert.assertEquals(getPrime(32), 137);
        Assert.assertEquals(getPrime(33), 139);
        Assert.assertEquals(getPrime(34), 149);
        Assert.assertEquals(getPrime(35), 151);
        Assert.assertEquals(getPrime(36), 157);
        Assert.assertEquals(getPrime(37), 163);
        Assert.assertEquals(getPrime(38), 167);
        Assert.assertEquals(getPrime(39), 173);
        Assert.assertEquals(getPrime(40), 179);
        Assert.assertEquals(getPrime(41), 181);
        Assert.assertEquals(getPrime(42), 191);
        Assert.assertEquals(getPrime(43), 193);
        Assert.assertEquals(getPrime(44), 197);
        Assert.assertEquals(getPrime(45), 199);
        Assert.assertEquals(getPrime(46), 211);
        Assert.assertEquals(getPrime(47), 223);
        Assert.assertEquals(getPrime(48), 227);
        Assert.assertEquals(getPrime(49), 229);
        Assert.assertEquals(getPrime(50), 233);
        Assert.assertEquals(getPrime(51), 239);
        Assert.assertEquals(getPrime(52), 241);
        Assert.assertEquals(getPrime(53), 251);
        Assert.assertEquals(getPrime(54), 257);
        Assert.assertEquals(getPrime(55), 263);
        Assert.assertEquals(getPrime(56), 269);
        Assert.assertEquals(getPrime(57), 271);
        Assert.assertEquals(getPrime(58), 277);
        Assert.assertEquals(getPrime(59), 281);
        Assert.assertEquals(getPrime(60), 283);
        Assert.assertEquals(getPrime(61), 293);
        Assert.assertEquals(getPrime(62), 307);
        Assert.assertEquals(getPrime(63), 311);
        Assert.assertEquals(getPrime(64), 313);
        Assert.assertEquals(getPrime(65), 317);
        Assert.assertEquals(getPrime(66), 331);
        Assert.assertEquals(getPrime(67), 337);
        Assert.assertEquals(getPrime(68), 347);
        Assert.assertEquals(getPrime(69), 349);
        Assert.assertEquals(getPrime(70), 353);
        Assert.assertEquals(getPrime(71), 359);
        Assert.assertEquals(getPrime(72), 367);
        Assert.assertEquals(getPrime(73), 373);
        Assert.assertEquals(getPrime(74), 379);
        Assert.assertEquals(getPrime(75), 383);
        Assert.assertEquals(getPrime(76), 389);
        Assert.assertEquals(getPrime(77), 397);
        Assert.assertEquals(getPrime(78), 401);
        Assert.assertEquals(getPrime(79), 409);
        Assert.assertEquals(getPrime(80), 419);
        Assert.assertEquals(getPrime(81), 421);
        Assert.assertEquals(getPrime(82), 431);
        Assert.assertEquals(getPrime(83), 433);
        Assert.assertEquals(getPrime(84), 439);
        Assert.assertEquals(getPrime(85), 443);
        Assert.assertEquals(getPrime(86), 449);
        Assert.assertEquals(getPrime(87), 457);
        Assert.assertEquals(getPrime(88), 461);
        Assert.assertEquals(getPrime(89), 463);
        Assert.assertEquals(getPrime(90), 467);
        Assert.assertEquals(getPrime(91), 479);
        Assert.assertEquals(getPrime(92), 487);
        Assert.assertEquals(getPrime(93), 491);
        Assert.assertEquals(getPrime(94), 499);
        Assert.assertEquals(getPrime(95), 503);
        Assert.assertEquals(getPrime(96), 509);
        Assert.assertEquals(getPrime(97), 521);
        Assert.assertEquals(getPrime(98), 523);
        Assert.assertEquals(getPrime(99), 541);
        Assert.assertEquals(getPrime(100), 547);
        Assert.assertEquals(getPrime(101), 557);
        Assert.assertEquals(getPrime(102), 563);
        Assert.assertEquals(getPrime(103), 569);
        Assert.assertEquals(getPrime(104), 571);
        Assert.assertEquals(getPrime(105), 577);
        Assert.assertEquals(getPrime(106), 587);
        Assert.assertEquals(getPrime(107), 593);
        Assert.assertEquals(getPrime(108), 599);
        Assert.assertEquals(getPrime(109), 601);
        Assert.assertEquals(getPrime(110), 607);
        Assert.assertEquals(getPrime(111), 613);
        Assert.assertEquals(getPrime(112), 617);
        Assert.assertEquals(getPrime(113), 619);
        Assert.assertEquals(getPrime(114), 631);
        Assert.assertEquals(getPrime(115), 641);
        Assert.assertEquals(getPrime(116), 643);
        Assert.assertEquals(getPrime(117), 647);
        Assert.assertEquals(getPrime(118), 653);
        Assert.assertEquals(getPrime(119), 659);
        Assert.assertEquals(getPrime(120), 661);
        Assert.assertEquals(getPrime(121), 673);
        Assert.assertEquals(getPrime(122), 677);
        Assert.assertEquals(getPrime(123), 683);
        Assert.assertEquals(getPrime(124), 691);
        Assert.assertEquals(getPrime(125), 701);
        Assert.assertEquals(getPrime(126), 709);
        Assert.assertEquals(getPrime(127), 719);
        Assert.assertEquals(getPrime(128), 727);
        Assert.assertEquals(getPrime(129), 733);
        Assert.assertEquals(getPrime(130), 739);
        Assert.assertEquals(getPrime(131), 743);
        Assert.assertEquals(getPrime(132), 751);
        Assert.assertEquals(getPrime(133), 757);
        Assert.assertEquals(getPrime(134), 761);
        Assert.assertEquals(getPrime(135), 769);
        Assert.assertEquals(getPrime(136), 773);
        Assert.assertEquals(getPrime(137), 787);
        Assert.assertEquals(getPrime(138), 797);
        Assert.assertEquals(getPrime(139), 809);
        Assert.assertEquals(getPrime(140), 811);
        Assert.assertEquals(getPrime(141), 821);
        Assert.assertEquals(getPrime(142), 823);
        Assert.assertEquals(getPrime(143), 827);
        Assert.assertEquals(getPrime(144), 829);
        Assert.assertEquals(getPrime(145), 839);
        Assert.assertEquals(getPrime(146), 853);
        Assert.assertEquals(getPrime(147), 857);
        Assert.assertEquals(getPrime(148), 859);
        Assert.assertEquals(getPrime(149), 863);
        Assert.assertEquals(getPrime(150), 877);
        Assert.assertEquals(getPrime(151), 881);
        Assert.assertEquals(getPrime(152), 883);
        Assert.assertEquals(getPrime(153), 887);
        Assert.assertEquals(getPrime(154), 907);
        Assert.assertEquals(getPrime(155), 911);
        Assert.assertEquals(getPrime(156), 919);
        Assert.assertEquals(getPrime(157), 929);
        Assert.assertEquals(getPrime(158), 937);
        Assert.assertEquals(getPrime(159), 941);
        Assert.assertEquals(getPrime(160), 947);
        Assert.assertEquals(getPrime(161), 953);
        Assert.assertEquals(getPrime(162), 967);
        Assert.assertEquals(getPrime(163), 971);
        Assert.assertEquals(getPrime(164), 977);
        Assert.assertEquals(getPrime(165), 983);
        Assert.assertEquals(getPrime(166), 991);
        Assert.assertEquals(getPrime(167), 997);

    }
}

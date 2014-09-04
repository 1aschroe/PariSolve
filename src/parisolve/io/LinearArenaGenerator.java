package parisolve.io;

import parisolve.backend.Arena;

public interface LinearArenaGenerator {
    public enum GeneratorType {
        WEAK, SOLITAIRE, RESILIENT, HARD, TWO_RING, CHAIN, HLB;
    }

    public Arena generateArena(final int n);

    static public Arena generateArena(final GeneratorType type, final int n) {
        LinearArenaGenerator generator;
        switch (type) {
        case WEAK:
            generator = new WeakArenaGenerator();
            break;
        case SOLITAIRE:
            generator = new SolitaireArenaGenerator();
            break;
        case RESILIENT:
            generator = new ResilientArenaGenerator();
            break;
        case HARD:
            generator = new HardArenaGenerator();
            break;
        case TWO_RING:
            generator = new TwoRingGenerator();
            break;
        case CHAIN:
            generator = new ChainArenaGenerator();
            break;
        case HLB:
            generator = new HlbArenaGenerator();
            break;
        default:
            throw new IllegalArgumentException(type
                    + " is not yet implemented.");
        }
        return generator.generateArena(n);
    }
}

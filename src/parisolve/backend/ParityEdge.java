package parisolve.backend;

import java.util.Objects;

public class ParityEdge {
    private final ParityVertex from;
    private final ParityVertex to;

    public ParityEdge(final ParityVertex from, final ParityVertex to) {
        this.from = from;
        this.to = to;
    }

    public ParityVertex getFrom() {
        return from;
    }

    public ParityVertex getTo() {
        return to;
    }

    @Override
    public boolean equals(Object obj) {
        return Objects.deepEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}

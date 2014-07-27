package parisolve.backend.algorithms.helper;

import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;

public class ModifyableEstimation extends Estimation {
    public ModifyableEstimation() {
        super(new ConcurrentHashMap<ParityVertex, Evaluation>());
    }

    public void put(final ParityVertex vertex, final Evaluation eva) {
        estimation.put(vertex, eva);
    }
}
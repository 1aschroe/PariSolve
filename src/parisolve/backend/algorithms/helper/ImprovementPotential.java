package parisolve.backend.algorithms.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.ParityVertex;

public class ImprovementPotential {
    private final Map<ParityVertex, Map<ParityVertex, Evaluation>> potentialMap = new ConcurrentHashMap<>();
    private final Map<ParityVertex, List<ParityVertex>> successorMap = new ConcurrentHashMap<>();

    public void put(final ParityVertex vertex,
            final ParityVertex successor, final Evaluation potential) {
        row(vertex).put(successor, potential);
        List<ParityVertex> successorList = getSuccessorsOf(vertex);
        if (!successorList.contains(successor)) {
            successorList.add(successor);
        }
    }

    private Map<ParityVertex, Evaluation> row(final ParityVertex vertex) {
        if (!potentialMap.containsKey(vertex)) {
            potentialMap.put(vertex, new ConcurrentHashMap<>());
        }
        return potentialMap.get(vertex);
    }

    public Evaluation get(final ParityVertex vertex,
            final ParityVertex successor) {
        return row(vertex).get(successor);
    }

    public List<ParityVertex> getSuccessorsOf(final ParityVertex vertex) {
        if (!successorMap.containsKey(vertex)) {
            successorMap.put(vertex, new ArrayList<>());
        }
        return successorMap.get(vertex);
    }

    @Override
    public String toString() {
        String returnString = "";
        for (final ParityVertex vertex : successorMap.keySet()) {
            for (final ParityVertex successor : getSuccessorsOf(vertex)) {
                returnString += ",\n(" + vertex.getName() + "->"
                        + successor.getName() + ") => "
                        + get(vertex, successor).toString();
            }
        }
        return returnString.substring(2);
    }
}
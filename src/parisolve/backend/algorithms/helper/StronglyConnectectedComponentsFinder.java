package parisolve.backend.algorithms.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import parisolve.backend.Arena;
import parisolve.backend.ParityVertex;
import parisolve.io.ArenaManager;

/**
 * implements Tarjan's algorithm for finding strongly connected components.
 * 
 * @author Arne Schröder
 */
public class StronglyConnectectedComponentsFinder {
    private StronglyConnectectedComponentsFinder() {
        // disable
    }

    private static int strongConnect(final ParityVertex vertex,
            int currentIndex, final Map<ParityVertex, Integer> index,
            final Map<ParityVertex, Integer> lowlink,
            final Stack<ParityVertex> stack, final Set<ParityVertex> inStack,
            final List<Set<ParityVertex>> componentList) {

        index.put(vertex, currentIndex);
        lowlink.put(vertex, currentIndex);
        currentIndex++;
        stack.push(vertex);
        inStack.add(vertex);
        int currentIndex1 = currentIndex;
        
        // consider successors
        for (final ParityVertex successor : vertex.getSuccessors()) {
            if (!index.containsKey(successor)) {
                currentIndex1 = strongConnect(successor, currentIndex1, index,
                        lowlink, stack, inStack, componentList);
                if (lowlink.get(successor) < lowlink.get(vertex)) {
                    lowlink.put(vertex, lowlink.get(successor));
                }
            } else if (inStack.contains(successor)) {
                if (index.get(successor) < lowlink.get(vertex)) {
                    lowlink.put(vertex, index.get(successor));
                }
            }
        }

        currentIndex = currentIndex1;

        if (index.get(vertex).equals(lowlink.get(vertex))) {
            final Set<ParityVertex> connectedComponent = new HashSet<ParityVertex>();
            ParityVertex vertexVisited;
            do {
                vertexVisited = stack.pop();
                inStack.remove(vertexVisited);
                connectedComponent.add(vertexVisited);
            } while (!vertexVisited.equals(vertex));

            componentList.add(connectedComponent);
        }
        return currentIndex;
    }

    /**
     * @return List of strongly connected components found in <code>arena</code>
     */
    public static List<Set<ParityVertex>> getStronglyConnectedComponents(
            final Arena arena) {
        final Map<ParityVertex, Integer> index = new ConcurrentHashMap<ParityVertex, Integer>();
        final Map<ParityVertex, Integer> lowlink = new ConcurrentHashMap<ParityVertex, Integer>();
        final Stack<ParityVertex> stack = new Stack<ParityVertex>();
        // used for faster querying stack.contains() similar to SetStackLiftable
        final Set<ParityVertex> inStack = new HashSet<ParityVertex>();
        final List<Set<ParityVertex>> components = new ArrayList<Set<ParityVertex>>();

        int currentIndex = 0;
        for (final ParityVertex v : arena) {
            if (!index.containsKey(v)) {
                currentIndex = strongConnect(v, currentIndex, index, lowlink,
                        stack, inStack, components);
            }
        }

        return components;
    }

    public static void main(String[] args) throws IOException {
        Arena arena = ArenaManager.generateRandomArena(100000, 1, 7);
        System.out.println(getStronglyConnectedComponents(arena).size());
    }
}
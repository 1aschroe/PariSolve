package parisolve.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkedArena implements Arena {
	class LinkedParityVertex implements ParityVertex {
		private int priority;
		private int player;
		private Set<LinkedParityVertex> successors = new HashSet<>();
		
		public LinkedParityVertex(int priority, int player) {
			this.priority = priority;
			this.player = player;
		}

		@Override
		public int getPriority() {
			return priority;
		}

		@Override
		public int getPlayer() {
			return player;
		}
		
		public Collection<? extends ParityVertex> getSuccessors() {
			return successors;
		}
		
		public void addSuccessor(LinkedParityVertex v) {
			successors.add(v);
		}
		
		public void removeSuccessor(LinkedParityVertex v) {
			successors.remove(v);
		}
		
		@Override
		public String toString() {
			return "[player=" + getPlayer() + ",label=\"" + getPriority() + "\"]";
		}
	}
	
	Map<String, LinkedParityVertex> vertices = new HashMap<>();
	
	public void addVertex(String name, int priority, int player) {
		vertices.put(name, new LinkedParityVertex(priority, player));
	}
	
	public void addEdge(String from, String to) {
		vertices.get(from).addSuccessor(vertices.get(to));
	}
	
	@Override
	public Collection<? extends ParityVertex> getVertices() {
		return new HashSet<>(vertices.values());
	}

	@Override
	public Collection<? extends ParityVertex> getSuccessors(ParityVertex vertex) {
		return ((LinkedParityVertex) vertex).getSuccessors();
	}

}

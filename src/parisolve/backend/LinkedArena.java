package parisolve.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LinkedArena implements Arena {
	class LinkedParityVertex implements ParityVertex {
		private int parity;
		private int player;
		private Set<LinkedParityVertex> successors = new HashSet<>();
		
		public LinkedParityVertex(int parity, int player) {
			this.parity = parity;
			this.player = player;
		}

		@Override
		public int getParity() {
			return parity;
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
	}
	
	Map<String, LinkedParityVertex> vertices = new HashMap<>();
	
	public void addVertex(String name, int parity, int player) {
		vertices.put(name, new LinkedParityVertex(parity, player));
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

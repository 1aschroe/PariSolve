package parisolve.backend;

import java.util.Collection;

public interface Arena {
	Collection<? extends ParityVertex> getVertices();
	
	Collection<? extends ParityVertex> getSuccessors(ParityVertex vertex);
}

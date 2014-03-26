package parisolve.backend;

import java.util.Collection;

public interface ParityVertex {
	int getPriority();
	
	int getPlayer();

    Collection<? extends ParityVertex> getSuccessors();
}

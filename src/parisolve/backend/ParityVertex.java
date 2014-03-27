package parisolve.backend;

import java.util.Collection;

public interface ParityVertex {
	int getPriority();
	
	Player getPlayer();

    Collection<? extends ParityVertex> getSuccessors();
}

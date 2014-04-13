package parisolve.backend;

import java.util.Collection;

public interface ParityVertex {
    String getName();
    
	int getPriority();
	
	Player getPlayer();

    Collection<? extends ParityVertex> getSuccessors();
}

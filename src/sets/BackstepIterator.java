package sets;

import java.util.Iterator;

public interface BackstepIterator<E> extends Iterator<E> {
	
	/** Makes this iterator return to the index position previous to the 
	 * last call to "next()". The function returns false if there is no 
	 * previous position available or there was not "next()" issued since
	 * the last call to backstep. 
	 *  
	 * @return boolean true = stepped back, false = remained unchanged
	 */
	boolean backstep ();
}

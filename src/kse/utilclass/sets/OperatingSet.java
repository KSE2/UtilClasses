package kse.utilclass.sets;

import java.util.Set;

public interface OperatingSet<E> extends Set<E> {

	/** Returns the reference to a new set which forms the intersection of
	 * this instance set with the parameter set. The operand sets are not
	 * modified.
	 * 
	 * @param a {@code Set<E>} argument set
	 * @return {@code Set<E>} resulting set
	 */
	OperatingSet<E> intersected (Set<E> a);
	
	/** Returns the reference to a new set which forms the unification of
	 * this instance set with the parameter set. The operand sets are not
	 * modified.
	 * 
	 * @param a {@code Set<E>} argument set
	 * @return {@code Set<E>} resulting set
	 */
	OperatingSet<E> united (Set<E> a);
	
	/** Returns the reference to a new set which forms the exclusion of
	 * the parameter set from this instance set. The operand sets are not
	 * modified.
	 * 
	 * @param a {@code Set<E>} argument set
	 * @return {@code Set<E>} resulting set
	 */
	OperatingSet<E> without (Set<E> a);
	
	/** Returns the reference to a new set which forms the XOR operation of
	 * this instance set with the parameter set. The operand sets are not
	 * modified.
	 * 
	 * @param a {@code Set<E>} argument set
	 * @return {@code Set<E>} resulting set
	 */
	OperatingSet<E> xored (Set<E> a);
	
	/** Retains only elements within this set which are contained in the
	 * parameter set. The method "equals()" is used to detect identity.
	 * This is a destructive method.
	 * 
	 * @param a {@code Set<E>} argument set
	 */
	void intersectWith (Set<E> a);
	
	/** Adds all elements from the parameter set which are not already contained
	 * in this set into this set. The method "equals()" is used to detect 
	 * identity. This is a creative method.
	 * 
	 * @param a {@code Set<E>} argument set
	 */
	void uniteWith (Set<E> a);
	
	/** Discards all elements from this set which are contained in the
	 * parameter set. The method "equals()" is used to detect identity.
	 * This is a destructive method.
	 * 
	 * @param a {@code Set<E>} argument set
	 */
	void exclude (Set<E> a);
	
	/** Retains elements of this set which are not contained in the
	 * parameter set, while it unites with all elements from the parameter set
	 * which are not contained in this set. The method "equals()" is used to 
	 * detect identity. This is both creative and destructive.
	 * 
	 * @param a {@code Set<E>} argument set
	 */
	void xorWith (Set<E> a);
	
	
}

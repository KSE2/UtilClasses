package kse.utilclass.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class SortedArrayList<E extends Comparable<E>> extends ArrayList<E> {

	private Comparator<E> comparator;
    
	/** New {@code SortedArrayList} with natural ordering of its elements.
	 */
    public SortedArrayList () {
    	resort();
    }
    
	/** New {@code SortedArrayList} with natural ordering of its elements, 
	 * comprising the given collection of initial elements.  
	 * 
	 * @param c {@code Collection}; may be null
	 */
    public SortedArrayList (Collection<? extends E> c) {
    	if (c != null) {
    		for (E e : c) {
    			add(e);
    		}
    	}
    }

	/** New {@code SortedArrayList} with natural ordering of its elements
	 * and the given initial capacity.
	 * 
	 * @param initialCapacity int
	 */
	public SortedArrayList (int initialCapacity) {
		super(initialCapacity);
	}

	/** New {@code SortedArrayList} with element ordering after the given
	 * {@code Comparator}. With parameter null natural ordering is in place.
	 * 
	 * @param comparator {@code Comparator<E>}; may be null
	 */
    public SortedArrayList (Comparator<E> comparator) {
    	this.comparator = comparator;
    }
    
	@Override
	public boolean add (E e) {
		int i = binarySearch(0, size(), e);
		if (i < 0) i = -(i+1);
		super.add(i, e);
		return true;
	}
    
    private int binarySearch (int fromIndex, int toIndex, E key) {
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            E midVal = get(mid);
            int cmp = comparator == null ? midVal.compareTo(key) 
            		  : comparator.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
    
    public void setComparator (Comparator<E> c) {
    	comparator = c;
    }
	
    /** Resorts the content of this list according to the active comparator.
     */
	@SuppressWarnings("unchecked")
	public void resort () {
		Object arr[] = toArray();
		clear();
		for (Object e : arr) {
			add((E)e);
		}
	}
}

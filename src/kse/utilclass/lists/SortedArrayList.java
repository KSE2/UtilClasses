package kse.utilclass.lists;

/*
*  File: SortedArrayList.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2022 by Wolfgang Keller, Munich, Germany
* 
This program is not public domain software but copyright protected to the 
author(s) stated above. However, you can use, redistribute and/or modify it 
under the terms of the GNU Library or Lesser General Public License as 
published by the Free Software Foundation, version 3.0 of the License.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the License along with this program; if not,
write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, 
Boston, MA 02111-1307, USA, or go to http://www.gnu.org/copyleft/gpl.html.
*/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.function.UnaryOperator;

/** A list structure derived from {@code ArrayList<E>} which orders its 
 * elements in a given or natural sorting. The natural sorting is in place if
 * no comparator is set by the user. 
 * <p>The list allows for <b>null</b> 
 * elements  if and only if a comparator is explicitly given and this 
 * comparator allows for null arguments to be compared. If this condition is
 * not met, a NullPointerException is thrown by any attempt to add the null
 * element.
 * <p>If instances have to be serialised, any comparator supplied needs to
 * implement the {@code java.io.Serializable} interface.
 * 
 * 
 * @param <E> elements must be comparable either by their nature (implementing
 * the {@code Comparable} interface) or through a user-supplied 
 * {@code Comparator<? super E>}.
 */
public class SortedArrayList<E> extends ArrayList<E> {
	
    private static final long serialVersionUID = -31342098202091228L;

	private Comparator<? super E> comparator;
    
	/** New {@code SortedArrayList} with natural ordering of its elements.
	 */
    public SortedArrayList () {
    }
    
	/** New {@code SortedArrayList} with natural ordering of its elements, 
	 * comprising the given collection as initial elements.  
	 * 
	 * @param c {@code Collection}; may be null
	 */
    public SortedArrayList (Collection<? extends E> c) {
    	addAll(c);
    }

	/** New {@code SortedArrayList} with natural ordering of its elements, 
	 * comprising the given array of initial elements.  
	 * 
	 * @param c {@code E[]}, may be null
	 */
    public SortedArrayList (E[] c) {
    	if (c != null)
    	for (E e : c) {
    		add(e);
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

	/** Creates a new sorted-list with the given element order (comparator). 
	 * With <b>null</b> natural ordering is in place.
	 * 
	 * @param comparator {@code Comparator<? super E>}; may be null
	 */
    public SortedArrayList (Comparator<? super E> comparator) {
    	this.comparator = comparator;
    }
    
	/** Creates an empty sorted-list with the given element comparator and
	 * initial capacity.
	 * 
	 * @param comparator {@code Comparator<? super E>}, null for natural
	 * @param initialCapacity int
	 */
	public SortedArrayList (Comparator<? super E> comparator, int initialCapacity) {
		super(initialCapacity);
		setComparator(comparator);
	}

	/** Creates a new sorted-list with the given {@code SortedSet} as initial
	 * content and the argument's comparator for sorting. With <b>null</b>
	 * this is equivalent to the empty constructor.
	 * 
	 * @param c {@code SortedSet<? extends E>}, may be null
	 */
	@SuppressWarnings("unchecked")
	public SortedArrayList (SortedSet<? extends E> c) {
		if (c == null) return;
		
		// transfer each single element (different set structure)
		ensureCapacity(c.size());
		for (E e : c) {
			add(e);
		}
		
		setComparator( (Comparator<? super E>) c.comparator() );
	}

	/** Adds the specified element to this list. If the element is not 
	 * comparable with the other elements of this list, an exception is thrown.
	 * <p>NOTE: <b>null</b> is a legal value if and only if a comparator has 
	 * been set up allowing for such comparison. 
	 * 
	 * @param e E element to add
	 * @return boolean true
	 * @throws ClassCastException if the specified object cannot be compared
	 * @throws NullPointerException if the specified element is null and this
	 *         list uses natural ordering, or its comparator does not permit
	 *         null elements 
	 */
	@Override
	public boolean add (E e) {
		if (e == null && comparator == null)
			throw new NullPointerException();
		
		int i = binarySearch(0, size(), e);
		if (i < 0) i = -(i+1);
		super.add(i, e);
		return true;
	}
    
	/** Returns either the object (if found) or the insert position of 'key'.
	 * The insert position x is coded as '-(x + 1)'.
	 *  
	 * @param fromIndex int start index (inclusive)
	 * @param toIndex int end index (exclusive)
	 * @param key E candidate
	 * @return int index position for key, may be negative for insert position
	 * @throws ClassCastException
	 * @throws NullPointerException
	 */
    @SuppressWarnings("unchecked")
	private int binarySearch (int fromIndex, int toIndex, E key) {
        int low = fromIndex;
        int high = toIndex - 1;
        int size = size();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            E midVal = get(mid);
            int cmp = comparator == null ? ((Comparable<E>)midVal).compareTo(key) 
            		  : comparator.compare(midVal, key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
            	// search for last element in a sequence of identical entries
            	midVal = null;
            	while (mid < size-1 && cmp == 0) {
                    midVal = get(++mid);
            		cmp = comparator == null ? ((Comparable<E>)midVal).compareTo(key) 
                  		  : comparator.compare(midVal, key);
            	}
    			if (cmp != 0 && midVal != null) mid--;
            	
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }
    
    /** This method has been overridden to be equivalent with 'add(e)', i.e.
     * the index parameter is ignored as pointed insertion is not allowed in a
     * sorted list.
     * 
	 * @param index int - no operation -
	 * @param e E element to add
	 * @throws ClassCastException if the specified object cannot be compared
	 * @throws NullPointerException if the specified element is null and this
	 *         list uses natural ordering, or its comparator does not permit
	 *         null elements 
     */
    @Override
	public void add(int index, E e) {
    	add(e);
    }

    /** Adds all elements of the given collection to this list. 
     * <p>This method has been overridden to be equivalent with 'addAll(c)', 
     * i.e. the index parameter is ignored as pointed insertion is not allowed
     * in a sorted list.
     * 
	 * @param index int - no operation -
	 * @param c {@code Collection<? extends E>}, may be null
	 * @throws ClassCastException if some object in the collection cannot be 
	 *  	   compared
	 * @throws NullPointerException if some object in the collection is null 
	 * 	       and this list uses natural ordering, or its comparator does not
	 *         permit null elements 
     */
    @Override
	public boolean addAll (int index, Collection<? extends E> c) {
    	return addAll(c);
    }
    
    /** Adds all elements of the given collection to this list.
     * 
	 * @param c {@code Collection<? extends E>}, may be null
	 * @throws ClassCastException if some object in the collection cannot be 
	 *  	   compared
	 * @throws NullPointerException if some object in the collection is null 
	 * 	       and this list uses natural ordering, or its comparator does not
	 *         permit null elements 
     */
    @Override
	public boolean addAll (Collection<? extends E> c) {
    	if (c == null) return false;
    	int sz = size();
    	for (Iterator<? extends E> it = c.iterator(); it.hasNext();) {
    		add(it.next());
    	}
    	return size() > sz;
    }
    
    /** Adds all elements of the given value array to this list.
     * 
	 * @param arr E[] array of entries to add, may be null
     * @return boolean true iff this list changed through this method
	 * @throws ClassCastException if some object in the collection cannot be 
	 *  	   compared
	 * @throws NullPointerException if some object in the collection is null 
	 * 	       and this list uses natural ordering, or its comparator does not
	 *         permit null elements 
     */
	public boolean addAll (E[] arr) {
    	if (arr == null) return false;
    	int sz = size();
    	for (E e : arr) {
    		add(e);
    	}
    	return size() > sz;
    }

	
    /** Adds all elements of the given value array from this list.
     * 
	 * @param c E[] array of entries to remove, may be null
     * @return boolean true iff this list changed through this method
     */
	public boolean removeAll (E[] c) {
		if (c == null) return false;
		boolean ch = false;
		for (E e : c) {
			ch |= remove(e);
		}
		return ch;
	}

	/** Sets the {@code Comparator} defining the order on the elements
	 * of this set. A value of <b>null</b> sets usage of the natural sorting 
	 * of the elements. If the set contains elements, it is implicitly resorted 
	 * by a call of this method.
	 * <p>NOTE: Natural sorting excludes the use of <b>null</b> elements.
	 * <p>NOTE: If this instance of {@code SortedArrayList} shall be serialised,
	 * the given comparator has to implement the {@code java.io.Serializable}
	 * interface.
	 *  
	 * @param c {@code Comparator<? super E>}, null for "natural ordering"
	 */
    public void setComparator (Comparator<? super E> c) {
    	if (c != comparator) {
    		comparator = c;
    		resort();
    	}
    }
	
    /** Returns the comparator used to order the elements in this list, or null
     *  if this list uses the natural ordering of its elements.
     *  
     *  @return {@code Comparator<? super E>} or null
     */
    public Comparator<? super E> comparator() {return comparator;}
	
    /** Resorts the content of this list according to the active sorting.
     */
	@SuppressWarnings("unchecked")
	protected void resort () {
		Object arr[] = toArray();
		clear();
		for (Object e : arr) {
			add((E)e);
		}
	}

	/** Replaces the element at the specified position in this list with the
	 * specified element. This operation only works for arguments which
	 * designate the same sorting-value as of the existing element at the
	 * given position.
	 * 
	 * @throws IllegalArgumentException if element has a different identity
	 *         than the list entry at index 
	 * @throws IndexOutOfBoundsException if the index is out of range 
	 *         {@code (index < 0 | index >= size())}
	 * @throws NullPointerException if the specified element is null and this
	 *         list uses natural ordering, or its comparator does not permit
	 *         null elements 
	 */
	@Override
	public E set (int index, E element) {
		if (element == null && comparator == null)
			throw new NullPointerException();
		
		E value = get(index);
        @SuppressWarnings("unchecked")
		int cmp = comparator == null ? ((Comparable<E>)value).compareTo(element) 
      		  : comparator.compare(value, element);
        if (cmp != 0) {
        	throw new IllegalArgumentException("unmatching sorting for argument");
        }
		return super.set(index, element);
	}

	/** The sort method from interface {@code List} sets the given comparator
	 * as ruling in this sorted-list and resorts the content. This is 
	 * equivalent with {@code setComparator(c)} in this implementation.
	 */
	@Override
	public void sort (Comparator<? super E> c) {
		setComparator(c);
	}
	
	/** This method is undefined in this class.
	 */
	@Override
	public void replaceAll (UnaryOperator<E> operator) {
		throw new UnsupportedOperationException();
	}

	
}

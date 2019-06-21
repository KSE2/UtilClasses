package sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * SortedArraySet is an extension of {@code ArraySet} implementing the
 * {@code SortedSet} interface.
 * All elements inserted into this set must implement the {@code Comparable} 
 * interface or be accepted by the specified comparator.
 * A sorted set has advantage when a single set is used iteratively with
 * dominating membership requests or set operations ({@code OperatingSet}), for
 * their execution time.
 * 
 * <p>Methods of the {@code SortedSet} interface which render views of a set as
 * subsets are currently not supported.
 * 
 * <p><b>Complexities (of the implementation of this class):</b>
 * <br>Single item: insertion, removal and iteration cost O(n), membership 
 * O(log n) execution time, n = size(). Set operations (s1, s2)
 * ({@code OperatingSet}) cost O(n+m) with n=|s1|, m=|s2|.
 *   
 * @param <E>
 */

public class SortedArraySet<E> extends ArraySet<E>  implements SortedSet<E> {
	
	private Comparator<? super E> comparator;

	public SortedArraySet () {
	}

	public SortedArraySet (int initialCapacity) {
		super(initialCapacity);
	}

	public SortedArraySet (Collection<? extends E> c) {
		super(c);
	}

	public SortedArraySet (SortedSet<E> c) {
		// transfer data
		if (c instanceof SortedArraySet) {
			// make a copy of the argument element array (same class)
			SortedArraySet<E> sa = (SortedArraySet<E>) c;
			this.elementData = Arrays.copyOf(sa.elementData, sa.elementData.length);

		} else {
			// transfer each single element (different set structure)
			if (c.comparator() != null) 
				throw new IllegalArgumentException("cannot comply with argument comparator");
			
			ensureCapacity(c.size());
			int i = 0;
			for (E e : c) {
				elementData[i++] = e;
			}
		}
		
		size = c.size();
		comparator = c.comparator();
	}

	public SortedArraySet (Comparator<? super E> comparator) {
		setComparator(comparator);
	}

	public SortedArraySet (Comparator<? super E> comparator, int initialCapacity) {
		super(initialCapacity);
		setComparator(comparator);
	}

	/** Sets the {@code Comparator} defining the total order on the elements
	 * of this set. A value of null sets usage of the natural ordering of
	 * the elements. If the set contains elements, it is resorted by this 
	 * method.
	 *  
	 * @param comparator {@code Comparator}, null for "natural ordering"
	 */
	@SuppressWarnings("unchecked")
	public void setComparator (Comparator<? super E> comparator) {
		this.comparator = comparator;
		
		// resort to new comparator
		if (comparator != null && size > 0) {
			Arrays.sort((E[])elementData, 0, size, comparator);
		}
		
//		E ob1 = null , ob2 = null;
//		comparator.compare(ob1, ob2);
	}

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}
	
	/** Returns the index position (>= 0) of the given object in the element 
	 * array. Returns a negative value (-insertPosition -1) if the object is not
	 * present in the array. This indicates the insert position.
	 *    
	 * @param e E search object 
	 * @return int index or insert position
	 * @throws IllegalArgumentException if e is not Comparable and comparator is
	 *         null
	 * @throws NullPointerException if argument is null
	 */
	@SuppressWarnings("unchecked")
	private int objectPosition (E e) {
		Objects.requireNonNull(e);
		int pos;
		
		// binary search for insert position of e
		if (comparator != null) {
			pos = Arrays.binarySearch((E[]) elementData, 0, size, e, comparator);
		} else {
			if (!(e instanceof Comparable)) {
				throw new IllegalArgumentException("argument is not comparable");
			}
			pos = Arrays.binarySearch(elementData, 0, size, e);
		}
		return pos;
	}

	
	@Override
	public boolean contains (Object o) {
		@SuppressWarnings("unchecked")
		E e = (E) o;
		return objectPosition(e) > -1;  
	}

	@Override
	public boolean add (E e) {
		int ipos = objectPosition(e);

		// ignore duplicates
		if (ipos > -1) return false;
		ipos = Math.abs(ipos + 1);
		
		// insert into array
	    ensureCapacity(size + 1);
	    System.arraycopy(elementData, ipos, elementData, ipos+1, size-ipos);
	    elementData[ipos] = e;
	    size++;
	    modCount++;
	    return true;
	}

	@Override
	public boolean remove (Object o) {
		@SuppressWarnings("unchecked")
		int pos = objectPosition((E) o);
		if (pos < 0) return false;

		System.arraycopy(elementData, pos+1, elementData, pos, --size-pos);
	    elementData[size] = null;
	    return true;
	}

//	/** Returns the index value of the given element object in this set's
//	 * data array, or -1 of unavailable.
//	 * 
//	 * @param e E
//	 * @return index >= 0 or -1 for not found 
//	 */
//	private int indexOf (E e) {
//		int pos = objectPosition(e); 
//		return pos < 0 ? -1 : pos;
//	}
//	
//	private SortedSet<E> subset (int fromIndex, int toIndex) {
//		return null;
//	}
	
	@Override
	public SortedSet<E> subSet (E fromElement, E toElement) {
		// TODO Auto-generated method stub
		if (toElement == null )
			throw new NullPointerException("toElement is null");
		if (fromElement == null )
			throw new NullPointerException("fromElement is null");

		return new SubSortedArraySet<>(this, fromElement, toElement);
//		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<E> headSet (E toElement) {
		// TODO Auto-generated method stub
		if (toElement == null )
			throw new NullPointerException();

		return new SubSortedArraySet<>(this, null, toElement);
//		throw new UnsupportedOperationException();
	}

	@Override
	public SortedSet<E> tailSet (E fromElement) {
		// TODO Auto-generated method stub
		// control parameter base values
		if (fromElement == null )
			throw new NullPointerException();

		return new SubSortedArraySet<>(this, fromElement, null);
//		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E first() {
		return size == 0 ? null : (E) elementData[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E last() {
		return size == 0 ? null : (E) elementData[size-1];
	}
	
// ----------------------------------------------------------
	
	private static class SubSortedArraySet<E> extends SortedArraySet<E> {
		private ArraySet<E> parentSet;
		private E lowBound, highBound;

		public SubSortedArraySet (SortedArraySet<E> set, E lowBound, E highBound) {
			super();
			
			// take over from set (i.e.redirect element data)
			setComparator(set.comparator());
			elementData = set.elementData;
			parentSet = set;
			this.lowBound = lowBound;
			this.highBound = highBound;

			// if both bound values are defined
			if (lowBound != null && highBound != null) {
				// compare low and high bound
				Comparator<? super E> comparator = comparator();
				int c = comparator == null ? ((Comparable<E>)highBound).compareTo(lowBound)
						: comparator.compare(highBound, lowBound);
				
				// control consistency of bounds parameters
				if (c < 0) {
					throw new IllegalArgumentException("high < low");
				}
			}			
		}

		/** Whether the given element value is within the value bounds set up 
		 * for this instance. Either one or two bounds can be set. If there are
		 * no bounds set, this always returns true.
		 * 
		 * @param v E
		 * @return boolean true = in range, false = out of range 
		 */
		private boolean isInbounds (E v) {
			Comparator<? super E> comparator = comparator();
			
			// verify lower bound compliance if defined
			if (lowBound != null) {
				int c = comparator == null ? ((Comparable<E>)v).compareTo(lowBound)
						: comparator.compare(v, lowBound);
				if (c < 0) return false;
			}

			// verify lower bound compliance if defined
			if (highBound != null) {
				int c = comparator == null ? ((Comparable<E>)v).compareTo(highBound)
						: comparator.compare(v, highBound);
				if (c > 0) return false;
			}
			return true;
		}
		
		@Override
		public boolean contains (Object o) {
			return isInbounds((E) o) && super.contains(o);
		}

		@Override
		public boolean add (E e) {
			// verify in-bound argument value
			if (!isInbounds(e)) 
				throw new IllegalArgumentException("value out of range: " + e);
			
			boolean ok = super.add(e);
			if (ok) {
				if (elementData != parentSet.elementData) {
					parentSet.elementData = elementData;
				}
				parentSet.modCount++;
				parentSet.size++;
			}
			return ok;
		}

		@Override
		public boolean remove (Object o) {
			if (!isInbounds((E) o)) return false;
			
			boolean ok = super.remove(o);
			if (ok) {
				if (elementData != parentSet.elementData) {
					parentSet.elementData = elementData;
				}
				parentSet.modCount++;
				parentSet.size--;
			}
			return ok;
		}

		@Override
		public void clear() {
			for (E e : this) {
				remove(e);
			}
		}

		@Override
		public Object clone() {
			@SuppressWarnings("unchecked")
			SubSortedArraySet<E> s = (SubSortedArraySet<E>) super.clone();
			s.elementData = s.parentSet.elementData;
			return s;
		}

		@Override
		public Iterator<E> iterator() {
			return new BoundsIterator(super.iterator());
		}

		@Override
		public int size() {
			int count = 0;
			for (E e : this) {
				count++;
			}
			return count;
		}

		private class BoundsIterator implements Iterator<E> {
			private Iterator<E> parent;
			private E next;
			private E current;

			BoundsIterator (Iterator<E> parent) {
				this.parent = parent;
				getNext();
			}
			
			private E getNext () {
				next = null;
				while (parent.hasNext() && next == null) {
					E pNext = parent.next();
					if (isInbounds(pNext)) {
						next = pNext;
					}
				}
				return next;
			}
			
			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public E next() {
				if (next == null)
					throw new NoSuchElementException();
				current = next;
				getNext();
				return current;
			}

			@Override
			public void remove() {
				if (current == null)
					throw new IllegalStateException();

				int ct = modCount;
				SubSortedArraySet.this.remove(current);
				current = null;
				modCount = ct;
			}
		}

	}
}

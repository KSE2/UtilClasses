package kse.utilclass.sets;

import java.io.Serializable;

/*
*  File: SortedArraySet.java
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

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

/**
 * {@code SortedArraySet} is an extension of {@code ArraySet} implementing the
 * {@code SortedSet} interface.
 * All elements inserted into this set must implement the {@code Comparable} 
 * interface or be accepted by a specified comparator.
 * A sorted set has advantage when a single set is used iteratively with
 * dominating membership requests or set operations ({@code OperatingSet}),
 * for their execution time.
 * 
 * <p>If instances have to be serialised, any comparator supplied needs to
 * implement the {@code java.io.Serializable} interface.
 * 
 * <p><b>Complexities (of the implementation of this class):</b>
 * <br>Single item: insertion, removal and iteration cost O(n), membership 
 * O(log n) execution time, n = size(). Set operations (s1, s2)
 * ({@code OperatingSet}) cost O(n+m) with n=|s1|, m=|s2|.
 *   
 * @param <E>
 */

public class SortedArraySet<E> extends ArraySet<E>  implements SortedSet<E> {
	
    private static final long serialVersionUID = -30952581123900119L;
	
	private Comparator<? super E> comparator;

	/** Creates an empty sorted-set with natural sorting of its elements.
	 */
	public SortedArraySet () {
	}

	/** Creates an empty sorted-set with natural sorting of its elements and
	 * the given initial capacity.
	 * 
	 * @param initialCapacity int
	 */
	public SortedArraySet (int initialCapacity) {
		super(initialCapacity);
	}

	/** Creates a new sorted-set with the given collection as initial content.
	 * The collection is not required to be sorted and may contain duplicate
	 * entries. Duplicate entries in the course of the collection's iterator
	 * are excluded.
	 * 
	 * @param c {@code Collection<? extends E>}, may be null
	 */
	public SortedArraySet (Collection<? extends E> c) {
		super(c);
	}

    /**
     * Constructs a set containing the elements of the specified array, 
     * excluding duplicates in the series of its growing index.
     * The size of the set hence may be smaller than the size of the argument.
     *
     * @param c {@code Collection} initial content of this set; may be null
     */
    public SortedArraySet (E[] c) {
    	super(c);
    }

	/** Creates a new sorted-set with the given {@code SortedSet} as initial
	 * content and the argument's comparator as sorting.
	 * 
	 * @param c {@code SortedSet<? extends E>}, may be null
	 */
	public SortedArraySet (SortedSet<E> c) {
		if (c == null) return;
		
		// transfer data
		if (c instanceof SortedArraySet) {
			// make a copy of the argument element array (same class)
			SortedArraySet<E> sa = (SortedArraySet<E>) c;
			this.elementData = Arrays.copyOf(sa.elementData, sa.elementData.length);

		} else {
			// transfer each single element (different set structure)
			ensureCapacity(c.size());
			int i = 0;
			for (E e : c) {
				elementData[i++] = e;
			}
		}
		
		size = c.size();
		comparator = c.comparator();
	}

	/** Creates an empty sorted-set with the given element comparator.
	 * 
	 * @param comparator {@code Comparator<? super E>}, may be null
	 */
	public SortedArraySet (Comparator<? super E> comparator) {
		setComparator(comparator);
	}

	/** Creates an empty sorted-set with the given element comparator and
	 * the given initial capacity.
	 * 
	 * @param comparator {@code Comparator<? super E>}, may be null
	 * @param initialCapacity int
	 */
	public SortedArraySet (Comparator<? super E> comparator, int initialCapacity) {
		super(initialCapacity);
		setComparator(comparator);
	}

	/** Sets the {@code Comparator} defining the total order on the elements
	 * of this set. A value of null sets usage of the natural ordering of
	 * the elements. If the set contains elements, it is implicitly resorted 
	 * by a call of this method.
	 * <p>NOTE: If this instance of {@code SortedArraySet} shall be serialised,
	 * the given comparator has to implement the {@code java.io.Serializable}
	 * interface and supply a unique {@code long serialVersionUID}.
	 *  
	 * @param comparator {@code Comparator}, null for "natural ordering"
	 */
	public void setComparator (Comparator<? super E> comparator) {
		this.comparator = comparator;
		resort();
	}

	@SuppressWarnings("unchecked")
	protected void resort () {
		// resort to new comparator
		if (comparator != null && size > 0) {
			Arrays.sort((E[])elementData, 0, size, comparator);
			modCount++;
		}
	}
	
	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}
	
	/** Returns the index position (>= 0) of the given object in the element 
	 * array. Returns a negative value (-insertPosition -1) if the object is not
	 * present in the array. This indicates the insert position.
	 * <p>NOTE: Arguments which are not of the class'es argument type are always
	 * prompted with -1.
	 *    
	 * @param e Object search object 
	 * @return int index or insert position
	 * @throws IllegalArgumentException if e is not Comparable and comparator is
	 *         null
	 * @throws NullPointerException if argument is null
	 */
	@SuppressWarnings("unchecked")
	private int objectPosition (Object o) {
		Objects.requireNonNull(o);
		int pos;

		try {
			// binary search for insert position of e
			if (comparator != null) {
				pos = Arrays.binarySearch((E[]) elementData, 0, size, (E)o, comparator);
			} else {
				if (!(o instanceof Comparable)) {
					throw new IllegalArgumentException("argument is not comparable");
				}
				pos = Arrays.binarySearch(elementData, 0, size, o);
			}
			return pos;
			
		} catch (ClassCastException ex) {
			return -1;
		}
	}

    @Override
    public int indexOf (Object o) {
   	    int pos = objectPosition(o);
    	return pos >= 0 ? pos : -1;
    }

	@Override
    public boolean contains (Object o) {
		return objectPosition(o) > -1;
	}
	
	/** Compares the specified object with this set for equality. Returns true
	 * if and only if the specified object is also a {@code SortedSet} both 
	 * sets have the same size, and all corresponding pairs of elements in the
	 * two sets are equal. (Two elements e1 and e2 are equal if 
	 * (e1==null ? e2==null : e1.equals(e2)).) In other words, two sorted-sets
	 * are defined to be equal if they contain the same elements in the same 
	 * order.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals (Object o) {
		if (o == this) return true;
		if (!(o instanceof SortedSet)) return false;
		int ourSize = size();
		try {
			Collection<E> col = (Collection<E>) o;
			if (col.size() != ourSize) return false;
			
			Iterator<E> it1 = iterator();
			Iterator<E> it2 = col.iterator();
			for (int i = 0; i < ourSize; i++) {
				if (!it1.next().equals(it2.next())) return false;
			}
		} catch (ClassCastException e) {
			return false;
		}
		return true;
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
		int pos = objectPosition(o);
		if (pos < 0) return false;

		System.arraycopy(elementData, pos+1, elementData, pos, --size-pos);
	    elementData[size] = null;
	    modCount++;
	    return true;
	}

	@Override
	public SortedSet<E> subSet (E fromElement, E toElement) {
		Objects.requireNonNull(toElement, "toElement is null");
		Objects.requireNonNull(fromElement, "fromElement is null");
		return new SubSortedSet(this, fromElement, toElement);
	}

	@Override
	public SortedSet<E> headSet (E toElement) {
		Objects.requireNonNull(toElement);
		return new SubSortedSet(this, null, toElement);
	}

	@Override
	public SortedSet<E> tailSet (E fromElement) {
		Objects.requireNonNull(fromElement);
		return new SubSortedSet(this, fromElement, null);
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
	
	/** Returns the element at the given index position in the sorted 
	 * sequence.  
	 * 
	 * @param index int index position counting from 0
	 * @return E
	 * @throws IndexOutOfBoundsException
	 */
	public E getElement (int index) {
		return get(index);
	}
	
// ----------------------------------------------------------
	
	private class SubSortedSet extends AbstractSet<E> implements OperatingSet<E>, SortedSet<E>, Serializable {
	    private static final long serialVersionUID = -30932555128001208L;
		private SortedSet<E> parentSet;
		private E lowBound, highBound;

		public SubSortedSet (SortedSet<E> set, E lowBound, E highBound) {
			super();
			
			// take over from set (i.e.redirect element data)
			setComparator(set.comparator());
			parentSet = set;
			this.lowBound = lowBound;
			this.highBound = highBound;

			// if both bound values are defined, check for consistency
			if (lowBound != null && highBound != null) {
				// compare low and high bound
				Comparator<? super E> comp = comparator();
				@SuppressWarnings("unchecked")
				int c = comp == null ? ((Comparable<E>)highBound).compareTo(lowBound)
						: comp.compare(highBound, lowBound);
				
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
		@SuppressWarnings("unchecked")
		private boolean isInbounds (E v) {
			Comparator<? super E> comp = comparator();
			
			// verify lower bound compliance if defined
			if (lowBound != null) {
				int c = comp == null ? ((Comparable<E>)v).compareTo(lowBound)
						: comp.compare(v, lowBound);
				if (c < 0) return false;
			}

			// verify lower bound compliance if defined
			if (highBound != null) {
				int c = comp == null ? ((Comparable<E>)v).compareTo(highBound)
						: comp.compare(v, highBound);
				if (c >= 0) return false;
			}
			return true;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public boolean contains (Object o) {
			return isInbounds((E) o) && parentSet.contains(o);
		}

		@Override
		public boolean add (E e) {
			// verify in-bound argument value
			if (!isInbounds(e)) 
				throw new IllegalArgumentException("value out of range: " + e);
			
			boolean ok = parentSet.add(e);
			return ok;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove (Object o) {
			Objects.requireNonNull(o);
			if (!isInbounds((E) o)) return false;
			
			boolean ok = parentSet.remove(o);
			return ok;
		}

		@Override
		public Iterator<E> iterator() {
			return new BoundsIterator((BackstepIterator<E>) parentSet.iterator());
		}

		@Override
		public int size() {
			int count = 0;
			for (@SuppressWarnings("unused") E e : this) {
				count++;
			}
			return count;
		}

		private class BoundsIterator implements Iterator<E> {
			private BackstepIterator<E> parent;
			private E next;
			private E current;

			BoundsIterator (BackstepIterator<E> parent) {
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
				SubSortedSet.this.remove(current);
				parent.backstep();
				current = null;
				modCount = ct;
			}
		}

		@Override
		public boolean isEmpty() {
			return !iterator().hasNext();
		}

		@Override
		public Comparator<? super E> comparator() {
			return parentSet.comparator();
		}

		@Override
		public SortedSet<E> subSet(E fromElement, E toElement) {
			if (!isInbounds(toElement) || !isInbounds(fromElement))
				throw new IllegalArgumentException("value out of range");
			return new SubSortedSet(this, fromElement, toElement);
		}

		@Override
		public SortedSet<E> headSet(E toElement) {
			if (!isInbounds(toElement))
				throw new IllegalArgumentException("value out of range");
			return new SubSortedSet(this, null, toElement);
		}

		@Override
		public SortedSet<E> tailSet(E fromElement) {
			if (!isInbounds(fromElement))
				throw new IllegalArgumentException("value out of range");
			return new SubSortedSet(this, fromElement, null);
		}

		@Override
		public E first() {
			Iterator<E> it = iterator();
			return it.hasNext() ? it.next(): null;
		}

		@Override
		public E last() {
			E element = null;
			Iterator<E> it = iterator();
			while (it.hasNext()) {
				element = it.next();
			}
			return element;
		}

		@Override
		public OperatingSet<E> intersected(Set<E> a) {
			SortedArraySet<E> set = (SortedArraySet<E>) emptyClone();
			for (E elem : this) {
				if (a.contains(elem)) {
					set.add(elem);
				}
			}
			return set;
		}

		@Override
		public OperatingSet<E> united(Set<E> a) {
			SortedArraySet<E> set = new SortedArraySet<>(this);
			for (E elem : a) {
				set.add(elem);
			}
			return set;
		}

		@Override
		public OperatingSet<E> without(Set<E> a) {
			SortedArraySet<E> set = new SortedArraySet<>(this);
			for (E elem : a) {
				if (this.contains(elem)) {
					set.remove(elem);
				}
			}
			return set;
		}

		@Override
		public OperatingSet<E> xored(Set<E> a) {
			SortedArraySet<E> set = (SortedArraySet<E>) without(a);
			for (E elem : a) {
				if (!this.contains(elem)) {
					set.add(elem);
				}
			}
			return set;
		}

		@Override
		public void intersectWith (Set<E> a) {
			retainAll(a);
		}

		@Override
		public void uniteWith (Set<E> a) {
			checkAllInbound(a);
			addAll(a);
		}

		private void checkAllInbound (Iterable<E> a) {
			for (Iterator<E> it = a.iterator(); it.hasNext();) {
				if (!isInbounds(it.next())) {
					throw new IllegalArgumentException("parameter contains illegal elements");
				}
			}
		}

		@Override
		public void exclude (Set<E> a) {
			removeAll(a);
		}

		@Override
		public void xorWith (Set<E> a) {
			Set<E> intersection = intersected(a);
			uniteWith(a);
			exclude(intersection);
		}
	}
}

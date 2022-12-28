package kse.utilclass.sets;

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
	}

	@Override
	public Comparator<? super E> comparator() {
		return comparator;
	}
	
//    @SuppressWarnings("unchecked")
//    @Override
//	public SortedArraySet<E> clone () {
//   	   return (SortedArraySet<E>) super.clone();
//	}
//
//    protected SortedArraySet<E> emptyClone () {
//   	   return (SortedArraySet<E>) super.clone();
// 	}
    
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
		if (index < 0 | index >= size()) 
			throw new IndexOutOfBoundsException();
		
		Iterator<E> it = iterator();
		E e = null;
		for (int i = 0; i <= index; i++) {
			e = it.next();
		}
		return e;
	}
	
	/** Returns the index position of the given element object in this SortedSet
	 * or -1 if this object is not contained.
	 *  
	 * @param o Object
	 * @return int index position or -1
	 */
	public int indexOf (Object o) {
		int ct = 0;
		for (Iterator<E> it = iterator(); it.hasNext(); ct++) {
			if (it.next().equals(o)) {
				return ct;
			}
		}
		return -1;
	}
	
// ----------------------------------------------------------
	
	private class SubSortedSet extends AbstractSet<E> implements OperatingSet<E>, SortedSet<E> {
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
				if (c >= 0) return false;
			}
			return true;
		}
		
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

package kse.utilclass.sets;

/*
*  File: ArraySet.java
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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

/** ArraySet is a {@code java.util.AbstractSet} which implements the 
 * package own {@code OperatingSet} interface for convenient set operations. 
 * Each member object can only be contained once and it allows <b>null</b> to be 
 * an element. It also works as a {@code Collection}. 
 * <p>The advantage of 
 * ArraySet over a TreeSet can be seen in the instant availability of the
 * conversion into an array, or in its block data structure which
 * causes less heap segmentation. There is, however, a disadvantage in higher
 * cardinalities, and in particular with the set operations, for their execution
 * time.
 * <p><b>Complexities (of the implementation of this class)</b>
 * <br>Single item: insertion costs O(1), iteration, removal and membership 
 * cost O(n) execution time, with n = size(). Set operations (s1, s2)
 * ({@code OperatingSet}) cost O(n*m) with n=|s1|, m=|s2|.
 *  
 * @param <E> element type of this container class
 */
public class ArraySet<E> extends AbstractSet<E> implements OperatingSet<E>, 
			java.io.Serializable, Cloneable 
{
    private static final long serialVersionUID = 8993772220011867823L;

    /**
     * Default initial capacity.
     */
    public static final int DEFAULT_CAPACITY = 16;

    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * The array buffer into which the elements of the ArrayList are stored.
     * The capacity of the ArrayList is the length of this array buffer. Any
     * empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
     * will be expanded to DEFAULT_CAPACITY when the first element is added.
     */
    protected Object[] elementData = EMPTY_ELEMENTDATA; 

    /**
     * The size of the ArrayList (the number of elements it contains).
     *
     * @serial
     */
    protected int size;
    
    protected int modCount;

    /**
     * Constructs an empty set with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArraySet (int initialCapacity) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("initialCapacity < 0");

        ensureCapacity(initialCapacity);
    }

    /**
     * Constructs an empty set.
     */
    public ArraySet () {
    }

    /**
     * Constructs a set containing the elements of the specified collection, 
     * excluding duplicates in the series of its iterator.
     * The size of the set hence may be smaller than the size of the argument
     * collection.
     *
     * @param c {@code Collection} initial content of this set; may be null
     */
    public ArraySet (Collection<? extends E> c) {
    	if (c != null) {
	    	ensureCapacity(c.size());
        	addAll(c);
    	}
    }

    /**
     * Constructs a set containing the elements of the specified collection, 
     * excluding duplicates in the series of its growing index.
     * The size of the set hence may be smaller than the size of the argument
     * array.
     *
     * @param a {@code E[]} initial content of this set; may be null
     */
    public ArraySet (E[] a) {
        if (a != null) {
        	ensureCapacity(a.length);
    		for (E e : a) add(e);
        }
    }

    /** Creates a new set of the given type with initial content consisting of 
     * the elements of the given array.
     *  
     * @param <T> type of elements
     * @param arr array of elements T
     * @return {@code ArraySet<T>}
     */
    public static <T> ArraySet<T> fromArray (T[] arr) {
    	ArraySet<T> set = new ArraySet<>();
    	if (arr != null) {
    		set.ensureCapacity(arr.length);
    		for (T e : arr) set.add(e);
    	}
    	return set;
    }
    
    /** Length of the current element array. 
     * 
     * @return int
     */
    int getCurrentCapacity () {
    	return elementData.length;
    }
    
    /** Enlarges the capacity of this ArraySet if necessary to encompass at 
     * least the number of elements given by the parameter. 
     * 
     * @param minCapacity int minimum capacity
     */
    public void ensureCapacity (int minCapacity) {
        // enlarge capacity
        if (minCapacity > elementData.length) {
            grow(minCapacity);
        }
    }

    /** If applicable, shrinks the capacity of this set to a value 
     * (size + size/4). This does not operate to enlarge the capacity.
     */
    public void taylorCapacity () {
       // shrink capacity
       int capacity = size + size/4; 
       if (capacity < elementData.length) {
          elementData = Arrays.copyOf(elementData, capacity);
       }
    }
    
    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity desired minimum capacity
     * @throws IllegalArgumentException if argument is negative
     * @throws OutOfMemoryError if argument is greater than MAX_ARRAY_SIZE
     */
    private void grow (int minCapacity) {
        if (minCapacity < 0)
            throw new IllegalArgumentException("minCapacity < 0");
        if (minCapacity > MAX_ARRAY_SIZE)
            throw new OutOfMemoryError("requested oversized array capacity");
        
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int quarter = oldCapacity/4;
        int factor = oldCapacity < 1000 ? 4 : oldCapacity < 12000 ? 3 : 1;
        int newCapacity = oldCapacity + Math.max(DEFAULT_CAPACITY, quarter * factor);
        if (newCapacity < 0 | newCapacity > MAX_ARRAY_SIZE) {
        	newCapacity = MAX_ARRAY_SIZE;
        }

        newCapacity = Math.max(minCapacity, newCapacity);
        elementData = Arrays.copyOf(elementData, newCapacity);
//        System.out.println("-- (ArraySet) growing capacity to  " + newCapacity);
    }

   @Override
   public Iterator<E> iterator() {
      return new ASIterator();
   }

   @Override
   public int size() {
      return size;
   }

   @Override
   public boolean add (E e) {
	  // ignore double element
      if (contains(e)) return false;
      
      // add new element
      ensureCapacity(size + 1);
      elementData[size++] = e;
      modCount++;
      return true;
   }

   /** Returns the index position of the given element in this array-set or
    * -1 if it is not contained.
    * 
    * @param o E 
    * @return index position or -1
    */
   public int indexOf (Object o) {
	   if (o == null) {
		   for (int i = 0; i < size; i++) {
			   if (elementData[i] == null) {
				   return i;
			   }
		   }
	   } else {
		   for (int i = 0; i < size; i++) {
			   if (o.equals(elementData[i])) {
				   return i;
			   }
		   }
	   }
	   return -1;
   }
   
	@Override
	public int hashCode() {
		int hash = 98982, ct = 1;
		boolean isSorted = this instanceof SortedSet;
		for (int i = 0; i < size; i++) {
		    Object obj = elementData[i];
		    hash += (obj == null ? 8567 : obj.hashCode()) * (isSorted ? ct++ : 1);
	    }
		return hash;
	}

   /** Returns the element at the given index position.
    *  
    * @param index int index of element
    * @return E element
    * @throws IndexOutOfBoundsException
    */
   @SuppressWarnings("unchecked")
   public E get (int index) {
	   if (index < 0 | index >= size)
		   throw new IndexOutOfBoundsException("index out of range: " + index);
	   return (E) elementData[index];
   }
   
   @Override
   public boolean contains (Object o) {
	   return indexOf(o) > -1;
   }

   @Override
   public Object[] toArray() {
	   Object[] res = new Object[size()];
	   System.arraycopy(elementData, 0, res, 0, size);
	   return res;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public <T> T[] toArray(T[] a) {
       // create typed array of cardinality
       T[] res = a.length >= size ? a : (T[])java.lang.reflect.Array
                 .newInstance(a.getClass().getComponentType(), size);
       
       // move data block
	   System.arraycopy(elementData, 0, res, 0, size);
       if (a.length > size) {
           a[size] = null;
       }
	   return res;
   }
   
   @Override
   public void clear() {
	  if (size > 0) {
		  elementData = EMPTY_ELEMENTDATA;
		  size = 0;
		  modCount++;
	  }
   }

   /** Returns a shallow clone of this ArraySet.
    * 
    * @return Object 
    */
   @SuppressWarnings("unchecked")
   @Override
   public Object clone() {
      try {
		 ArraySet<E> copy = (ArraySet<E>)super.clone();
         copy.elementData = Arrays.copyOf(elementData, size);
         return copy;
      } catch (CloneNotSupportedException e) {
         return null;
      }
   }

   protected ArraySet<E> emptyClone () {
      try {
    	@SuppressWarnings("unchecked")
		ArraySet<E> copy = (ArraySet<E>)super.clone();
         copy.elementData = new Object[2*DEFAULT_CAPACITY];
         copy.size = 0;
         copy.modCount = 0;
         return copy;
      } catch (CloneNotSupportedException e) {
         return null;
      }
   }

	@Override
	public OperatingSet<E> intersected (Set<E> a) {
		ArraySet<E> set = emptyClone();
		for (E elem : this) {
			if (a.contains(elem)) {
				set.add(elem);
			}
		}
		return set;
	}
	
	@Override
	public OperatingSet<E> united (Set<E> a) {
		@SuppressWarnings("unchecked")
		ArraySet<E> set = (ArraySet<E>) this.clone();
		for (E elem : a) {
			set.add(elem);
		}
		return set;
	}
	
	@Override
	public OperatingSet<E> without (Set<E> a) {
		ArraySet<E> set = emptyClone();
		for (E elem : this) {
			if (!a.contains(elem)) {
				set.add(elem);
			}
		}
		return set;
	}
	
	@Override
	public OperatingSet<E> xored (Set<E> a) {
		ArraySet<E> set = (ArraySet<E>) without(a);
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
		addAll(a);
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
	
//  -----------------------------------------------
	
    /**
     * Iterator to traverse defined element data of this ArraySet.
     */
    protected class ASIterator implements BackstepIterator<E> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedModCount = modCount;

        ASIterator() {}

        @Override
		public boolean hasNext() {
            return cursor != size;
        }

        @Override
		@SuppressWarnings("unchecked")
        public E next() {
            checkForComodification();
            int i = cursor;
            if (i >= size)
                throw new NoSuchElementException();
            Object[] data = ArraySet.this.elementData;
            if (i >= data.length)
                throw new ConcurrentModificationException();
            cursor = i + 1;
            return (E) data[lastRet = i];
        }

        @Override
		public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
            	// remove element index = lastRet from array
            	int i = lastRet;
            	System.arraycopy(elementData, i+1, elementData, i, --size-i);
            	elementData[size] = null;
                cursor = lastRet;
                lastRet = -1;
                
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException(ex);
            }
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

		@Override
		public boolean backstep() {
			if (cursor > 0 | lastRet < 0) {
				cursor--;
				lastRet = -1;
			}
			return false;
		}
    }

}

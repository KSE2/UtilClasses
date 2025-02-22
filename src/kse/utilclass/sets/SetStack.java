/*
*  File: SetStack.java
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

package kse.utilclass.sets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/** Class working as {@code Set<E>} interface but adds functionality of the 
 * {@code Stack<E>} and {@code List<E>} interfaces, in other words: adds an 
 * ordering aspect to the set of elements while at the same time can operate
 * as a stack.
 * 
 *  <p>This is a stack where each element can only be contained once. It is a
 *  sorting device for sets where each push() of an element relocates it to the
 *  top of the stack. The "add()" method works now like the "push()"
 *  method and the implicit ordering of the set is available through a 
 *  <code>List</code> view running from the oldest (bottom) to the newest (top) 
 *  entry.
 *  
 *  <p>The logic of "add(E)" is slightly modified for the case of
 *  double entry attempts. Instead of suppressing addition of a second, equal
 *  element as in <code>ArraySet</code>, the older element is removed and the 
 *  newer one always inserted. Methods {@code add(int,E)} and {@code set(int,E)}
 *  work as described but have additional semantics as they make sure that
 *  duplicates of the inserted element are removed.
 *  
 *  <p>Confusion may arise from usage of the index values as the underlying
 *  structure orders in reverse to the stack-index. Both stack- and list-index
 *  range from zero to size-1. Note that the {@code search()} function 
 *  returns the stack-index + 1. Enjoy!
 *
 * @param <E>
 */
public class SetStack<E> extends ArrayList<E> implements Set<E> {
	
   private static final long serialVersionUID = 8223386640083767823L;

   public SetStack() {
   }

   /** Creates a new stack with the given initial capacity.
    *  
    * @param initialCapacity int
    * @throws IllegalArgumentException if the argument is negative
    */
   public SetStack (int initialCapacity) {
	   super(initialCapacity);
   }

   /** Creates a new stack with the given collection as initial content.
    * The last element will be the top of the stack.
    *  
    * @param c {@code Collection<E>}
    */
   public SetStack (Collection<E> c) {
	   for (E i : c) {
		   add(i);
	   }
   }

   /** Creates a new stack with the given value array as initial content.
    * The last element will be the top of the stack.
    *  
    * @param c {@code E[]} array of elements
    */
   public SetStack (E[] c) {
	   for (E i : c) {
		   add(i);
	   }
   }

   @Override
   /** Adds the specified element to this set on top of the stack and 
    * removes any previous occurrence of the entry if it was already present. 
    * 
    *  @param e E entry to add
    */
   public boolean add (E e) {
      // remove element if already contained in list
      remove(e);
      
      // put element on top of 
      return super.add(e);
   }

   /** Replaces the element at the specified index-position in 
    * this list with the specified element and returns the previous element.
    * <p>If an entry of same identity was already present in the list, it is 
    * returned as replaced if it was at the same position or eliminated 
    * otherwise. In the latter case the list shrinks in size by one and the
    * index-position of the new entry may change and differ from the given
    * value. (The stack is a living entity!) 
    * 
    * <p>NOTE: The index is a value of the underlying list structure as
    * returned by method {@code getList()}. It is not the stack-distance
    * as returned by method {@code search()} and it is not an index of the 
    * stack-list.
    * 
    * @param index int list index position
    * @param e E new entry
    * @return E entry that has been replaced
    */
   @Override
   public E set (int index, E e) {
	   int i = indexOf(e);
	   E old = super.set(index, e);
	   
	   // if we have a previous identical entry and its position is elsewhere
	   if (i > -1 && i != index) {
		   super.remove(i);
	   }
	   return old;
   }

   /** Inserts the specified element at the specified position in this list.
    * If an entry of same identity was already present in the list, it is 
    * removed, otherwise the list grows by one. If the list size remains
    * the same, the index position of the inserted element may result
    * different than what is given.
    * 
    * <p>NOTE: The index is a value of the underlying list structure as
    * returned by method {@code getList()}. It is not the stack-distance
    * as returned by method {@code search()} and it is not an index of the 
    * stack-list.
    *   
    * @param index int position of insertion
    * @param e E entry to insert
    */
   @Override
   public void add (int index, E e) {
	   int i = indexOf(e);
	   super.add(index, e);
	   if (i > -1) {
	       remove(index <= i ? i+1 : i);
	   }
   }

   /** Adds all elements of the given collection into this list starting from
    * list-index {@code index} in the order of its iterator. If the argument
    * collection is not null or empty, true is returned. 
    * 
    * <p>NOTE: The index is a value of the underlying list structure as
    * returned by method {@code getList()}. It is not the stack-distance
    * as returned by method {@code search()} and it is not an index of the 
    * stack-list.
    * 
    * @param index int start position of insertion
    * @param c {@code Collection<? extends E>}, may be null
    * @return boolean true = list has changed, false = list is unchanged
    */
   @Override
   public boolean addAll (int index, Collection<? extends E> c) {
	   if (c == null) return false;
	   int size = size();
	   for (E e : c) {
		   add(index, e);
		   if (size() > size) {
			   size++; index++;
		   }
	   }
	   return !c.isEmpty();
   }

   /** Pushes all elements of the given collection onto this stack as if
    * by calling {@code push(E}} for all elements in the order of the 
    * collection'S iterator. If the argument collection is not null or empty, 
    * true is returned. 
    * 
    * @param c {@code Collection<? extends E>}, may be null
    */
   @Override
   public boolean addAll (Collection<? extends E> c) {
	   return addAll(size(), c);
   }
   
   /** Pushes a new element on top of this stack. If an equal element is 
    * already contained, it is removed before the new element is inserted.
    * 
    * @param item E
    * @return E the added element
    */
   public E push (E item) {
      add(item);
      return item;
   }
 
   /** Returns the top element of this stack by removing it  
    * or null if this stack is empty.
    * 
    * @return E or null
    */
   public E pop () {
      E el = null;
      int index = size()-1;
      if (index > -1) {
         el = remove(index);
      }
      return el;
   }
   
   /** Returns the top element of this stack without removing it 
    * or null if the stack is empty.
    * 
    * @return E or null
    */
   public E peek () {
      int size = size();
      if (size > 0) {
         return get(size-1);
      }
      return null;
   }
   
   /** Returns the distance of object o from the top of the stack, starting
    * with 1 at the top position. Caution! This is not the list-index value,
    * instead it is the stack-index plus 1.
    * 
    * @param o Object to search
    * @return distance in 1-steps from top or -1 if object is not contained
    */
   public int search (Object o) {
      int size = size();
      int index = indexOf(o);
      return index == -1 ? index : size - index;
   }
   
   /** Returns an iterator over the elements in this stack in the order
    * of the stack-list, from top of the stack to the bottom. 
    * 
    * @return {@code Iterator<E>}
    */
   @Override
   public Iterator<E> iterator() {
	   return getStackList().iterator();
   }

   /** Returns a list-iterator over the elements in this stack in the order
    * of the stack-list, from top of the stack to the bottom. 
    * 
    * @return {@code ListIterator<E>}
    */
	@Override
	public ListIterator<E> listIterator() {
		return getStackList().listIterator();
	}
	
   /** Returns a list-iterator over a subset of the elements in this stack 
    * in the order of the stack-list, i.e. from top of the stack to the bottom. 
    * 
    * @param index int index of first element to be returned
    * @return {@code ListIterator<E>}
    */
	@Override
	public ListIterator<E> listIterator(int index) {
		return getStackList().listIterator(index);
	}

/** Sorting is not supported in this class.
    * 
    * @throws UnsupportedOperationException
    */
   @Override
   public void sort (Comparator<? super E> c) {
	   throw new UnsupportedOperationException();
   }

   /** Returns a list view of this set-stack with elements ordered from the
    * oldest to the youngest entries by the index growing. The view is a 
    * shallow copy of the contents and modifications to the list do 
    * not write through to this stack.
    * 
    * @return {@code List<E>} 
    */
   public List<E> getList () {
      return new ArrayList<E>(this);
   }
   
   /** Returns the order-inversion of a stack- or list-index value.
    *  <p>Both stack- and list-indices range from zero to size()-1. This
    *  function transforms a stack-index into a list-index or a list-index
    *  into a stack-index.
    *  
    * @param index int index value ranging from 0 to size()-1
    * @return int reverse index value
    * @throws IllegalArgumentException
    */
   public int getReverseIndex (int index) {
	   if (index < 0 | index >= size())
		   throw new IndexOutOfBoundsException("illegal value: " + index);
	   return size() - index -1;
   }
   
   /** Returns a list view of this set-stack with elements ordered from the
    * the youngest to the oldest entries by the index growing, i.e. index zero
    * is the top of the stack. The view can be modified without striking 
    * through to this stack.
    * 
    * @return {@code List<E>} 
    */
   public List<E> getStackList () {
	  List<E> list = getList();
	  Collections.reverse(list);
      return list;
   }
   
   /** Whether this stack contains no elements. Same as "isEmpty()", this is a
    * convenience method to comply with the {@code Stack} interface.
    * 
    * @return boolean
    */
   public boolean empty () {return isEmpty();}
}

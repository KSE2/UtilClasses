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
import java.util.List;
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
 *  only operate for entries which are not contained in the set or which occupy 
 *  the same position as expressed by the parameter.
 *
 * @param <E>
 */
public class SetStack<E> extends ArrayList<E> implements Set<E>{

   public SetStack() {
   }

   public SetStack(Collection<E> c) {
      super(c);
   }

   @Override
   public boolean add (E e) {
      // remove element if candidate is already contained in list
      remove(e);
      
      // put element on top of 
      return super.add(e);
   }

   @Override
   public E set (int index, E e) {
	   int i = indexOf(e);
	   if (i > -1 && i != index) {
		   throw new IllegalArgumentException("duplicate element entry");
	   }
	   return super.set(index, e);
   }

   @Override
   public void add (int index, E e) {
	   int i = indexOf(e);
	   if (i > -1 && i != index) {
		   throw new IllegalArgumentException("duplicate element entry");
	   }
       remove(e);
	   super.add(index, e);
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
 
   /** Returns the top element of this stack by removing it from the structure 
    * or null if this stack is empty.
    * 
    * @return E or null
    */
   public E pop () {
      E el = null;
      int index = size()-1;
      if (index > -1) {
         el = get(index);
         remove(index);
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
    * with 1 at the top position.
    * 
    * @param o Object to search
    * @return distance in 1-steps from top or -1 if object is not contained
    */
   public int search (Object o) {
      int size = size();
      int index = indexOf(o);
      return index == -1 ? index : size - index;
   }
   
   /** Returns a list view of this set-stack. The view is a shallow copy
    * of the contents and modifications to the returned list do not write 
    * through to this stack.
    * 
    * @return {@code List<E>} 
    */
   public List<E> getList () {
      return new ArrayList<E>(this);
   }
   
   /** Whether this stack contains no elements. Same as "isEmpty()", this is a
    * convenience method to comply with the {@code Stack} interface.
    * 
    * @return boolean
    */
   public boolean empty () {return isEmpty();}
}

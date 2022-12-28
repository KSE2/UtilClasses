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

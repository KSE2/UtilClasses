package kse.utilclass.lists;

/*
*  File: ArrayIterator.java
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

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {

	private T[] arr;
	private int p;
	
	public ArrayIterator (T[] array) {
		if (array == null)
			throw new NullPointerException();
		
		arr = array;
	}

	@Override
	public boolean hasNext() {
		return p < arr.length;
	}

	@Override
	public T next() {
		if ( p >= arr.length ) 
			throw new NoSuchElementException();
		
		T obj = arr[p++];
		return obj;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

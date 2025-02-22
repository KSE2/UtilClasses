package kse.utilclass.misc;

/*
*  File: LongResult.java
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

/**
 *  Wrapper class for a integer value (int) which may be referenced and modified.
 *  This can be used e.g. to pass a result value from a method to the caller
 *  via the parameter list. 
 */
public class LongResult implements Cloneable, java.io.Serializable {
	
    private static final long serialVersionUID = 186404157768715381L;
    private long value;
   
	/**
	 * Empty Constructor defines a zero integer value. 
	 */
	public LongResult () {}
	
	/**
	 * Constructor which defines the argument integer value. 
	 */
	public LongResult ( long v ) {
	   value = v;
	}
	
	public long getValue () {return value;}
	
	public void setValue ( long v ) {
	   value = v;
	}
	
	@Override
	public int hashCode() {
		return (int)value ^ (int)(value >> 32);
	}

	@Override
	public boolean equals (Object obj) {
		if (obj instanceof LongResult) {
		    return value == ((LongResult)obj).value;
		} 
		return false;
	}

	@Override
	public String toString() {
		return Long.toString(value);
	}
	
	
}

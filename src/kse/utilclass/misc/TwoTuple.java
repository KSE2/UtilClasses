package kse.utilclass.misc;

/*
*  File: TwoTuple.java
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

public class TwoTuple implements Cloneable {
	public long v1, v2; 
	
	public TwoTuple (long v1, long v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public TwoTuple (int v1, int v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	@Override
	public Object clone() {
		TwoTuple c;
		try {
			c = (TwoTuple) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
		return c;
	}
	
	@Override
	public int hashCode() {
		return (int) (v1 ^ v2);
	}

	@Override
	public boolean equals (Object obj) {
		if (!(obj instanceof TwoTuple)) return false;
		TwoTuple o = (TwoTuple)obj;
		return o.v1 == v1 && o.v2 == v2;
	}

	@Override
	public String toString() {
		return "(" + v1 + ", " + v2 + ")";
	}

	public int intVal1 () {return (int)v1;}

	public int intVal2 () {return (int)v2;}
	
	public long longVal1 () {return v1;}

	public long longVal2 () {return v2;}
	
}

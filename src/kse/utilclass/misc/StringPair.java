package kse.utilclass.misc;

/*
*  File: StringPair.java
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

import java.util.Objects;

/** Represents a pair of text strings, which can be defined or analysed from
 * another string.
 */
public class StringPair {
	public String s1, s2;
	
	StringPair (String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}
	
	/** Returns a string-pair as a 2-partition of the given string where the cut
	 * is made at the last occurrence of the given separator character. The
	 * last separator is not included in any of the resulting part-strings.
	 * If the given string does not contain the separator, s1 assumes the value 
	 * null.
	 *  
	 * @param s String string to analyse
	 * @param sep char separator
	 * @return {@code StringPair}
	 */
	public static final StringPair analysePath (String s, char sep) {
		Objects.requireNonNull(s, "path is null");
		StringPair pair = new StringPair(null, s);
		int index = s.lastIndexOf(sep);
		if (index > -1) {
			pair.s1 = s.substring(0, index);
			pair.s2 = s.substring(index+1);
		}
		return pair;
	}
}
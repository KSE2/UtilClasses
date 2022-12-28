package kse.utilclass.misc;

/*
*  File: CRC_32.java
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

import java.util.zip.Adler32;
import java.util.zip.Checksum;

/** CRC_32 is an {@code Adler32} class extended by functionality to update
 * the checksum under various parameter types. 
 */
public class CRC_32 extends Adler32 implements Checksum {

	public CRC_32() {
	}
	
	/** Updates the checksum with the 4 bytes of an integer.
	 * 
	 * @param v int
	 */
	public void updateInt (int v) {
		for (int i = 0; i < 4; i++) {
			update( v >> (i * 8) ); 
		}
	}

	/** Updates the checksum with the 8 bytes of a long integer.
	 * 
	 * @param v long
	 */
	public void updateLong (long v) {
		for (int i = 0; i < 8; i++) {
			update((int) (v >> (i * 8))); 
		}
	}

	/** Updates the checksum with all characters of the given string value.
	 * Does nothing if the argument is null.
	 * 
	 * @param s String, may be null
	 */
	public void updateString (String s) {
		if (s == null) return;
		char[] arr = s.toCharArray();
		for (char c : arr) {
			for (int j = 0; j < 2; j++) {
				update( c >> (j * 8) ); 
			}
		}
	}
}

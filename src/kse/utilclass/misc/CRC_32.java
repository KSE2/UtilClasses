package kse.utilclass.misc;

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

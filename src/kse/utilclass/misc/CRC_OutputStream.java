package kse.utilclass.misc;

/*
*  File: CRC_OutputStream.java
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

import java.io.IOException;
import java.io.OutputStream;

/** Class to reflect data which is sent to this output stream into a CRC_32
 * checksum value. The CRC_32 is an Adler32 value.
 *
 * @author Wolfgang Keller
 */
public class CRC_OutputStream extends OutputStream {
	CRC_32 crc32 = new CRC_32();
	

	@Override
	public void write (int b) throws IOException {
		crc32.update(b);
	}

	@Override
	public void write (byte[] b, int off, int len) throws IOException {
		crc32.update(b, off, len);
	}

	/** Returns the current checksum value. All data so far written to this 
	 * output stream are reflected into this value. 
	 * <p>Currently the returned value is a CRC32 value. 
	 * 
	 * @return long
	 */
	public long getValue () {
		return crc32.getValue();
	}

	public void writeLong (long value) {
		byte[] buf = new byte[8];
		Util.writeLongLittle(value, buf, 0);
		crc32.update(buf, 0, 8);
	}
	
	public void writeInt (int value) {
		byte[] buf = new byte[4];
		Util.writeIntLittle(value, buf, 0);
		crc32.update(buf, 0, 4);
	}
	
	public void writeChar (char c) {
		writeInt(c);
	}
}

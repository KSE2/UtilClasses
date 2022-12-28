package kse.utilclass.misc;

/*
*  File: TestC_Util.java
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.CharBuffer;
import java.util.Arrays;

import org.junit.Test;

public class TestC_Util {

	public TestC_Util() {
	}

	private void readIntegerCase (String text, int pos, long rval, int rpos, boolean exception) {
		// report
		System.out.println("RI - case: [" + text + "], pos " + pos + " --> " + rval + ", exc=" + exception);
		
		CharBuffer cbuf = CharBuffer.wrap(text.toCharArray());
		cbuf.position(pos);
		try {
			long result = Util.readInteger(cbuf);
			assertTrue("result invalid, received " + result, result == rval);
			assertTrue("unexpected buffer position: " + cbuf.position(), cbuf.position() == rpos);
		
			assertFalse("missing NumberFormatException", exception); 
		} catch (NumberFormatException e) {
			assertTrue("unexpected NumberFormatException", exception);
		}
	}
	
	@Test
	public void read_integer () {
		// nonsense text
		readIntegerCase("", 0, 0, 0, true);
		readIntegerCase("iuere iurzq jlqj", 0, 0, 0, true);
		readIntegerCase("iuere iurzq jlqj", 5, 0, 0, true);
		readIntegerCase("cremona-", 7, 0, 8, true);
		readIntegerCase("cremona--", 7, 0, 8, true);
		
		// pure value text
		readIntegerCase("0", 0, 0, 1, false);
		readIntegerCase("398470", 0, 398470, 6, false);
		readIntegerCase("-398470", 0, -398470, 7, false);
		readIntegerCase("399827293878470", 0, 399827293878470L, 15, false);
		readIntegerCase("-399827293878470", 0, -399827293878470L, 16, false);

		// mixed text
		readIntegerCase("0-kandel", 0, 0, 1, false);
		readIntegerCase("0-kandel", 1, 0, 2, true);
		readIntegerCase("-62782-kandel", 0, -62782, 6, false);
		readIntegerCase("62782-kandel", 0, 62782, 5, false);
		readIntegerCase("-62782-kandel", 6, -62782, 6, true);
		readIntegerCase("-62782-kandel", 13, 0, 13, true);
		readIntegerCase("cremona-0", 7, 0, 9, false);
		readIntegerCase("cremona-33799743", 7, -33799743, 16, false);
		readIntegerCase("cremona-33799743", 8, 33799743, 16, false);
		
		readIntegerCase("cremona-33799743-kandel55", 8, 33799743, 16, false);
	}
	
	private void readVersionSequenceCase (String text, int pos, int[] rval, boolean exception) {
		// report
		long zet = (rval[3] & 0x3FF) | (rval[2] << 10) | ((long)rval[1] << 20) | ((long)rval[0] << 30);
		System.out.println("RVS - case: [" + text + "], pos " + pos + " --> " + zet + ", exc=" + exception);
		
		try {
			long res = Util.readVersionExpression(text, pos);
			assertTrue("result invalid, expected " + zet + ", received " + res , res == zet);
			assertFalse("missing exception", exception); 

			// reconstruct values
			int[] rco = new int[4];
			rco[3] = (int) (res & 0x3FFL);
			rco[2] = (int) ((res >>> 10) & 0x3FFL);
			rco[1] = (int) ((res >>> 20) & 0x3FFL);
			rco[0] = (int) ((res >>> 30) & 0x3FFL);
			assertTrue("error in reconstruction", Arrays.equals(rco, rval));
			
		} catch (IllegalArgumentException  e) {
			assertTrue("unexpected exception: " + e, exception);
		}
	}
	
	@Test
	public void read_version_sequence () {
		// nonsense input
		readVersionSequenceCase("", 0, new int[4], true);
		readVersionSequenceCase("jhd112kjh 34jeh8 ad", 0, new int[4], true);
		readVersionSequenceCase("jhd112kjh 34jeh8 ad", 13, new int[4], true);
		
		readVersionSequenceCase("0", 0, new int[4], false);
		readVersionSequenceCase("0.2", 0, new int[] {0,2,0,0}, false);
		readVersionSequenceCase("0.2.0.0", 0, new int[] {0,2,0,0}, false);
		readVersionSequenceCase("1.45.201", 0, new int[] {1,45,201,0}, false);
		readVersionSequenceCase("1.2.3.4", 0, new int[] {1,2,3,4}, false);
		readVersionSequenceCase("-1.-2.-3.-4-", 0, new int[] {1,2,3,4}, false);
		readVersionSequenceCase("-1.--2.-3.-4-", 0, new int[] {1,0,0,0}, false);
		readVersionSequenceCase("999.999.999.999", 0, new int[] {999,999,999,999}, false);
		
		readVersionSequenceCase("7/22/35/4", 0, new int[] {7,22,35,4}, false);
		readVersionSequenceCase("7-22-35.4", 0, new int[] {7,22,35,0}, false);
		
		readVersionSequenceCase("jhd112.7kjh 34jeh8 ad", 12, new int[] {34,0,0,0}, false);
		readVersionSequenceCase("jhd112.7kjh 34jeh8 ad", 3, new int[] {112,7,0,0}, false);
		readVersionSequenceCase("Kandelaber (abra haber 6.13.0) jhd-112 ad", 23, new int[] {6,13,0,0}, false);
		
		
	}
}

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

/* *****************************************************************************
*
* Some parts originally published by
* Jacksum version 1.7.0 - checksum utility in Java
* Copyright (C) 2001-2006 Dipl.-Inf. (FH) Johann Nepomuk Loefflmann,
* All Rights Reserved, http://www.jonelo.de
*
**************************************************************************** */

public class CRC64  
{
   // CRC-64 look-up table
 private static final long[] crc64tab = new long[] {
   0x0110000000000000L, 0x01b0000000000000L, 0x0360000000000000L,
   0x02d0000000000000L, 0x06c0000000000000L, 0x0770000000000000L,
   0x05a0000000000000L, 0x0410000000000000L, 0x0d80000000000000L,
   0x0c30000000000000L, 0x0ee0000000000000L, 0x0f50000000000000L,
   0x0b40000000000000L, 0x0af0000000000000L, 0x0820000000000000L,
   0x0990000000000000L, 0x1b00000000000000L, 0x1ab0000000000000L,
   0x1860000000000000L, 0x19d0000000000000L, 0x1dc0000000000000L,
   0x1c70000000000000L, 0x1ea0000000000000L, 0x1f10000000000000L,
   0x1680000000000000L, 0x1730000000000000L, 0x15e0000000000000L,
   0x1450000000000000L, 0x1040000000000000L, 0x11f0000000000000L,
   0x1320000000000000L, 0x1290000000000000L, 0x3600000000000000L,
   0x37b0000000000000L, 0x3560000000000000L, 0x34d0000000000000L,
   0x30c0000000000000L, 0x3170000000000000L, 0x33a0000000000000L,
   0x3210000000000000L, 0x3b80000000000000L, 0x3a30000000000000L,
   0x38e0000000000000L, 0x3950000000000000L, 0x3d40000000000000L,
   0x3cf0000000000000L, 0x3e20000000000000L, 0x3f90000000000000L,
   0x2d00000000000000L, 0x2cb0000000000000L, 0x2e60000000000000L,
   0x2fd0000000000000L, 0x2bc0000000000000L, 0x2a70000000000000L,
   0x28a0000000000000L, 0x2910000000000000L, 0x2080000000000000L,
   0x2130000000000000L, 0x23e0000000000000L, 0x2250000000000000L,
   0x2640000000000000L, 0x27f0000000000000L, 0x2520000000000000L,
   0x2490000000000000L, 0x6c00000000000000L, 0x6db0000000000000L,
   0x6f60000000000000L, 0x6ed0000000000000L, 0x6ac0000000000000L,
   0x6b70000000000000L, 0x69a0000000000000L, 0x6810000000000000L,
   0x6180000000000000L, 0x6030000000000000L, 0x62e0000000000000L,
   0x6350000000000000L, 0x6740000000000000L, 0x66f0000000000000L,
   0x6420000000000000L, 0x6590000000000000L, 0x7700000000000000L,
   0x76b0000000000000L, 0x7460000000000000L, 0x75d0000000000000L,
   0x71c0000000000000L, 0x7070000000000000L, 0x72a0000000000000L,
   0x7310000000000000L, 0x7a80000000000000L, 0x7b30000000000000L,
   0x79e0000000000000L, 0x7850000000000000L, 0x7c40000000000000L,
   0x7df0000000000000L, 0x7f20000000000000L, 0x7e90000000000000L,
   0x5a00000000000000L, 0x5bb0000000000000L, 0x5960000000000000L,
   0x58d0000000000000L, 0x5cc0000000000000L, 0x5d70000000000000L,
   0x5fa0000000000000L, 0x5e10000000000000L, 0x5780000000000000L,
   0x5630000000000000L, 0x54e0000000000000L, 0x5550000000000000L,
   0x5140000000000000L, 0x50f0000000000000L, 0x5220000000000000L,
   0x5390000000000000L, 0x4100000000000000L, 0x40b0000000000000L,
   0x4260000000000000L, 0x43d0000000000000L, 0x47c0000000000000L,
   0x4670000000000000L, 0x44a0000000000000L, 0x4510000000000000L,
   0x4c80000000000000L, 0x4d30000000000000L, 0x4fe0000000000000L,
   0x4e50000000000000L, 0x4a40000000000000L, 0x4bf0000000000000L,
   0x4920000000000000L, 0x4890000000000000L, 0xd800000000000000L,
   0xd9b0000000000000L, 0xdb60000000000000L, 0xdad0000000000000L,
   0xdec0000000000000L, 0xdf70000000000000L, 0xdda0000000000000L,
   0xdc10000000000000L, 0xd580000000000000L, 0xd430000000000000L,
   0xd6e0000000000000L, 0xd750000000000000L, 0xd340000000000000L,
   0xd2f0000000000000L, 0xd020000000000000L, 0xd190000000000000L,
   0xc300000000000000L, 0xc2b0000000000000L, 0xc060000000000000L,
   0xc1d0000000000000L, 0xc5c0000000000000L, 0xc470000000000000L,
   0xc6a0000000000000L, 0xc710000000000000L, 0xce80000000000000L,
   0xcf30000000000000L, 0xcde0000000000000L, 0xcc50000000000000L,
   0xc840000000000000L, 0xc9f0000000000000L, 0xcb20000000000000L,
   0xca90000000000000L, 0xee00000000000000L, 0xefb0000000000000L,
   0xed60000000000000L, 0xecd0000000000000L, 0xe8c0000000000000L,
   0xe970000000000000L, 0xeba0000000000000L, 0xea10000000000000L,
   0xe380000000000000L, 0xe230000000000000L, 0xe0e0000000000000L,
   0xe150000000000000L, 0xe540000000000000L, 0xe4f0000000000000L,
   0xe620000000000000L, 0xe790000000000000L, 0xf500000000000000L,
   0xf4b0000000000000L, 0xf660000000000000L, 0xf7d0000000000000L,
   0xf3c0000000000000L, 0xf270000000000000L, 0xf0a0000000000000L,
   0xf110000000000000L, 0xf880000000000000L, 0xf930000000000000L,
   0xfbe0000000000000L, 0xfa50000000000000L, 0xfe40000000000000L,
   0xfff0000000000000L, 0xfd20000000000000L, 0xfc90000000000000L,
   0xb400000000000000L, 0xb5b0000000000000L, 0xb760000000000000L,
   0xb6d0000000000000L, 0xb2c0000000000000L, 0xb370000000000000L,
   0xb1a0000000000000L, 0xb010000000000000L, 0xb980000000000000L,
   0xb830000000000000L, 0xbae0000000000000L, 0xbb50000000000000L,
   0xbf40000000000000L, 0xbef0000000000000L, 0xbc20000000000000L,
   0xbd90000000000000L, 0xaf00000000000000L, 0xaeb0000000000000L,
   0xac60000000000000L, 0xadd0000000000000L, 0xa9c0000000000000L,
   0xa870000000000000L, 0xaaa0000000000000L, 0xab10000000000000L,
   0xa280000000000000L, 0xa330000000000000L, 0xa1e0000000000000L,
   0xa050000000000000L, 0xa440000000000000L, 0xa5f0000000000000L,
   0xa720000000000000L, 0xa690000000000000L, 0x8200000000000000L,
   0x83b0000000000000L, 0x8160000000000000L, 0x80d0000000000000L,
   0x84c0000000000000L, 0x8570000000000000L, 0x87a0000000000000L,
   0x8610000000000000L, 0x8f80000000000000L, 0x8e30000000000000L,
   0x8ce0000000000000L, 0x8d50000000000000L, 0x8940000000000000L,
   0x88f0000000000000L, 0x8a20000000000000L, 0x8b90000000000000L,
   0x9900000000000000L, 0x98b0000000000000L, 0x9a60000000000000L,
   0x9bd0000000000000L, 0x9fc0000000000000L, 0x9e70000000000000L,
   0x9ca0000000000000L, 0x9d10000000000000L, 0x9480000000000000L,
   0x9530000000000000L, 0x97e0000000000000L, 0x9650000000000000L,
   0x9240000000000000L, 0x93f0000000000000L, 0x9120000000000000L,
   0x9090000000000000L
 };

 	private long value, length;
 

   public CRC64() {
       value = length = 0;
   }

   public void reset() {
       value = length = 0;
   }

   /** Update one byte.
    * @param b byte
    */
   public void update (byte b) {
       value = (value >>> 8) ^ crc64tab[(((int)value ^ b)) & 0xFF];
       length++;
   }

   /** Update one integer (4 bytes).
    * @param b int 
    */
   public void update(int b) {
       update((byte)((b >>> 24) & 0xFF));
       update((byte)((b >>> 16) & 0xFF));
       update((byte)((b >>> 8) & 0xFF));
       update((byte)(b & 0xFF));
   }

   /** Update one long integer (8 bytes).
    * @param b long
    */
   public void update (long b) {
	   update((int)b);
	   update((int)(b >>> 32));
   }
   
   /**
    * Updates the current checksum with the specified array of bytes.
    * @param bytes the byte array to update the checksum with
    * @param offset the start offset of the data
    * @param length the number of bytes to use for the update
    */
   public void update(byte[] bytes, int offset, int length) {
       for (int i = offset; i < length + offset; i++) {
    	   int index = ((int)value & 0xFF) ^ (bytes[i] & 0xFF);
           value = (value >>> 8) ^ crc64tab[index]; 
       }
   }

   /**
    * Updates the current checksum with the specified array of bytes.
    */
   public void update(byte[] bytes) {
       update(bytes, 0, bytes.length);
   }

//  ******** RETURNS *************
   
   public byte[] getByteArray() {
       long val = getValue();
       return new byte[]
       {(byte)((val>>56)&0xff),
        (byte)((val>>48)&0xff),
        (byte)((val>>40)&0xff),
        (byte)((val>>32)&0xff),
        (byte)((val>>24)&0xff),
        (byte)((val>>16)&0xff),
        (byte)((val>>8)&0xff),
        (byte)(val&0xff)};
   }

   public byte[] getByteArray2() {
       long val = getValue();
       return new byte[]
       {(byte)((val>>56)),
        (byte)((val>>48)),
        (byte)((val>>40)),
        (byte)((val>>32)),
        (byte)((val>>24)),
        (byte)((val>>16)),
        (byte)((val>>8)),
        (byte)(val)};
   }

   /**
    * Returns the value of the checksum.
    *
    * @see #getByteArray()
    */
   public long getValue() {
       return value;
   }

   // @author wolfgang keller 2011
   public String toString() {
       return Util.bytesToHex(getByteArray());
   }

   /**
    * Returns true only if the specified checksum is equal to this object.
    * author wolfgang keller, modified 2011
    */
   public boolean equals(Object anObject) {
       if (anObject != null && anObject instanceof CRC64) {
           return value == ((CRC64)anObject).value;
       }
       return false;
   }

   // @author wolfgang keller 2011
   public int hashCode() {
       return (int) value;
   }

   /**
    * Returns the length of the processed bytes.
    */
   public long getLength() {
       return length;
   }
}

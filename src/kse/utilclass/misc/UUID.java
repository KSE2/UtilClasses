package kse.utilclass.misc;

/*
*  File: UUID.java
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * A naive but fairly effective implementation of a UUID class.
 * <p>"UUID" is a "Universal Unique Identifier". A UUID is meant to occur
 * universally unique, within the limits of computational probability.
 * By convention it is constructed as a 16 byte bit-array. This implementation
 * uses time, random bytes and SHA1 to reach at a qualified unique value.
 * 
 * @author Kevin Preece
 * @author Wolfgang Keller
 */
@XmlRootElement(name = "UUID")
@XmlAccessorType(XmlAccessType.FIELD)
public final class UUID implements Cloneable, Comparable<UUID>, Serializable {
	
    private static final long serialVersionUID = 701182055172594442L;
    
	private final byte []		uidValue	= new byte[16];
	private       int           hashcode;

	/**
	 * Constructs this object as a a new random UUID.
    * 
    * @throws IllegalStateException if creation fails
    *         (should happen only on out of memory) 
	 */
	public UUID() {
      SHA256 sha = new SHA256();
      ByteArrayOutputStream bstream = new ByteArrayOutputStream();
      DataOutputStream stream = new DataOutputStream( bstream );
      try {
   		 long time = System.currentTimeMillis();
//System.out.println( "UUID time value: " + new Date( time ).toGMTString() );      
//System.out.println( "System time value: " + new Date( System.currentTimeMillis() ).toGMTString() );      

         stream.writeLong( time );
         stream.write( Util.randBytes(8) );
         stream.writeLong( Runtime.getRuntime().freeMemory() );
         stream.writeLong( Runtime.getRuntime().totalMemory() );
         
         sha.update( bstream.toByteArray() );
         byte[] result = sha.digest();
         System.arraycopy( result, 0, uidValue, 0, uidValue.length );
         hashcode = Util.arrayHashcode(uidValue);
//System.out.println( "UUID value: " + toString() );      

      } catch ( Exception e ) {        
         e.printStackTrace();
         throw new IllegalStateException("corrupted UUID creation");
      }
	}  // constructor

	/**
	 * Constructs the UUID from a 16 byte array parameter (identical).
	 *   
	 * @param uuid the 16 bytes array to use as the UUID
     * @throws IllegalArgumentException
     * @throws NullPointerException
	 */
	public UUID( byte [] uuid )	{
		Objects.requireNonNull(uuid);
		if ( uuid.length != uidValue.length )
			throw new IllegalArgumentException("illegal length of argument array: " + uuid.length);

        System.arraycopy( uuid, 0, uidValue, 0, uidValue.length );
        hashcode = Util.arrayHashcode(uidValue);
	}  // constructor

    /**
     * Constructs the UUID from a hexadecimal text representation of
     * a 16 byte UUID value.
     *   
     * @param ids 32 char hexadecimal text value of the UUID
     * @throws IllegalArgumentException
     */
    public UUID( String ids ) {
       byte[] uuid = Util.hexToBytes( ids );
       if ( uuid.length != uidValue.length )
           throw new IllegalArgumentException( "illegal UUID string: " + ids );

       System.arraycopy( uuid, 0, uidValue, 0, uidValue.length );
       hashcode = Util.arrayHashcode(uidValue);
    }  // constructor

	/**
	 * Compares this <code>UUID</code> object to another one and determines
     * equality of both. 
	 * 
	 * @param obj a <code>UUID</code> object to compare to
     * @return <b>true</b> if and only if all bytes of the 16 byte UUID value 
     *         are equal
	 */
	@Override
	public boolean equals( Object obj )	{
      if ( obj == null || !(obj instanceof UUID) )
         return false;
      
      return Util.equalArrays( uidValue, ((UUID)obj).uidValue );
	}

	/** A hashcode coherent with <code>equals()</code>.
	 */ 
   @Override
   public int hashCode() {
      return hashcode;
   }

   @Override
   public int compareTo ( UUID obj ) {
       int i = 0;
       while (i < uidValue.length && uidValue[i] == obj.uidValue[i])  i++;
       int result = i == uidValue.length ? 0 : (uidValue[i] - obj.uidValue[i]);
       return result;
   }

   /**
	 * Returns a byte array containing a copy of the 16 byte value
    * of this UUID.
	 * 
	 * @return byte array (length 16)
	 */
	public byte [] getBytes() {
		return uidValue.clone();
	}

    /**
     * Returns a hexadecimal string representation of the 16 byte value
     * of this UUID.
     * 
     * @return String
     */
    public String toHexString () {
       return Util.bytesToHex( uidValue );
    }
    
   /**
    * Makes a deep clone of this UUID object.
    */
   @Override
   public Object clone () {
      try {  
    	  return super.clone();  
      } catch ( CloneNotSupportedException e ) {
         return null;
      }
   }
   
	/**
	 * Converts this UUID into human-readable form.  The string has the format:
	 * {01234567-89ab-cdef-0123-456789abcdef}.
	 * 
	 * @return <code>String</code> representation of this <code>UUID</code>
	 */
	@Override
	public String toString () {
		return toString( uidValue );
	}

	/**
	 * Converts a <code>uuid</code> value into human-readable form.  The resulting
    * string has the format: {01234567-89ab-cdef-0123-456789abcdef}.
	 * 
	 * @param uuid the 16 byte array to convert; must be of length 16! 
	 * @return <code>String</code> representation of the parameter <code>UUID</code>
    *         value
	 */
	public static String toString( byte[] uuid ) {
		if ( uuid.length != 16 )
			throw new IllegalArgumentException();

		StringBuffer sb = new StringBuffer();

		sb.append( Util.bytesToHex(uuid, 0, 4) );
		sb.append( '-' );
		sb.append( Util.bytesToHex(uuid, 4, 2) );
		sb.append( '-' );
		sb.append( Util.bytesToHex(uuid, 6, 2) );
		sb.append( '-' );
		sb.append( Util.bytesToHex(uuid, 8, 2) );
		sb.append( '-' );
		sb.append( Util.bytesToHex(uuid, 10, 6) );
		
		return sb.toString();
	}
}

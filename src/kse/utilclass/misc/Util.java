package kse.utilclass.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.zip.CRC32;

public class Util {

	private static Random random = new Random();

	/** Whether the given set is sorted in ascending order via its iterator and
	 * the given comparator. An empty set is regarded sorted.
	 *  
	 * @param set {@code Set<T>}
	 * @param comp {@code Comparator<? super T>}, null for "natural" ordering
	 * @return boolean true = sorted ascending, false = else
	 */
	public static <T> boolean isSortedSet (Set<T> set, Comparator<? super T> comp) {
		T t = null;
		for (T o : set) {
			if (t != null) {
				int c = comp == null ? ((Comparable<T>)o).compareTo(t) : comp.compare(o, t);
				if (c < 0) {
					return false;
				} 
			}
			t = o;
		}
		return true;
	}

	/** Returns the number of elements in the given iterator.
	 * 
	 * @param it {@code Iterator<?>}
	 * @return int number of elements
	 */
	public static int countIterator (Iterator<?> it) {
		int ct = 0;
		while (it.hasNext()) {
			ct++; 
			it.next();
		}
		return ct;
	}

	/** Returns a String consisting of random character values (lower case)
	 * and of random length ranging 0 <= len <= maxLength.
	 * 
	 * @param maxLength int maximum string length
	 * @return String
	 */
	public static String randomString (int maxLength) {
		int n = random.nextInt(maxLength);
		StringBuffer buf = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			char c = (char) (random.nextInt(26) + 'a');
			buf.append(c);
		}
		return buf.toString();
	}

	/** Returns a random String value which sorts below the given value "a".
	 * 
	 * @param a String reference value
	 * @param maxLength int maximum length of the return value
	 * @return String random value (length 0..maxLength)
	 */
	public static String getRandomValueBelow (String a, int maxLength) {
		String v;
		do {
			v = randomString(maxLength);
		} while (v.compareTo(a) >= 0);
		return v;
	}
	
	/** Returns a random String value which sorts above the given value "a".
	 * 
	 * @param a String reference value
	 * @param maxLength int maximum length of the return value
	 * @return String random value (length 0..maxLength)
	 */
	public static String getRandomValueAbove (String a, int maxLength) {
		String v;
		do {
			v = randomString(maxLength);
		} while (v.compareTo(a) <= 0);
		return v;
	}
	
	public static void requirePositive (int i) {
		if (i < 0)
			throw new IllegalArgumentException("argument is negative");
	}
	
	public static void requirePositive (int i, String argument) {
		if (i < 0) {
			String arg = argument == null ? "argument" : argument;
			throw new IllegalArgumentException(arg.concat(" is negative"));
		}
	}

	/** Returns the value from the given iterator at index position.
	 * 
	 * @param it {@code Iterator<T>}
	 * @param index int counting from 0
	 * @return T or null if iterator has no value
	 * @throws IllegalArgumentException if index is negative
	 */
	public static <T> T getIterValue (Iterator<T> it, int index) {
		requirePositive(index, "index");
		if (!it.hasNext()) {
			return null;
		}
		
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it.next();
	}

	/**
	    * Converts a byte array to a hexadecimal string.  
	    * 
	    * @param b      the array to be converted.
	    * @return A string representation of the byte array.
	    */
	   public static String bytesToHex( byte [] b ) {
	      return bytesToHex( b, 0, b.length );
	   }

	/**
	    * Converts a byte array to a hexadecimal string.  Conversion starts at byte 
	    * <code>offset</code> and continues for <code>length</code> bytes.
	    * 
	    * @param b      the array to be converted.
	    * @param offset the start offset within <code>b</code>.
	    * @param length the number of bytes to convert.
	    * @return A string representation of the byte array.
	    * @throws IllegalArgumentException if offset and length are misplaced
	    */
	   public static String bytesToHex( byte [] b, int offset, int length ) {
	      int top = offset + length;
	      if ( length < 0 || top > b.length )
	         throw new IllegalArgumentException();
	
	      StringBuffer sb = new StringBuffer();
	      for ( int i = offset; i < top; i++ ) {
	         sb.append(byteToHex(b[i]));
	      }
	      String result = sb.toString();
	      return result;
	   }

	/** Returns a two char hexadecimal String representation of a single byte.
	    * 
	    * @param v integer with a byte value (-128 .. 255); other values get truncated
	    * @return an absolute hex value representation (unsigned) of the input
	    */
	   public static String byteToHex ( int v ) {
	      String hstr = Integer.toString( v & 0xff, 16 );
	      return hstr.length() == 1 ? "0".concat(hstr) : hstr;
	   }

	/** Converts a textual hexadecimal integer representation into a corresponding
	 *  byte value array. 
	 * 
	 * @param hex String textual representation of a hex value
	 * @return array of derived value bytes
	 * @throws NumberFormatException if the value is not parsable
	 * @throws NullPointerException
	 */
	public static byte[] hexToBytes ( String hex ) {
	   Objects.requireNonNull(hex);
	   if (hex.length() % 2 != 0) {
		   hex = "0".concat(hex);
	   }
	   
	   ByteArrayOutputStream out = new ByteArrayOutputStream( hex.length() / 2 );
	   int pos = 0;
	   while ( pos < hex.length() ) {
	      int i = Integer.parseInt(hex.substring(pos, pos+2), 16);
	      out.write(i);
	      pos += 2;
	   }
	   return out.toByteArray();
	}

	/**
	 * Returns a hashcode based on the content of the parameter array (instead of 
	 * its address).
	 * 
	 * @param b the byte array to investigate
	 * @return a content based hashcode for the array
	 * @throws NullPointerException
	 */
	public static int arrayHashcode ( byte[] b ) {
	   long lv = 0;
	   int j = 0;
	   for ( int i = 0 ; i < b.length; i++ ) {
	      lv += (b[i] & 0xffL) << j++;
	      if ( j > 23 ) {
	    	  j = 0;
	      }
	   }
	   return (int) lv;
	}

	/** Ensures the existence of the directory specified by the parameter. If the
	    *  directory does not exist, an attempt is performed to create it including all
	    *  necessary parent directories that may be implied by the specification.
	    *  @param dir File specifying the intended directory; if the specified
	    *  path is a relative path, it is made absolute against <code>defaultDir</code>.
	   *   If <code>defaultDir</code> is <b>null</b> the System directory "user.dir" is
	    *  assumed as default.
	    *  
	    *  @return <b>true</b> iff the specified file exists and is a directory
	    *          after this function terminates
	    */
	   public static boolean ensureDirectory ( File dir, File defaultDir ) {
		  Objects.requireNonNull(dir, "dir = null");
	      boolean success = true;
	
	      if ( !dir.isAbsolute() ) {
	         dir = new File(defaultDir, dir.getPath());
	      }
	
	      if ( !dir.isDirectory() ) {
	         success = !dir.isFile() && dir.mkdirs();
	         if ( !success ) {
	             System.err.println("failed while trying to create directory: "+ dir.toString() );
	         }
          }
	      return success;
	   }

	   /** Ensures the existence of the directory which may be part of the path
	    *  specification of parameter <code>file</code>. If the specified  file is a
	    *  relative path, it is made absolute against <code>defaultDir</code>. If
	    *  <code>defaultDir</code> is <b>null</b> the System property "user.dir" is
	    *  assumed as default directory.
	    *  
	    *  @return <b>true</b> iff the parent directory of the specified file
	    *          exists after this function terminates
	    */
	   public static boolean ensureFilePath ( File file, File defaultDir ) {
	      File parent = file.getParentFile();
	      if (parent != null) {
	         return ensureDirectory(parent, defaultDir);
	      }
	      return true;
	   }

	   /** Whether two byte arrays have equal contents.
	    * 
	    * @param a first byte array to compare
	    * @param b second byte array to compare
	    * @return <b>true</b> if and only if a) a and b have the same length, and 
	    *          b) for all indices i for 0 to length holds a[i] == b[i]
	    */
	   public static boolean equalArrays ( byte[] a, byte[] b ) {
	      if (a.length != b.length) return false;
	      for (int i = 0; i < a.length; i++) {
	         if (a[i] != b[i]) return false;
	      }
	      return true;
	   }

	   /**
	    * Returns the next random integer value in the range 0 .. <code>range</code>-1.
	    * 
	    * @param range a positive integer greater 0
	    * @return random value in the range 0 .. <code>range</code>-1
	    */
	   public static int nextRand ( int range ) {
	      if (range < 1)
	         throw new IllegalArgumentException("range must be positive greater zero");
	      
	      return random.nextInt( range );
	   }

	   /**
	    * Returns a byte array of the specified length filled with random values. 
	    * 
	    * @param length length of the returned byte array in bytes
	    * @return random byte array
	    */
	   public static byte[] randBytes ( int length ) {
	      byte[] buf = new byte[ length ];
	      random.nextBytes( buf );
	      return buf;
	   }

	   /**
	    * Returns a random String of the specified length. The characters will be
	    * in the range (char)30 .. (char)137.
	    */
	   public static String randString ( int length ) {
	      StringBuffer sb = new StringBuffer( length );
	      for (int i = 0; i < length; i++) {
	         sb.append((char)(nextRand(108) + 30));
	      }
	      return sb.toString();
	   }

	/** Returns a SHA-256 fingerprint value of the parameter byte buffer.
	 * 
	 * @param buffer data to digest
	 * @return SHA256 digest
	 */
	public static byte[] fingerPrint ( byte[] buffer ) {
	   Objects.requireNonNull(buffer, "buffer is null");
	   SHA256 sha = new SHA256();
	   sha.update( buffer );
	   return sha.digest();
	}

	/**
	 * Writes a 32-bit integer value to a byte array as
	 * 4 sequential bytes in a Big-Endian manner 
	 * (most significant stored first).
	 *  
	 * @param v int, the value to be written
	 * @param dest the destination byte array
	 * @param offs the start offset in <code>dest</code>
	 */
	public static void writeInt ( int v, byte[] dest, int offs ) {
	   dest[ offs ]     = (byte)(  (v >>> 24) );
	   dest[ offs + 1 ] = (byte)(  (v >>> 16) );
	   dest[ offs + 2 ] = (byte)(  (v >>>  8) );
	   dest[ offs + 3 ] = (byte)(  v );
	}

	/**
	 * Writes a 32-bit integer value to a byte array as
	 * 4 sequential bytes in a Little-Endian manner 
	 * (least significant stored first).
	 *  
	 * @param v int, the value to be written
	 * @param dest the destination byte array
	 * @param offs the start offset in <code>dest</code>
	 */
	public static void writeIntLittle ( int v, byte[] dest, int offs ) {
	   dest[ offs ]     = (byte)(  v );
	   dest[ offs + 1 ] = (byte)(  (v >>>  8) );
	   dest[ offs + 2 ] = (byte)(  (v >>> 16) );
	   dest[ offs + 3 ] = (byte)(  (v >>> 24) );
	}

	/**
	 * Writes a long integer value to a byte array as 8 sequential bytes in a 
	 * Big-Endian manner (Java-standard).
	 *  
	 * @param v long, the value to be written
	 * @param dest the destination byte array
	 * @param offs the start offset in <code>dest</code>
	 */
	public static void writeLong ( long v, byte[] dest, int offs ) {
	   dest[ offs ]     = (byte)( (v >>> 56) );
	   dest[ offs + 1 ] = (byte)( (v >>> 48) );
	   dest[ offs + 2 ] = (byte)( (v >>> 40) );
	   dest[ offs + 3 ] = (byte)( (v >>> 32) );
	   dest[ offs + 4 ] = (byte)( (v >>> 24) );
	   dest[ offs + 5 ] = (byte)( (v >>> 16) );
	   dest[ offs + 6 ] = (byte)( (v >>>  8) );
	   dest[ offs + 7 ] = (byte)( (v >>>  0) );
	}

	/**
	 * Writes a 64-bit integer value to a byte array as 
	 * 8 sequential bytes in a Little-Endian manner 
	 * (least significant stored first).
	 *  
	 * @param v long, the value to be written
	 * @param dest the destination byte array
	 * @param offs the start offset in <code>dest</code>
	 */
	public static void writeLongLittle ( long v, byte[] dest, int offs ) {
	   dest[ offs ]     = (byte)(  v );
	   dest[ offs + 1 ] = (byte)(  (v >>>  8) );
	   dest[ offs + 2 ] = (byte)(  (v >>> 16) );
	   dest[ offs + 3 ] = (byte)(  (v >>> 24) );
	   dest[ offs + 4 ] = (byte)(  (v >>> 32) );
	   dest[ offs + 5 ] = (byte)(  (v >>> 40) );
	   dest[ offs + 6 ] = (byte)(  (v >>> 48) );
	   dest[ offs + 7 ] = (byte)(  (v >>> 56) );
	}

	/**
	 * Returns a byte array of same length as the input buffers where the
	 * result has XORed each ordinal position in both arrays (a XOR b).
	 *  
	 * @param a input byte array (same length as b)
	 * @param b input byte array (same length as a)
	 * @return XOR-ed a and b
	 * @throws IllegalArgumentException if a and b have differing length
	 */
	public static final byte[] XOR_buffers ( byte[] a, byte[] b ) {
	   if ( a.length != b.length )
	      throw new IllegalArgumentException( "buffer a,b length must be equal" );
	   
	   int len = a.length;
	   byte[] res = new byte[ len ];
	   for ( int i = 0; i < len; i++ ) {
	      res[i] = (byte) (a[i] ^ b[i]);
	   }
	   return res;
	}

	/**
	 * Modifies parameter a with (a XOR b).
	 *  
	 * @param a input byte array (same length as b)
	 * @param b input byte array (same length as a)
	 * @throws IllegalArgumentException if a and b have differing length
	 */
	public static final void XOR_buffers2 ( byte[] a, byte[] b ) {
	   if ( a.length != b.length )
	      throw new IllegalArgumentException( "buffer a,b length must be equal" );
	   
	   int len = a.length;
	   for ( int i = 0; i < len; i++ ) 
	      a[i] = (byte) (a[i] ^ b[i]);
	}

	/**
	 * Reads a 4-byte integer value from a byte array as 4 sequential bytes in a 
	 * Big-Endian manner (Java-standard).
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return int integer as read from the byte sequence
	 */
	public static int readInt ( byte[] b, int offs )
	{
	   return
	   (((int)b[ offs + 0 ] & 0xff) <<  24) |
	   (((int)b[ offs + 1 ] & 0xff) <<  16) |
	   (((int)b[ offs + 2 ] & 0xff) <<   8) |
	   (((int)b[ offs + 3 ] & 0xff) <<   0);
	}

	/**
	 * Reads a integer value from a byte array as 4 sequential bytes in a 
	 * Little-Endian manner (least significant stored first).
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return int integer as read from the byte sequence
	 */
	public static int readIntLittle ( byte[] b, int offs )
	{
	   return
	   ((int)b[ offs ] & 0xff) | 
	   (((int)b[ offs + 1 ] & 0xff) <<  8) |
	   (((int)b[ offs + 2 ] & 0xff) <<  16) |
	   (((int)b[ offs + 3 ] & 0xff) <<  24);
	}

	/**
	 * Reads a long integer value from a byte array as 8 sequential bytes in a 
	 * Big-Endian manner (Java-standard).
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return long integer as read from the byte sequence
	 */
	public static long readLong ( byte[] b, int offs )
	{
	   return
	   (((long)b[ offs + 0 ] & 0xff) <<  56) |
	   (((long)b[ offs + 1 ] & 0xff) <<  48) |
	   (((long)b[ offs + 2 ] & 0xff) <<  40) |
	   (((long)b[ offs + 3 ] & 0xff) <<  32) |
	   (((long)b[ offs + 4 ] & 0xff) <<  24) |
	   (((long)b[ offs + 5 ] & 0xff) <<  16) |
	   (((long)b[ offs + 6 ] & 0xff) <<   8) |
	   (((long)b[ offs + 7 ] & 0xff) <<   0);
	}

	/**
	 * Reads a long (signed) integer value from a byte array as
	 * 8 sequential bytes in a Little-Endian manner 
	 * (least significant stored first).
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return long integer as read from the byte sequence
	 */
	public static long readLongLittle ( byte[] b, int offs )
	{
	   return
	   ((long)b[ offs ] & 0xff) | 
	   (((long)b[ offs + 1 ] & 0xff) <<  8) |
	   (((long)b[ offs + 2 ] & 0xff) <<  16) |
	   (((long)b[ offs + 3 ] & 0xff) <<  24) |
	   (((long)b[ offs + 4 ] & 0xff) <<  32) |
	   (((long)b[ offs + 5 ] & 0xff) <<  40) |
	   (((long)b[ offs + 6 ] & 0xff) <<  48) |
	   (((long)b[ offs + 7 ] & 0xff) <<  56);
	}

	/**
	 * Reads an unsigned 4-byte integer value from a byte array in a 
	 * Little-Endian manner (least significant stored first). The
	 * returned value is a long integer. 
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return long unsigned 32-bit integer as read from the byte sequence
	 */
	public static long readUIntLittle ( byte[] b, int offs )
	{
	   return readIntLittle( b, offs ) & 0xFFFFFFFFL; 
	}

	/** Returns a copy of the parameter byte array of the same length. 
	 * 
	 * @param b data source
	 * @return array copy
	 */
	public static byte[] arraycopy ( byte[] b )	{
	   return arraycopy( b, 0, b.length );
	}

	/** Returns a copy of the parameter byte array of the given length. 
	 *  The result will be identical, a shortage or a prolongation of the parameter 
	 *  value, depending on the length setting.
	 * 
	 * @param b data source
	 * @param length length in b
	 * @return array segment within b from start offset 0
	 */
	public static byte[] arraycopy ( byte[] b, int length )	{
	   return arraycopy( b, 0, length );
	}

	/** Returns a copy of the parameter byte array of the given length. 
	 *  The result will be identical, a shortage or a prolongation of the parameter 
	 *  value, depending on the length setting.
	 * 
	 * @param b data source
	 * @param start offset in b
	 * @param length length in b
	 * @return array segment of b
	 */
	public static byte[] arraycopy ( byte[] b, int start, int length ) {
	   byte[] copy = new byte[ length ];
	   System.arraycopy( b, start, copy, 0, Math.min( length, b.length-start ));
	   return copy;
	}

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached, using the given data buffer.
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (non-null)
	 * @param buffer transfer buffer
	 * @throws java.io.IOException
	 */
	public static void transferData ( InputStream input, OutputStream output,
	      byte[] buffer ) throws java.io.IOException {
	   Objects.requireNonNull(buffer, "transfer buffer is null");
	   int len;
	
	//   Log.log( 10, "(Util) data transfer start" ); 
	   while ( (len = input.read( buffer )) > 0 ) {
	      output.write( buffer, 0, len );
	   }
	//   Log.log( 10, "(Util) data transfer end" );
	}

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached.
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (non-null)
	 * @param bufferSize the size of the transfer buffer
	 * @throws java.io.IOException
	 */
	public static void transferData ( InputStream input, OutputStream output,
	      int bufferSize  ) throws java.io.IOException {
	   byte[] buffer = new byte[ bufferSize ];
	   transferData(input, output, buffer);
	}

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached. This function version returns 
	 * a CRC32 value over the entire data stream transferred.
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (if null, a valid CRC value is still created)
	 * @param bufferSize the size of the transfer buffer
	 * @return int CRC value of the data stream read
	 * @throws java.io.IOException
	 */
	public static int transferData2 ( InputStream input, OutputStream output,
	      int bufferSize  ) throws java.io.IOException {
	   CRC32 crc = new CRC32();
	   byte[] buffer = new byte[ bufferSize ];
	   int len;
	
	//   Log.log( 10, "(Util) data transfer start" ); 
	   while ( (len = input.read( buffer )) > 0 ) {
	      if ( output != null ) {
	         output.write( buffer, 0, len );
	      }
	      crc.update( buffer, 0, len );
	   }
	//   Log.log( 10, "(Util) data transfer end" );
	   return (int)crc.getValue();
	}  // transferData2

}


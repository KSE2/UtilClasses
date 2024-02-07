package kse.utilclass.misc;

/*
*  File: Util.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2023 by Wolfgang Keller, Munich, Germany
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.GZIPOutputStream;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import kse.utilclass.dialog.GUIService;

public class Util {
	
	/** Milliseconds of a day */
	public static final long TM_DAY = 24 * 60 * 60 * 1000;
	/** Milliseconds of one hour */
	public static final long TM_HOUR = 60 * 60 * 1000;
	/** Milliseconds of one minute */
	public static final long TM_MINUTE = 60 * 1000;
	/** Milliseconds of one second */
	public static final long TM_SECOND= 1000;
	/** A String array of size zero. */
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	public static final long KILO = 1000;
	public static final long MEGA = 1000000;
	public static final long GIGA = 1000000000;
	public static final long TERA = 1000000000000L;
	public static final int DEFAULT_BUFFER_SIZE = 4096*2;
	
	private static Random random = new Random();
	private static boolean isWindows;

	private static final BitSet URI_UNRESERVED_CHARS;
	
	static {
		BitSet bs  = new BitSet(128);
		bs.set('A', 'Z');
		bs.set('a', 'z');
		bs.set('0', '9');
		bs.set('-');
		bs.set('.');
		bs.set('_');
		bs.set('~');
		URI_UNRESERVED_CHARS = bs;
        isWindows = System.getProperty("os.name","").toLowerCase().indexOf("windows") > -1;
	}
	
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
				@SuppressWarnings("unchecked")
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

	/** Returns a copy of the given text where any double occurrence of ' '
	 * is replaced by a single occurrence.
	 * 
	 * @param s String text
	 * @return String condensed text (blank reduction)
	 */
	public static String condensedStr (String s) {
		int i;
		while ((i=s.indexOf("  ")) > -1) {
			s = s.substring(0, i) + s.substring(i+1);
		}
		return s;
	}
	
	/** Returns a String consisting of random character values (lower case)
	 * and of random length ranging {@code 0 <= len <= maxLength}.
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
	
	/** Returns a translation of the given character array into a byte array
	 * where each char is written out in two bytes (most significant first).
	 * 
	 * @param ca char[], may be null
	 * @return byte[] or null if the argument was null
	 */
	public static byte[] charToBytes (char[] ca) {
		if (ca == null) return null;
		byte[] ba = new byte[ca.length*2];
		for (int i = 0; i < ca.length; i++) {
			ba[i*2] = (byte) ((ca[i] >> 8) & 0xFF);
			ba[i*2+1] = (byte) (ca[i] & 0xFF);
		}
		return ba;
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
	
	/** Throws an IllegalArgumentException if the integer argument is
	 * negative.
	 * 
	 * @param i int integer to test
	 * @throws IllegalArgumentException
	 */
	public static void requirePositive (int i) {
		if (i < 0)
			throw new IllegalArgumentException("int argument is negative: " + i);
	}
	
	/** Throws an IllegalArgumentException if the integer argument is
	 * negative.
	 * 
	 * @param i long integer to test
	 * @throws IllegalArgumentException
	 */
	public static void requirePositive (long i) {
		if (i < 0)
			throw new IllegalArgumentException("long argument is negative: " + i);
	}
	
	/** Throws an IllegalArgumentException if the integer argument is
	 * negative.
	 * 
	 * @param i int integer to test
	 * @param argument String name of the integer variable 
	 *        (element of exception message)
	 * @throws IllegalArgumentException
	 */
	public static void requirePositive (int i, String argument) {
		if (i < 0) {
			String arg = argument == null ? "argument" : argument;
			throw new IllegalArgumentException(arg + " is negative: " + i);
		}
	}

	/** Throws an IllegalArgumentException if the integer argument is
	 * negative.
	 * 
	 * @param i long integer to test
	 * @param argument String name of the integer variable 
	 *        (element of exception message)
	 * @throws IllegalArgumentException
	 */
	public static void requirePositive (long i, String argument) {
		if (i < 0) {
			String arg = argument == null ? "argument" : argument;
			throw new IllegalArgumentException(arg + " is negative: " + i);
		}
	}

	/** Tests the given integer value of element in 1..MAX_INT and throws 
	 * an IllegalArgumentException with the given message if this is not the 
	 * case.
	 * 
	 * @param i int investigated integer
	 * @param message String exception text; may be null for default message
	 * @return int i
	 * @throws IllegalArgumentException
	 */
	public static int requireNPositive(int i, String message) {
		if (i < 1) 
			throw new IllegalArgumentException(message != null ? message : "argument value below 1");
		return i;
	}

	/** Tests the given integer value of element in 1..MAX_INT and throws 
	 * an IllegalArgumentException with a default message if this is not the
	 * case.
	 * 
	 * @param i int investigated integer
	 * @return int i
	 * @throws IllegalArgumentException
	 */
	public static int requireNPositive (int i) {
		if (i < 1) 
			throw new IllegalArgumentException("argument value below 1");
		return i;
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

	   /** Returns a 4 char hexadecimal String representation of a single short integer.
	    * 
	    * @param v integer with a short value; other values get truncated
	    * @return an absolute hex value representation (unsigned) of the input
	    */
	   public static String shortToHex ( int v ) {
	      String hstr;
	      hstr = Integer.toString( v & 0xffff, 16 );
	      return "0000".substring( hstr.length() ) + hstr;
	   }

	   /** Returns a 8 char hexadecimal String representation of a single integer (int).
	    * 
	    * @param v integer with a short value; other values get truncated
	    * @return an absolute hex value representation (unsigned) of the input
	    */
	   public static String intToHex ( long v ) {
	      String hstr;
	      hstr = Long.toString( v & 0xffffffffL, 16 );
	      return "00000000".substring( hstr.length() ) + hstr;
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

	/** Returns the rear part of the given string, comprising the given length.
	 * If the given string is smaller than the given length, the argument itself 
	 * is returned.
	 * 
	 * @param s String
	 * @param len int substring length
	 * @return String
	 */
	public static String tailStr (String s, int len) {
		requirePositive(len);
		int start = s.length()-len;
		String res = start < 0 ? s : s.substring(start, len);
		return res;
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
	    *  directory does not exist, an attempt is made to create it including all
	    *  hierarchy directories that may be part of the specification.
	    *  
	    *  @param dir File directory to ensure; if dir is a relative path, 
	    *             it is made absolute against <code>defaultDir</code>
	    *  @param defaultDir File default parent directory; if <b>null</b> the 
	    *             system directory "user.dir" is assumed 
	    *  @return <b>true</b> iff the specified file exists and is a directory
	    *          after this function terminates
	    */
	   public static boolean ensureDirectory ( File dir, File defaultDir ) {
		  Objects.requireNonNull(dir, "dir = null");
	      boolean success = true;
	
	      if ( !dir.isAbsolute() ) {
	    	 if (defaultDir == null) {
	    		 defaultDir = new File(System.getProperty("user.dir"));
	    	 }
	         dir = new File(defaultDir, dir.getPath());
	      }
	
	      if ( !dir.isDirectory() ) {
	         success = !dir.isFile() && dir.mkdirs();
	         if ( !success ) {
	             System.err.println("* failed while trying to create directory: "+ dir.toString() );
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
	    * @param file File 
	    * @param defaultDir File 
	    * @return <b>true</b> iff the parent directory of the specified file
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

	   /** Whether two character arrays have equal contents.
	    * 
	    * @param a first char array to compare
	    * @param b second char array to compare
	    * @return <b>true</b> if and only if a) a and b have the same length, and 
	    *          b) for all indices i for 0 to length holds a[i] == b[i]
	    */
	   public static boolean equalArrays ( char[] a, char[] b ) {
	      if ( a.length != b.length ) return false;
	      for ( int i = 0; i < a.length; i++ ) {
	         if ( a[i] != b[i] ) return false;
	      }
	      return true;
	   }

	   /** Whether byte array a is contained in byte array b
	    * at a specified location. Returns true if both arguments are null.
	    * 
	    * @param a byte[] first byte array to compare; may be null
	    * @param b byte[] second byte array to compare; may be null
	    * @param offset int offset in b where to start comparison
	    * @return <b>true</b> if and only if 1) b has a minimum length of a.length + offset,
	    *          and 2) content of a equals b[ offset..offset+a.length ]
	    */
	   public static boolean equalArrays ( byte[] a, byte[] b, int offset ) {
	      if ( a == b ) return true;
	      if ( a == null || b == null || b.length < a.length + offset )
	         return false;
	      
	      for ( int i = 0; i < a.length; i++ ) {
	         if ( a[i] != b[i+offset] )
	            return false;
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
	    * Returns a random String of the specified length. The characters 
	    * will be in the range (char)30 .. (char)137.
	    * 
	    * @param length int target string length
	    * @return String 
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
	 * @param buffer byte[] data to digest
	 * @return byte[] SHA256 digest (32 bytes)
	 */
	public static byte[] fingerPrint ( byte[] buffer ) {
	   Objects.requireNonNull(buffer, "input is null");
	   return sha256(buffer);
	}

	/** Returns a SHA-256 fingerprint value of the parameter byte buffer.
	 * 
	 * @param buffer char[] data to digest
	 * @return byte[] SHA256 digest (32 bytes)
	 */
	public static byte[] fingerPrint ( char[] buffer ) {
		return fingerPrint(charToBytes(buffer));
	}

	/** Returns a SHA-256 fingerprint value of the parameter string buffer.
	 * 
	 * @param buffer <code>String</code> data to digest
	 * @return byte[] SHA256 digest (32 bytes)
	 */
	public static byte[] fingerPrint ( String buffer ) {
		return fingerPrint(buffer == null ? null : buffer.toCharArray());
	}

	/** A 32 bytes fingerprint value calculated from a SHA-256 update of the
	 * given data block.
	 *  
	 * @param data byte[] input
	 * @return byte[] SHA-256 digest
	 */
	public static byte[] sha256 ( byte[] data ) {
	    SHA256 sha = new SHA256();
	    sha.update(data);
	    return sha.digest(); 
	}

	/** A 64 bytes fingerprint value calculated from a SHA-512 update of the
	 * given data block.
	 *  
	 * @param data byte[] input
	 * @return byte[] SHA-512 digest
	 */
	public static byte[] sha512 ( byte[] data ) {
	    SHA512 sha = new SHA512();
	    sha.update(data);
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
	 * Modifies parameter byte array a in-situ with (a XOR b).
	 *  
	 * @param a input byte array (same length as b)
	 * @param b input byte array (same length as a)
	 * @throws IllegalArgumentException if a and b have differing length
	 */
	public static final void XOR_buffers2 ( byte[] a, byte[] b ) {
	   if ( a.length != b.length )
	      throw new IllegalArgumentException( "buffer a,b length must be equal" );
	   
	   int len = a.length;
	   for ( int i = 0; i < len; i++ ) {
	      a[i] = (byte) (a[i] ^ b[i]);
	   }
	}

	/**
	 * Modifies a section of byte array a in-situ with a section of b,
	 * XOR-ing length bytes.
	 *  
	 * @param a input/output byte array
	 * @param startA int offset in a
	 * @param b input byte array
	 * @param startB int offset in b
	 * @param length long 
	 * @throws IllegalArgumentException if any buffer's length is exceeded
	 */
	public static final void XOR_buffers2 ( byte[] a, int startA, byte[] b, int startB, int length ) {
	   requirePositive(length);
	   if ( startA + length > a.length | startB + length > b.length )
	      throw new IllegalArgumentException( "buffer length overflow" );
	   
	   for ( int i = 0; i < length; i++ ) {
	      a[i + startA] = (byte) (a[i + startA] ^ b[i + startB]);
	   }
	}

	/**
	 * Reads a 4-byte integer value from a byte array as 4 sequential bytes in a 
	 * Big-Endian manner (Java-standard).
	 *  
	 * @param b the source byte array
	 * @param offs the start offset in <code>dest</code>
	 * @return int integer as read from the byte sequence
	 */
	public static int readInt ( byte[] b, int offs ) {
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
	public static int readIntLittle ( byte[] b, int offs ) {
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
	public static long readUIntLittle ( byte[] b, int offs ) {
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

	/** Returns a new array of bytes in which the order of bytes is reversed.
	 * 
	 * @param b byte[] source array
	 * @return byte[]
	 */
	public static byte[] reversedArray (byte[] b) {
		byte[] b1 = new byte[b.length];
		for (int i = 0; i < b.length; i++) {
			b1[b.length -i -1] = b[i]; 
		}
		return b1;
	}
	
	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached, using the given data buffer.
	 * Operation stops unfinished when interrupted state of the current thread
	 * is detected. The interrupted state is cleared end an exception is thrown. 
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (non-null)
	 * @param buffer transfer buffer
	 * @return long number of bytes transferred
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static long transferData (InputStream input, OutputStream output,
	      byte[] buffer) throws java.io.IOException, InterruptedException {
	   Objects.requireNonNull(buffer, "transfer buffer is null");
	   long sum = 0;
	   int len;
	   while ((len = input.read(buffer)) > -1) {
       	  if (Thread.interrupted()) {
     		 throw new InterruptedException();
       	  }
	      output.write(buffer, 0, len);
	      sum += len;
	   }
	   return sum;
	}

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached. A buffer is created.
	 * Operation stops unfinished when interrupted state of the current thread
	 * is detected. The interrupted state is cleared end an exception is thrown. 
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (non-null)
	 * @param bufferSize the size of the transfer buffer
	 * @return long number of bytes transferred
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static long transferData (InputStream input, OutputStream output,
	      int bufferSize) throws java.io.IOException, InterruptedException {
	   byte[] buffer = new byte[bufferSize];
	   return transferData(input, output, buffer);
	}

	/** Makes every attempt to remove all files in the given directory, 
	 * optionally including sub-directories. Files set to 'read-only' are
	 * nevertheless included, the argument directory itself does not get 
	 * removed. If the argument is the empty path name or is not a directory
	 * then nothing is done and <b>false</b> returned.
	 * <p>NOTE: Success depends on the settings of access rights for the
	 * given directory and its contents. Only files can be removed for which the
	 * user has access rights.
	 * 
	 * @param dir File directory to be purged
	 * @param includeSubs boolean
	 * @return boolean true == directory is empty, false == unremoved elements
	 * @throws IOException 
	 */
	public static boolean purgeDirectory (File dir, boolean includeSubs) throws IOException {
		if (!dir.isDirectory() || dir.getPath().isEmpty()) return false;
		String dirPath = dir.getCanonicalPath();
		boolean allOk = true;
		System.out.println("-- PURGING directory (subs=" + includeSubs + "): " + dirPath);
		
		File[] farr = dir.listFiles();
		for (File f : farr) {
			String path = f.getCanonicalPath();
			boolean isLink = !path.startsWith(dirPath);
			System.out.println("    -- file (" + (isLink ? "link" : "") + ", "  
					+ (f.isDirectory() ? "dir" : "") + "): " + path);
			
			// don't deal with self 
			if (path.equals(dirPath)) continue;
			
			// deal w/ sub-directory (possibly recurse into)
			if (f.isDirectory()) {
				if (includeSubs) {
					f.setWritable(true);
					if (!isLink) {
						allOk &= purgeDirectory(f, true);
					}
					boolean ok = f.delete();
					allOk &= ok;
					System.out.println("    -- try remove dir (" + ok + "): " + f);
				}
				continue;
			}
			
			// deal w/ "archive" files
			f.setWritable(true);
			boolean ok = f.delete();
			allOk &= ok;
			System.out.println("    -- try remove file (" + ok + "): " + f);
		}

		return allOk;
	}

	/** Returns the TEMP directory as noted in JVM system properties.
	 * @return File
	 * @throws IOException  if canonical file fails
	 */
	public static File getTempDir() throws IOException {
		String path = System.getProperty("java.io.tmpdir");
		return new File(path).getCanonicalFile();
	}

	/** Appends the contents of file <code>top</code> to the end of file <code>
	 *  bottom</code>. <code>top</code> remains unchanged.
	 * 
	 * @param bottom File source (lower part) and target file
	 * @param top File upper part
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void concatFiles ( File bottom, File top ) 
				throws IOException, InterruptedException {
	   FileOutputStream out = new FileOutputStream( bottom, true );
	   FileInputStream in = new FileInputStream( top );
	   try {
		   transferData( in, out, 4*2048 );
	   } finally {
		   in.close();
		   out.close();
	   }
	}

	   /** A security file copy method. 
	    * Copies the contents of any disk file to a specified output file.  If 
	    * the output file is a relative path, it is made absolute against the 
	    * directory of the input file. The parent path of the output file is
	    * created if it does not exist. The output file will be overwritten if
	    * it exist. Function reports errors to <code>System.err</code>.
	    * The time mark of the resulting file is the operation time.
	    * Operation stops unfinished when interrupted state of the current thread
	    * is detected. The interrupted state is cleared and an 
	    * InterruptedException thrown. 
	    * <p>A CRC32 check is performed to compare source and copy after the 
	    * transfer process and if results negative a 
	    * {@code StreamCorruptedException} is thrown.
	    * <p>What is more, an intermediate temporary file is created into which
	    * the source is copied before it is renamed to the target. This ensures
	    * only completed and tested data transfers will be shown by the target
	    * file name. 
	    *  
	    * @param source File source File object
	    * @param target File target File object
	    * @throws StreamCorruptedException if CRC check failed on the copy
	    * @throws IOException if the function could not be completed
	    *         because of an IO error
	    * @throws InterruptedException if the calling thread was interrupted 
	    *         while copying was unfinished
	    */
	   public static void copyFile (File source, File target)
                                throws java.io.IOException, InterruptedException {
		   copyFile(source, target, false);
	   }
	   
	   /** A security file copy method. 
	    * Copies the contents of any disk file to a specified output file.  If 
	    * the output file is a relative path, it is made absolute against the 
	    * directory of the input file. The parent path of the output file is
	    * created if it does not exist. The output file will be overwritten if
	    * it exist. Function reports errors to <code>System.err</code>.
	    * The time mark of the resulting file can optionally be the operation 
	    * time or the original file's time.
	    * Operation stops unfinished when interrupted state of the current thread
	    * is detected. The interrupted state is cleared and an 
	    * InterruptedException thrown. 
	    * <p>A CRC32 check is performed to compare source and copy after the 
	    * transfer process and if results negative a 
	    * {@code StreamCorruptedException} is thrown.
	    * <p>What is more, an intermediate temporary file is created into which
	    * the source is copied before it is renamed to the target. This ensures
	    * only completed and tested data transfers will be shown by the target
	    * file name. 
	    *  
	    * @param source File source File object
	    * @param target File target File object
	    * @param carryTime boolean if true the "last modified" time is set to 
	    *        source value otherwise assumes operation time
	    * @throws StreamCorruptedException if CRC check failed on the copy
	    * @throws IOException if the function could not be completed
	    *         because of an IO error
	    * @throws InterruptedException if the calling thread was interrupted 
	    *         while copying was unfinished
	    */
	   public static void copyFile (File source, File target, boolean carryTime)
                                   throws java.io.IOException, InterruptedException {
	      File parent;
	      File copy = null;
	      FileInputStream in = null;
	      FileOutputStream out = null;
	      CRC32 crcSum;
	
	      // control parameter
	      if (source == null || target == null)
	         throw new NullPointerException("an argument is null");
	      if (source.equals(target)) return;
	
	      try {
	         // make output file absolute (if not already)
	         if ( !target.isAbsolute() ) {
		        parent=source.getAbsoluteFile().getParentFile();
	            target = new File(parent, target.getPath());
	         }
	
	         // make sure the directory for the output file exists
	         parent=target.getAbsoluteFile().getParentFile();
	         ensureFilePath(target, parent);
	
			 // create the download file (intermediate copy)
			 copy = File.createTempFile("copy-", ".tmp", parent);
				
	         // create file streams and transfer data
	         out = new FileOutputStream(copy);
	         in = new FileInputStream(source);
	         long time = source.lastModified();
	         int len;
	         byte[] buffer = new byte[4*2048];
	         int writeCrc = transferData2(in, out, buffer);
	         in.close();
	         out.close();
	         if (carryTime) {
	        	 copy.setLastModified( time );
	         }
	         
	         // control copy file CRC
	         in = new FileInputStream(copy);
	         crcSum = new CRC32();
	         while ((len = in.read(buffer)) != -1) {
	        	if (Thread.interrupted()) {
	        		throw new InterruptedException();
	        	}
	            crcSum.update(buffer, 0, len);
	         }
	         in.close();
	         if (writeCrc != (int)crcSum.getValue()) {
	            throw new StreamCorruptedException("bad copy CRC on " + target);
	         }

			 // create the target file (rename completed copy)
			 target.delete();
			 if (!copy.renameTo(target)) {
			 	throw new IOException("rename to target failed: " + target);
			 }

	      } catch (IOException e) {
	         System.err.println(
	            "*** error during file copy: " + target.getAbsolutePath());
	         System.err.println(e);
	         throw e;

	      } finally {
	         if (in != null) in.close();
	         if (out != null) out.close();
	         if (copy != null) copy.delete();
	      }
	   } // copyFile

	   /** A security file copy method. 
	    * Copies the contents of any disk file to a specified output file.  If 
	    * the output file is a relative path, it is made absolute against the 
	    * directory of the input file. The parent path of the output file is
	    * created if it does not exist. The output file will be overwritten if
	    * it exist. Function reports errors to <code>System.err</code>.
	    * The time mark of the resulting file can optionally be the operation 
	    * time or the original file's time.
	    * Operation stops with an IOException when interrupted state of the 
	    * current thread is detected. The interrupted state is cleared, the
	    * IOException contains the cascaded information of InterruptedException.
	    * <p>A CRC32 check is performed to compare source and copy after the 
	    * transfer process and if results negative a 
	    * {@code StreamCorruptedException} is thrown.
	    * <p>What is more, an intermediate temporary file is created into which
	    * the source is copied before it is renamed to the target. This ensures
	    * only completed and tested data transfers will be shown by the target
	    * file name. 
	    *  
	    * @param source File source File object
	    * @param target File target File object
	    * @param carryTime boolean if true the "last modified" time is set to 
	    *        source value otherwise assumes operation time
	    * @throws StreamCorruptedException if CRC check failed on the copy
	    * @throws IOException if the function could not be completed
	    *         because of an IO error
	    */
	   public static void copyFile2 (File source, File target, boolean carryTime)
                                   throws java.io.IOException {
		   try {
			   copyFile(source, target, carryTime);
		   } catch (InterruptedException e) {
			   throw new IOException("interrupted while copying to " + target, e);
		   }
	   }
	   
	   /** A security file copy method. 
	    * Copies the contents of any disk file to a specified output file.  If 
	    * the output file is a relative path, it is made absolute against the 
	    * directory of the input file. The parent path of the output file is
	    * created if it does not exist. The output file will be overwritten if
	    * it exist. Function reports errors to <code>System.err</code>.
	    * The time mark of the resulting file is the operation time.
	    * Operation stops with an IOException when interrupted state of the 
	    * current thread is detected. The interrupted state is cleared, the
	    * IOException contains the cascaded information of InterruptedException.
	    * <p>A CRC32 check is performed to compare source and copy after the 
	    * transfer process and if results negative a 
	    * {@code StreamCorruptedException} is thrown.
	    * <p>What is more, an intermediate temporary file is created into which
	    * the source is copied before it is renamed to the target. This ensures
	    * only completed and tested data transfers will be shown by the target
	    * file name. 
	    *  
	    * @param source File source File object
	    * @param target File target File object
	    * @throws StreamCorruptedException if CRC check failed on the copy
	    * @throws IOException if the function could not be completed
	    *         because of an IO error
	    */
	   public static void copyFile2 (File source, File target) throws java.io.IOException {
		   copyFile2(source, target, false);
	   }
	   
	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached, using the given data buffer. 
	 * This function version returns a CRC32 value of the entire data stream 
	 * transferred.
	 * <p>Operation stops unfinished when interrupted state of the current thread
	 * is detected. The interrupted state is cleared and an InterruptedException
	 * thrown. 
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (if null, a valid CRC value is still created)
	 * @param buffer byte[] the transfer buffer
	 * @return int CRC value of the data stream read
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static int transferData2 ( InputStream input, OutputStream output,
	      byte[] buffer ) throws java.io.IOException, InterruptedException {
	   CRC32 crc = new CRC32();
	   int len;
	
	   while ( (len = input.read( buffer )) > -1) {
       	  if (Thread.interrupted()) {
    		 throw new InterruptedException();
       	  }
	      if (output != null) {
	         output.write(buffer, 0, len);
	      }
	      crc.update(buffer, 0, len);
	   }
	   return (int)crc.getValue();
	}

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached. A buffer is created. 
	 * This function version returns a CRC32 value of the entire data stream 
	 * transferred.
	 * Operation stops unfinished when interrupted state of the current thread
	 * is detected. The interrupted state is cleared and an InterruptedException
	 * thrown. 
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (if null, a valid CRC value is still created)
	 * @param bufferSize the size of the transfer buffer
	 * @return int CRC value of the data stream read
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static int transferData2 (InputStream input, OutputStream output,
	      int bufferSize) throws java.io.IOException, InterruptedException {
	   byte[] buffer = new byte[bufferSize];
	   return transferData2(input, output, buffer);
	}

	/** Renders a string based on <code>text</code> where any occurrence of
	 *  <code>token</code> is replaced by <code>substitute</code>. Replace
	 *  takes place iteratively until not further occurrence exists.
	 *  
	 *  @param text String source text
	 *  @param token String sequence to be replaced
	 *  @param substitute String replacement
	 *  @return String the result of transformation; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 */
	public static String substituteText (String text, String token, String substitute) {
	   int index;
	
	   if ( text == null | token == null | substitute == null || 
	         (index=text.indexOf( token )) < 0 )
	       return text;
	
	   while ( index > -1 ) {
	      text = text.substring( 0, index ) + substitute +
	             text.substring( index+token.length() );
	      index = text.indexOf( token );
	   }
	   return text;
	}

	/** Renders a string based on <code>text</code> where the first occurrence of
	 *  <code>token</code> is replaced by <code>substitute</code>.
	 *  <br>(Returns the original if any of the parameters is <b>null</b> or length or
	 *  <tt>token</tt> is zero.)
	 *  
	 *  @param text String source text
	 *  @param token String sequence to be replaced
	 *  @param substitute String replacement
	 *  @return String the result of substitute; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 */
	public static String substituteTextS (String text, String token, String substitute) {
	   int index;
	
	   if ( text == null | token == null | substitute == null || 
	        token.length() == 0 || (index=text.indexOf( token )) < 0 )
	      return text;
	
	   if ( index > -1 ) {
	      text = text.substring( 0, index ) + substitute +
	             text.substring( index+token.length() );
	   }
	   return text;
	}

	/** Reads the contents of the given file and returns them as a byte array.
	 * 
	 * @param file File file to read
	 * @return byte[] file content or null if argument is null
	 * @throws IOException
	 */
	public static byte[] readFile (File file) throws IOException {
		if (file == null) return null;
		RandomAccessFile f = new RandomAccessFile(file, "r");
		byte[] buffer = new byte[(int) f.length()];
		f.readFully(buffer);
		f.close();
		return buffer;
	}

	/** Creates a new buffer of the given length and reads from the given data-file
	 * at the given position. An exception is thrown if there is not enough data
	 * in the file to fill the requested length.
	 * 
	 * @param file File 
	 * @param pos long data position
	 * @param length int data length
	 * @return byte[] data buffer
	 * @throws EOFException
	 * @throws IOException
	 */
	public static byte[] readFileSpace (File file, long pos, int length) throws IOException {
		byte[] buffer = new byte[length];
		RandomAccessFile f = new RandomAccessFile(file, "r");
		try {
			f.seek(pos);
			f.readFully(buffer);
		} finally {
			f.close();
		}
		return buffer;
	}
	   
	/** Reads the contents of the given file and returns them as a text string.
	 * 
	 * @param file File file to read
	 * @param encoding String the text encoding; null for JVM default
	 * @return String content text or null if file is null
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	public static String readTextFile (File file, String encoding) throws IOException {
		if (file == null) return null;
		if (encoding == null) {
			encoding = System.getProperty("file.encoding");
		}
		
		byte[] buf = readFile(file);
		String text = new String(buf, encoding);
		return text;
	}
	
	/** Writes the given byte array to the given file in overwrite modus.
	 * 
	 * @param f File output file
	 * @param data byte[] data to write; may be null
	 * @throws IOException
	 */
	public static void writeFile (File f, byte[] data) throws IOException {
		Objects.requireNonNull(f, "file is null");
		FileOutputStream out = new FileOutputStream(f);

		try {
			if (data != null) {
				out.write(data);
			}
		} finally {
			out.close();
		}
	}
	
	/** Tests whether the given directory can be accessed by the calling thread
	 * in order to create a new file. Returns false if the argument is null or
	 * not a directory.
	 * 
	 * @param dir File directory, may be null
	 * @return boolean true = can write to dir, false = unable to write
	 */
    public static boolean testCanWrite (File dir) {
    	if (dir != null && dir.isDirectory()) {
    		try {
				File f = File.createTempFile("test-", ".file", dir);
				f.delete();
				return true;
			} catch (IOException | SecurityException e) {
			}
    	}
    	return false;
    }

	/** Writes a given String text to a given file using the given character
	 * encoding. An existing file will be overwritten.
	 *  
	 * @param f File output file
	 * @param text String text to write; may be null
	 * @param encoding String character encoding or null for default encoding
	 * @throws IOException
	 */
	public static void writeTextFile (File f, String text, String encoding) throws IOException {
		Objects.requireNonNull(f, "file is null");
		if (encoding == null) {
			encoding = System.getProperty("file.encoding", "UTF-8");
		}
		FileOutputStream out = new FileOutputStream(f);
		OutputStreamWriter writer = new OutputStreamWriter(out, encoding);

		try {
			if (text != null) {
				writer.write(text);
			}
		} finally {
			writer.close();
		}
	}
	
	/** Attempts to read an integer value from the current position of the given
	 * character buffer. Moves the buffer pointer for the number of digits read.
	 * 
	 * @param buf {@code CharBuffer}
	 * @return long read signed integer
	 * @throws NumberFormatException 
	 */
	public static long readInteger (CharBuffer buf) throws NumberFormatException {
		// detect length and location of the digit sequence
		int start = buf.position();
		int end = start;
		boolean isFirst = true;
		try { 
			char c;
			while (((c = buf.get()) >= '0' && c <= '9') || (isFirst && c == '-')) {
				end++;
				isFirst = false;
			}
		} catch (BufferUnderflowException e) {
		}
		int length = end - start;

		// if nothing readable throw format exception
		if (length == 0) {
			String msg = buf.position() == start ? "remaining length is zero" : "non-digit text";
			buf.position(start);
			throw new NumberFormatException(msg);
		}
		
		// extract digit string and parse long value
		char[] ca = new char[length];
		buf.position(start);
		buf.get(ca);
		long res = Long.parseLong(new String(ca));
		return res;
	}

	/** Reads a version number expression of up to 4 segments, delimited by
	 * one of the characters '.', '-' or '/'. Analysis breaks at the end of the
	 * string or the first character which is not a digit or a valid delimiter.
	 * <p>The return value encodes a four part version number where each part
	 * is related into a 10 bit unsigned integer representation in the return 
	 * value. The part-numbers (1.2.3.4) are situated from right to left in the
	 * return value: 4, 3, 2, 1. This entails that encoded values are comparable
	 * in a meaningful way.
	 * 
	 * @param line String
	 * @param offset int
	 * @return long coded version value (comparable)
	 * @throws NumberFormatException if text position renders no number
	 * @throws IllegalArgumentException if offset is out of range
	 */
	public static long readVersionExpression (String line, int offset) {
		CharBuffer cbuf = CharBuffer.wrap(line.toCharArray());
		cbuf.position(offset);
		long zet = 0;
		int p1, p2, p3, p4;
		
		// read first part of four of a version number sequence (must read)
		p1 = Math.abs((int) Util.readInteger(cbuf));
		p2 = p3 = p4 = 0;
		
		// read another three parts if available (conditional read) 
		try {
			char c = cbuf.get();
			if (c == '.' || c == '-' || c == '/') {
				char delim = c;
				p2 = Math.abs((int) Util.readInteger(cbuf));
				c = cbuf.get();
				if (c == delim) {
					p3 = Math.abs((int) Util.readInteger(cbuf));
					c = cbuf.get();
					if (c == delim) {
						p4 = Math.abs((int) Util.readInteger(cbuf));
					}
				}
			}
		} catch (NumberFormatException | BufferUnderflowException e) {
		}

		// encode parts into resulting value
		zet = ((long)(p1 & 0x3FF) << 30) | ((p2 & 0x3FF) << 20) | ((p3 & 0x3FF) << 10) | (p4 & 0x3FF);
		return zet;
	}

	/** Returns a text expression of the given encoded version value.
	 * 
	 * @param version long package version code
	 * @param delim int separator character, zero for default
	 * @return String text
	 */
	public static String getVersionText (long version, int delim) {
		if (delim == 0) delim = '.';
		int p1 = (int) ((version >>> 30) & 0x3FF);
		int p2 = (int) ((version >>> 20) & 0x3FF);
		int p3 = (int) ((version >>> 10) & 0x3FF);
		int p4 = (int) (version & 0x3FF);
		String tz = "" + (char)delim;
		String text = p1 + tz + p2; 
		if (p3 > 0) text = text + tz + p3;
		if (p4 > 0) text = text + tz + p4;
		return text;
	}
	
	/** Returns the given time value in milliseconds.
	 * 
	 * @param time long value
	 * @param unit {@code TimeUnit}
	 * @return long
	 */
	public static long getMilliTime (long time, TimeUnit unit) {
		long v = 0;
		switch (unit) {
		case DAYS: v = time * TM_DAY; 
			break;
		case HOURS: v = time * TM_HOUR;
			break;
		case MICROSECONDS: v = time / 1000;
			break;
		case MILLISECONDS: v = time;
			break;
		case MINUTES: v = time * TM_MINUTE;
			break;
		case NANOSECONDS: v = time / 1000000;
			break;
		case SECONDS: v = time * TM_SECOND;
			break;
		default:
		}
		return v;
	}

	/** Makes the current thread sleep for the given amount of time or until
	 * it is interrupted. Interruption is not indicated.
	 * 
	 * @param millis long time value in milliseconds
	 */
	public static void sleep (long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
		}
	}
	
	/** Returns the encoded program version value consisting of four consecutive 
	 * version detail numbers.
	 *  
	 * @param v1 int first order number
	 * @param v2 int second order number
	 * @param v3 int thrid order number
	 * @param v4 int fourth order number
	 * @return long coded value
	 */
	public static long programVersionCode (int v1, int v2, int v3, int v4) {
		long zet = ((long)(v1 & 0x3FF) << 30) | ((v2 & 0x3FF) << 20) | ((v3 & 0x3FF) << 10) | (v4 & 0x3FF);
		return zet;
	}

	/** Translates the given text into a HTML-encoded text displayable in HTML 
	 * environments. This version of the method does not prepended the 
	 * {@code '<html>'} tag. 
	 * <p>Substitutes conflicting characters in the text with HTML 
	 * expressions and translates line breaks. 
	 * 
	 * @param text String source text
	 * @return String translated text
	 */
	public static String htmlEncoded ( String text ) {
		return htmlEncoded(text, false);
	}
	
	/** Translates the given text into a HTML-encoded text displayable in HTML 
	 * environments. Optionally the text can be prepended by {@code '<html>'}
	 * if this tag is not yet there. 
	 * <p>Substitutes conflicting characters in the text with HTML 
	 * expressions and translates line breaks.
	 * 
	 * @param text String source text
	 * @param headed boolean if true the resulting text will start with 
	 * 			{@code '<html>'}
	 * @return String translated text
	 */
	public static String htmlEncoded ( String text, boolean headed ) {
      
       if (text == null) return null;
       if (text.startsWith("<html>") || text.startsWith("HTML")) return text;

       // prepend if required
       int len = text.length();
       StringBuffer b = new StringBuffer(len * 3);
       if (headed && !text.startsWith("<html>")) {
    	   b.append("<html>");
       }
       
       for (int i = 0; i < len; i++) {
          char c = text.charAt(i);
          switch ( c ) {
          case '<':  b.append( "&lt;" );
          break;
          case '>':  b.append( "&gt;" );
          break;
          case '&':  b.append( "&amp;" );
          break;
          case '"':  b.append( "&quot;" );
          break;
          case '\n':  b.append( "<br>" );
          break;
          case '\r':  
          break;
          default: b.append( c );
          }
       }
       return b.toString();
	}

	/** Returns a translation of the given text into an URI-encoded version, 
	 * or null iff the argument was null. The translated version contains 
	 * characters w/ code-points outside of the core-set for URI expressions
	 * in an URI-encoded form ("percentage encoding").
	 * <p>The given character-set is used for the encoding of translated 
	 * characters. The default set is UTF-8; common alternatives are 
	 * ISO-8859-1 or ISO-8859-15.
	 * <p>NOTE: Such a function should be used for common text which is 
	 * injected into an URI-phrase; it should not be used for the URI-phrase
	 * in total as this would destroy the URI's syntactical shape.  
	 * 
	 * @param text String source text
	 * @param charset String name of character set to encode invalid
	 *                characters; null for default (UTF-8)
	 * @return String translated text
	 */
	public static String uriEncoded (String text, String charset) {
//		for (int i = 0; i < 128; i++) {
//			System.out.println("   " + i + "  ->  " + (char)i);
//		}
		
		if (text == null) return null;
		if (text.isEmpty()) return text;
		if (charset == null) charset = "UTF-8"; 
		Charset cs = Charset.forName(charset);
		StringBuffer buf = new StringBuffer(text.length());
		char[] ca = new char[1];
		
		// translate
		for (char c : text.toCharArray()) {
			if (!URI_UNRESERVED_CHARS.get(c)) {
				ca[0] = c;
				ByteBuffer bytes = cs.encode(new String(ca));
				while (bytes.hasRemaining()) {
					buf.append('%');
					buf.append(Util.byteToHex(bytes.get()));
				}
			} else {
				buf.append(c);
			}
		}
		return buf.toString();
	}
	
	/** Returns the delta in milliseconds between the given time value and
	 * the current time.
	 * 
	 * @param time long milliseconds
	 * @return milliseconds
	 */
	public static long timeDelta (long time) {
		return System.currentTimeMillis() - time;
	}

	/** Returns a {@code BufferedReader} object created to read the given
	 * input file. 
	 * <p>NOTE: This method creates an open input stream for the given file,
	 * the resulting reader should be closed after use in order to release
	 * the file resource.
	 * 
	 * @param f File text input file
	 * @return {@code BufferedReader} 
	 * @throws FileNotFoundException
	 */
	public static BufferedReader createReader (File f) throws FileNotFoundException {
		FileInputStream in = new FileInputStream(f);
		Reader reader = new InputStreamReader(in);
		BufferedReader rd = new BufferedReader(reader); 
		return rd;
	}

	/** Returns a {@code BufferedWriter} object created to write to the given
	 * output file in the default character encoding. An existing file is
	 * overwritten.
	 * <p>NOTE: This method creates an open output stream for the given file,
	 * the resulting writer should be closed after use in order to release
	 * the file resource.
	 * 
	 * @param f File text output file
	 * @return {@code BufferedWriter} 
	 * @throws FileNotFoundException
	 */
	public static BufferedWriter createWriter (File f) throws FileNotFoundException {
		FileOutputStream out = new FileOutputStream(f);
		Writer writer = new OutputStreamWriter(out);
		BufferedWriter wr = new BufferedWriter(writer); 
		return wr;
	}

    /** Returns the standard time string for the user's (VM) time zone.
    * 
    * @param time long universal time value in epoch milliseconds
    * @return String formated time string (length == 19)
    */
   public static String standardTimeString ( long time ) {
      return standardTimeString( time, TimeZone.getDefault() );
   }

    /** Returns the standard time string for the given epoch time and time zone.
    * 
    * @param time long universal time value in epoch milliseconds
    * @param tz <code>TimeZone</code> for which to interpret the time
    *        or <b>null</b> for the current default time zone 
    * @return String formated time string (length == 19)
    */
   public static String standardTimeString ( long time, TimeZone tz ) {
      GregorianCalendar cal;
      StringBuffer sbuf;
      
      if ( tz == null )
         tz = TimeZone.getDefault();
      
      cal = new GregorianCalendar( tz );
      cal.setTimeInMillis( time );
      sbuf = new StringBuffer(20);
      sbuf.append( number( cal.get( GregorianCalendar.YEAR ), 4 ) );
      sbuf.append( '-' );
      sbuf.append( number( cal.get( GregorianCalendar.MONTH ) + 1, 2 ) );
      sbuf.append( '-' );
      sbuf.append( number( cal.get( GregorianCalendar.DAY_OF_MONTH ), 2 ) );
      sbuf.append( ' ' );
      sbuf.append( number( cal.get( GregorianCalendar.HOUR_OF_DAY ), 2 ) );
      sbuf.append( ':' );
      sbuf.append( number( cal.get( GregorianCalendar.MINUTE ), 2 ) );
      sbuf.append( ':' );
      sbuf.append( number( cal.get( GregorianCalendar.SECOND ), 2 ) );
      return sbuf.toString();
   }  // standardTimeString

	/** Returns a number representation of the parameter integer value.
    *  The minimum length of the number is guaranteed by leading '0'
    *  characters.   
    * 
    * @param v long integer value
    * @param length minimum length of number string 
    * @return String number
    */
   public static String number ( long v, int length ) {
      StringBuffer sbuf;
      String hstr;
      int i, n;
      
      sbuf = new StringBuffer( length );
      hstr = String.valueOf( v );
      n = length - hstr.length();
      for ( i = 0; i < n; i++ ) {
         sbuf.append( '0' );
      }
      sbuf.append(  hstr );
      return sbuf.toString();
   }

	/** Returns a number representation of the parameter integer value
	 *  interpreted to the given radix. Letters appear in upper case.
	 *  The minimum length of the number is guaranteed by leading '0'
	 *  characters.   
	 * 
	 * @param v long integer value
	 * @param length int minimum length of number string
	 * @param radix int radix of the integer  
	 * @return String number
	 */
	public static String number ( long v, int length, int radix ) {
	   StringBuffer sbuf = new StringBuffer( length );
	   String hstr = Long.toString(v, radix).toUpperCase();
	   int n = length - hstr.length();
	   for ( int i = 0; i < n; i++ )
	      sbuf.append( '0' );
	   sbuf.append(  hstr );
	   return sbuf.toString();
	}
	
	/** Returns true if and only if the two parameters are not equal.
	 * (This makes a quick return for null and instance identity, and
	 * probes the "equals()" relation otherwise.)
	 * 
	 * @param v1 Object one 
	 * @param v2 Object two
	 * @return boolean true == the references are not equivalent
	 */
	public static boolean notEqual (Object v1, Object v2) {
	   boolean equal = v1 == v2 || (v1 != null && v1.equals(v2));
	   return !equal;
	}

	/** Returns true if and only if the two parameters are equal.
	 * (This makes a quick return for null and instance identity, and
	 * probes the "equals()" relation otherwise.)
	 * 
	 * @param v1 Object one 
	 * @param v2 Object two
	 * @return boolean true == both references are equivalent
	 */
	public static boolean equal (Object v1, Object v2) {
	   return !notEqual(v1, v2);
	}

	/**
	 * Defines new window bounds (location + dimension) by interpreting the relation
	 * of a window <tt>win</tt> to a screen dimension <tt>screen</tt>. Window locations 
	 * are expressed in coordinates of the screen. Returns <b>null</b> if
	 * window was <b>null</b>.
	 *    
	 * @param screen Dimension the screen dimensions in x and y coordinates;
	 *               <b>null</b> defaults to system screen
	 * @param win Rectangle window size and position in screen coordinates
	 * @param resizable boolean if <b>true</b> the window will get resized if it
	 *        exceeds screen limits
	 * @param clipping boolean if <b>true</b> it is ok if only a part of the window
	 *        is visible on the screen while its top-bar remains functional
	 * @return Rectangle new bounds definition (potentially corrected) of window <tt>win</tt>
	 *         or <b>null</b> if <tt>win</tt> was <b>null</b>
	 */
	public static Rectangle correctedWindowBounds ( Dimension screen, Rectangle win, 
	                                          boolean resizable, boolean clipping )	{
	   Rectangle result;
	   Dimension size;
	   Point loc;
	   int minVisHig, minVisWid, diff, minHeight, minWidth;

	   if ( win == null ) return null;
	   
	   // get system screen if no screen supplied
	   if ( screen == null ) {
	      screen = Toolkit.getDefaultToolkit().getScreenSize();
	   }
	   
	   size = win.getSize();
	   loc = win.getLocation();
	   result = win.getBounds();
	   minVisHig = 50; minVisWid = 75;
	   
	   // correct window size if opted and necessary
	   if ( resizable ) {
	      if ( size.height > screen.height )
	         size.height = screen.height;
	      if ( size.width > screen.width )
	         size.width = screen.width;
	      result.setSize( size );
	   }
	   
	   // correct window location if necessary
	   // correct top window bar
	   minHeight = clipping ? minVisHig : result.height;
	   if ( (diff = screen.height - loc.y - minHeight) < 0 ) {
	      loc.y += diff;
	   }
	   loc.y =  Math.max( loc.y, 0 );

	   minWidth = clipping ? minVisWid : result.width;
	   if ( (diff = screen.width - loc.x - minWidth) < 0 ) {
	      loc.x += diff;
	   }
	   loc.x =  Math.max( loc.x, 0 );
	   
	   result.setLocation( loc );
	   return result;
	}

	/**
	 * Defines new window bounds (location + dimension) by interpreting the relation
	 * of a window <tt>win</tt> to the size of the current system screen (primary display).
	 * Window locations are expressed in coordinates of the screen.
	 *    
	 * @param win Rectangle window size and position in screen coordinates
	 * @param resizable boolean if <b>true</b> the window will get resized if it
	 *        exceeds screen limits
	 * @param clipping boolean if <b>true</b> it is ok if only a part of the window
	 *        is visible on the screen while its top-bar remains functional
	 * @return Rectangle new bounds definition (potentially corrected) of window <tt>win</tt> 
	 */
	public static Rectangle correctedWindowBounds ( 
			Rectangle win, boolean resizable, boolean clipping ) {
	   Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	   screen.height -= 30;
	   return correctedWindowBounds( screen, win, resizable, clipping );
	}

	/**
	 * Returns a technical time string apt for collation purposes
	 * for the user's (VM) time zone.
	 * 
	 * @param time long universal time value in epoch milliseconds
	 * @return String time string (length == 14)
	 */
	public static String technicalTimeString ( long time ) {
	   return technicalTimeString( time, null );
	}

	/**
	 * Returns a technical time string apt for collation purposes.
	 * 
	 * @param time long universal time value in epoch milliseconds
	 * @param tz <code>TimeZone</code> for which to interpret the time
	 *        or <b>null</b> for the current default time zone 
	 * @return String time string (length == 14)
	 */
	public static String technicalTimeString ( long time, TimeZone tz )	{
	   GregorianCalendar cal;
	   StringBuffer sbuf;
	   
	   if ( tz == null ) {
	      tz = TimeZone.getDefault();
	   }
	   
	   cal = new GregorianCalendar( tz );
	   cal.setTimeInMillis( time );
	   sbuf = new StringBuffer(20);
	   sbuf.append( number( cal.get( GregorianCalendar.YEAR ), 4 ) );
	   sbuf.append( number( cal.get( GregorianCalendar.MONTH ) + 1, 2 ) );
	   sbuf.append( number( cal.get( GregorianCalendar.DAY_OF_MONTH ), 2 ) );
	   sbuf.append( number( cal.get( GregorianCalendar.HOUR_OF_DAY ), 2 ) );
	   sbuf.append( number( cal.get( GregorianCalendar.MINUTE ), 2 ) );
	   sbuf.append( number( cal.get( GregorianCalendar.SECOND ), 2 ) );
	   return sbuf.toString();
	}  

	/** Returns <b>true</b> if and only if the parameter string
	 * complies with some formal requirements for an email address.
	 * Note that leading and trailing blanks in the string are ignored.
	 *  
	 * @param hstr String text
	 * @return boolean
	 */
	public static boolean isEmailAddress ( String hstr ) {
	   int p1, p2, p3, len;
	   
	   if ( hstr != null && hstr.length() > 4 ) {
	      hstr = hstr.trim();
	      len = hstr.length();
	      p1 = hstr.indexOf( '@' );
	      p2 = hstr.lastIndexOf( '.' );
	      p3 = hstr.indexOf( ' ' );
	      return len < 255 & p3 == -1 & p1 > 0 & p2 > p1 & p2 < len-1;
	   }
	   return false;
	}

	/**
	 * Returns the human readable time string for the given epoch time, 
	 * expressed in the current VM timezone and locale.
	 *  
	 * @param time long universal time value in epoch milliseconds
	 * @return String formatted time string (variable length)
	 */ 
	@SuppressWarnings("deprecation")
	public static String localeTimeString ( long time )	{
	   return new Date( time ).toLocaleString();
	}

	public static byte[] getTreeExpansionInfo (JTree tree) {
		TreePath root = tree.getPathForRow(0);
		if (root == null) return new byte[0];
		
		BitSet set = new BitSet();
		Enumeration<TreePath> en = tree.getExpandedDescendants(root);
		if (en == null) return new byte[0];

		while (en.hasMoreElements()) {
			TreePath p = en.nextElement();
			int row = tree.getRowForPath(p);
			if (row >= 0) {
				set.set(row);
			}
		}

		byte[] arr = set.toByteArray();
		return arr;
	}
	
	public static void setTreeExpansionFromInfo (JTree tree, byte[] info) {
		if (info == null) return;
		
		Runnable run = new Runnable() {
			@Override
			public void run() {
				BitSet set = BitSet.valueOf(info);
				
				// ensure all nodes are collapsed
				for (int i = tree.getRowCount(); i >= 0; i--) {
					tree.collapseRow(i);
				}
				
				int x = -1;
				while ((x = set.nextSetBit(x+1)) > -1) {
					tree.expandRow(x);
				}
			}
		};
		GUIService.performOnEDT(run);
	}

	/** The number of elements in the set of values of the given characters.
	 *  
	 * @param ca char[]
	 * @return int
	 */
	public static int textVariance (char[] ca) {
	    BitSet set = new BitSet();
	    for (int i = 0; i < ca.length; i++) {
	        set.set( ca[i] );
	    }
	    return set.cardinality();         
    }

	/** The number of elements in the set of values of the given bytes.
	 *  
	 * @param ba byte[]
	 * @return int
	 */
	public static int textVariance (byte[] ba) {
	    BitSet set = new BitSet();
	    for (int i = 0; i < ba.length; i++) {
	        set.set( ba[i] & 0xFF );
	    }
	    return set.cardinality();         
    }

	/** Returns the String defined by a section of the given data buffer
	 * terminated with a zero value. Returns null if no zero was found
	 *
	 * @param buf byte[] data buffer
	 * @param start int starting position in buffer
	 * @param charset String character encoding applied; may be null
	 * @return String or null
	 * @throws UnsupportedEncodingException 
	 */
	public static String getZTermString (byte[] buf, int start, String charset) 
				throws UnsupportedEncodingException {
		Objects.requireNonNull(buf, "buffer is null");
		Util.requirePositive(start);
		if (charset == null) {
			charset = Charset.defaultCharset().name();
		}
		
		// search for the next zero value (delimiter)
		int index = -1;
		for (int i = start; i < buf.length; i++) {
			if (buf[i] == 0) {
				index = i;
				break;
			}
		}

		// create the result iff zero was found
		if (index > -1) {
			String hs = new String(buf, start, index, charset);
			return hs;
		}
		return null;
	}
	
	/** Transforms the given text into a HTML encoded version which is 
	 * broken down into multiple lines on the given column limit.
	 * 
	 * @param text String, may be null
	 * @param limit int max number of columns in a line
	 * @return String HTML text or null if input is null 
	 */
	public static String breakDownText (String text, int limit) {
		if (text == null) return null;
		StringBuffer sbuf = new StringBuffer(text.length() + 100);
		if (!text.toLowerCase().startsWith("<html>")) {
			sbuf.append("<html>");
		}
	
		String pcs[] = text.split(" ");
		int cols = 0;
		for (String hs : pcs) {
			int pclen = hs.length();
			if (cols > 0 && cols + pclen > limit) {
				sbuf.append("<br>");
				cols = 0;
			}
			sbuf.append(hs);
			sbuf.append(" ");
			cols += pclen + 1; 
		}
		return sbuf.toString();
	}
	
	
	/** Removes typical HTML contained text which is out-commented with 
	 * "{@code <!-- .. -->}" brackets and returns the cleared result. The length of
	 * result is equal to or lower than the length of the argument. 
	 * 
	 * @param text String Html text
	 * @return cleared argument text
	 */
	public static String removeHtmlComments (String text) {
		String result = text;
		if (text != null) {
			int index = 0;
			String work = text;
			StringBuffer sbuf = null;
			while (index > -1) {
				index = work.indexOf("<!--");
				if (index > -1) {
					if (sbuf == null) {
						sbuf = new StringBuffer(1024);
					}
					sbuf.append(work.substring(0, index));
					work = work.substring(index+4);
					index = work.indexOf("-->");
					if (index > -1) {
						work = work.substring(index+4);
					}
				}
			}
			if (sbuf != null) {
				sbuf.append(work);
				result = sbuf.toString();
			}
		}
		return result;
	}
	
	/** Returns the CRC32 of the given properties set of entries as a 32-bit
	 *  int value.
	 * 
	 * @param properties {@code Properties}
	 * @return int CRC32 value
	 */
	public static int getPropertiesCrc (Properties properties) {
		final CRC_32 crc = new CRC_32();
		Object[] arr = properties.keySet().toArray();
		Arrays.sort(arr);
		
		for (Object o : arr) {
			Object value = properties.getProperty((String)o);
			crc.updateString((String)value);
		}
		return (int) crc.getValue();
	}

	/** Returns a list of Strings which were gained by splitting the given
	 * text around the given regular expression (delimiting sequence).
	 * Elements are trimmed and empty elements are omitted (not contained in 
	 * the resulting list). If the argument is null an empty list is returned.
	 * 
	 * @param s String input text; may be null
	 * @param expr String regular expression (delimiting char sequence)
	 * @return {@code List<String>}
	 */
	public static List<String> separatedNames (String s, String expr) {
		Objects.requireNonNull(expr, "expression is null");
		List<String> list = new ArrayList<>();
		if (s != null && !s.isEmpty()) {
			String[] arr = s.split(expr);
			for (String hs : arr) {
				hs = hs.trim();
				if (!hs.isEmpty()) {
					list.add(hs);
				}
			}
		}
		return list;
	}

	/** Returns the concatenation of all strings contained in the given 
	 * array in the natural sequence of indices. If the delimiter value is 
	 * defined it is inserted between consecutive elements. Null values are 
	 * ignored, empty strings are valid elements. No trimming is performed on 
	 * the elements or the result.
	 * 
	 * @param str String[]
	 * @param delimiter String delimiter token; may be null
	 * @return String
	 */
	public static String mergeStrings(String[] str, String delimiter) {
		StringBuffer sbuf = new StringBuffer();
		for (String s : str) {
			if (s == null) continue;
			if (delimiter != null && sbuf.length() > 0) {
				sbuf.append(delimiter);
			}
			sbuf.append(s);
		}
		return sbuf.toString();
	}

	/**
	 * Returns a URL object construed from the given file path. This method 
	 * first attempts to interpret <code>filepath</code> as a regular URL 
	 * nominator. If this fails it attempts to see <code>filepath</code> as
	 * a local file path nominator and returns a "file:" protocol URL. Local
	 * filepaths get canonised.
	 *    
	 * @param filepath
	 * @return URL for parameter file path
	 * @throws MalformedURLException if <code>filepath</code> is malformed
	 * @throws IOException if some IO error occurs 
	 */
	public static URL makeFileURL (String filepath) throws IOException {
	   URL url;
	   
	   // first attempt: if filepath is a qualified url nominator
	   try { 
		   url = new URL( filepath ); 
	   } catch ( MalformedURLException e ) {
	      // second attempt: generate URL from assumed local filepath
	      File file = new File( filepath ).getCanonicalFile();
	      String path = file.getAbsolutePath();
	      if ( !path.startsWith("/") ) {
	         path = "/" + path;
	      }
	      url = new URL( "file:" + path );
	   }
	   return url; 
	}

	/**
	 * Transforms a char array into a byte array by sequentially writing characters.
	 * Each char is stored in Little-Endian manner as unsigned short integer value 
	 * in the range 0..65535.
	 * 
	 * @param carr char[] the source char array
	 * @return byte array, the transformed state of the parameter with double length
	 *         of the parameter  
	 */
	public static byte[] getByteArray ( char[] carr ) {
	   byte[] buff;
	   char ch;
	   int i;
	   
	   // transfer content to internal cipher block
	   buff = new byte[ carr.length * 2 ];
	   for ( i = 0; i < carr.length; i++ ) {
	      ch = carr[ i ];
	      buff[ i*2 ] = (byte)ch;
	      buff[ i*2+1 ] = (byte)(ch >>> 8);
	   }
	   return buff;
	}

	/** Returns the MD5-value of the given file. The value is a newly
	 * created by reading the given file.
	 * 
	 * @param f File
	 * @return byte[] 16-byte MD5 value
	 * @throws IOException 
	 * @throws IllegalStateException if MD5 should not be available
	 */
	public static byte[] getMD5Val (File f) throws IOException {
		InputStream input = null;
		try {
			// open input file
			input = new FileInputStream(f);
			
			// create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			
			// read and translate file content
			byte[] buf = new byte[4*2048];
			int r;
			while ((r=input.read(buf)) > -1) {
				digest.update(buf, 0, r);
			}
			
			// return digest result
			byte[] res = digest.digest();
			return res;

		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 class not found", e);
			
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	/** Whether the given object (2nd parameter) is contained in the given
	 * array (1st parameter).
	 * 
	 * @param arr Object[]
	 * @param obj Object
	 * @return boolean true == obj is contained in arr, false obj is not 
	 * contained in arr
	 */
	public static boolean arrayContains (Object[] arr, Object obj) {
		Objects.requireNonNull(arr, "array is null");
		Objects.requireNonNull(obj, "object is null");
		for (Object o : arr) {
			if (o.equals(obj)) return true;
		}
		return false;
	}

	/** Whether the given directory 'd1' is an ancestor directory to
	 * the given directory 'd2'.
	 * 
	 * @param d1 File directory 
	 * @param d2 File directory 
	 * @return boolean true = d1 is ancestor of d2, false = d1 is not an
	 *         ancestor of d2
	 */
	public static boolean isAncestor (File d1, File d2) {
		Objects.requireNonNull(d1, "argument 'ancestor' is null");
		Objects.requireNonNull(d2, "argument 'ofDir' is null");
		File parent = d2;
		while ((parent = parent.getParentFile()) != null) {
			if (d1.equals(parent)) return true;
		}
		return false;
	}

	/** Fills a specified section of the parameter file with a given pattern 
	 * value. This moves the file-pointer to off + length.
	 *
	 * @param f <code>RandomAccessFile</code>
	 * @param off long start offset in file of destined write section; 
	 *        if below 0 the function works from the current file pointer 
	 *        position 
	 * @param length long length of write section; if below 0 the method does 
	 *        nothing
	 * @param pattern byte[] containing a pattern which is applied iteratively
	 * @param cyclus int length of a pattern cycle; if greater 0 then 
	 *        must obey {@code length % cyclus == 0} 
	 * @throws IOException
	 * @throws IllegalArgumentException if there is a mismatch in parameters
	 */   
	public static final void fillFileSpace ( RandomAccessFile f, 
	                                         long off, 
	                                         long length,
	                                         byte[] pattern,
	                                         int cyclus )	throws IOException	{
	   if ( length >= 0 ) {
	      if ( cyclus > 0 ) {
	         if ( length % cyclus > 0 )
	            throw new IllegalArgumentException( "length / cyclus mismatch" );
	         if ( pattern.length % cyclus > 0 )
	            throw new IllegalArgumentException( "pattern / cyclus mismatch" );
	      }
	
	      if ( off >= 0 ) {
	         f.seek( off );
	      }
	
	      while ( length > 0 ) {
	         int len = (int)Math.min( length, pattern.length );
	         f.write( pattern, 0, len );
	         length -= len;
	      }
	   }
	}  // fillFileSpace
	
	/** Fills a specified section of the given byte-channel with a given pattern 
	 * value. This moves the channel's position to off + length.
	 *
	 * @param c <code>SeekableByteChannel</code>
	 * @param off long start offset in file of destined write section; 
	 *        if below 0 the function works from the current file pointer 
	 *        position 
	 * @param length long length of write section; if below 0 the method does 
	 *        nothing
	 * @param pattern byte[] containing a pattern which is applied iteratively
	 * @param cyclus int length of a pattern cycle; if greater 0 then 
	 *        must obey {@code length % cyclus == 0} 
	 * @throws IOException
	 * @throws IllegalArgumentException if there is a mismatch in parameters
	 */   
	public static final void fillFileSpace ( SeekableByteChannel c, 
	                                         long off, 
	                                         long length,
	                                         byte[] pattern,
	                                         int cyclus )	throws IOException	{
	   if (length >= 0) {
	      if (cyclus > 0) {
	         if (length % cyclus > 0)
	            throw new IllegalArgumentException( "length / cyclus mismatch" );
	         if (pattern.length % cyclus > 0)
	            throw new IllegalArgumentException( "pattern / cyclus mismatch" );
	      }
	
	      if ( off >= 0 ) {
	         c.position( off );
	      }
	
	      ByteBuffer bbuf = ByteBuffer.wrap(pattern, 0, pattern.length);
	      while ( length > 0 ) {
	         int len = (int)Math.min( length, pattern.length );
	         bbuf.position(0);
	         bbuf.limit(len);
	         int wlen = c.write(bbuf);
	         if (wlen != len) {
	        	 throw new IOException("(Util.fillFileSpace) incomplete pattern writing, FileChannel");
	         }
	         length -= len;
	      }
	   }
	}  // fillFileSpace

	/** Returns a new byte array which is the concatenation of the given arrays 
	 * a1 and a2. The length of the result is a1.length + a2.length.
	 * 
	 * @param a1 byte[]
	 * @param a2 byte[]
	 * @return byte[]
	 */
	public static byte[] concatArrays (byte[] a1, byte[] a2) {
		byte[] buf = new byte[a1.length + a2.length];
		System.arraycopy(a1, 0, buf, 0, a1.length);
		System.arraycopy(a2, 0, buf, a1.length, a2.length);
		return buf;
	}

	/** Returns a new String array which is the concatenation of the given 
	 * arrays a1 and a2. The length of the result is a1.length + a2.length.
	 * 
	 * @param a1 String[]
	 * @param a2 String[]
	 * @return String[]
	 */
	public static String[] concatArrays (String[] a1, String[] a2) {
		String[] buf = new String[a1.length + a2.length];
		System.arraycopy(a1, 0, buf, 0, a1.length);
		System.arraycopy(a2, 0, buf, a1.length, a2.length);
		return buf;
	}

	public static Object[] concatArrays (Object[] a1, Object[] a2) {
		Object[] buf = new Object[a1.length + a2.length];
		System.arraycopy(a1, 0, buf, 0, a1.length);
		System.arraycopy(a2, 0, buf, a1.length, a2.length);
		return buf;
	}

	/** Whether all object elements of the given array are null.
	 * 
	 * @param arr Object[]
	 * @return boolean true = all positions null, false = some position not null
	 */
	public static boolean isNullArray (Object[] arr) {
		for (Object o : arr) {
			if (o != null) return false;
		}
		return true;
	}
	
	/** Whether the given object is an element of a given array.
	 * 
	 * @param o Object
	 * @param arr array of Object
	 * @return boolean <b>true</b> if and only if one of the elements of <code>arr</code>
	 *         <code>equals()</code> parameter <code>o</code>
	 */
	public static boolean isArrayElement ( Object o, Object[] arr )	{
	   for ( int i = 0; i < arr.length; i++ ) {
	      if ( o.equals( arr[i] ) ) return true;
	   }
	   return false;
	}

	/** Returns a CRC_32 value over the given byte array.
	 * Returns zero if the argument is null.
	 * 
	 * @param data byte[], may be null
	 * @return CRC_32 (Adler) or 0
	 */
	public static int blockCRC (byte[] data) {
		if (data == null) return 0;
		CRC_32 crc = new CRC_32();
		crc.update(data);
		return (int) crc.getValue();
	}

	/** Returns a CRC_32 value over the given byte array.
	 * Returns zero if the argument is null.
	 * 
	 * @param data byte[], may be null
	 * @param start int offset in data
	 * @param length int length to read
	 * @return CRC_32 (Adler) or 0
	 */
	public static int blockCRC (byte[] data, int start, int length) {
		if (data == null) return 0;
		CRC_32 crc = new CRC_32();
		crc.update(data, start, length);
		return (int) crc.getValue();
	}

	public static boolean isWindows () {
	   return isWindows;
	}

	/** Returns the user home directory. This refers to the system properties.
	 * 
	 * @return File
	 * @throws IOException if canonical file fails
	 */
	public static File getUserHomeDir () throws IOException {
		String hs = System.getProperty("user.home");
		if (hs == null) {
			throw new IllegalStateException("system failure: no user home defined");
		}
		return new File(hs).getCanonicalFile();
	}
	
	/** Returns the current user directory. This refers to the system properties.
	 * 
	 * @return File
	 * @throws IOException  if canonical file fails
	 */
	public static File getUserDir () throws IOException {
		String hs = System.getProperty("user.dir");
		if (hs == null) {
			throw new IllegalStateException("system failure: no user home defined");
		}
		return new File(hs).getCanonicalFile();
	}

   /** Returns the best guess for the location of the program files (directory).
    * 
    * @return File directory of running program in canonical form
    * @throws IOException if canonical file name fails
    */   
   public static File getProgramDir () throws IOException {
      String path, hstr;
      int i;
      
      if ( (path = System.getProperty( "java.class.path" )) != null ) {
         // extract first path entry
         char sep = isWindows() ? ';' : ':';
         if ( (i = path.indexOf(sep)) > -1 ) {
            path = path.substring( 0, i );
         }
         path = path.trim();
         
         // get canonical value 
         if (path != null && path.length() > 0) {
            // get canonical (this may resolve a symbolic link of caller)
            path = new File(path).getCanonicalPath(); 
         
            // take parent dir if path denotes JAR or EXE file
            hstr = path.toLowerCase();
            if (hstr.endsWith(".jar") || hstr.endsWith(".exe")) {
               path = new File(path).getParent();
            }
         }
      }
      
      // if no path found in java.class.path then take user.dir 
      if ( path == null || path.isEmpty() || !new File(path).isDirectory() ) {
         path = System.getProperty("user.dir");
      }
   
      path = new File(path).getCanonicalPath(); 
      Log.debug( 8, "(Util.getProgramDir) --- program directory: [" + path + "]");
      return new File(path);
   }
	   
	/** Decodes a {@code Rectangle} text code. 
	 * <p>The valid code may contain 0, 2 or 4 elements separated by ',' char.
	 * The 2-element version contains width and height (dimension and zero 
	 * location); the 4-element version contains x, y, width, height (location
	 * and dimension).
	 * 
	 * @param code String
	 * @return Rectangle
	 * @throws IllegalArgumentException if argument has illegal encoding
	 */
	public static Rectangle decodeBounds (String code) {
		Objects.requireNonNull(code);
		String[] sa = code.split(",");
		int x = 0, y = 0, w = 0, h = 0, i = 0; 
		switch (sa.length) {
		case 4: x = Integer.parseInt(sa[i++]);
				y = Integer.parseInt(sa[i++]);
		case 2: w = Integer.parseInt(sa[i++]);
				h = Integer.parseInt(sa[i]);
		case 0:	break;
		default: throw new IllegalArgumentException("illegal Bounds code"); 
		}
		return new Rectangle(x, y, w, h);
	}

	/** Decodes a {@code Dimension} text code. 
	 * <p>The valid code may contain 0 to 2 elements separated by ',' char,
	 * where the first element is width, the second is height.
	 * 
	 * @param code String
	 * @return Dimension
	 * @throws IllegalArgumentException if false encoding
	 */
	public static Dimension decodeDimension (String code) {
		String[] sa = code.split(",");
		int x = 0, y = 0;
		switch (sa.length) {
		case 2: y = Integer.parseInt(sa[1]);
		case 1:	x = Integer.parseInt(sa[0]);
		case 0:	break;
		default: throw new IllegalArgumentException("incorrect Dimension code"); 
		}
		return new Dimension(x, y);
	}

	public static String encodeBounds (Rectangle r) {
		return r.x + "," + r.y + "," + r.width + "," + r.height;
	}

	public static String encodeDimension (Dimension size) {
		return size.width + "," + size.height;
	}

	public static String encodeDimension (Point point) {
		return point.x + "," + point.y;
	}

	/** Transforms the given input stream into ZIP data and returns them 
	 * as a new input stream. The parameter input stream gets closed. The
	 * temporary file for the resulting input stream gets closed when the 
	 * resulting input stream gets closed.
	 * 
	 * @param input {@code InputStream} uncompressed data stream
	 * @param length long expected length of input stream (may be estimate)
	 * @param crc IntResult return CRC value of the input stream (uncompressed)
	 * @return {@code InputStream} ZIP-data stream
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static InputStream getZipInputStream (InputStream input, long length, IntResult crc) 
				throws FileNotFoundException, IOException {
		InputStream stream;
		OutputStream out;
		final File zipF;
		boolean smallSize = length <= 10*MEGA;
		
		// create an appropriate output stream (buffered data)
		if (smallSize) {
			out = new DirectByteOutputStream((int)length);
			zipF = null;
		} else {
		    zipF = File.createTempFile("Util-", ".dat");
			out = new FileOutputStream(zipF);
		}
		
	    // create a ZIP output-stream 
	    GZIPOutputStream zipOut = new GZIPOutputStream(out, true);
	
	    // transform the source stream from argument into ZIP data
		// determine cleartext stream CRC
	    int sourceCrc;
		try {
			sourceCrc = Util.transferData2(input, zipOut, DEFAULT_BUFFER_SIZE);
		} catch (InterruptedException e) {
			throw new IOException("interrupted during output");
		}
	    crc.setValue(sourceCrc);
	    input.close();
	    zipOut.close();
		
	    // create the new input stream
	    if (smallSize) {
	    	// create memory block read stream
	    	DirectByteOutputStream bout = (DirectByteOutputStream) out;
	    	stream = new ByteArrayInputStream(bout.getBuffer(), 0, bout.size());
	    } else {
		    // open the ZIP-file as input stream
		    stream = new FileInputStream(zipF) {
				@Override
				public void close() throws IOException {
					super.close();
					zipF.delete();
				}
		    };
		}
		return stream;
	}

	/** Overwrites the given character array with zero values.
	 * 
	 * @param ca char[]
	 */
	public static void destroy (char[] ca) {
		for (int i = 0; i < ca.length; i++) {
			ca[i] = 0;
		}
	}

   /**
    * Destroys the contents of the parameter byte array by assigning zero to
    * all elements.
    * 
    * @param ba byte[]
    */
   public static void destroy ( byte[] ba ) {
	  if (ba != null)
      for ( int i = 0; i < ba.length; i++ ) {
         ba[i] = 0;
      }
   }

  /**
   * Returns a string representation of the parameter long integer value
   * including decimal separation signs (after VM default locale).
   *  
   * @param value long integer
   * @return dotted text representation
   */
  public static String dottedNumber (long value) {
	  String hstr = String.valueOf(value);
	  String out= "";
	  char sep = (new DecimalFormatSymbols()).getGroupingSeparator();
	  int len= hstr.length();
	  while( len > 3 ) {
	     out= sep + hstr.substring(len-3, len) + out;
	     hstr= hstr.substring(0, len-3);
	     len= hstr.length();
	  }
	  return hstr + out;
  }
	
	/** Encodes the identity of the given font so that the result can serve as
	 * an input to the static method 'Font.decode()'.
	 * 
	 * @param f Font
	 * @return String simple font serialisation
	 */
	public static String encodeFont (Font f) {
		String style = "PLAIN";
		switch (f.getStyle()) {
		case Font.BOLD: style = "BOLD"; break; 
		case Font.ITALIC: style = "ITALIC"; break; 
		case Font.BOLD + Font.ITALIC: style = "BOLDITALIC"; break; 
		}
		String code = f.getFamily() + "-" + style + "-" + f.getSize();
		return code;
	}

	/** Returns a possibly modified version of the input text where all lines
	 * (separated by '\n') are removed which start with the given marker
	 * expression. A null marker returns the given text as is.
	 *  
	 * @param text String input text
	 * @param marker String comment marker at start of line, may be null
	 * @return String modified text
	 */
	public static String removeComments (String text, String marker) {
		Objects.requireNonNull(text, "text is null");
		if (marker == null || marker.isEmpty() || text.isEmpty() || 
			text.indexOf(marker) == -1) return text;
		String[] arr = text.split("\n", -1);
		
		// reduce for comment lines
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].startsWith(marker)) {
				arr[i] = null;
			}
		}
		
		// recompile text
		StringBuffer buf = new StringBuffer(text.length());
		for (String s : arr) {
			if (s != null) {
				buf.append(s);
				if (s != arr[arr.length-1]) {
					buf.append('\n');
				}
			}
		}
		return buf.toString();
	}

	/** Returns the next pseudo-random integer.
	 *  
	 * @return int
	 */
	public static int nextRand() {
		return random.nextInt();
	}

	/**
	 * Returns a random value within the range 0 .. 255.
	 * (non-cryptographical random)
	 * 
	 * @return a value <code>0 .. 255</code>
	 */
	public static int nextRandByte () {
	   return random.nextInt(256);
	}

	/** Returns the next pseudo-random boolean.
	 * 
	 * @return boolean
	 */
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}
	
}


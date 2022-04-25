package kse.utilclass.misc;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.BitSet;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import kse.utilclass.dialog.GUIService;

public class Util {

	public static final long TM_DAY = 24 * 60 * 60 * 1000;
	public static final long TM_HOUR = 60 * 60 * 1000;
	public static final long TM_MINUTE = 60 * 1000;
	public static final long TM_SECOND= 1000;
	
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
	
	public static void requirePositive (long i) {
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

	/** Returns a SHA-256 fingerprint value of the parameter string buffer.
	 * 
	 * @param buffer <code>String</code> data to digest
	 * @return SHA256 digest
	 */
	public static byte[] fingerPrint ( String buffer ) {
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
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static void transferData (InputStream input, OutputStream output,
	      byte[] buffer) throws java.io.IOException, InterruptedException {
	   Objects.requireNonNull(buffer, "transfer buffer is null");
	   int len;
	   while ((len = input.read(buffer)) > -1) {
       	  if (Thread.interrupted()) {
     		 throw new InterruptedException();
       	  }
	      output.write(buffer, 0, len);
	   }
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
	 * @throws java.io.IOException
	 * @throws InterruptedException if the calling thread was interrupted
	 */
	public static void transferData (InputStream input, OutputStream output,
	      int bufferSize) throws java.io.IOException, InterruptedException {
	   byte[] buffer = new byte[bufferSize];
	   transferData(input, output, buffer);
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
	 */
	public static File getTempDir() {
		String path = System.getProperty("java.io.tmpdir");
		return new File(path);
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
	   transferData( in, out, 4*2048 );
	   in.close();
	   out.close();
	}

	   /** A security file copy method. 
	    * Copies the contents of any disk file to a specified output file.  If 
	    * the output file is a relative path, it is made absolute against the 
	    * directory of the input file.  The output file will be overwritten if
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
	    * @throws IOException if the function could not be completed
	    *         because of an IO or CRC-check error or if the thread was 
	    *         interrupted
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
	    * directory of the input file.  The output file will be overwritten if
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
	    * @throws IOException if the function could not be completed
	    *         because of an IO or CRC-check error or if the thread was 
	    *         interrupted
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
	            throw new StreamCorruptedException("bad copy CRC");
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
	 *  @return String the result of transformation; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 */
	public static String substituteText ( String text, String token, String substitute ) {
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
	 *  @return String the result of substitute; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 */
	public static String substituteTextS ( String text, String token, String substitute ) {
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
	   
	/** Reads the contents of the given file and returns them as a byte array.
	 * 
	 * @param file File file to read
	 * @param encoding String the text encoding; null for JVM default
	 * @return String text or null if argument is null
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
	
	/** Attempts to read an integer value from the current position of the given
	 * character buffer. Moves the buffer pointer for the number of digits read.
	 * 
	 * @param buf {@code CharBuffer}
	 * @return long read signed integer
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
	 * environments. Optionally the text can be prepended by {@code '<html>'}
	 * if this tag is not yet there. 
	 * <p>Substitutes conflicting characters in the text with HTML-masked 
	 * expressions. This will remove all '\n' and '\r' occurrences from the 
	 * text. 
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
	 * output file. 
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
      for ( i = 0; i < n; i++ )
         sbuf.append( '0' );
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
			set.set(row);
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

	public static int textVariance ( char[] ca )
	   {
	      BitSet set = new BitSet();
	      int i;
	      
	      for ( i = 0; i < ca.length; i++ )
	         set.set( ca[i] );
	      return set.cardinality();         
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
}


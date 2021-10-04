package kse.utilclass.misc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.CharBuffer;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.zip.CRC32;

public class Util {

	public static final long TM_DAY = 60 * 60 * 24 * 1000;
	public static final long TM_HOUR = 60 * 60 * 1000;
	
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
	 */
	public static void concatFiles ( File bottom, File top ) throws IOException {
	   FileOutputStream out = new FileOutputStream( bottom, true );
	   FileInputStream in = new FileInputStream( top );
	   transferData( in, out, 4*2048 );
	   in.close();
	   out.close();
	}

	/** Copies the contents of any disk file to a specified output file.  If the
	    *  output file is a relative path, it is made absolute against the directory
	    *  of the input file.  The output file will be overwritten if it exist.
	    *  A CRC32 check is performed to compare source and copy after the copy process
	    *  and if results negative a <code>StreamCorruptedException</code> is thrown.
	    *  Function reports errors to <code>System.err</code>.
	    *  @param input a source File object
	    *  @param output a copy File object
	    *  @param carryTime if true the "last modified" time is set to source value
	    *  @throws java.io.IOException if the function could not be completed
	    *  because of an IO or CRC check error
	    */
	   public static void copyFile( File input, File output, boolean carryTime )
	                                   throws java.io.IOException {
	      File parent;
	      FileInputStream in = null;
	      FileOutputStream out = null;
	      CRC32 crcSum;
	      int writeCrc;
	      long time;
	
	      // control parameter
	      if ( input == null || output == null )
	         throw new IllegalArgumentException( "null pointer" );
	      if ( input.equals( output ) )
	         throw new IllegalArgumentException( "illegal self reference" );
	
	      try {
	         // make output file absolute (if not already)
	         parent=input.getAbsoluteFile().getParentFile();
	         if ( !output.isAbsolute() ) {
	            output = new File( parent, output.getPath() );
	         }
	
	         // make sure the directory for the output file exists
	         ensureFilePath( output, parent );
	
	         // create file streams
	         out = new FileOutputStream(output);
	         in = new FileInputStream(input);
	         time = input.lastModified();
	
	         int len;
	         byte[] buffer = new byte[2*2048];
	         writeCrc = transferData2(in, out, buffer);
	         in.close();
	         out.close();
	         if (carryTime) {
	        	 output.setLastModified( time );
	         }
	
	         // control output CRC
	         in = new FileInputStream( output );
	         crcSum = new CRC32();
	         while ((len = in.read(buffer)) != -1) {
	            crcSum.update( buffer, 0, len );
	         }
	         if ( writeCrc != (int)crcSum.getValue() ) {
	            throw new StreamCorruptedException( "bad copy CRC" );
	         }

	      } catch (IOException e) {
	         System.err.println(
	            "*** error during file copy: " + output.getAbsolutePath());
	         System.err.println(e);
	         throw e;
	      } finally {
	         if ( in != null )
	            in.close();
	         if ( out != null )
	            out.close();
	      }
	   } // copyFile

	/**
	 * Transfers the contents of the input stream to the output stream
	 * until the end of input stream is reached. This function version returns 
	 * a CRC32 value over the entire data stream transferred.
	 * 
	 * @param input the input stream (non-null)
	 * @param output the output stream (if null, a valid CRC value is still created)
	 * @param buffer byte[] the transfer buffer
	 * @return int CRC value of the data stream read
	 * @throws java.io.IOException
	 */
	public static int transferData2 ( InputStream input, OutputStream output,
	      byte[] buffer ) throws java.io.IOException {
	   CRC32 crc = new CRC32();
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

	/** Renders a string based on <code>text</code> where any occurence of
	 *  <code>token</code> is replaced by <code>substitute</code>. Replace
	 *  takes place iteratively until not further occurence exists.
	 *  
	 *  @return String the result of transformation; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 *  @since 0-4-0        
	 */
	public static String substituteText ( String text, String token, String substitute )
	{
	   int index;
	
	   if ( text == null | token == null | substitute == null || 
	         (index=text.indexOf( token )) < 0 )
	       return text;
	
	   while ( index > -1 )
	   {
	      text = text.substring( 0, index ) + substitute +
	             text.substring( index+token.length() );
	      index = text.indexOf( token );
	   }
	   return text;
	}  // substituteText

	/** Renders a string based on <code>text</code> where the first occurrence of
	 *  <code>token</code> is replaced by <code>substitute</code>.
	 *  <br>(Returns the original if any of the parameters is <b>null</b> or length or
	 *  <tt>token</tt> is zero.)
	 *  
	 *  @return String the result of substitute; <b>null</b> if any of the
	 *          parameters is <b>null</b>
	 *  @since 0-4-0        
	 */
	public static String substituteTextS ( String text, String token, 
	      String substitute )
	{
	   int index;
	
	   if ( text == null | token == null | substitute == null || 
	        token.length() == 0 || (index=text.indexOf( token )) < 0 )
	      return text;
	
	   if ( index > -1 )
	   {
	      text = text.substring( 0, index ) + substitute +
	             text.substring( index+token.length() );
	   }
	   return text;
	}  // substituteText

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
}


package kse.utilclass2.misc;

/*
*  File: CryptoRandom.java
* 
*  Project JUtilClasses
*  @author Wolfgang Keller
*  Created 
* 
*  Copyright (c) 2024 by Wolfgang Keller, Germany
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
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.SHA256;
import kse.utilclass.misc.SHA512;
import kse.utilclass.misc.Util;

/**
 *  Enhanced random generator for cryptographic purposes. Claims to be 
 *  thread-safe.
 *   
 *  <p>This random generator is based on the SHA-512 function which is assumed 
 *  to generate a set of cryptologically qualifying random values over a 
 *  sufficiently qualified entropy data pool. The data pool is acquired from
 *  a mixture of system based random, random from class
 *  {@code java.security.SecureRandom} and a cyclic counter increment.
 *  Optionally random seed material can be supplied from the user's context
 *  on a once or cyclic pattern.
 *  
 *  <p>Instances can be used straight away with any constructor. A good job 
 *  can be expected by only this. Opportunity for additional user pool data 
 *  exists with method {@code getUserSeed()}. It is called with every data 
 *  pool refresh cycle and can be activated through overriding in a subclass. 
 *  Applications which are in hold of real random values on a regular basis 
 *  can meaningfully use this feature, though it is not regarded essential. It
 *  is not required that each call for user seeds renders a unique value. 
 *  Alternatively, method {@code recollect()} can be called for a one-time
 *  update of random data. Likewise constructors can be used to transfer a
 *  seeding user-side random data set.
 */
public class CryptoRandom {
	
   private static long timerstart = System.currentTimeMillis();
   private static int instanceCounter;
   
   private int instanceID;
   private Random rand;

   /** system based immutable random (calculated only once) */
   private byte[] systemSeed;
   /** last user supplied random value */
   private byte[] userSeed;
   /** random seed calculated by 'collectPool()' */
   private byte[] pool;
   /** current random data sheet (interface output values) */
   private byte[] data = new byte[ 2 * SHA512.HASH_SIZE ];
   /** sheet refresh counter */
   private long counter;
   /** number of sheet refreshs for a single call of 'collectPool()' */ 
   private int period = 16;
   /** current data pointer into sheet */ 
   private int pos;
   

/**
 * Constructs a random generator with standard values for refresh cycle 
 * and random seed. The default cycle period is 16.
 */
public CryptoRandom () {
   init( null );
}

/**
 * Constructs a random generator with user definition of the pool refresh cycle
 * loops. Higher values of <code>cycle</code> reduce execution cost but
 * also might reduce long-term random quality.
 * 
 * @param cycle int number of loops to use a single random pool incarnation
 * @throws IllegalArgumentException if cycle is negative
 */
public CryptoRandom ( int cycle ) {
   this( cycle, null );
}

/**
 * Constructs a random generator with the given user random seed data and
 * a cycle period of 16.
 * 
 * @param init byte[] initial random seed data, may be <b>null</b>
 */
public CryptoRandom ( byte[] init ) {
   init( init );
}

/**
 * Constructs a random generator with initial user seed data and a setting
 * for the pool refresh period. 
 * 
 * @param cycle int number of loops to use a single random pool incarnation
 * @param init byte[] initial random seed data; may be <b>null</b>
 * @throws IllegalArgumentException if cycle is negative
 */
public CryptoRandom ( int cycle, byte[] init ) {
	Util.requirePositive(cycle, "cycles");
   
    period = cycle;
    init( init );
}

private void init ( byte[] init ) {
   instanceID = instanceCounter++;
   rand = new SecureRandom();
   if (init != null) {
	   userSeed = Util.sha512(init);
   }
   if ( Log.getLogLevel() >= 9 ) {
	   String hs = init == null ? "null" : Util.bytesToHex(userSeed);
	   Log.log( 9, "(CryptoRandom.init) new instance ID = " + instanceID + ", user-init (fingerprint) = " + hs);
   }

   rand.nextBytes( data );
   recalculate();
}

/** The random seed value last authorised by the user. This may be a derived
 * value in place of the user supplied data block.
 *  
 * @return byte[] or null
 */
protected byte[] getUserRandom () {return userSeed;}

/** The random pool data collected as base for the cyclic rebuilding of the
 * random data sheet.
 * 
 * @return byte[]
 */
byte[] getPoolData () {return pool;}


byte[] getSheet () {return data;}

/** Returns the seed data originating from entropy of JVM and operating system
 * properties. This value is calculated only once per instance.
 *  
 * @return byte[] seed material 
 */
protected byte[] getSystemSeed () {
	if (systemSeed != null) return systemSeed;
	
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	DataOutputStream dout = new DataOutputStream( out );
	
	try {
	    // system properties
	    dout.writeBytes( System.getProperty( "java.class.path", "" ) );
	    dout.writeBytes( System.getProperty( "java.library.path", "" ) );
	    dout.writeBytes( System.getProperty( "java.vm.version", "" ) );
	    dout.writeBytes( System.getProperty( "os.arch", "" ) );
	    dout.writeBytes( System.getProperty( "os.version", "" ) );
	    dout.writeBytes( System.getProperty( "os.name", "" ) );
	    dout.writeBytes( System.getProperty( "user.dir", "" ) );
	    dout.writeBytes( System.getProperty( "user.name", "" ) );
	    dout.writeBytes( System.getProperty( "user.timezone", "" ) );
	    dout.writeBytes( System.getProperty( "user.language", "" ) );
	
	    // object addresses
	    dout.writeInt( this.hashCode() );
	    dout.writeInt( rand.hashCode() );
	    dout.writeInt( out.hashCode() );
	
  	    // screen dimension data
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension dim = tk.getScreenSize();
        dout.writeInt( dim.height );
        dout.writeInt( dim.width );
        dout.writeInt( tk.getScreenResolution() );
        dout.writeInt( tk.hashCode() );
      
        // mouse position
        Point p = MouseInfo.getPointerInfo().getLocation();
        dout.writeInt(p.x);
        dout.writeInt(p.y);
	      
    } catch ( Exception e ) {
    	e.printStackTrace();
    }
    
    // summarise the content
	SHA256 sha = new SHA256();
	byte[] buf = out.toByteArray();
	sha.update(buf);
	systemSeed = sha.digest();
	return  systemSeed;
}

/** Collects 64 bytes of random pool data from system and user entropy sources
 * and stores it in member variable 'pool'.
 */
private void collectPool () {
   if ( Log.getLogLevel() >= 9 )
   Log.log( 9, "(CryptoRandom.collectPool) [" + instanceID + "] collecting pool data" );
   Runtime rt = Runtime.getRuntime();

   // collect random pool data
   ByteArrayOutputStream out = new ByteArrayOutputStream();
   DataOutputStream dout = new DataOutputStream( out );
   try {
      // half of the current data
      dout.write( data, SHA512.HASH_SIZE/2, SHA512.HASH_SIZE );
      
      // current time related
      long now = System.currentTimeMillis();
      dout.writeLong( now );
      dout.writeLong( now - timerstart );
      
      // system entropy
      dout.write( getSystemSeed() );
      
      // "secure" random bytes (24)
      byte[] buf = new byte[ 24 ];
      rand.nextBytes( buf );
      dout.write( buf );
      
      // get new user seed data if available 
      byte[] init = getUserSeed();
      if ( init != null ) {
    	  userSeed = Util.sha512(init);
      }
      
      // use any available user seed
      if ( userSeed != null ) {
         if (Log.getDebugLevel() >= 9) {
             Log.debug( 9, "(CryptoRandom.collectPool) [" + instanceID + "] available user pool data, fingerprint: " 
                   + Util.bytesToHex(userSeed));
         }
         dout.write( userSeed );
      }
      
      // current object addresses
      dout.writeInt( Thread.currentThread().hashCode() );
      dout.writeInt( buf.hashCode() );
      dout.writeInt( out.hashCode() );
      dout.writeInt( dout.hashCode() );
      
      // memory status
      dout.writeInt( (int)rt.totalMemory() );
      dout.writeInt( (int)rt.maxMemory() );
      dout.writeInt( (int)rt.freeMemory() );
      
      // screen related
      try {
	      // mouse position
	      Point p = MouseInfo.getPointerInfo().getLocation();
	      dout.writeShort(p.x);
	      dout.writeShort(p.y);
      } catch ( Exception e ) {
      }
      
      // store collection as abstract 64 byte in member
      byte[] buffer = out.toByteArray();
      pool = Util.sha512(buffer); 

      if ( Log.getDebugLevel() >= 9 )
      Log.debug( 9, "(CryptoRandom.collectPool) [" + instanceID + "] pool data: " + Util.bytesToHex(pool));

   } catch ( IOException e ) {
      System.err.println( "*** SEVERE ERROR IN INIT CRYPTORANDOM : " + e );
   }
}

/**
 * Callback function to obtain the user's random seed data array in a subclass.
 * You can override this to supply specific seed data to this generator.
 * <p>(This is an alternative method compared to <code>recollect()</code>.
 * This method updates the random pool data more reliably because it is a 
 * user-passive method, compared to  <code>recollect()</code> which is a 
 * user-active method. However, frequent recollection may be expensive in time.)
 * 
 * @return byte[] random seeds
 */
public byte[] getUserSeed () {return null;}

/** The number of recalculation cycles which have been performed since start.
 * 
 * @return long
 */
public long getCounter () {return counter;}

/** The unique identifier for this instance.
 * 
 * @return int
 */
public int getInstanceID () {return instanceID;}

/** The period in number of recalculation cycles by which the pool data is
 * recollected.
 * 
 * @return int
 */
public int getCyclePeriod () {return period;}

/** Causes this random generator to collect a new random data pool including
 *  the user's seed data as given by the parameter.
 * 
 *  @param init byte[] user seed data or <b>null</b>
 */
public synchronized void recollect ( byte[] init ) {
    if ( init != null ) {
  	  userSeed = Util.sha512(init);
    }
    collectPool();
}

/** Creates a new 2*HASHSIZE bytes random data block. */
private void recalculate () {
   byte[] buf, prevData;
   
   if ( Log.getLogLevel() >= 9 )
   Log.log( 9, "(CryptoRandom.recalculate) [" + instanceID + "] recalculating data sheet, count " + counter );
   
   SHA512 sha = new SHA512();
   int hashLen = sha.getDigestLength();
   prevData = Util.arraycopy( data );
   
   if ( counter % period == 0 ) {
      collectPool();
   }
   
   sha.update( pool );
   sha.update( counter++ );
   buf = sha.digest();
   System.arraycopy( buf, 0, data, 0, hashLen );

   sha.reset();
   sha.update( Util.XOR_buffers( buf, Util.arraycopy(prevData, hashLen) ));
   buf = sha.digest();
   System.arraycopy( buf, 0, data, hashLen, hashLen );
   pos = 0;

   if ( Log.getDebugLevel() >= 9 ) 
   Log.debug( 9, "(CryptoRandom.recalculate) [" + instanceID + "] new random data: " + Util.bytesToHex(data));
}

/** Returns a random <code>byte</code> value.
 * 
 *  @return byte
 */
public synchronized byte nextByte () {
   return nextByteIntern();
}

/** Returns a random integer value ranging from 0 including to n excluding. 
 *  n must be positive. 
 *  
 *  @param n int size of value range 
 *  @return int random value
 *  @throws IllegalArgumentException if n is negative or zero
 */
public synchronized int nextInt ( int n ) {
   Util.requireNPositive(n, "n");

   int bits = nextIntIntern();
   bits = (bits < 0) ? -bits : bits;
   int val = bits % n;
   return val;
}

/** Returns a random <code>int</code> value. The value ranges from 
 *  Integer.MINVALUE to Integer.MAXVALUE.
 * 
 *  @return int random value
 */
public synchronized int nextInt () {
   return nextIntIntern();
}

/** Returns a random <code>long</code> value. 
 * 
 *  @return long random value
 */
public synchronized long nextLong () {
   return ((long)nextIntIntern() << 32) | (nextIntIntern() & 0xFFFFFFFFL);
}

/**
 * Returns a random data byte array of the length as specified by the parameter.
 * 
 * @param num int length of output byte array
 * @return byte[] random bytes
 * @throws IllegalArgumentException if n is negative
 */
public synchronized byte[] nextBytes ( int num ) {
   Util.requirePositive(num, "num");
   
   byte[] buf = new byte[ num ];
   for ( int i = 0; i < num; i++ ) {
      buf[i] = nextByteIntern();
   }
   return buf;
}

/** Returns a random <code>boolean</code> value. 
 * 
 *  @return boolean random value
 */
public synchronized boolean nextBoolean () {
   return nextByteIntern() < 0;
}

/** Returns a random <code>byte</code> value. 
 * 
 *  @return byte
 */
private byte nextByteIntern () {
   if ( pos == data.length ) {
      recalculate();
   }
      
   return data[ pos++ ];
}

/** Returns a random <code>int</code> value. The value ranges from 
 *  Integer.MINVALUE to Integer.MAXVALUE.
 * 
 *  @return int random value
 */
private int nextIntIntern () {
   return
    nextByteIntern() << 24 |
   (nextByteIntern() & 0xFF) << 16 |
   (nextByteIntern() & 0xFF) <<  8 |
   (nextByteIntern() & 0xFF);
}
}

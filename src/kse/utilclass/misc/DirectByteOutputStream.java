package kse.utilclass.misc;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/** Memory based output stream extending {@code java.io.ByteArrayOutputStream} 
 * in order to make available the accumulated data buffer in direct access.
 * The buffer can be accessed as original or through a {@code ByteBuffer}
 * wrapping instance.
 */
public class DirectByteOutputStream extends ByteArrayOutputStream {

	public DirectByteOutputStream () {
		super();
	}

	/** Creates a new byte array output stream with the given initial buffer 
	 * capacity.
	 *  
	 * @param size int initial buffer size
	 */
	public DirectByteOutputStream (int size) {
		super(size);
	}

	/** Returns the raw data buffer of this output stream. The length of this
	 * buffer may be larger than the usable length, which is defined through
	 * 'size()'.
	 *  
	 * @return byte[] data buffer
	 */
	public byte[] getBuffer () {return buf;}
	
	/** Returns a {@code ByteBuffer} with direct use of this streams data-buffer
	 * and the usable length defined by the current value of 'size()'.
	 * The pointer is set to zero, the limit is 'size()'.
	 *  
	 * @return {@code ByteBuffer}
	 */
	public ByteBuffer getByteBuffer () {
		return ByteBuffer.wrap(buf, 0, count);
	}
	
	/** Erases the internal byte buffer and resets the object. 
	 */
	public void clear () {
	   Util.destroy(this.buf);
	   this.reset();
	}

}

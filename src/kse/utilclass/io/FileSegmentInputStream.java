/*
 *  FileSegmentInputStream 
 * 
 *  Project Util-Classes
 *  @author Wolfgang Keller
 *  Created 2022
 * 
 *  Copyright (c) 2005 by Wolfgang Keller, Munich, Germany
 * 
 This program is not freeware software but copyright protected to the author(s)
 stated above. However, you can use, redistribute and/or modify it under the terms 
 of the GNU General Public License as published by the Free Software Foundation, 
 version 2 of the License.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place - Suite 330, Boston, MA 02111-1307, USA, or go to
 http://www.gnu.org/copyleft/gpl.html.
 */

package kse.utilclass.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.Objects;

import kse.utilclass.misc.Util;

/**
 *  An InputStream representing a section of a file channel. The class
 *  offers additional features compared to the regular {@code InputStream}.
 *  <p>If a file channel is supplied with the constructor it will not
 *  get closed through this class'es <code>close()</code> method. 
 * 
 *  <p> Mark is supported. After creation the mark position is defined at zero.
 *  This class is not synchronised. Applications must make sure
 *  that no multiple accesses take place through different threads.
 */
public class FileSegmentInputStream extends InputStream {

	/** Time in milliseconds to wait for the file channel to respond to a
	 * read request.
	 */
	public static final long READ_TIMEOUT = 5000;

	private RandomAccessFile raf;
    private FileChannel	fileChannel;
    private long streamLength;
    private long startPos;
    private long markPos;
    private long pos;
    private ByteBuffer bbuf;
    private boolean closed;
   
/**
 * Creates a new segment input stream with the given start position in the
 * given file-channel, covering the given stream length.
 * <p>NOTE: This input stream will not close the file channel when it is closed.
 * 
 * @param channel {@code FileChannel}
 * @param start long position in channel
 * @param length long length of rendered input stream
 * @throws IllegalArgumentException if the requested space is beyond file 
 *         length
 */
public FileSegmentInputStream (FileChannel	channel, long start, long length) throws IOException {
   Objects.requireNonNull(channel, "channel is null");
   Util.requirePositive(start, "start");
   Util.requirePositive(length, "length");

   init(channel, start, length);
}

/**
 * Creates a new segment input stream from a newly opened file channel for the
 * given file.
 * <p>NOTE: This input stream needs to be closed to close its internal file 
 * channel.
 * 
 * @param file {@code File} file to be read
 * @param start long position in channel
 * @param length long length of rendered input stream
 * @throws IllegalArgumentException if the requested space is beyond file 
 *         length
 */
public FileSegmentInputStream (File file, long start, long length) throws IOException {
   Objects.requireNonNull(file, "file is null");
   Util.requirePositive(start, "start");
   Util.requirePositive(length, "length");

   raf = new RandomAccessFile(file, "r");
   init(raf.getChannel(), start, length);
}

private void init (FileChannel	channel, long start, long length) throws IOException {
   if (start+length > channel.size()) {
	   close();
	   throw new IllegalArgumentException( "requested space beyond file size" );
   }

   fileChannel = channel;
   startPos = start;
   streamLength = length; 
}

private int readChannel(ByteBuffer buffer) throws IOException {
   checkNotClosed();
	
   boolean timeout = false;
   long stamp = System.currentTimeMillis();
   int read;
   do {
     read = fileChannel.read(buffer, startPos+pos);
     if ( read == 0 ) synchronized(this) {
    	try {  wait(50);
		} catch (InterruptedException e) {
		}
    	timeout = System.currentTimeMillis()-stamp > READ_TIMEOUT;
     }
   } while (read == 0 & !timeout);
   
   if (timeout) {
	   throw new IOException("timeout on failed channel read, position " + (startPos+pos));
   }
   return read;
}

@Override
public int available () throws IOException {
   return closed ? 0 : (int)Math.min( streamLength - pos, Integer.MAX_VALUE );
}
   
@Override
public void close () throws IOException {
	if (!closed) { 
		if (raf != null) {
			raf.close();
		}
		fileChannel = null;
		bbuf = null;
		closed = true;
	}
}

/** Whether this stream has been closed.
 * @return boolean
 */
public boolean isClosed () {return closed;}
   
@Override
public synchronized void mark (int readlimit) {
    markPos = pos;
}
   
@Override
public boolean markSupported () {return true;}
   
@Override
public synchronized void reset () throws IOException {
    checkNotClosed();
    pos = markPos;
}

/** Causes this stream to translate its start address in the file.
 * All subsequent operations work with reference to the new value.
 * 
 * @param chunk {@code Chunk}
 * @param delta long shift delta for chunk
 * @throws IllegalArgumentException if delta is out of range
 * @throws IOException 
 */
public void translate (long delta) throws IOException {
	long newStart = startPos + delta; 
	if (newStart < 0 || newStart + streamLength > fileChannel.size()) {
		throw new IllegalArgumentException("illegal translate delta: " + delta);
	}
	startPos = newStart;
}

/** Sets the stream-pointer position in the stream. The next read operation will
 * start from the given stream position. The new value is legal between zero 
 * and 'length()'.
 * 
 * @param newPos long new read position
 * @throws ClosedChannelException 
 */
public void position (long newPos) throws ClosedChannelException {
   checkNotClosed();
	if (newPos < 0 | newPos > streamLength)
		throw new IllegalArgumentException("new position out of range");
	pos = newPos;
}

/** Returns the current stream-pointer value.
 * 
 * @return long read position
 */
public long position () {return pos;}

/** Returns the total length of this input stream in bytes.
 *  
 * @return long stream length
 */
public long length () {return streamLength;}

/** Returns the number of bytes which lie ahead of the current stream pointer 
 * position (pointer including).
 * 
 * @return long bytes unread
 */
public long remaining () {
	return streamLength - pos;
}

@Override
public int read () throws IOException {
   checkNotClosed();
   if (pos == streamLength) return -1;
   if (bbuf == null) {
	   bbuf = ByteBuffer.allocate(1);
   } else {
	   bbuf.clear();
   }
   int rd = readChannel(bbuf);
   pos++;
   bbuf.rewind();
   int val = bbuf.get() & 0xFF;
   return rd < 1 ? -1 : val; 
}

@Override
public int read ( byte[] b, int off, int length ) throws IOException {
   checkNotClosed();
   Util.requirePositive(off, "offset");
   Util.requirePositive(length, "length");
   if (pos == streamLength) return -1;
   length = Math.min(length, available());
   ByteBuffer buf = ByteBuffer.wrap(b, off, length);
   int rd = readChannel(buf);
   if (rd > 0) {
      pos += rd;
   }
   return rd;
}

@Override
public long skip ( long n ) throws IOException {
   checkNotClosed();
   Util.requirePositive(n);
   if (closed) throw new ClosedChannelException();
   long oldPos = pos;
   pos = Math.min(streamLength, pos + n);
   return pos - oldPos;
}

private void checkNotClosed () throws ClosedChannelException {
	if (closed) throw new ClosedChannelException();
}

@Override
protected void finalize() throws Throwable {
	close();
	super.finalize();
}

}

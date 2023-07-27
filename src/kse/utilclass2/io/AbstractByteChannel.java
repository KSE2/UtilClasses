package kse.utilclass2.io;

/*
*  File: AbstractByteChannel.java
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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Objects;

import kse.utilclass.misc.Util;

/** Abstract class to implement the basics of a {@code SeekableByteChannel},
 * furthermore {@code GatheringByteChannel} and {@code ScatteringByteChannel}.
 * Only four IO-methods need to be implemented by a sub-class, which can be 
 * held quite concise.
 * 
 * <p>Sub-class implementations need to administer the 'channelSize' value
 * and return it as efficiently as possible in the delegated 'getChannelSize()'
 * method. This allows for additional methods in the subclass for IO modifying 
 * this value.
 * 'position' is administered by this class as long as the standard methods are
 * used for data exchange. Otherwise the position value needs to be updated with
 * the 'position(long)' method. The initial position is zero.
 * 
 * <p>Operation modi can be set over the constructor. They are:
 * <br>READ-ONLY, WRITE-ONLY, READ-WRITE, APPEND-ONLY, READ-APPEND. In both
 * APPEND modi all write-methods will always append to the top of the channel
 * and disregard position settings or parameters. In APPEND mode truncation of
 * the channel is disallowed. 
 * 
 *  @author Wolfgang Keller
 */
public abstract class AbstractByteChannel implements SeekableByteChannel, 
			GatheringByteChannel, ScatteringByteChannel {

	private long position;
	private boolean isClosed;
	private boolean canRead;
	private boolean canWrite;
	private boolean isAppend;

	/** Creates a new {@code AbstractByteChannel} open for reading and writing.
	 * 
	 * @param channelSize long initial channel size
	 */
	public AbstractByteChannel (long channelSize) {
		this("rw");
	}

	/** Creates a new {@code AbstractByteChannel} with the given operation 
	 * modus.
	 * <p>This channel is open for possibly reduced or specialised abilities 
	 * for reading and writing. As modus can be stated: 'r' for reading,
	 * 'w' for writing, 'rw' for reading and writing, 'a' for append mode
	 * (writing only) and 'ra' for append mode with reading. 
	 *
	 * @param modus String operation parameters
	 */
	public AbstractByteChannel (String modus) {
		Objects.requireNonNull(modus);
		isAppend = modus.indexOf('a') > -1;
		canRead = modus.indexOf('r') > -1;
		canWrite = isAppend || modus.indexOf('w') > -1;
	}

	public boolean isAppend () {return isAppend;}

	@Override
	public synchronized boolean isOpen() {return !isClosed;}

	@Override
	public synchronized void close() throws IOException {
		if (!isClosed) {
			closeImpl();
			isClosed = true;
		}
	}

	/** Throws {@code ClosedChannelException} if this channel is closed.
	 * @throws ClosedChannelException 
	 */
	protected void checkClosedState () throws ClosedChannelException {
		if (isClosed)
			throw new ClosedChannelException();
	}

	/** Checks whether this channel can operate reading methods. Also checks
	 * for open state.
	 * 
	 * @throws ClosedChannelException
	 * @throws NonReadableChannelException
	 */
	protected void checkCanRead () throws ClosedChannelException, NonReadableChannelException {
		if (isClosed)
			throw new ClosedChannelException();
		if (!canRead)
			throw new NonReadableChannelException();
	}

	/** Checks whether this channel can operate writing methods. Also checks
	 * for open state.
	 * 
	 * @throws ClosedChannelException
	 * @throws NonWritableChannelException
	 */
	protected void checkCanWrite () throws ClosedChannelException, NonWritableChannelException {
		if (isClosed)
			throw new ClosedChannelException();
		if (!canWrite)
			throw new NonWritableChannelException();
	}

	@Override
	public synchronized long write (ByteBuffer[] srcs, int offset, int length) throws IOException {
		checkCanWrite();
		Objects.requireNonNull(srcs, "buffer array is null");
		if (length < 0 | offset < 0 | offset+length > srcs.length)
			throw new IndexOutOfBoundsException();

		long sum = 0;
		for (int i = offset; i < offset+length; i++) {
			sum += write(srcs[i]);
		}
		return sum;
	}

	@Override
	public long write (ByteBuffer[] srcs) throws IOException {
		return write(srcs, 0, srcs.length);
	}

	@Override
	public synchronized long read (ByteBuffer[] dsts, int offset, int length) throws IOException {
		checkCanRead();
		Objects.requireNonNull(dsts, "array is null");
		if (length < 0 | offset < 0 | offset+length > dsts.length)
			throw new IndexOutOfBoundsException();

		long oldPos = position;
		long sum = 0;
		int rl = 0;
		for (int i = offset; i < offset+length; i++) {
			rl = readImpl(dsts[i]);
			if (rl == -1) break;
			sum += rl;
			position = oldPos + sum;
		}
		return rl == -1 ? -1 : sum;
	}

	@Override
	public long read (ByteBuffer[] dsts) throws IOException {
		return read(dsts, 0, dsts.length);
	}
	

	@Override
	public synchronized long position () throws ClosedChannelException {
		checkClosedState();
		return position;
	}

	@Override
	public synchronized long size () throws IOException {
		checkClosedState();
		return getChannelSize();
	}

	@Override
	public synchronized SeekableByteChannel position (long newPosition) throws IOException {
		checkClosedState();
		Util.requirePositive(newPosition, "newPosition");
		position = newPosition;
		return this;
	}
	
	

	@Override
	public synchronized int read (ByteBuffer dst) throws IOException {
		checkCanRead();
		Objects.requireNonNull(dst);
		
		long oldPos = position;
		int rlen = readImpl(dst);
		position = oldPos + Math.max(0, rlen); 
		return rlen;
	}

	/** Reads a sequence of bytes from this channel into the given buffer, 
	 * starting at the given channel position. This method does not involve
	 * the channel's current position.
	 *  
	 * @param dst {@code ByteBuffer} target data buffer
	 * @param offset long start position in the channel
	 * @return int length of data read, possibly zero, or -1 if the given 
	 * position is greater than or equal to the channel's current size
	 * @throws IOException 
	 */
	public synchronized int read (ByteBuffer dst, long offset) throws IOException {
		checkCanRead();
		Util.requirePositive(offset, "position");
		long oldPos = position;
		position = offset;
		try {
			return readImpl(dst);
		} finally {
			position = oldPos;
		}
	}
	
	@Override
	public synchronized int write (ByteBuffer src) throws IOException {
		checkCanWrite();
		Objects.requireNonNull(src);
		
		long oldPos = position;
		if (isAppend) {
			position = getChannelSize();
		}
		int wlen = 0;
		try {
			wlen = writeImpl(src);
		} finally {
			if (isAppend) {
				position = oldPos;
			} else {
				position = oldPos + wlen;
			}
		}
		return wlen;
	}

	/** Writes a sequence of bytes to this channel from the given buffer, 
	 * starting at the given channel position. This method does not involve
	 * the channel's current position.
	 *  
	 * @param src {@code ByteBuffer} source data buffer
	 * @param offset long start position in the channel
	 * @return int length of data written, possibly zero 
	 * @throws IOException 
	 */
	public synchronized int write (ByteBuffer src, long offset) throws IOException  {
		checkCanWrite();
		Objects.requireNonNull(src);
		Util.requirePositive(offset, "position");
		
		long oldPos = position;
		position = offset;
		try {
			return write(src);
		} finally {
			position = oldPos;
		}
	}
	
	@Override
	public synchronized SeekableByteChannel truncate (long size) throws IOException {
		checkCanWrite();
		Util.requirePositive(size, "size");
		if (isAppend) 
			throw new UnsupportedOperationException("forbidden to truncate in APPEND mode");
		
		if (size < getChannelSize()) {
			truncateImpl(size);
		}

		// catch high-flying channel position
		if (position > size) {
			position = size;
		}
		return this;
	}

	/** Basic read method as described in 
	 * java.nio.channels.ReadableByteChannel.read(ByteBuffer).
	 * This implementation does not need to synchronise or call 
	 * {@code checkCanRead()}. It also does not need to update the channels
	 * 'position' value.
	 * 
	 * @param dst {@code ByteBuffer}
	 * @return int number of bytes read, possibly zero, -1 if end of channel
	 *         is reached
	 * @throws IOException
	 */
	protected abstract int readImpl (ByteBuffer dst) throws IOException;

	/** Basic write method as described in 
	 * java.nio.channels.WritableByteChannel.write(ByteBuffer).
	 * This implementation does not need to synchronise or call 
	 * {@code checkCanWrite()}. It also does not need to update the channels
	 * 'position' value.
	 * 
	 * @param src {@code ByteBuffer}
	 * @return int number of bytes written, possibly zero
	 * @throws IOException
	 */
	protected abstract int writeImpl (ByteBuffer src) throws IOException;

	/** Basic method to set the channel size as described in 
	 * java.nio.channels.SeekableByteChannel.truncate(long).
	 * This implementation does not need to synchronise or call 
	 * {@code checkCanWrite()}. 
	 * 
	 * @param size long file length limit
	 * @throws IOException
	 */
	protected abstract void truncateImpl (long size) throws IOException;
	
	/** Basic method to retrieve the current data volume of this channel.
	 *  
	 * @return long length in bytes
	 * @throws IOException
	 */
	protected abstract long getChannelSize ()  throws IOException;

	/** Basic method to close this channel. This method is called only once
	 * by {@code AbstractByteChannel} even if there are multiple calls to its
	 * 'close()' method.
	 * 
	 * @throws IOException
	 */
	protected abstract void closeImpl() throws IOException;

}

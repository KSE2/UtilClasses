package kse.utilclass2.io;

/*
*  File: CoreByteChannel.java
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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import kse.utilclass.misc.Util;

/** This class implements the {@code SeekableByteChannel} interface with a 
 * core memory base. Channel size and the position are limited to the type 
 * 'int' value range. At any time while the channel is open the current state
 * of the content can be obtained as a byte array copy.
 * 
 * <p>Operation modi can be set over the constructor. They are:
 * <br>READ-ONLY, WRITE-ONLY, READ-WRITE, APPEND-ONLY, READ-APPEND. In both
 * APPEND modi all write-methods will always append to the top of the channel
 * and disregard position settings or parameters. In APPEND mode truncation of
 * the channel is disallowed. 
 * 
 *  @author Wolfgang Keller
 */
public class CoreByteChannel extends AbstractByteChannel 
							 implements SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel {

	private byte[] buffer = new byte[0];
	private int channelSize;

	/** Creates a new, empty {@code CoreByteChannel} with default properties.
	 * <p>This channel is open for reading and writing.
	 */
	public CoreByteChannel () {
		this("rw");
	}

	/** Creates a new  {@code CoreByteChannel} with default properties and
	 * the given initial content. The channel's position is at zero; 
	 * the argument is copied into a new buffer.
	 * <p>This channel is open for reading and writing.
	 * 
	 * @param content byte[] initial content
	 */
	public CoreByteChannel (byte[] content) {
		this(content, "rw");
	}

	/** Creates a new, empty {@code CoreByteChannel} with properties as given 
	 * in the modus parameter.
	 * <p>This channel is open for possibly reduced or specialised abilities 
	 * for reading and writing. As modus can be stated: 'r' for reading,
	 * 'w' for writing, 'rw' for reading and writing, 'a' for append mode
	 * (writing only) and 'ra' for append mode with reading. 
	 */
	public CoreByteChannel (String modus) {
		super(modus);
	}

	/** Creates a new  {@code CoreByteChannel} with default properties and
	 * the given initial content. The channel's position is at zero; 
	 * the argument content is copied into a new buffer.
	 * <p>This channel is open for possibly reduced or specialised abilities 
	 * for reading and writing. As modus can be stated: 'r' for reading,
	 * 'w' for writing, 'rw' for reading and writing, 'a' for append mode
	 * (writing only) and 'ra' for append mode with reading. 
	 * 
	 * @param content byte[] initial content
	 * @param modus String operation parameters
	 */
	public CoreByteChannel (byte[] content, String modus) {
		super(modus);
		buffer = Util.arraycopy(content);
		channelSize = content.length;
	}

	@Override
	protected void closeImpl() {
		buffer = null;
		channelSize = 0;
	}

	private void checkPositionValue (long value) {
		if (value > Integer.MAX_VALUE) 
			throw new IndexOutOfBoundsException("illegal channel position: " + value);
	}
	
	@Override
	protected int readImpl (ByteBuffer dst) throws IOException {
		checkPositionValue(position());
		int position = (int) position();
		
		// end-of-file signal
		if (position >= channelSize) return -1;

		// transfer data into buffer and update position
		int len = Math.min(channelSize-position, dst.remaining());
		dst.put(buffer, position, len);
		return len;
	}

	@Override
	protected int writeImpl (ByteBuffer src) throws ClosedChannelException {
		checkPositionValue(position() + src.remaining());
		int position = (int) position();
		
		// enlarge buffer if required
		int tlen = src.remaining();
		int clen = position + tlen;
		ensureBufferSize(clen);
		
		// transfer from source buffer
		src.get(buffer, position, tlen);
		channelSize = Math.max(channelSize, clen);
		return tlen;
	}

	@Override
	protected void truncateImpl (long size) throws ClosedChannelException {
		if (size > Integer.MAX_VALUE) return;

		if (size < channelSize) {
			// reduce buffer size if remainder is over-sized
			int request = (int) size;
			int limit = (int) Math.min((request * 1.75), Integer.MAX_VALUE);
			if (buffer.length > limit) {
				byte[] newBuf = new byte[limit];
				System.arraycopy(buffer, 0, newBuf, 0, request);
				buffer = newBuf;
			}
			channelSize = request;
		}
	}

	/** Returns a new byte array containing the current content of this channel.
	 * 
	 * @return byte[]
	 * @throws ClosedChannelException 
	 */
	public byte[] toArray () throws ClosedChannelException   {
		checkClosedState();
		int length = channelSize;
		byte[] a = new byte[length];
		System.arraycopy(buffer, 0, a, 0, length);
		return a;
	}
	
	/** Ensures this channel's buffer size is adequate to the given channel 
	 * size request. Does not change channel size and position. 
	 * 
	 * @param length int requested channel size
	 */
	private void ensureBufferSize (int length) {
		int newLen;
		byte[] newBuf;
		
		if (buffer.length < length) {
			newLen = Math.max(length,  (int) (buffer.length * 1.75));
			newBuf = new byte[newLen];
			System.arraycopy(buffer, 0, newBuf, 0, channelSize);
			buffer = newBuf;
		}
	}
	
	private int transferImpl2 (ReadableByteChannel src, WritableByteChannel target, 
			int offset, int count) throws IOException {
	Objects.requireNonNull(src);
	Objects.requireNonNull(target);
	Util.requirePositive(offset, "position");
	Util.requirePositive(count, "count");
	if (count == 0) return 0;
	
	int length;
	ByteBuffer buf;
	if (src == this) {
		// we are source
		int delta = offset + count - channelSize;
		if (delta >= count) return 0;
		buf = ByteBuffer.wrap(buffer, offset, count - Math.max(delta, 0));
		length = target.write(buf);
	} else {
		// we are target
		checkPositionValue((long)offset + count);
		ensureBufferSize(offset + count);
		buf = ByteBuffer.wrap(buffer, offset, count);
		length = src.read(buf);
		if (length > 0) {
			channelSize = Math.max(channelSize, offset + length);
		}
	}
	return Math.max(length, 0);
	}

	/** Transfers bytes into this channel from the given readable byte channel,
	 * starting to write at the given position in this channel. This will not
	 * modify the position pointer of this channel. 
	 * <p>Fewer than the requested number of bytes will be transferred if the 
	 * source channel has fewer than count bytes remaining, or if the source 
	 * is non-blocking and has fewer than count bytes available in its input 
	 * buffer. If the given position is greater than the current size then
	 * this channel will be expanded accordingly. 
	 *   
	 * @param src {@code ReadableByteChannel}
	 * @param position int start position in this channel
	 * @param count int number of bytes to read
	 * @return int number of bytes transferred
	 * @throws IllegalArgumentException
	 * @throws ClosedChannelException
	 * @throws NonReadableChannelException
	 * @throws NonWritableChannelException
	 * @throws IOException
	 */
	public synchronized int transferFrom (ReadableByteChannel src, int position, int count) 
			throws IOException {
		checkCanWrite();
		if (isAppend()) {
			position = channelSize;
		}
		return transferImpl2(src, this, position, count);
	}
	
	/** Transfers bytes from this channel to the given writable byte channel,
	 * starting to read from the given position in this channel. This will not
	 * modify the position pointer of this channel. 
	 * <p>Fewer than the requested number of bytes will be transferred if this 
	 * channel has fewer than count bytes remaining from the given position or
	 * if the target channel is non-blocking and it has fewer than count bytes 
	 * free in its output buffer. If the given position is greater than the 
	 * current size of this channel then no bytes will be transferred.
	 * 
	 * @param position int start position in this channel
	 * @param count int number of bytes to write
	 * @param target {@code WritableByteChannel}
	 * @return int number of bytes transferred
	 * @throws IllegalArgumentException
	 * @throws ClosedChannelException
	 * @throws NonReadableChannelException
	 * @throws NonWritableChannelException
	 * @throws IOException
	 */
	public synchronized int transferTo (int position, int count, WritableByteChannel target) 
			throws IOException {
		checkCanRead();
		return  transferImpl2(this, target, position, count);
	}

	@Override
	protected long getChannelSize() throws IOException {
		return channelSize;
	}
}

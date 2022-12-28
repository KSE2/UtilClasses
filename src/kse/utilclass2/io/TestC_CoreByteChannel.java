package kse.utilclass2.io;

/*
*  File: TestC_CoreByteChannel.java
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;

import org.junit.Test;

import kse.utilclass.misc.Util;

public class TestC_CoreByteChannel {

	public TestC_CoreByteChannel() {
	}

	@Test
	public void init () throws IOException {
		CoreByteChannel ch = new CoreByteChannel();
		assertTrue(ch.isOpen());
		assertTrue(ch.size() == 0);
		assertTrue(ch.position() == 0);
		assertNotNull(ch.toArray());
		assertTrue(ch.toArray().length == 0);
		ch.close();
		
		int dlen = 500;
		byte[] data = Util.randBytes(dlen);
		ch = new CoreByteChannel(data);
		assertTrue(ch.isOpen());
		assertTrue(ch.size() == dlen);
		assertTrue(ch.position() == 0);
		assertNotNull(ch.toArray());
		assertTrue(ch.toArray().length == 500);
		assertTrue("initial content error", Util.equalArrays(data, ch.toArray()));

		ch.close();
		assertFalse(ch.isOpen());
	}

	@SuppressWarnings("resource")
	@Test
	public void position () throws IOException {
		// empty channel
		CoreByteChannel ch = new CoreByteChannel();
		ch.position(100);
		assertTrue("position not set beyond channel size", ch.position() == 100);
		
		// write propagation
		byte[] data = Util.randBytes(100);
		ch.write(ByteBuffer.wrap(data));
		assertTrue("position not propagated if beyond start", ch.position() == 200);
		
		// back position
		ch.position(56);
		assertTrue("position not set backstep", ch.position() == 56);
		
		// read propagation
		ch.read(ByteBuffer.allocate(100));
		assertTrue("position not propagated through reading", ch.position() == 156);

		// modification through truncate
		assertTrue(ch.size() == 200);
		ch.position(200);
		ch.truncate(100);
		assertTrue("position not corrected through truncate", ch.position() == 100);
		
		// zero position on initial content
		ch = new CoreByteChannel(Util.randBytes(200));
		assertTrue("expected zero position (initial content)", ch.position() == 0);
		
		// FAILURES
		
		try {
			ch.position(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		ch.close();
		try {
			ch.position();
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.position(500);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void write_1 () throws IOException {
		// write from channel position (empty channel)
		CoreByteChannel ch = new CoreByteChannel();
		byte[] data = Util.randBytes(100);
		ByteBuffer buf1 = ByteBuffer.wrap(data);
		int wlen = ch.write(buf1);
		
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 100);
		assertTrue("data error", Util.equalArrays(ch.toArray(), data));
		assertTrue(buf1.remaining() == 0);
		
		// write second parcel
		byte[] data2 = Util.randBytes(200);
		buf1 = ByteBuffer.wrap(data2);
		buf1.position(100);
		wlen = ch.write(buf1);
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 200);
		assertTrue("data error", Util.equalArrays(ch.toArray(), 
				Util.concatArrays(data, Util.arraycopy(data2, 100, 100))));
		assertTrue(buf1.remaining() == 0);

		// write in unreal position
		data = Util.randBytes(100);
		buf1 = ByteBuffer.wrap(data);
		ch.position(400);
		wlen = ch.write(buf1);
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 500);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 400, 100), data));
		
		// write in middle position (backstep)
		buf1.rewind();
		ch.position(250);
		wlen = ch.write(buf1);
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 500);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 250, 100), data));
		ch.close();

		// FAILURE closed channel
		try {
			ch.write(buf1);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void write_2 () throws IOException {
		// write from channel start position (empty channel)
		CoreByteChannel ch = new CoreByteChannel();
		byte[] data = Util.randBytes(100);
		ByteBuffer buf1 = ByteBuffer.wrap(data);
		int wlen = ch.write(buf1, 0);
		
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 100);
		assertTrue("error in channel position", ch.position() == 0); // we don't change this value
		assertTrue("data error", Util.equalArrays(ch.toArray(), data));
		assertTrue(buf1.remaining() == 0);

		int chPos = 80;
		ch.position(chPos);
		
		// write at extended position (beyond channel size)
		buf1.rewind();
		wlen = ch.write(buf1, 900);
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 1000);
		assertTrue("error in channel position", ch.position() == chPos); // we don't change this value
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 900, 100), data));
		assertTrue("data error (old data)", Util.equalArrays(Util.arraycopy(ch.toArray(), 0, 100), data));
		assertTrue(buf1.remaining() == 0);
		ch.close();

		// write insert to existing test
		data = Util.randBytes(1000);
		ch = new CoreByteChannel(data);
		
		byte[] data2 = Util.randBytes(200);
		buf1 = ByteBuffer.wrap(data2);
		wlen = ch.write(buf1, 400);

		assertTrue("error in write length", wlen == 200);
		assertTrue("error in channel size", ch.size() == 1000);
		assertTrue("error in channel position", ch.position() == 0);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 400, 200), data2));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 0, 400),
				   Util.arraycopy(data, 0, 400)));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 600, 400),
				   Util.arraycopy(data, 600, 400)));
		
		ch.close();

		// FAILURE closed channel
		try {
			ch.write(buf1, 200);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void write_3 () throws IOException {
		// write from channel start position (empty channel)
		byte[][] data = new byte[][] {Util.randBytes(100), Util.randBytes(200), Util.randBytes(400)};

		ByteBuffer buffA1[] = new ByteBuffer[] {ByteBuffer.wrap(data[0])};
		ByteBuffer buffA2[] = new ByteBuffer[] {ByteBuffer.wrap(data[0]), ByteBuffer.wrap(data[1])};
		ByteBuffer buffA3[] = new ByteBuffer[] {ByteBuffer.wrap(data[0]), ByteBuffer.wrap(data[1]), ByteBuffer.wrap(data[2])};
		
		CoreByteChannel ch = new CoreByteChannel();

		// write single element buffer array
		long wlen = ch.write(buffA1);
		assertTrue("error in write length", wlen == 100);
		assertTrue("error in channel size", ch.size() == 100);
		assertTrue("error in channel position", ch.position() == 100);
		assertTrue("data error", Util.equalArrays(ch.toArray(), data[0]));
		assertTrue(buffA1[0].remaining() == 0);

		// append double element buffer array at position
		ch.position(1000);
		wlen = ch.write(buffA2);
		assertTrue("error in write length", wlen == 300);
		assertTrue("error in channel size", ch.size() == 1300);
		assertTrue("error in channel position", ch.position() == 1300);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1000, 100), data[0]));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1100, 200), data[1]));

		// does not make sense to re-apply
		assertTrue("error in write length", ch.write(buffA2) == 0);
		
		// write addressed buffer element
		ch.position(500);
		wlen = ch.write(buffA3, 2, 1);
		assertTrue("error in write length", wlen == 400);
		assertTrue("error in channel size", ch.size() == 1300);
		assertTrue("error in channel position", ch.position() == 900);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 500, 400), data[2]));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1000, 100), data[0]));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1100, 200), data[1]));
		assertTrue(buffA1[0].remaining() == 0);
		
		// write addressed buffer element
		wlen = ch.write(buffA3, 0, 3);
		assertTrue("error in write length", wlen == 300);
		assertTrue("error in channel size", ch.size() == 1300);
		assertTrue("error in channel position", ch.position() == 1200);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 900, 100), data[0]));
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1000, 200), data[1]));
		
		// FAILURES
		try {
			ch.write(buffA3, -1, 1);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
		}
		
		try {
			ch.write(buffA3, 1, 3);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
		}
		
		ch.close();
		try {
			ch.write(buffA3, 1, 1);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.write(buffA3);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void read_1 () throws IOException {
		// empty channel
		CoreByteChannel ch = new CoreByteChannel();

		// read from channel (empty channel)
		ByteBuffer buf1 = ByteBuffer.allocate(200);
		int rlen = ch.read(buf1);
		assertTrue("error reading on empty channel", rlen == -1);
		assertTrue("buffer error", buf1.position() == 0);
		
		// channel w/ 1000 random content
		byte[] data = Util.randBytes(1000);
		ch.write(ByteBuffer.wrap(data));
		ch.position(0);

		// read from beginning
		rlen = ch.read(buf1);
		assertTrue("read length error, was " + rlen, rlen == 200);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 0, 200), buf1.array()));
		assertTrue("buffer error", buf1.position() == 200);

		// read w/ empty buffer
		rlen = ch.read(buf1);
		assertTrue("read length error", rlen == 0);
		
		// read second segment
		buf1.rewind();
		rlen = ch.read(buf1);
		assertTrue("read length error, was " + rlen, rlen == 200);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 200, 200), buf1.array()));
		assertTrue("buffer error", buf1.position() == 200);

		// read end segment w/ position, half
		ch.position(900);
		buf1.rewind();
		rlen = ch.read(buf1);
		assertTrue("read length error, was " + rlen, rlen == 100);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 900, 100), 
				   Util.arraycopy(buf1.array(), 0, 100)));
		assertTrue("buffer error", buf1.position() == 100);
		
		// EOF signal
		rlen = ch.read(buf1);
		assertTrue("EOF signal error", rlen == -1);
		ch.position(2000);
		rlen = ch.read(buf1);
		assertTrue("EOF signal error", rlen == -1);
		
		// read half buffer
		ch.position(300);
		rlen = ch.read(buf1);
		assertTrue("read length error", rlen == 100);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 300, 100), 
				   Util.arraycopy(buf1.array(), 100, 100)));
		assertTrue("buffer error", buf1.position() == 200);
		
		assertTrue(ch.size() == 1000);
		ch.close();
		
		// read from initial content
		ch = new CoreByteChannel(data);
		buf1.rewind();
		rlen = ch.read(buf1);
		assertTrue("read length error", rlen == 200);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 0, 200), buf1.array()));
		assertTrue(ch.size() == 1000);

		ch.close();
		
		// FAILURE closed channel
		try {
			ch.read(buf1);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void read_2 () throws IOException {
		// empty channel
		CoreByteChannel ch = new CoreByteChannel();

		// read from channel (empty channel)
		ByteBuffer buf1 = ByteBuffer.allocate(200);
		int rlen = ch.read(buf1, 0);
		assertTrue("error reading on empty channel", rlen == -1);
		assertTrue("buffer error", buf1.position() == 0);
		assertTrue("error in channel size", ch.size() == 0);
		assertTrue("error in channel position", ch.position() == 0);
		ch.close();

		// preset content channel
		byte[] data = Util.randBytes(1000);
		ch = new CoreByteChannel(data);

		// read from zero
		rlen = ch.read(buf1, 0);
		assertTrue("read length error", rlen == 200);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 0, 200), buf1.array()));
		assertTrue("error in channel position", ch.position() == 0); // we don't change this value
		assertTrue("buffer error", buf1.position() == 200);

		int chPos = 2000;
		ch.position(chPos);
		
		// read middle
		buf1.position(100);
		rlen = ch.read(buf1, 200);
		
		assertTrue("read length error", rlen == 100);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 200, 100), 
					Util.arraycopy(buf1.array(), 100, 100)));
		assertTrue("error in channel position", ch.position() == chPos);
		assertTrue("buffer error", buf1.position() == 200);

		// encounter end-of-channel
		buf1.rewind();
		rlen = ch.read(buf1, 900);
		assertTrue("read length error", rlen == 100);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 900, 100), 
					Util.arraycopy(buf1.array(), 0, 100)));
		assertTrue("error in channel position", ch.position() == chPos);
		assertTrue("buffer error", buf1.position() == 100);
		
		// outer space position
		buf1.rewind();
		rlen = ch.read(buf1, 1000);
		assertTrue("read length error", rlen == -1);
		assertTrue("error in channel position", ch.position() == chPos);
		assertTrue("buffer error", buf1.position() == 0);
		
		ch.close();
	}
	
	@Test
	public void read_3 () throws IOException {
		ByteBuffer buffA1[] = new ByteBuffer[] {ByteBuffer.allocate(200)};
		ByteBuffer buffA3[] = new ByteBuffer[] {ByteBuffer.allocate(100), ByteBuffer.allocate(200), ByteBuffer.allocate(400)};

		byte[] data = Util.randBytes(2000);
		CoreByteChannel ch = new CoreByteChannel(data);

		// read single element
		long rlen = ch.read(buffA1);
		assertTrue("read length error", rlen == 200);
		assertTrue("error in channel position, was " + ch.position(), ch.position() == 200);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 0, 200), buffA1[0].array()));
		assertTrue("buffer error", buffA1[0].remaining() == 0);
		
		// read multiple elements
		rlen = ch.read(buffA3, 0, 3);
		assertTrue("read length error", rlen == 700);
		assertTrue("error in channel position", ch.position() == 900);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 200, 100), buffA3[0].array()));
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 300, 200), buffA3[1].array()));
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(data, 500, 400), buffA3[2].array()));
		assertTrue("buffer error", buffA1[0].remaining() == 0);
		
		// does not make sense to re-apply
		assertTrue("error in write length", ch.read(buffA3) == 0);
		
		// FAILURES
		try {
			ch.read(buffA3, -1, 1);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
		}
		
		try {
			ch.read(buffA3, 1, 3);
			fail("expected IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
		}
		
		ch.close();
		try {
			ch.read(buffA3, 1, 1);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.read(buffA3);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
	}
	
	@Test
	public void truncate () throws IOException {
		// channel w/ 1000 random content
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data);
		
		ByteBuffer buf1 = ByteBuffer.allocate(2000);
		int rlen = ch.read(buf1);
		assertTrue("read length error", rlen == 1000);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(buf1.array(), 0, 1000), data));

		// truncate to 500
		ch.truncate(500);
		assertTrue("channel position error", ch.position() == 500);
		assertTrue("channel size error", ch.size() == 500);

		buf1.rewind();
		rlen = ch.read(buf1);
		assertTrue("EOF signal expected", rlen == -1);

		ch.position(0);
		rlen = ch.read(buf1);
		assertTrue("read length error", rlen == 500);
		assertTrue("read data error", Util.equalArrays(Util.arraycopy(buf1.array(), 0, 500), 
				Util.arraycopy(data, 0, 500)));
		
		// conserve channel position if feasible
		ch.position(100);
		ch.truncate(300);
		assertTrue("channel position error", ch.position() == 100);
		
		// truncate to outer space
		ch.position(3500);
		ch.truncate(2000);
		assertTrue("channel position error", ch.position() == 2000);
		assertTrue("channel size error", ch.size() == 300);
		
		// truncate to zero
		ch.truncate(0);
		assertTrue("channel position error", ch.position() == 0);
		assertTrue("channel size error", ch.size() == 0);
		buf1.rewind();
		rlen = ch.read(buf1);
		assertTrue("EOF signal expected", rlen == -1);

		// FAILURES
		try {
			ch.truncate(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		ch.close();
	}
	
	@Test
	public void close () throws IOException {
		// channel w/ 1000 random content
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data);
		ch.position(300);
		ch.close();
		
		ByteBuffer buf = ByteBuffer.allocate(2000);
		ByteBuffer[] bufArr = new ByteBuffer[] {ByteBuffer.allocate(200), ByteBuffer.allocate(100), ByteBuffer.allocate(500)};
		
		try {
			ch.position();
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.position(200);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.write(buf);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.read(buf);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.size();
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.toArray();
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.truncate(2);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.write(bufArr);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.write(bufArr, 1, 1);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.read(bufArr);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		try {
			ch.read(bufArr, 1, 2);
			fail("expected ClosedChannelException");
		} catch (ClosedChannelException e) {
		}
		
		// multiple close calls
		ch.close();
		ch.isOpen();
	}
	
	@Test
	public void toArray () throws IOException {
		// channel w/ 1000 random content
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data);
		ch.position(1000);

		assertTrue(Util.equalArrays(data, ch.toArray()));
		assertTrue("position error", ch.position() == 1000);
		assertTrue("size error", ch.size() == 1000);
		
		ch.truncate(500);
		assertTrue(Util.equalArrays(Util.arraycopy(data, 0, 500), ch.toArray()));

		byte[] data2 = Util.randBytes(500);
		System.arraycopy(data2, 0, data, 500, 500);
		ch.write(ByteBuffer.wrap(data2));
		assertTrue(Util.equalArrays(data, ch.toArray()));
		
		ch.close();
	}
	
	@Test
	public void transfer_from () throws IOException {
		CoreByteChannel ch = new CoreByteChannel();
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch2 = new CoreByteChannel(data);
		
		// expand empty target
		ch.position(1000);
		int len = ch.transferFrom(ch2, 0, 500);
		
		assertTrue("transfer length error", len == 500);
		assertTrue("target position error", ch.position() == 1000);
		assertTrue("target size error, was " + ch.size(), ch.size() == 500);
		assertTrue("source position error", ch2.position() == 500);
		assertTrue("data error", Util.equalArrays(ch.toArray(), Util.arraycopy(data, 0, 500)));

		// expand target w/ offset position
		len = ch.transferFrom(ch2, 2000, 500);
		
		assertTrue("transfer length error", len == 500);
		assertTrue("target position error", ch.position() == 1000);
		assertTrue(ch.size() == 2500);
		assertTrue("source position error", ch2.position() == 1000);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 2000, 500), 
					Util.arraycopy(data, 500, 500)));

		// incomplete transfer
		ch2.position(750);
		len = ch.transferFrom(ch2, 1000, 1000);

		assertTrue("transfer length error", len == 250);
		assertTrue("target position error", ch.position() == 1000);
		assertTrue(ch.size() == 2500);
		assertTrue("source position error", ch2.position() == 1000);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 1000, 250), 
					Util.arraycopy(data, 750, 250)));
		
		// unreal source position
		ch2.position(1700);
		len = ch.transferFrom(ch2, 0, 1000);

		assertTrue("transfer length error", len == 0);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch.toArray(), 0, 500), 
				Util.arraycopy(data, 0, 500)));
		
		ch.close();
	}

	@Test
	public void transfer_to () throws IOException {
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data);
		CoreByteChannel ch2 = new CoreByteChannel();
		
		// expand empty target
		ch.position(2000);
		int len = ch.transferTo(0, 500, ch2);
		
		assertTrue("transfer length error", len == 500);
		assertTrue("source position error", ch.position() == 2000);
		assertTrue("target size error", ch2.size() == 500);
		assertTrue("target position error", ch2.position() == 500);
		assertTrue("data error", Util.equalArrays(ch2.toArray(), Util.arraycopy(data, 0, 500)));

		// unreal source position
		len = ch.transferTo(1000, 500, ch2);
		
		assertTrue("transfer length error", len == 0);
		assertTrue("target position error", ch.position() == 2000);
		assertTrue(ch.size() == 1000);
		assertTrue(ch2.size() == 500);
		assertTrue("target position error", ch2.position() == 500);
		assertTrue("data error", Util.equalArrays(ch2.toArray(), Util.arraycopy(data, 0, 500)));

		// incomplete transfer
		len = ch.transferTo(750, 1000, ch2);

		assertTrue("transfer length error", len == 250);
		assertTrue("source position error", ch.position() == 2000);
		assertTrue("target size error", ch2.size() == 750);
		assertTrue("target position error", ch2.position() == 750);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch2.toArray(), 500, 250), 
					Util.arraycopy(data, 750, 250)));
		
		// extended target position
		ch2.position(2000);
		len = ch.transferTo(0, 500, ch2);
		
		assertTrue("transfer length error", len == 500);
		assertTrue("source position error", ch.position() == 2000);
		assertTrue("target size error", ch2.size() == 2500);
		assertTrue("target position error", ch2.position() == 2500);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch2.toArray(), 2000, 500), 
					Util.arraycopy(data, 0, 500)));
		
		ch.close();
	}
	
	@Test
	public void read_only () throws IOException {
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data, "r");
		CoreByteChannel ch2 = new CoreByteChannel("r");
		CoreByteChannel ch3 = new CoreByteChannel(Util.randBytes(500));
		ByteBuffer buf1 = ByteBuffer.allocate(100);
		ByteBuffer buf2 = ByteBuffer.allocate(200);

		// test we can read and position in READ-ONLY modus
		int rlen = ch.read(buf1);
		assertTrue(rlen == 100);
		assertTrue(ch.position() == 100);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(data, 0, 100)));

		buf1.rewind();
		rlen = ch2.read(buf1);
		assertTrue(rlen == -1);
		
		ch.position(800);
		rlen = ch.read(buf1, 500);
		assertTrue(rlen == 100);
		assertTrue(ch.position() == 800);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(data, 500, 100)));
		
		buf1.rewind();
		ch2.position(800);
		rlen = ch2.read(buf1, 500);
		assertTrue(rlen == -1);
		assertTrue(ch2.position() == 800);
		
		buf1.rewind();
		ByteBuffer[] bufA1 = new ByteBuffer[] {buf1, buf2};
		ch.position(100);
		rlen = (int) ch.read(bufA1);
		assertTrue(rlen == 300);
		assertTrue(ch.position() == 400);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(data, 100, 100)));
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(data, 200, 200)));
		
		buf1.rewind();
		buf2.rewind();
		rlen = (int) ch2.read(bufA1);
		assertTrue("read length error, was " + rlen, rlen == -1);
		assertTrue(ch2.position() == 800);
		
		// transfers
		rlen = ch.transferTo(300, 300, ch3);
		assertTrue(rlen == 300);
		assertTrue(ch.position() == 400);
		assertTrue(ch3.position() == 300);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 0, 300),
						Util.arraycopy(data, 300, 300)));
		
		rlen = ch3.transferFrom(ch, 500, 100);
		assertTrue(ch3.size() == 600);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 500, 100),
				Util.arraycopy(data, 400, 100)));
		
		// test we cannot write
		ch.position(500);
		long size = ch.size();
		buf1.rewind();
		try {
			ch.write(buf1);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}
		
		try {
			ch.write(buf1, 400);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}
		
		try {
			ch.write(bufA1);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}
		
		try {
			ch.truncate(0);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}

		try {
			ch.transferFrom(ch3, 0, 100);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}

		try {
			ch3.transferTo(0, 100, ch);
			fail("expected NonWritableChannelException");
		} catch (NonWritableChannelException e) {
		}
		assertTrue(ch.size() == size);
		assertTrue(ch.position() == 500);
		
		ch.close();
		ch2.close();
	}

	private void test_cannot_read (CoreByteChannel ch) throws IOException {
		ByteBuffer buf1 = ByteBuffer.wrap(Util.randBytes(100));
		ByteBuffer buf2 = ByteBuffer.wrap(Util.randBytes(200));
		ByteBuffer[] bufA1 = new ByteBuffer[] {buf1, buf2};
		CoreByteChannel ch3 = new CoreByteChannel(Util.randBytes(500));

		ch.position(500);
		int size = (int) ch.size();
		buf1.rewind();
		try {
			ch.read(buf1);
			fail("expected NonReadableChannelException");
		} catch (NonReadableChannelException e) {
		}
		
		try {
			ch.read(buf1, 400);
			fail("expected NonReadableChannelException");
		} catch (NonReadableChannelException e) {
		}
		
		try {
			ch.read(bufA1);
			fail("expected NonReadableChannelException");
		} catch (NonReadableChannelException e) {
		}
		
		try {
			ch.transferTo(0, 100, ch3);
			fail("expected NonReadableChannelException");
		} catch (NonReadableChannelException e) {
		}

		try {
			ch3.transferFrom(ch, 0, 100);
			fail("expected NonReadableChannelException");
		} catch (NonReadableChannelException e) {
		}
		assertTrue(ch.size() == size);
		assertTrue(ch.position() == 500);
	}
	
	@Test
	public void write_only () throws IOException {
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel(data, "w");
		CoreByteChannel ch2 = new CoreByteChannel("w");
		CoreByteChannel ch3 = new CoreByteChannel(Util.randBytes(500));
		ByteBuffer buf1 = ByteBuffer.wrap(Util.randBytes(100));
		ByteBuffer buf2 = ByteBuffer.wrap(Util.randBytes(200));
		
		// test we can write and position in WRITE-ONLY modus
		int rlen = ch.write(buf1);
		assertTrue(rlen == 100);
		assertTrue(ch.position() == 100);
		assertTrue("data error", Util.equalArrays(buf1.array(), 
				Util.arraycopy(ch.toArray(), 0, 100)));

		buf1.rewind();
		rlen = ch2.write(buf1);
		assertTrue(rlen == 100);
		assertTrue(ch2.position() == 100);
		assertTrue(ch2.size() == 100);
		assertTrue("data error", Util.equalArrays(buf1.array(), 
				Util.arraycopy(ch2.toArray(), 0, 100)));
		
		ch.position(800);
		rlen = ch.write(buf2);
		assertTrue(rlen == 200);
		assertTrue(ch.position() == 1000);
		assertTrue(ch.size() == 1000);
		assertTrue("data error", Util.equalArrays(buf2.array(), 
				Util.arraycopy(ch.toArray(), 800, 200)));
		
		buf1.rewind();
		ch.position(200);
		rlen = ch.write(buf1, 500);
		assertTrue(rlen == 100);
		assertTrue(ch.position() == 200);
		assertTrue(ch.size() == 1000);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch.toArray(), 500, 100)));
		
		buf1.rewind();
		ch2.position(200);
		rlen = ch2.write(buf1, 500);
		assertTrue(rlen == 100);
		assertTrue(ch2.position() == 200);
		assertTrue(ch2.size() == 600);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch2.toArray(), 500, 100)));
		
		buf1.rewind();
		buf2.rewind();
		ByteBuffer[] bufA1 = new ByteBuffer[] {buf1, buf2};
		ch.position(100);
		rlen = (int) ch.write(bufA1);
		assertTrue(rlen == 300);
		assertTrue(ch.position() == 400);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch.toArray(), 100, 100)));
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(ch.toArray(), 200, 200)));
		
		// transfers
		rlen = ch.transferFrom(ch3, 300, 300);
		assertTrue(rlen == 300);
		assertTrue(ch.position() == 400);
		assertTrue(ch3.position() == 300);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 0, 300),
						Util.arraycopy(ch.toArray(), 300, 300)));
		
		rlen = ch3.transferTo(200, 100, ch);
		assertTrue(rlen == 100);
		assertTrue("size was " + ch.size(), ch.size() == 1000);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 200, 100),
				Util.arraycopy(ch.toArray(), 400, 100)));
		
		// truncate does
		ch.truncate(800);
		assertTrue(ch.size() == 800);
		long size = ch2.size();
		ch2.truncate(800);
		assertTrue(ch2.size() == size);

		// test we cannot read
		test_cannot_read(ch);
		
		ch.close();
		ch2.close();
	}

	@Test
	public void append_only () throws IOException {
		byte[] data = Util.randBytes(1000);
		CoreByteChannel ch = new CoreByteChannel("a");
		CoreByteChannel ch2 = new CoreByteChannel(data, "a");
		CoreByteChannel ch3 = new CoreByteChannel(Util.randBytes(500));
		ByteBuffer buf1 = ByteBuffer.wrap(Util.randBytes(100));
		ByteBuffer buf2 = ByteBuffer.wrap(Util.randBytes(200));
		ByteBuffer[] bufA1 = new ByteBuffer[] {buf1, buf2};
		
		ch.write(buf1);
		assertTrue(ch.size() == 100);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf1.array(), ch.toArray()));

		buf1.rewind();
		ch2.write(buf1);
		assertTrue(ch2.size() == 1100);
		assertTrue(ch2.position() == 0);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch2.toArray(), 1000, 100)));
		
		ch.write(buf2, 700);
		assertTrue(ch.size() == 300);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf2.array(),  Util.arraycopy(ch.toArray(), 100, 200)));

		ch2.position(380);
		buf2.rewind();
		ch2.write(buf2, 500);
		assertTrue("channel size error", ch2.size() == 1300);
		assertTrue(ch2.position() == 380);
		assertTrue("data error", Util.equalArrays(buf2.array(),  Util.arraycopy(ch2.toArray(), 1100, 200)));

		buf1.rewind();
		buf2.rewind();
		ch.write(bufA1);
		assertTrue("channel size error", ch.size() == 600);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch.toArray(), 300, 100)));
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(ch.toArray(), 400, 200)));
		
		buf1.rewind();
		buf2.rewind();
		ch2.write(bufA1);
		assertTrue("channel size error", ch2.size() == 1600);
		assertTrue(ch2.position() == 380);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch2.toArray(), 1300, 100)));
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(ch2.toArray(), 1400, 200)));
		
		// transfer from
		ch.transferFrom(ch3, 0, 300);
		assertTrue("channel size error", ch.size() == 900);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 0, 300), 
					Util.arraycopy(ch.toArray(), 600, 300)));
		
		// test we cannot read
		test_cannot_read(ch);
		
		// FAILURE
		
		try {
			ch.truncate(0);
			fail("expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		
		try {
			ch2.truncate(0);
			fail("expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		
		ch.close();
		ch2.close();
	}
	
	@Test
	public void append_read () throws IOException {
		CoreByteChannel ch = new CoreByteChannel("ra");
		CoreByteChannel ch3 = new CoreByteChannel(Util.randBytes(500));
		ByteBuffer buf1 = ByteBuffer.wrap(Util.randBytes(100));
		ByteBuffer buf2 = ByteBuffer.wrap(Util.randBytes(200));
		ByteBuffer[] bufA1 = new ByteBuffer[] {buf1, buf2};
		
		ch.write(buf1);
		assertTrue(ch.size() == 100);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf1.array(), ch.toArray()));

		ch.write(buf2, 700);
		assertTrue(ch.size() == 300);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf2.array(),  Util.arraycopy(ch.toArray(), 100, 200)));

		buf1.rewind();
		buf2.rewind();
		ch.write(bufA1);
		assertTrue("channel size error", ch.size() == 600);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch.toArray(), 300, 100)));
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(ch.toArray(), 400, 200)));
		
		// transfer from
		ch.transferFrom(ch3, 0, 300);
		assertTrue("channel size error", ch.size() == 900);
		assertTrue(ch.position() == 0);
		assertTrue("data error", Util.equalArrays(Util.arraycopy(ch3.toArray(), 0, 300), 
					Util.arraycopy(ch.toArray(), 600, 300)));
		
		// test we can read
		buf1 = ByteBuffer.allocate(100);
		buf2 = ByteBuffer.allocate(200);
		bufA1 = new ByteBuffer[] {buf1, buf2};
		
		int rlen = ch.read(buf1);
		assertTrue(rlen == 100);
		assertTrue(ch.position() == 100);
		assertTrue(ch.size() == 900);
		assertTrue("data error", Util.equalArrays(buf1.array(), Util.arraycopy(ch.toArray(), 0, 100)));
		
		rlen = ch.read(buf2, 300);
		assertTrue(rlen == 200);
		assertTrue(ch.position() == 100);
		assertTrue(ch.size() == 900);
		assertTrue("data error", Util.equalArrays(buf2.array(), Util.arraycopy(ch.toArray(), 300, 200)));
		
		buf1.rewind();
		buf2.rewind();
		rlen = (int) ch.read(bufA1);
		assertTrue(rlen == 300);
		assertTrue(ch.position() == 400);
		assertTrue(ch.size() == 900);
		
		// FAILURE
		
		try {
			ch.truncate(0);
			fail("expected UnsupportedOperationException");
		} catch (UnsupportedOperationException e) {
		}
		
		ch.close();
	}
		
}

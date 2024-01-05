package kse.utilclass.io;

/*
*  File: IOService.java
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.concurrent.Semaphore;

/** Singleton class providing file access synchronisation and XML marshalling /
 * unmarshalling using the Java JAXB package. For using XML-services the
 * application has to annotate the intended classes as described in
 * javax.xml.bind.
 */
public class IOService {

	private static IOService instance = new IOService();

	/** Returns the singleton IOService. 
	 * 
	 * @return {@code IOService}
	 */
	public static IOService get () {
		return instance;
	}
	

	private Hashtable<File, Semaphore> semaphTable = new Hashtable<>();

	
	private IOService () {
	}
	
	/** Holds the current thread until an access permit is available
	 * for the given file. When this method returns, access to the file
	 * is reserved until a corresponding releaseFileAccess() is called.
	 * Does nothing on a null argument.
	 * 
	 * @param file {@code File} permit reference; may be null
	 * @throws InterruptedException if the current thread is interrupted 
	 * 			while waiting for a permit
	 */
	public void acquireFileAccess (File file) throws InterruptedException {
		if (file != null) {
			Semaphore sema;
			synchronized (semaphTable) {
				sema = semaphTable.get(file);
				if (sema == null) {
					sema = new Semaphore(1, true);
					semaphTable.put(file, sema);
				}
			}
			sema.acquire();
		}
	}
	
	/** Returns a file access permit to the access pool.
	 * Does nothing on a null argument.
	 * 
	 * @param file {@code File} permit reference; may be null 
	 */
	public synchronized void releaseFileAccess (File file) {
		if (file != null) {
			Semaphore sema = semaphTable.get(file);
			if (sema != null && sema.availablePermits() == 0) {
				sema.release();
			}
		}
	}
	
	/** Whether access to the given file is currently blocked in this service
	 * (through use of method 'acquireFileAccess()').
	 *  
	 * @param file File
	 * @return boolean true == file is blocked, false == file is not blocked
	 */
	public boolean isFileAccessBlocked (File file) {
		boolean res = false;
		if (file != null) {
			Semaphore sema = semaphTable.get(file);
			res = sema != null && sema.availablePermits() == 0;
		}
		return res;
	}
	
	/** Waits until access permit is available to the given file and then
	 * creates and returns a {@code FileInputStream} for this file. Access
	 * to the file is reserved until the stream's 'close()' method is called.
	 * <p>NOTE: This method also blocks file access for secondary input streams
	 * (iterated calls to this method), hence the returned stream should be 
	 * closed as soon as possible.
	 * 
	 * @param file File file of the input stream
	 * @return {@code FileInputStream}
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public FileInputStream getBlockingFileInputStream (File file) 
				throws FileNotFoundException, InterruptedException {
		acquireFileAccess(file);
		FileInputStream in;
		try {
			in = new FileBlockingInputStream(file);
			return in;
		} catch (FileNotFoundException e) {
			releaseFileAccess(file);
			throw e;
		}
	}
	
	/** Waits until access permit is available to the given file and then
	 * creates and returns a {@code FileOutputStream} for this file. Access
	 * to the file is reserved in the service until the stream's 'close()' 
	 * method is called.
	 * 
	 * @param file File file of the output stream
	 * @param append boolean if true written bytes will be appended to an
	 *        existing file, if false an existing file will be overwritten 
	 * @return {@code FileOutputStream}
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public FileOutputStream getBlockingFileOutputStream (File file, boolean append) 
				throws FileNotFoundException, InterruptedException {
		acquireFileAccess(file);
		FileOutputStream out;
		try {
			out = new FileBlockingOutputStream(file, append);
			return out;
		} catch (FileNotFoundException e) {
			releaseFileAccess(file);
			throw e;
		}
	}
	
	/** Waits until access permit is available to the given file and then
	 * creates and returns a {@code FileOutputStream} for this file. An
	 * existing file will be overwritten. Access to the file is reserved in the
	 * service until the stream's 'close()' method is called.
	 * 
	 * @param file File file of the output stream
	 * @return {@code FileOutputStream}
	 * @throws FileNotFoundException
	 * @throws InterruptedException
	 */
	public FileOutputStream getBlockingFileOutputStream (File file) 
				throws FileNotFoundException, InterruptedException {
		return getBlockingFileOutputStream(file, false);
	}
	
	private class FileBlockingInputStream extends FileInputStream {
		File file;
		boolean closed;
		
		public FileBlockingInputStream (File file) throws FileNotFoundException {
			super(file);
			this.file = file;
		}

		@Override
		public synchronized void close() throws IOException {
			super.close();
			if (!closed) {
				releaseFileAccess(file);
				closed = true;
			}
		}
	}
	
	private class FileBlockingOutputStream extends FileOutputStream {
		File file;
		boolean closed;
		
		public FileBlockingOutputStream (File file) throws FileNotFoundException {
			super(file);
			this.file = file;
		}

		public FileBlockingOutputStream (File file, boolean append) throws FileNotFoundException {
			super(file, append);
			this.file = file;
		}

		@Override
		public synchronized void close() throws IOException {
			super.close();
			if (!closed) {
				releaseFileAccess(file);
				closed = true;
			}
		}
	}
	
}

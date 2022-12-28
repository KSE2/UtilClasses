package kse.utilclass2.io;

/*
*  File: LayeredFileSafe.java
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

/** Facility to store security copies of files in a history frame and pattern. 
 * With the constructor the user defines how many days,
 * months and years the frame shall arrange. Each day, month and year builds
 * a storage slot into which file copies are automatically moved when a new
 * version of the file is stored. When required, an array of history files
 * for a particular filename can be called up by the user.
 * <p>Different files can be stored and managed by a single instance. Files are
 * stored into a single directory which is also defined at creation. 
 * It is to be noted that only the last part of the pathname of a file 
 * ('file.getName()') is evaluated for the file's identity.
 *  
 */
public class LayeredFileSafe {

	private final File saveDir;
	private final int days;
	private final int months;
	private final int years;
	private final int nrSlots;
	
	/** Delta in days (backwards) as thresholds for the storage slots (unmodifiable). */
	private final int[] slotDelta;
	/** File name appendices corresponding to slots (unmodifiable). */
	private final String[] slotText;
	/** Real time deltas (in days from now) of files assigned to slots. */
	private final long[] slotTime;

	private String name;
	private long timeNow;
	
	/** Creates a new safe with 6 days, 5 months and 3 years storage slots
	 * per file.
	 * 
	 * @param dir File storage directory
	 */
	public LayeredFileSafe (File dir) {
		this(dir, 6, 5, 3);
	}

	/** Creates a new safe with the given number of storage slots per file.
	 * 
	 * @param dir File storage directory
	 * @param days int number slots for daily copies
	 * @param months int number slots for monthly copies
	 * @param years int number slots for yearly copies
	 */
	public LayeredFileSafe (File dir, int days, int months, int years) {
		Objects.requireNonNull(dir, "directory is null");
		Util.requirePositive(days, "days");
		Util.requirePositive(months, "days");
		Util.requirePositive(years, "days");
		
		// probe for inconsistencies
		int monthsLimit = years > 0 ? 12 : 0;
		int daysLimit = months > 0 ? 30 : 0;
		if (daysLimit == 0) {
			daysLimit = years > 0 ? 360 : 0;
		}
		if (daysLimit > 0 && days >= daysLimit) {
			throw new IllegalArgumentException("illegal days value: " + days);
		}
		if (monthsLimit > 0 && months >= monthsLimit) {
			throw new IllegalArgumentException("illegal months value: " + months);
		}
		
		// create instance values
		saveDir = dir;
		this.days = days;
		this.months = months;
		this.years = years;
		nrSlots = days + months + years;
		slotDelta = createDeltas();
		slotText = createTexts();
		slotTime = new long[nrSlots];
	}

	/** Sets a name for this storage instance. This is for user's purpose only.
	 * 
	 * @param name String
	 */
	public void setName (String name) {this.name = name;}

	/** Returns the name which was assigned by the user to this storage 
	 * instance or <b>null</b> if nothing was assigned.
	 * 
	 * @return String or null
	 */
	public String getName () {return name;}
	
	private int[] createDeltas () {
		int[] s = new int[nrSlots];
		for (int i = 0; i < days; i++) {
			s[i] = i+1;
		}
		for (int i = 0; i < months; i++) {
			s[i + days] = (i+1) * 30;
		}
		for (int i = 0; i < years; i++) {
			s[i + days + months] = (i+1) * 365;
		}
		return s;
	}
	
	private String[] createTexts () {
		String[] s = new String[nrSlots];
		for (int i = 0; i < days; i++) {
			s[i] = " -- day " + (i+1);
		}
		for (int i = 0; i < months; i++) {
			s[i + days] = " -- month " + (i+1);
		}
		for (int i = 0; i < years; i++) {
			s[i + days + months] = " -- year " + (i+1);
		}
		return s;
	}
	
	/** Returns the storage filepath for a given object file which is to be
	 * stored in the given slot. The parent section of the given filepath is
	 * irrelevant.
	 * 
	 * @param file File object file (not a storage file!)
	 * @param slot int slot index
	 * @return File storage file
	 */
	private File slotFile (File file, int slot) {
		if (slot < 0 | slot >= nrSlots)
			throw new IllegalArgumentException("illegal slot number: " + slot);
		
		String name = file.getName();
		return new File(saveDir, name + slotText[slot]);
	}
	
	/** Reads the file time deltas of the storage files related to the given
	 * object file and stores them into the time-slots.
	 *   
	 * @param file File user object file
	 */
	private void readFiles (File file) {
		Objects.requireNonNull(file, "file is null");
		for (int i = 0; i < nrSlots; i++) {
			File storeFile = slotFile(file, i);
			slotTime[i] = delta(storeFile);
		}
	}
	
	/** The real time delta in number of days from now for the given file.
	 * Returns zero if the file does not exist, otherwise a minimum of 1.
	 * 
	 * @param file File
	 * @return int
	 */
	private int delta (File file) {
		if (!file.isFile()) return 0;
		long time = timeNow > 0 ? timeNow : System.currentTimeMillis();
		int delta = (int) ((time - file.lastModified()) / Util.TM_DAY);
		return Math.max(1, delta);
	}
	
	/** Rearranges the storage positions for the given cardinal file if 
	 * required. A single call leaves the framework for the file completely 
	 * updated. It is to be noted that only the last part of the pathname 
	 * ('file.getName()') is evaluated for the file's identity.
	 * <p>Execution of this method is
	 * implied within a call to 'storeFile()' but it can be called separately 
	 * when the store has to be updated without a new file version entered.
	 * 
	 * @param file File object file (user file)
	 * @throws IOException
	 */
	public synchronized void promote (File file) throws IOException {
		readFiles(file);

		boolean event;
		do {
			event = false;
			
			// pass 1: move outdated files
			// moves (renames) storage files between slots or erases them
			for (int i = nrSlots-1; i >= 0; i--) {
				// if the slot-file expires from the slot (maximum delta days exceeded)
				if (slotTime[i] > slotDelta[i]) {
					// delete file if last slot
					if (i == nrSlots-1) {
						slotFile(file, i).delete();
						slotTime[i] = 0;
						Log.log(8, "(LayeredFileSafe.analyse) removing last slot file");
	
					// otherwise move file to next slot position
					// (current inhabitant of next slot gets deleted)
					} else {
						slotTime[i+1] = slotTime[i];
						slotTime[i] = 0;
						File thisFile = slotFile(file, i);
						File nextFile = slotFile(file, i+1);
						nextFile.delete();
						thisFile.renameTo(nextFile);
						Log.log(8, "(LayeredFileSafe.analyse) renaming " + thisFile.getName() 
								+ " to " + nextFile.getName());
					}
					event = true;
				}
			}
			
			// pass 2: refill empty slots if feasible w/ index-1 (copy of predecessor)
			for (int i = nrSlots-1; i > 0; i--) {
				File source = slotFile(file, i-1);
				boolean feasible = i == 1 || delta(source) > slotDelta[i-2];
				if (feasible && slotTime[i] == 0 && source.isFile()) {
					slotTime[i] = slotTime[i-1];
					File target = slotFile(file, i);
					Util.copyFile2(source, target, true);
					event = true;
					Log.log(8, "(LayeredFileSafe.analyse) drawn up file " + target.getName()); 
				}
			}
		} while (event);
	}
	
	/** Promotes the framework of all files contained in this store.
	 *  
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public synchronized void promote () throws IOException {
		File[] files = getFiles();
		for (File f : files) {
			promote(f);
		}
	}
	
	/** Removes all files stored.
	 */
	public synchronized void clear () {
		for (File f : getFiles()) {
			clearFile(f);
		}
	}
	
	/** Removes all history copies of the given file in this store.
	 * Note that only the last name of the filepath is considered for file
	 * identity.
	 * 
	 * @param file File file to remove
	 */
	public synchronized void clearFile (File file) {
		List<File> files = getFiles(file);
		for (File f : files) {
			f.delete();
		}
	}
	
	/** Creates a copy of the given file in the store system. The time-stamp
	 * assigned to the stored file is TIME-NOW. TIME-NOW is by default the
	 * current system time but can be a user set value with method 
	 * 'setTimeNow()'. 
	 * <p>Note that only the last name of the filepath is considered for file
	 * identity ('file.getName()'). Therefore it is in the responsibility of the
	 * user to not enter various file identities with the same last name
	 * into a single store.

	 * 
	 * @param file File object file to save
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public synchronized void storeFile (File file) throws IOException {
		Objects.requireNonNull(file, "file is null");
		File target = slotFile(file, 0);
		Util.copyFile2(file, target);
		if (timeNow > 0) {
			target.setLastModified(timeNow);
		}
		Log.log(6, "(LayeredFileSafe.storeFile) storing file to index 0: " + file.getAbsolutePath());
		promote(file);
	}
	
	/** Whether the given file is a member in this store.
	 * 
	 * @param file File file to investigate
	 * @return boolean true = file is stored, false = file is unknown
	 */
	public synchronized boolean contains (File file) {
		readFiles(file);
		for (long i : slotTime) {
			if (i > 0) return true;
		}
		return false;
	}
	
	/** Sets a time assumption for the calculations of this store.
	 * If this value is not zero, time-now is always at this value; otherwise,
	 * if this value is zero, time-now is newly fetched with 
	 * 'System.currentTimeMillis()' where required. The default value is zero.
	 * 
	 * @param time long milliseconds
	 */
	public void setTimeNow (long time) {
		Util.requirePositive(time, "time");
		timeNow = time;
	}

	/** The current "timeNow" setting; this is a millisecond value.
	 * A return value of zero means there is no time setting.
	 * 
	 * @return long milliseconds or zero
	 */
	public long getTimeNow () {return timeNow;}
	
	/** Returns an array of files in descending time order (youngest first)
	 * of the security file copies in this store assigned to the given filepath.
	 * Note that only the last name of the filepath is considered for file
	 * identity.
	 *    
	 * @param file File cardinal file 
	 * @return {@code File[]}
	 */
	public synchronized File[] getHistory (File file) {
		// collect the filepaths which correspond to non-zero slot-times
		List<File> list = getFiles(file);
		
		// filter out double entry files 
		File pred = null;
		for (Iterator<File> it = list.iterator(); it.hasNext();) {
			File f = it.next();
			if (pred != null && pred.lastModified() == f.lastModified()) {
				it.remove();
			}
			pred = f;
		}
		
		return list.toArray(new File[list.size()]);
	}

	/** Returns the set of file names which have a representation in this store.
	 * 
	 * @return File[]
	 */
	public synchronized File[] getFiles () {
		File[] flist = saveDir.listFiles();
		if (flist == null) return new File[0];
		Map<File, File> map = new HashMap<>();
		for (File f : flist) {
			String name = f.getName();
			int ix = name.lastIndexOf(" --");
			if (ix > -1) {
				File f2 = new File(name.substring(0, ix));
				map.put(f2, f2);
			}
		}
		return map.keySet().toArray(new File[map.size()]);
	}
	
	/** Returns a list of file copies in this store for the given cardinal
	 * filepath. The separate list is sorted in descending order of the file 
	 * time.
	 *  
	 * @param file File cardinal filepath
	 * @return {@code List<File>}
	 */
	List<File> getFiles (File file) {
		readFiles(file);
		
		// collect the filepaths which correspond to non-zero slot-times
		List<File> list = new ArrayList<>();
		for (int i = 0; i < nrSlots; i++) {
			if (slotTime[i] > 0) {
				list.add(slotFile(file, i));
			}
		}
		return list;
	}
	
	/** The number of storage slots per file identity.
	 * 
	 * @return int number of slots
	 */
	public int getNrSolts () {return nrSlots;}
	
	/** The delta days (number of days) for each storage slot in the frame.
	 * 
	 * @return int[] 
	 */
	int[] getDeltas () {return slotDelta;}
	
	/** The file name extensions (Strings) for each storage slot in the frame.
	 * 
	 * @return String[]
	 */
	String[] getSlotTexts () {return slotText;}

	/** The current values in the slot time vector. These values are updated
	 * by each call for a particular file, so with 'storeFile()', 'promote()',
	 * 'getHistory()' and 'readFiles()'. After these methods are finished, the
	 * values are irrelevant for the store.
	 * 
	 * @return long[]
	 */
	long[] getSlotTimes () {return slotTime.clone();}
	
	public File getDirectory () {return saveDir;}
	
	public synchronized void report () {
		System.out.print("\n# REPORT: LayeredFileSafe, " + days + " days, " + months + " months, " + years + " years");
		System.out.println(", save-dir = " + saveDir.getAbsolutePath());
		System.out.print("    \"" + (name == null ? "" : name) + "\", " + nrSlots + " slots, deltas = [");
		int j = 0;
		for (Integer i : slotDelta) {
			System.out.print((j++ > 0 ? ", " : "") + i);
		}
		System.out.println("]");
		
		for (File f : getFiles()) {
			System.out.println("    file = " + f);
			for (File f1 : getFiles(f)) {
				System.out.println("       " + f1);
			}
		}
		
//		int i = 0;
//		for (String s : slotText) {
//			System.out.println("    " + i++ + ": " + s);
//		}
		
	}
}

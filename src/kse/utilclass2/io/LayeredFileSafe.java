package kse.utilclass2.io;

import java.io.BufferedReader;

/*
*  File: LayeredFileSafe.java
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;
import kse.utilclass.sets.ArraySet;

/** Facility to store security copies of files in a history frame and pattern. 
 * With the constructor the user defines how many days,
 * months and years the frame shall arrange. Each day, month and year builds
 * a storage slot into which file copies are automatically moved when a new
 * version of the file is stored. When required, an array of history files
 * for a particular filepath can be called up by the user.
 * <p>Different files can be stored and managed by a single instance. Files are
 * stored into a single directory which is also defined at creation. 
 * It is to be noted that only the last part of the pathname of a file 
 * ('file.getName()') is evaluated for the file's identity.
 *  
 */
public class LayeredFileSafe {

	private static final Long ZERO_VALUE = Long.valueOf(0);
	private static final String TABLE_FILE_EXTENSION = ".ftab";
	private static final String DATA_FILE_EXTENSION = ".dat";
	
	private final File saveDir;
	private final int days;
	private final int months;
	private final int years;
	private final int nrSlots;
	
	/** Relation from canonical filepaths of object files into their file-tables */ 
	private Map<String, FileTable> tableMap = new HashMap<>();
	
	/** Delta in days (backwards) as thresholds for the storage slots (unmodifiable). */
	private final int[] slotDelta;
	/** Key names of the file storage slots (unmodifiable). */
	private final String[] slotText;

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
	 * This creates the storage directory if it does not exist.
	 * 
	 * @param dir File storage directory
	 * @param days int number slots for daily copies
	 * @param months int number slots for monthly copies
	 * @param years int number slots for yearly copies
	 * @throws IllegalArgumentException if the directory is invalid or cannot
	 *         be written or any of the slot dimensions are invalid
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
		
		// test safe directory
		if (!Util.ensureDirectory(dir, null)) {
			throw new IllegalArgumentException("invalid safe directory: " + dir);
		}
		if (!Util.testCanWrite(dir)) {
			throw new IllegalArgumentException("unable to write to safe directory: " + dir);
		}
		
		// create instance values
		saveDir = dir;
		this.days = days;
		this.months = months;
		this.years = years;
		nrSlots = days + months + years;
		slotDelta = createDeltas();
		slotText = createTexts();
		
		loadFileTables();
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
	
	/** Creates the maximum day-ages for all available save-slots.
	 * 
	 * @return int[] array of age values
	 */
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
	
	/** Creates the identifier texts for all available save-slots.
	 * 
	 * @return String[]
	 */
	private String[] createTexts () {
		String[] s = new String[nrSlots];
		for (int i = 0; i < days; i++) {
			s[i] = "D-" + (i+1);
		}
		for (int i = 0; i < months; i++) {
			s[i + days] = "M-" + (i+1);
		}
		for (int i = 0; i < years; i++) {
			s[i + days + months] = "Y-" + (i+1);
		}
		return s;
	}
	
	/** The real time delta in number of days from now for the given file-time.
	 * Returns zero if the argument is zero, otherwise a minimum of 1.
	 * 
	 * @param fileTime long modified time marker of a file 
	 * @return int days
	 */
	private int delta (long fileTime) {
		Util.requirePositive(fileTime);
		if (fileTime == 0) return 0;
		long time = timeNow > 0 ? timeNow : System.currentTimeMillis();
		int delta = (int) ((time - fileTime) / Util.TM_DAY);
		return Math.max(1, delta);
	}
	
	/** Returns the file-table for the given object file or null if such a 
	 * mapping is unknown.
	 * 
	 * @param file File object file
	 * @return {@code FileTable} or null
	 * @throws IOException
	 */
	private FileTable getFileTable (File file) throws IOException {
		file = file.getCanonicalFile();
		synchronized (tableMap) {
			return tableMap.get(file.getAbsolutePath());
		}
	}
	
	/** Returns the file-table for the given object file. If the object file
	 * is unknown, a new file-table is created.
	 * 
	 * @param file File object file
	 * @return {@code FileTable}
	 * @throws IOException
	 */
	private FileTable ensureFileTable (File file) throws IOException {
		file = file.getCanonicalFile();
		String path = file.getAbsolutePath();
		
		synchronized (tableMap) {
			FileTable table = tableMap.get(path);
			if (table == null) {
				table = new FileTable(file);
				tableMap.put(path, table);
			}
			return table;
		}
	}
	
	/** Returns the currently active file-tables in an array.
	 * 
	 * @return {@code FileTable[]}
	 */
	private FileTable[] getFileTables () {
		synchronized (tableMap) {
			FileTable[] tables = tableMap.values().toArray(new FileTable[tableMap.size()]);
			return tables;
		}
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
		FileTable table = getFileTable(file);
		if (table == null)
			throw new IllegalArgumentException("unknown object file: " + file.getAbsolutePath());

		if (table.promote()) {
			table.writeObject();
		}
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
	
	/** Removes all save-files stored and all file-tables.
	 * @throws IOException 
	 */
	public synchronized void clear () throws IOException {
		for (File f : getFiles()) {
			clearFile(f);
		}
	}
	
	/** Removes all history copies of the given object file in this store.
	 * 
	 * @param file File file to remove
	 * @throws IOException 
	 */
	public synchronized void clearFile (File file) throws IOException {
		List<File> files = getFiles(file);
		for (File f : files) {
			f.delete();
		}
		FileTable table = getFileTable(file);
		if (table != null) {
			table.removeTableFile();
			synchronized (tableMap) {
				tableMap.remove(table.getFilepath());
			}
		}
	}
	
	/** Creates a copy of the given file in the store system. The time-stamp
	 * assigned to the stored file is TIME-NOW. TIME-NOW is by default the
	 * current system time but can assume a fix user defined value with method 
	 * 'setTimeNow()'. A call to 'promote(file)' is implied after the file
	 * has been stored.
	 * 
	 * @param file File object file to save
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public synchronized void storeFile (File file) throws IOException {
		Objects.requireNonNull(file, "file is null");
		Log.log(6, "(LayeredFileSafe.storeFile) ---------------------- ");
		FileTable table = ensureFileTable(file);
		File stoF = table.storeFile(file);
		table.writeObject();
		Log.log(6, "(LayeredFileSafe.storeFile) stored file to index 0: " + file.getAbsolutePath() 
				+ "  " + stoF.lastModified());
	}
	
	/** Whether the given object file is a member in this store.
	 * 
	 * @param file File object file to investigate; may be null
	 * @return boolean true = file is stored, false = file is unknown
	 * @throws IOException 
	 */
	public boolean contains (File file) throws IOException {
		if (file == null) return false;
		file = file.getCanonicalFile();
		synchronized (tableMap) {
			return tableMap.containsKey(file.getAbsolutePath());
		}
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
	 *    
	 * @param file File cardinal file 
	 * @return {@code File[]}
	 * @throws IOException 
	 */
	public synchronized File[] getHistory (File file) throws IOException {
		// collect the filepaths which correspond to non-zero slot-times
		List<File> list = getFiles(file);
		
		// filter out double entry files 
		File pred = null;
		long predMod = 0;
		for (Iterator<File> it = list.iterator(); it.hasNext();) {
			File f = it.next();
			long mod = f.lastModified();
			if (pred != null && predMod == mod) {
				it.remove();
			}
			pred = f;
			predMod = mod;
		}

//		System.out.println("+++ LayeredFileSafe.getHistory(file) : " + list.size() + " files for " + file);
//		for (File f : list) {
//			System.out.println("   file: " + file);
//		}
		
		return list.toArray(new File[list.size()]);
	}

	/** Returns the set of file names which have a representation in this store.
	 * The result is not sorted.
	 * 
	 * @return File[]
	 */
	public synchronized File[] getFiles () {
		FileTable[] tables = getFileTables();
		File[] arr = new File[tables.length];
		int i = 0;
		for (FileTable t : tables) {
			arr[i++] = new File(t.getFilepath());
		}
		return arr;
	}
	
	/** Returns a list of file copies in this store for the given object
	 * filepath. The separate list is sorted in descending order of the file 
	 * time.
	 * <p>The returned list may contain identical file copies under different
	 * names. This list is for technical maintenance purposes. For a list of 
	 * unique history files refer to "getHistory(File)".
	 *  
	 * @param file File object filepath
	 * @return {@code List<File>}
	 * @throws IOException 
	 */
	public synchronized List<File> getFiles (File file) throws IOException {
		List<File> list = new ArrayList<>();
		FileTable table = getFileTable(file);
		if (table != null) {
			list = table.getStoredFiles();
			
			// sorting in descending order of file times
			Comparator<File> comp = new Comparator<File> () {
				@Override
				public int compare (File o1, File o2) {
					long tm1 = o1.lastModified();
					long tm2 = o2.lastModified();
					return tm1 > tm2 ? -1 : tm1 < tm2 ? 1 : 0;
				}
			};
			Collections.sort(list, comp);
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

	/** The storage directory of this file-safe.
	 * 
	 * @return File directory
	 */
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

		try {
			File[] baseFiles = getFiles();
			for (File f : baseFiles) {
				System.out.println("    file = " + f);
				for (File f1 : getFiles(f)) {
					System.out.println("       " + f1 + "           " + f1.lastModified());
				}
				System.out.println("    History:");
				for (File f1 : getHistory(f)) {
					System.out.println("       " + f1 + "           " + f1.lastModified());
				}
			}
		} catch (IOException e) {
			System.err.println("** ERROR: (LayeredFileSafe.report) file access failure: " + e);
		}
	}
	
	/** Creates and reads an instance of {@code FileTable} from the given 
	 * file-table serialisation file.
	 * 
	 * @param file File file-table file
	 * @return {@code FileTable}
	 * @throws FileNotFoundException
	 * @throws StreamCorruptedException if the file had language failures
	 * @throws IOException
	 */
	private FileTable readTable (File file) throws IOException {
		FileTable table = new FileTable();
		table.readObject(file);
		Log.log(8, "(LayeredFileSafe) read table data: " + file.getAbsolutePath());
		return table;
	}
	
	/** Loads all available file-tables from analysing the safe directory and
	 * reading table serialisation files.
	 * 
	 * @throws IOException 
	 */
	private void loadFileTables () {
		File[] flist = saveDir.listFiles();
		if (flist == null) return;

		synchronized (tableMap) {
		for (File f : flist) {
			String name = f.getName();
			if (name.endsWith(TABLE_FILE_EXTENSION)) {
				try {
					FileTable table = readTable(f);
					tableMap.put(table.getFilepath(), table);
				} catch (IOException e) {
					System.err.println("** ERROR: (LayeredFileSafe) table serialisation file reading\n" + e);
				}
			}
		}
		}
	}
	
//  *************  INNER CLASSES  ***************	
	
	private class FileTable {
		/** Object file identifier */
		private String filepath;
		/** Representation of object filepath (hexadecimal literal) */
		private String storeName;
		/** Relation from slot-names into time values of save-files (last modified); 
		 *  value zero signals that no file exists for this mapping. All slot-names 
		 *  of the Safe are always mapped. */
		private HashMap<String, Long> map = new HashMap<>();
		
		public FileTable () {
			initTable();
		}

		/** Creates a new file-table for the given object file.
		 * 
		 * @param file File object file
		 * @throws IOException
		 */
		public FileTable (File file) throws IOException {
			file = file.getCanonicalFile();
			filepath = file.getAbsolutePath();
			storeName = storeToken(filepath);
			initTable();
		}
		
		private String storeToken (String filepath) {
			return Util.bytesToHex(Util.fingerPrint(filepath), 0, 6);
		}
		
		private void initTable () {
			for (String key : slotText) {
				map.put(key, ZERO_VALUE);
			}
		}
		
		/** Returns the absolute filepath of the object file.
		 *  
		 * @return String filepath
		 */
		public String getFilepath () {return filepath;}

//		/** Returns the unique token (symbol) for the object file. This
//		 * symbol is used to name the storage files of the history.
//		 * 
//		 * @return String file token
//		 */
//		public String getFileToken () {return storeName;}
		
		/** Returns the filename of the serialisation file of this table.
		 * <p>The name starts with the object file's identification followed by
		 * an abbreviation of its filename followed by the file-type extension
		 * of the serialisation file.
		 * 
		 * @return String
		 */
		private String tableFileName () {
			String hs = new File(filepath).getName();
			int i = hs.indexOf('.');
			if (i > -1) {
				hs = hs.substring(0, i);
			}
			if (hs.length() > 35) {
				hs = hs.substring(0, 35);
			}
			return storeName + " -- " + hs + TABLE_FILE_EXTENSION;
		}

		/** The time value stored in the given slot. The value represents the
		 * 'last-modified' value of a stored save-file.
		 * 
		 * @param slot String slot name
		 * @return long time in milliseconds (epoch time)
		 */
		private long getSlotTime (String slot) {
			Long time = map.get(slot);
			return time == null ? 0 : time;
		}
		
		/** Stores a 'last-modified' value of a save-file into the slot relation.
		 * 
		 * @param slot String slot name
		 * @param time long time in milliseconds (epoch time)
		 */
		private void storeSlotTime (String slot, long time) {
			synchronized (map) {
				map.put(slot, time);
			}
		}
		
		/** Rearranges the storage positions for the given cardinal file if 
		 * required. A single call leaves the framework for the file completely 
		 * updated. 
		 * <p>Execution of this method is implied within a call to 'storeFile()'.
		 * 
		 * @param file File object file (user file)
		 * @return boolean true = table was modified, false = table unmodified
		 * @throws IOException
		 */
		public boolean promote () throws IOException {
			boolean event, modified;
			Set<Long> replacedSet = new ArraySet<>();

			modified = false;
			do {
				event = false;
				
				// pass 1: move outdated files
				// moves (renames) storage files between slots or erases them
				for (int i = nrSlots-1; i > 0; i--) {
					// obtain time deltas
					long prevTime = getSlotTime(slotText[i-1]);
					long thisTime = getSlotTime(slotText[i]);
					int thisDelta = delta(thisTime);
					int prevDelta = delta(prevTime);
					
					// if the slot-file expires from the slot (maximum delta days exceeded)
					// or if the slot is empty
					if (thisDelta == 0 || thisDelta > slotDelta[i]) {
					    // draw up predecessor (younger slot) if not empty
						// the younger slot becomes empty
						if (prevDelta > 0 && (thisDelta == 0 || prevDelta < thisDelta)) {
							storeSlotTime(slotText[i], prevTime);
//							storeSlotTime(slotText[i-1], 0);
							Log.log(8, "(LayeredFileSafe.promote) " + storeName + " - drawing up slot " + slotText[i]
										+ " val=" + prevTime + " from " + slotText[i-1]);
							
							// remove save-file of old value (stale value)
							if (thisTime > 0) {
								replacedSet.add(thisTime);
//								File file = storeFileName(thisTime);
//								if (file.delete()) {
//									Log.log(8, "(LayeredFileSafe.promote) removed safe-file of slot " 
//											+ slotText[i] + " : " + file.getAbsolutePath());
//								}
							}
							
							event = true;
							modified = true;
						}
					}
				}
				
				
			} while (event);
			
			// control set of replaced time values if their files can be deleted
			// this is the case if no other table-entry hold the same value
			for (Long time : replacedSet) {
				if (!map.containsValue(time)) {
					removeStorageFile(time);
				}
			}
			
			return modified;
		}
		
		/** Removes the save-file of the given time value. Does nothing if the
		 * argument is zero.
		 * 
		 * @param time long file time value
		 */
		private void removeStorageFile (long time) {
			if (time == 0) return;
			File file = storeFileName(time);
			if (file.delete()) {
				Log.log(8, "(LayeredFileSafe.promote) removed stale safe-file: " + file.getAbsolutePath());
			}
		}
		
		/** Removes the serialisation file of this table.
		 */
		public void removeTableFile () {
			new File(saveDir, tableFileName()).delete();
		}
		
		/** Returns the set of all stored save-files in this file-table as a 
		 * list. The result has no sorting defined; only existing files are 
		 * contained.
		 * 
		 * @return {@code List<File>}
		 */
		public List<File> getStoredFiles () {
			List<File> list = new ArrayList<>();
			for (Entry<String, Long> e : map.entrySet()) {
				if (e.getValue() != 0) {
					File file = storeFileName(e.getValue());
					if (file.isFile() && !list.contains(file)) {
						list.add(file);
					}
				}
			}
			return list;
		}
		
		private File storeFileName (long time) {
			String name = storeName + " -- " + time + DATA_FILE_EXTENSION;
			return new File(saveDir, name);
		}

		/** Stores a copy of the given file into this Safe and enters a 
		 * notification into the table at slot index 0. Implies calls to
		 * 'promote()'.
		 * 
		 * @param file File object source file
		 * @return File the store file
		 * @throws IOException
		 */
		public File storeFile (File file) throws IOException {
			// bring the storage system into shape
			promote();
			long oldTime = getSlotTime(slotText[0]);

			// store the file in a named copy
			long time = timeNow > 0 ? timeNow : System.currentTimeMillis();
			File target = storeFileName(time);
			Util.copyFile2(file, target);
			target.setLastModified(time);

			// write slot entry
			storeSlotTime(slotText[0], time);
			promote();
			
			// remove file of previous entry if stale
			if (!map.containsValue(oldTime)) {
				removeStorageFile(oldTime);
			}
			return target;
		}

		/** Writes this file-table to its systematic serialisation file. The
		 * name of the file is created automatically.
		 * 
		 * @throws IOException
		 */
		public void writeObject () throws IOException {
			if (filepath == null | storeName == null)
				throw new IllegalStateException("filepath undefined");
			
			File target = new File(saveDir, tableFileName());
			OutputStream out = new FileOutputStream(target);
			Writer writer = new OutputStreamWriter(out, "UTF-8");
			writer.write("# LayeredFileSafe file-table, 0\n");
			writer.write("file = " + filepath + "\n");
			writer.write("token = " + storeName + "\n");
			
			Set<Entry<String, Long>> set = map.entrySet();
			if (set.size() > 0) {
				List<Entry<String, Long>> list = new ArrayList<>(set);
				Comparator<Entry<String, Long>> comp = new Comparator<Entry<String, Long>> () {
					@Override
					public int compare (Entry<String, Long> o1, Entry<String, Long> o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				};
				Collections.sort(list, comp);

				for (Entry<String, Long> e : list) {
					String line = e.getKey() + "\t" + e.getValue() + "\n";
					writer.write(line);
				}
			}
			writer.close();
		}
		
		@SuppressWarnings("resource")
		public void readObject (File file) throws IOException {
			InputStream in = new FileInputStream(file);
			try {
				Reader inReader = new InputStreamReader(in, "UTF-8");
				BufferedReader reader = new BufferedReader(inReader);
				
				// control file header and type
				String line = reader.readLine();
				if (!line.startsWith("# LayeredFileSafe file-table, 0")) {
					throw new IOException("unknown file type, " + file.getAbsolutePath());
				}
				
				// read related filepath
				line = reader.readLine();
				reader.readLine();
				if (!line.startsWith("file = ")) {
					throw new StreamCorruptedException("missing file definition in " + file.getAbsolutePath());
				}
				filepath = line.substring(7);
				storeName = storeToken(filepath);

				// read slot entries
				int lineNr = 2;
				while ((line=reader.readLine()) != null) {
					lineNr++;
					line = line.trim();
					if (line.startsWith("#") || line.isEmpty()) continue;
					int idx = line.indexOf('\t');
					if (idx == -1) {
						throw new StreamCorruptedException("illegal slot entry, line " + lineNr);
					}
					String key = line.substring(0, idx);
					if (isValidSlot(key)) {
						try {
							Long value = Long.parseLong(line.substring(idx+1));
							map.put(key, value);
						} catch (Exception e) {
							throw new StreamCorruptedException("illegal slot entry, line " + lineNr);
						}
					}
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} finally {
				in.close();
			}
		}

		/** Whether the given name designates a valid storage slot.
		 * 
		 * @param name String slot name, may be null
		 * @return boolean
		 */
		private boolean isValidSlot (String name) {
			if (name != null) { 
			for (String s : slotText) {
				if (name.equals(s)) return true;
			}
			}
			return false;
		}
	}
}

package kse.utilclass2.io;

/*
*  File: TestC_LayeredFileSafe.java
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

public class TestC_LayeredFileSafe {

	public TestC_LayeredFileSafe() {
		Log.setDebug(true);
		Log.setLogging(true);
		Log.setDebugLevel(10);
		Log.setLogLevel(10);
	}

	@Test
	public void init () throws IOException {
		// complex constructor
		test_init_state(6, 5, 3);
		test_init_state(60, 0, 12);
		test_init_state(300, 0, 0);
		test_init_state(3, 24, 0);
		test_init_state(0, 0, 0);
		test_init_state(0, 1, 1);
		test_init_state(1, 1, 0);
		
		// simple constructor
		File dir = Util.getTempDir();
		LayeredFileSafe safe = new LayeredFileSafe(dir);
		assertTrue("nr of slots", safe.getNrSolts() == 14);
		assertTrue("directory error", safe.getDirectory().equals(dir));
		safe.report();
		
		// general tests
		assertNull("name must be null initially", safe.getName());
		assertTrue("time now zero expected", safe.getTimeNow() == 0);
		assertFalse("contains error", safe.contains(new File("aberystwyth")));
		assertFalse("contains error", safe.contains(null));
		
		// some settings
		String name = "Aberystwyth";
		safe.setName(name);
		assertTrue("set name error", name == safe.getName());
		long time = 89323987;
		safe.setTimeNow(time);
		assertTrue("set time error", time == safe.getTimeNow());
		
		// FAILURES
		
		try {
			test_init_state(30, 12, 0);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			test_init_state(360, 0, 1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
		
		try {
			test_init_state(0, 12, 1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		try {
			test_init_state(-1, 3, 2);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		try {
			test_init_state(0, -1, 2);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}

		try {
			test_init_state(0, 3, -1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}
	
	private void test_init_state (int days, int months, int years) throws IOException {
		File dir = Util.getTempDir();
		LayeredFileSafe safe = new LayeredFileSafe(dir, days, months, years);

		int nrSlots = safe.getNrSolts();
		assertTrue("nr of slots", nrSlots == days + months + years);
//		assertTrue("nr of time-slots", safe.getSlotTimes().length == nrSlots);
		assertTrue("nr of deltas", safe.getDeltas().length == nrSlots);
		assertTrue("history not empty", safe.getHistory(File.createTempFile("test-", ".dat")).length == 0);
		assertTrue("directory error", safe.getDirectory().equals(dir));
		assertNotNull("getFiles() is null", safe.getFiles());
		assertTrue("getFiles() not empty", safe.getFiles().length == 0);

//		// all slot times are zero
//		for (Long t : safe.getSlotTimes()) {
//			assertTrue("initial slot time", t == 0);
//		}
		
		// all deltas are not zero and ascending values
		int prev = 0;
		for (Integer i : safe.getDeltas()) {
			assertTrue("slot delta is zero", i != 0);
			assertTrue("slot value fault: " + i, i > prev);
			prev = i;
		}
		
		// all slot texts are not empty and unique
		Map<String, String> map = new HashMap<>();
		for (String s : safe.getSlotTexts()) {
			assertTrue("slot text is empty", s != null && !s.isEmpty());
			map.put(s, s);
		}
		assertTrue("some slot-texts are not unique", map.size() == nrSlots);
	}
	
	@Test
	public void store_files () throws IOException, InterruptedException {
		File dir = new File(Util.getTempDir(), "LayerSafe");
		LayeredFileSafe safe = new LayeredFileSafe(dir, 4, 3, 3);
		long tm = System.currentTimeMillis();
		safe.clear();
		safe.report();

		File f1 = File.createTempFile("lsafe-", ".dat");
		String fcontent = "Niemand wußte mehr bescheid .. als der Bär!";
		Util.writeTextFile(f1, fcontent, "UTF-8");
		safe.storeFile(f1);
		assertTrue("contains error", safe.contains(f1));
		assertTrue("number of files (stored)", safe.getFiles().length == 1);
		assertTrue("object files error", safe.getFiles()[0].equals(f1));
		assertTrue("number of files (history)", safe.getHistory(f1).length == 1);
		
		Log.log(1, "TEST: time-now = 1 days");
		safe.setTimeNow(tm + Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();
		assertTrue("contains error", safe.contains(f1));
		assertTrue("number of object files", safe.getFiles().length == 1);
		assertTrue("object files error", safe.getFiles()[0].equals(f1));
		assertTrue("number of files (history)", safe.getHistory(f1).length == 2);

		Log.log(1, "TEST: time-now = 2 days");
		safe.setTimeNow(tm + 2*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();
//		assertTrue("number of files (history)", safe.getHistory(f1).length == 3);

		Log.log(1, "TEST: time-now = 3 days");
		safe.setTimeNow(tm + 3*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();
		assertTrue("number of files (history)", safe.getHistory(f1).length == 3);

		Log.log(1, "TEST: time-now = 4 days");
		safe.setTimeNow(tm + 4*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 5 days");
		safe.setTimeNow(tm + 5*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 6 days");
		safe.setTimeNow(tm + 6*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		// print history files
		File[] hist = safe.getHistory(f1);
		System.out.println("\nStored History:");
		for (File f : hist) {
			System.out.println("   " + f);
		}
		
	
//		safe.report();
		
	}

	@Test
	public void promote_files () throws IOException, InterruptedException {
		File dir = new File(Util.getTempDir(), "LayerSafe");
		LayeredFileSafe safe = new LayeredFileSafe(dir, 4, 3, 3);
		long tm = System.currentTimeMillis();
		safe.clear();
		safe.report();

		File f1 = File.createTempFile("lsafe-", ".dat");
		long fileTime = f1.lastModified();
		safe.storeFile(f1);
		assertTrue("number of files (storeFile)", safe.getHistory(f1).length == 1);
		
		Log.log(1, "TEST: time-now = 2 days");
		safe.setTimeNow(tm + 2*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.promote(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 3 days");
		safe.setTimeNow(tm + 3*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.promote(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 4 days, new entry");
		safe.setTimeNow(tm + 4*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 5 days");
		safe.setTimeNow(tm + 5*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 6 days");
		safe.setTimeNow(tm + 6*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 30 days");
		safe.setTimeNow(tm + 30*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 31 days");
		safe.setTimeNow(tm + 31*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 91 days");
		safe.setTimeNow(tm + 91*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.promote();
//		safe.storeFile(f1);
		safe.report();

		Log.log(1, "TEST: time-now = 731 days");
		safe.setTimeNow(tm + 731*Util.TM_DAY + 30*Util.TM_MINUTE);
		safe.promote();
//		safe.storeFile(f1);
		safe.report();

		
	
//		safe.report();
		
	}
}

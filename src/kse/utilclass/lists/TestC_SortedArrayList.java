package kse.utilclass.lists;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import kse.utilclass.io.Serialiser;
import kse.utilclass.misc.Util;
import kse.utilclass.sets.ArraySet;
import kse.utilclass.sets.SortedArraySet;

public class TestC_SortedArrayList {

	public TestC_SortedArrayList() {
	}
	
	private void reportList (List<?> list) {
		System.out.println();
		int ct = 0;
		for (Object el : list) {
			System.out.println("   " + ++ct + ": [" + el + "]");
		}
	}

	private SortedArrayList<String> preloadedStr (int n) {
		SortedArrayList<String> set = new SortedArrayList<String>(n);
		while (set.size() < n) {
			set.add(Util.randomString(50));
		}
		return set;
	}

	private ArraySet<String> preloadedStr_set (int n) {
		ArraySet<String> set = new ArraySet<String>(n);
		while (set.size() < n) {
			String s = Util.randomString(50);
			if (!s.isEmpty()) {
				set.add(s);
			}
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void initial () {
		SortedArrayList<Object> s1, s2, s3;
		
		// empty lists
		s1 = new SortedArrayList<>();
		s2 = new SortedArrayList<>();
		assertTrue(s1.isEmpty());
		assertTrue(s2.isEmpty());
		assertTrue(s1.equals(s2));
		assertTrue(Util.isSortedSet(s1, null));
		assertFalse(s1.contains(null));
		assertNull(s1.comparator());

		s3 = (SortedArrayList<Object>) s1.clone();
		assertNotNull("no clone returned", s3);
		assertTrue(s1.equals(s3));
		
		// array initialiser on natural sorting
		String st1 = "Hometrucker Loadplane";
		String st2 = "Carry a pencil";
		String st3 = "   ";
		String st4 = "Segelsetzer der Regattakanäle";
		String st5 = "";
		String st6 = new String(st2);

		String[] sarr = {st1, st2, st3, st4, st5, st6, st1};
		s1 = new SortedArrayList<Object>(sarr);
		assertNull(s1.comparator());
		assertFalse(s1.isEmpty());
		assertTrue(s1.size() == 7);
		assertFalse(s1.equals(s2));
		assertFalse(s1.contains(null));
		assertTrue(s1.contains(st1));
		assertTrue(s1.contains(st2));
		assertTrue(s1.contains(st3));
		assertTrue(Util.isSortedSet(s1, null));
		reportList(s1);

		// clone
		s3 = (SortedArrayList<Object>) s1.clone();
		assertNotNull("no clone returned", s3);
		assertTrue(s1.equals(s3));
		assertTrue(Util.isSortedSet(s3, null));

		// make external list for comparison
		List<String> set1 = new ArrayList<>();
		for (Object str : s1) {
			set1.add((String) str);
		}
		
		// toArray
		String[] sarr2 = s1.toArray(new String[s1.size()]);
		assertTrue("toArray error", sarr2.length == sarr.length);
		List<String> set2 = Arrays.asList(sarr2);
		assertTrue("toArray error", set1.equals(set2));
		assertTrue("toArray sorting error", Util.isSortedSet(set2, null));

		try {
			s1.add(null);
			fail("expected NullPointerException");
		} catch (NullPointerException e) {
		}
		
		// collection parameter
		s2 = new SortedArrayList<Object>(set1);
		assertTrue(s2.equals(s1));
		assertTrue(Util.isSortedSet(s2, null));
		
		// sorted set parameter
		SortedArraySet<String> aset = new SortedArraySet<>(sarr);
		Comparator<String> rcomp = new ReverseComparator<String>();
		aset.setComparator(rcomp);
		s2 = new SortedArrayList<Object>(aset);
		assertTrue(s2.comparator() == aset.comparator());
		reportList(s2);
		
		// test equality of content
		int size = s2.size();
		assertTrue(size == aset.size());
		Iterator<Object> it1 = s2.iterator();
		Iterator<String> it2 = aset.iterator();
		for (int i = 0; i < size; i++) {
			assertTrue("error in sorted-set parameter digestion", it1.next().equals(it2.next()));
		}

		// comparator parameter
		SortedArrayList<String> s4 = new SortedArrayList<String>(rcomp);
		s4.addAll(sarr);
		assertTrue(s4.size() == sarr.length);
		assertTrue(Util.isSortedSet(s4, rcomp));

		// test iterator sorting
		sarr2 = Arrays.copyOf(sarr, sarr.length);
		Arrays.sort(sarr2, rcomp);
		Iterator<String> it = s4.iterator();
		for (String s : sarr2) {
			assertTrue("sorting error w/ comparator constructor", s.equals(it.next()));
		}

		// test toArray sorting
		Object[] sarr3 = s4.toArray();
		assertTrue(sarr3.length == s4.size());
		int i = 0;
		for (String s : sarr2) {
			assertTrue("sorting error w/ comparator constructor", s.equals(sarr3[i++]));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void serialisation () throws IOException {
		int n1 = 12;
		SortedArrayList<String> set1 = preloadedStr(n1);
		SortedArrayList<String> set2 = preloadedStr(12);
		SortedArrayList<String> set3, set4, set5, set6;
		Serialiser sss = new Serialiser();
		
		// serialise
		byte[] ser1 = sss.serialise(set1);
		assertNotNull("serialisation is null", ser1);
		assertTrue("serialisation is empty, len = " + ser1.length, ser1.length > 100);
		byte[] ser2 = sss.serialise(set2);
		assertNotNull("serialisation is null", ser2);
		assertTrue("serialisation is empty", ser2.length > 100);
		
		// de-serialise
		set3 = (SortedArrayList<String>) sss.deserialiseObject(ser1);
		assertNotNull("de-serialisation is null", set3);
		assertTrue("serialisation is invalid", set3.equals(set1));
		assertTrue("unsortiert", Util.isSortedSet(set3, null));
	
		set4 = (SortedArrayList<String>) sss.deserialiseObject(ser2);
		assertNotNull("de-serialisation is null", set4);
		assertTrue("serialisation is invalid", set4.equals(set2));
		
		// empty value
		set5 = new SortedArrayList<String>();
		assertTrue(set5.isEmpty());
		byte[] ser3 = sss.serialise(set5);
		assertNotNull("serialisation is null", ser3);
		assertTrue("serialisation is empty", ser3.length > 10);
		set6 = (SortedArrayList<String>) sss.deserialiseObject(ser3);
		assertTrue(set6.isEmpty());
		assertTrue("serialisation is invalid", set5.equals(set6));
		
		// set w/ non-serialisable comparator
		try {
			set3.setComparator(new ReverseComparator2<String>());
			ser1 = sss.serialise(set3);
			fail("expected NotSerializableException");
		} catch (NotSerializableException e) {
			System.err.println(e);
		}
		
		// set w/ serialisable comparator
		Comparator<String> comp = new ReverseComparator<String>();
		set3.setComparator(comp);
		assertTrue("unsortiert", Util.isSortedSet(set3, comp));
		ser1 = sss.serialise(set3);
		set4 = (SortedArrayList<String>) sss.deserialiseObject(ser1);
		assertTrue("serialisation w/ comparator is invalid", set3.equals(set4));
		assertNotNull("comparator missing after restore", set4.comparator());
		assertNotNull("illegal comparator after restore", set4.comparator() instanceof ReverseComparator);
		assertTrue("unsortiert", Util.isSortedSet(set4, comp));
	}
	
//  --------------------
	
	@Test
	public void add_remove () {
		// objects
		String st1 = "Hometrucker Loadplane";
		String st2 = "Carry a pencil";
		String st3 = "Architecture of Sightseeing";
		String st4 = "Segelsetzer der Regattakanäle";
		String st5 = "";
		boolean ok;
	
		// empty constructor
		SortedArrayList<Object> s1 = new SortedArrayList<Object>();
		
		// add one
		ok = s1.add(st1);
		assertTrue("object not added", ok);
		assertTrue(s1.size() == 1);
		assertTrue(s1.contains(st1));
		assertFalse(s1.contains("Aberystwyth"));
		assertTrue(Util.countIterator(s1.iterator()) == 1);
		
		// remove one
		ok = s1.remove(st1);
		assertTrue("object not removed", ok);
		assertTrue(s1.size() == 0);
		assertFalse(s1.contains(st1));
		assertTrue(Util.countIterator(s1.iterator()) == 0);
		assertTrue("unsortiert", Util.isSortedSet(s1, null));
		
		// add three
		ok = s1.add(st1);
		assertTrue("object not added", ok);
		ok = s1.add(st2);
		assertTrue("object not added", ok);
		ok = s1.add(st3);
		assertTrue("object not added", ok);
		assertTrue(s1.size() == 3);
		assertTrue(s1.contains(st1));
		assertTrue(s1.contains(st2));
		assertTrue(s1.contains(st3));
		assertFalse(s1.contains("Aberystwyth"));
		assertTrue(Util.countIterator(s1.iterator()) == 3);
		assertTrue("unsortiert", Util.isSortedSet(s1, null));
	
		// remove one
		ok = s1.remove(st2);
		assertTrue("object not removed", ok);
		assertTrue(s1.size() == 2);
		assertFalse(s1.contains(st2));
		assertTrue(s1.contains(st1));
		assertTrue(s1.contains(st3));
		assertTrue(Util.countIterator(s1.iterator()) == 2);
		assertTrue("unsortiert", Util.isSortedSet(s1, null));
		
		// remove one
		ok = s1.remove(st1);
		assertTrue("object not removed", ok);
		assertTrue(s1.size() == 1);
		assertFalse(s1.contains(st1));
		assertFalse(s1.contains(st2));
		assertTrue(s1.contains(st3));
		assertTrue(Util.countIterator(s1.iterator()) == 1);
		
		// remove last
		ok = s1.remove(st3);
		assertTrue("object not removed", ok);
		assertTrue(s1.size() == 0);
		assertFalse(s1.contains(st1));
		assertFalse(s1.contains(st2));
		assertFalse(s1.contains(st3));
		assertTrue(Util.countIterator(s1.iterator()) == 0);
		assertTrue("unsortiert", Util.isSortedSet(s1, null));
		
		// add bulk
		String[] blk1 = {st5, st1, st2, st5, st3, st3, st4, st5};
		ArrayList<String> lst1 = new ArrayList<String>();
		for (String s : blk1) lst1.add(s);
		assertTrue(lst1.size() == 8);
		
		s1 = new SortedArrayList<Object>();
		ok = s1.addAll(lst1);
		assertTrue("bulk not added", ok);
		assertTrue(s1.size() == 8);
		assertTrue(s1.contains(st1));
		assertTrue(s1.contains(st2));
		assertTrue(s1.contains(st3));
		assertTrue(s1.contains(st4));
		assertTrue(s1.contains(st5));
		assertFalse(s1.contains("Aberystwyth"));
		assertTrue(Util.countIterator(s1.iterator()) == 8);
		assertTrue("unsortiert", Util.isSortedSet(s1, null));
	
		// remove bulk
		ok = s1.removeAll(lst1);
		assertTrue("bulk not removed", ok);
		assertTrue(s1.size() == 0);
		for (String s : blk1) {
			assertFalse("failing element: " + s, s1.contains(s));
		}
		assertFalse(s1.contains("Aberystwyth"));
		assertTrue(Util.countIterator(s1.iterator()) == 0);
	}

	@Test
	public void add_remove_bulk () {
		SortedArrayList<String> list1 = preloadedStr(10);
		ArraySet<String> set1 = preloadedStr_set(10);
		ArraySet<String> set2 = preloadedStr_set(10);
		assertTrue(Util.isSortedSet(list1, null));
		assertFalse(Util.isSortedSet(set1, null));
		assertFalse(Util.isSortedSet(set2, null));
		
		// add set of elements
		list1.addAll(set1);
		assertTrue(list1.size() == 20);
		assertTrue(Util.isSortedSet(list1, null));
		assertTrue(list1.containsAll(set1));
	
		// add array of elements
		String[] arr = set2.toArray(new String[0]);
		list1.addAll(arr);
		assertTrue(list1.size() == 30);
		assertTrue(Util.isSortedSet(list1, null));
		assertTrue(list1.containsAll(set2));

		// remove set of elements
		assertTrue(list1.removeAll(set1));
		assertTrue("list size error: " + list1.size(), list1.size() == 20);
		assertTrue(Util.isSortedSet(list1, null));
		for (String s : set1) {
			assertFalse(list1.contains(s));
		}

		// remove array of elements
		assertTrue(list1.removeAll(arr));
		assertTrue("list size error: " + list1.size(), list1.size() == 10);
		assertTrue(Util.isSortedSet(list1, null));
		for (String s : arr) {
			assertFalse(list1.contains(s));
		}
	}
	
	@Test
	public void set_index () {
		int n1 = 12;
		SortedArrayList<String> list1 = preloadedStr(n1);
		String st1 = list1.get(5);
		int index = list1.indexOf(st1);
		assertTrue(index == 5);
		
		String st2 = new String(st1);
		assertFalse(st2 == st1);
		
		// replace object at index 5 (same sort identity)
		String st3 = list1.set(5, st2);
		assertTrue(st3 == st1);
		assertTrue(list1.get(5) == st2);
		assertTrue(Util.isSortedSet(list1, null));
		
		// fails: replace at 6 w/ deviating identity
		st3 = list1.get(6);
		assertFalse(st3 == st2);
		try {
			list1.set(5, st3);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
		}
	}
	
	@Test
	public void comparator () {
		SortedArrayList<String> list1 = preloadedStr(20);
		assertTrue(Util.isSortedSet(list1, null));
		assertNull(list1.comparator());
		
		// set reverse order
		Comparator<String> comp = new ReverseComparator<String>();
		list1.setComparator(comp);
		assertTrue(list1.comparator() == comp);
		assertFalse(Util.isSortedSet(list1, null));
		assertTrue(Util.isSortedSet(list1, comp));
	}
	
	private static class ReverseComparator<E extends Comparable<E>> implements Comparator<E>, Serializable {
	    private static final long serialVersionUID = 988276557223200L;

		@Override
		public int compare (E o1, E o2) {
			if (o1 == null | o2 == null)
				throw new NullPointerException();
			int c = o1.compareTo(o2);
			return -c;
		}
	}
	
	private static class ReverseComparator2<E extends Comparable<E>> implements Comparator<E> {

		@Override
		public int compare (E o1, E o2) {
			if (o1 == null | o2 == null)
				throw new NullPointerException();
			int c = o1.compareTo(o2);
			return -c;
		}
	}
}

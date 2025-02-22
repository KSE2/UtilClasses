package kse.utilclass.sets;

/*
*  File: TestC_SortedArraySet.java
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

public class TestC_SortedArraySet {

//	private Random random = new Random();
	private Comparable<?> defObject;
	
	public TestC_SortedArraySet() {
		defObject = new Comparable<Object>() {
			@Override
			public int compareTo(Object o) {
				return 0;
			}
		};
	}

private SortedArraySet<String> preloadedStr (int n) {
	SortedArraySet<String> set = new SortedArraySet<String>(n);
	while (set.size() < n) {
		set.add(Util.randomString(50));
	}
	return set;
}

@Test
public void initial () {
	// empty constructor
	SortedArraySet<Object> s1 = new SortedArraySet<Object>();
	assertTrue(s1.getCurrentCapacity() == 0);
	assertTrue(s1.size() == 0);
	assertFalse(s1.contains(defObject));
	assertTrue(Util.countIterator(s1.iterator()) == 0);

	// initial capacity constructor of zero
	int capacity = 0;
	SortedArraySet<Object> s2 = new SortedArraySet<Object>(capacity);
	assertTrue(s2.getCurrentCapacity() == capacity);
	assertTrue(s2.size() == 0);
	assertFalse(s2.contains(defObject));
	assertTrue(Util.countIterator(s2.iterator()) == 0);
	
	// initial capacity constructor of 68
	capacity = 68;
	s2 = new SortedArraySet<Object>(capacity);
	assertTrue(s2.getCurrentCapacity() == capacity);
	assertTrue(s2.size() == 0);
	assertFalse(s2.contains(defObject));
	assertTrue(Util.countIterator(s2.iterator()) == 0);
	
	// constructor w/ zero collection
	ArrayList<String> sarr = new ArrayList<>();
	SortedArraySet<Object> s3 = new SortedArraySet<Object>(sarr);
	assertTrue(s3.getCurrentCapacity() == 0);
	assertTrue(s3.size() == 0);
	assertFalse(s3.contains(defObject));
	assertTrue(Util.countIterator(s3.iterator()) == 0);
	
	// constructor w/ string collection (size 3)
	sarr.add("Bernd das Brot");
	sarr.add("Sendung mit der Maus");
	sarr.add("Karina");
	s3 = new SortedArraySet<Object>(sarr);
	assertTrue(s3.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s3.size() == 3);
	assertFalse(s3.contains("Aberystwyth"));
	assertTrue(s3.contains("Karina"));
	assertTrue(s3.contains("Bernd das Brot"));
	assertTrue(s3.contains("Sendung mit der Maus"));
	assertTrue(Util.countIterator(s3.iterator()) == 3);
	assertTrue("unsortiert", Util.isSortedSet(s3, null));
	
	// constructor w/ string collection (size 4) incl. duplicate
	sarr.add("Bernd das Brot");
	sarr.add("Sendung mit der Maus");
	sarr.add("Karina");
	sarr.add("Bernd das Brot");
	s3 = new SortedArraySet<Object>(sarr);
	assertTrue(s3.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s3.size() == 3);
	assertFalse(s3.contains("Aberystwyth"));
	assertTrue(s3.contains("Karina"));
	assertTrue(s3.contains("Bernd das Brot"));
	assertTrue(s3.contains("Sendung mit der Maus"));
	assertTrue(Util.countIterator(s3.iterator()) == 3);
	assertTrue("unsortiert", Util.isSortedSet(s3, null));
	
	// constructor w/ SortedSet parameter
	SortedArraySet<Object> s4 = new SortedArraySet<Object>(s3);
	assertTrue(s4.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s4.size() == 3);
	assertFalse(s4.contains("Aberystwyth"));
	assertTrue(s4.contains("Karina"));
	assertTrue(s4.contains("Bernd das Brot"));
	assertTrue(s4.contains("Sendung mit der Maus"));
	assertTrue(Util.countIterator(s4.iterator()) == 3);
	assertTrue("unsortiert", Util.isSortedSet(s4, null));
	
	
	// fails: constructor w/ negative capacity
	try {
		s1 = new SortedArraySet<Object>(-1);
		fail("expected IllegalArgumentException on negative capacity");
	} catch (IllegalArgumentException e) {
	}
	
	// fails: constructor w/ overflow capacity
	try {
		s1 = new SortedArraySet<Object>(Integer.MAX_VALUE);
		fail("expected IllegalArgumentException on oversized capacity");
	} catch (OutOfMemoryError e) {
	}
}

//@Test
//public void initial_types () {
//	SortedArraySet<Dialog> sdia = new SortedArraySet<Dialog>();
//	Comparator<Dialog> comp = new Comparator<Dialog>() {
//
//		@Override
//		public int compare (Dialog d1, Dialog d2) {
//			String t1 = d1.getTitle();
//			String t2 = d2.getTitle();
//			return t1 == null || t2 == null ? 0 : t1.compareTo(t2);
//		}
//		
//	};
//	sdia.setComparator(comp);
//	
//	Dialog d1 = new Dialog((Frame)null);
//	sdia.add(d1);
//	
//	SortedArraySet<Window> swin = new SortedArraySet<Window>(comp);
//	swin.addAll(sdia);
//	assertTrue(swin.contains(d1));
//	assertEquals(swin.comparator(), comp);
//}

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
	SortedArraySet<Object> s1 = new SortedArraySet<Object>();
	
	// add one
	ok = s1.add(st1);
	assertTrue("object not added", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 1);
	assertTrue(s1.contains(st1));
	assertFalse(s1.contains("Aberystwyth"));
	assertTrue(Util.countIterator(s1.iterator()) == 1);
	
	// remove one
	ok = s1.remove(st1);
	assertTrue("object not removed", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
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
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
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
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 2);
	assertFalse(s1.contains(st2));
	assertTrue(s1.contains(st1));
	assertTrue(s1.contains(st3));
	assertTrue(Util.countIterator(s1.iterator()) == 2);
	assertTrue("unsortiert", Util.isSortedSet(s1, null));
	
	// remove one
	ok = s1.remove(st1);
	assertTrue("object not removed", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 1);
	assertFalse(s1.contains(st1));
	assertFalse(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue(Util.countIterator(s1.iterator()) == 1);
	
	// remove last
	ok = s1.remove(st3);
	assertTrue("object not removed", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
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
	
	s1 = new SortedArraySet<Object>();
	ok = s1.addAll(lst1);
	assertTrue("bulk not added", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 5);
	assertTrue(s1.contains(st1));
	assertTrue(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue(s1.contains(st4));
	assertTrue(s1.contains(st5));
	assertFalse(s1.contains("Aberystwyth"));
	assertTrue(Util.countIterator(s1.iterator()) == 5);
	assertTrue("unsortiert", Util.isSortedSet(s1, null));

	// remove bulk
	ok = s1.removeAll(lst1);
	assertTrue("bulk not removed", ok);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 0);
	for (String s : blk1)
		assertFalse("failing element: " + s, s1.contains(s));
	assertFalse(s1.contains("Aberystwyth"));
	assertTrue(Util.countIterator(s1.iterator()) == 0);
}

@Test
public void contains () {
	int n = 32;
	SortedArraySet<String> s1 = preloadedStr(n);

	assertTrue(s1.size == n);
	assertTrue(Util.isSortedSet(s1, null));
	assertTrue(Util.countIterator(s1.iterator()) == n);
	
	// contains itself
	int count = 0;
	for (String s : s1) {
		assertTrue("not contained element: " + s, s1.contains(s));
		System.out.println(++count + " : " + s);
	}
	
	// contains alien class
	Integer i = 2988;
	assertFalse(s1.contains(i));
}

@Test
@SuppressWarnings("unchecked")
public void clone_test () {
	int n = 12;
	SortedArraySet<String> s1 = preloadedStr(n);
	SortedArraySet<String> s2 = (SortedArraySet<String>) s1.clone();
	assertTrue(s2.size() == n);
	
	Iterator<String> it1 = s1.iterator();
	Iterator<String> it2 = s2.iterator();
	for (int i = 0; i < n; i++) {
		assertTrue("clone compare test failed", it1.next().equals(it2.next()));
	}
}

@Test
@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
public void equals_test () {
	int n = 32;
	SortedArraySet<String> s1 = preloadedStr(n);
	SortedArraySet<String> s2 = (SortedArraySet<String>) s1.clone();
	SortedArraySet<String> s3 = preloadedStr(n);

	// equal relations
	assertTrue("compare equals failed", s1.equals(s2));
	assertTrue("compare equals failed", s2.equals(s1));
	assertTrue("compare equals failed", s2.equals(s2));
	assertTrue("compare equals hashcode failed", s1.hashCode() == s2.hashCode());

	// non-equal relations
	assertFalse("compare non-equals failed", s1.equals(null));
	assertFalse("compare non-equals failed", s1.equals(s3));
	assertFalse("compare non-equals failed", s3.equals(s1));
	
	s2.setComparator(new ReverseComparator<String>());
	assertFalse("non-equals reverse sorting failed", s1.equals(s2));
	assertFalse("reverse sorting hashcode failed", s1.hashCode() == s2.hashCode());
	
	// alien compare
	assertFalse("alien compare failed", s1.equals(new Integer(8)));
	assertFalse("null compare failed", s1.equals(null));
	List<String> list = new ArrayList<>(s1);
	assertTrue("identical collection compare failed", s1.equals(list));
}

@Test
public void clear () {
	// objects
	String st1 = "Hometrucker Loadplane";
	String st2 = "Carry a pencil";
	String st3 = "   ";
	String st4 = "Segelsetzer der Regattakanäle";
	String st5 = "";

	// empty constructor
	SortedArraySet<Object> s1 = new SortedArraySet<Object>();
	s1.add(st1); s1.add(st2); s1.add(st3); s1.add(st4); s1.add(st5);
	assertTrue(s1.getCurrentCapacity() == SortedArraySet.DEFAULT_CAPACITY);
	assertTrue(s1.size() == 5);
	
	s1.clear();
	assertTrue(s1.getCurrentCapacity() == 0);
	assertTrue(s1.size() == 0);
	assertFalse(s1.contains(st1));
	assertFalse(s1.contains("Aberystwyth"));
	assertTrue(Util.countIterator(s1.iterator()) == 0);
}

@Test
public void test_clone () {
	int n = 50;
	SortedArraySet<String> s1 = preloadedStr(n);
	
	assertTrue(s1.getCurrentCapacity() == n);
	assertTrue(s1.size() == n);
	
	@SuppressWarnings("unchecked")
	SortedArraySet<String> s2 = (SortedArraySet<String>) s1.clone();
	assertTrue(s2.getCurrentCapacity() == n);
	assertTrue(s2.size() == n);
	
	// test content is identical
	for (String a : s1) {
		assertTrue(s2.contains(a));
	}
	for (String a : s2) {
		assertTrue(s1.contains(a));
	}

	// test sorting
	assertTrue("unsortiert", Util.isSortedSet(s1, null));
	assertTrue("unsortiert", Util.isSortedSet(s2, null));

	
	// test for data independence
	Object[] content = s2.toArray();
	s1.clear();
	assertTrue(Arrays.equals(content, s2.toArray()));
}

@Test
public void ensure_capacity () {
	// internal
	int n = 50;
	SortedArraySet<String> s1 = preloadedStr(n);
	assertTrue(s1.getCurrentCapacity() == n);
	assertTrue("size reported: " + s1.size(), s1.size() == n);
	
	// internal enlarge capacity 
	s1.add("new string content");
	assertTrue(s1.getCurrentCapacity() == n * 2);
	assertTrue(s1.size() == n+1);

	// external enlarge capacity
	Object[] content = s1.toArray();
	int m = s1.getCurrentCapacity() * 3;
	s1.ensureCapacity(m);
	assertTrue(s1.getCurrentCapacity() == m);
	assertTrue(s1.size() == n+1);
	assertTrue(Arrays.equals(content, s1.toArray()));
	for (Object a : content) {
		assertTrue(s1.contains(a));
	}
	
	// noop: ensure less than current capacity
	m = s1.getCurrentCapacity();
	s1.ensureCapacity(m - m/2);
	assertTrue(s1.getCurrentCapacity() == m);
	assertTrue(s1.size() == n+1);
	
	// noop: negative capacity -1
	s1.ensureCapacity(-1);
	assertTrue(s1.getCurrentCapacity() == m);
	assertTrue(s1.size() == n+1);

	// noop: negative capacity -2*m
	s1.ensureCapacity(-2*m);
	assertTrue(s1.getCurrentCapacity() == m);
	assertTrue(s1.size() == n+1);
}

@Test
public void taylorCapacity () {
	
}

@Test
public void iterator () {
	int n = 20;
	SortedArraySet<String> s1 = preloadedStr(n);
	Object[] content = s1.toArray();
	List<Object> vlist = Arrays.asList(content);
	List<String> itlist = new ArrayList<String>();
	assertTrue(vlist.size() == n);
	
	// all iterated elements are in the content set 
	for (Iterator<String> it = s1.iterator(); it.hasNext();) {
		String s = it.next(); 
		assertTrue("iterated element not in content: ".concat(s), vlist.contains(s));
		itlist.add(s);
	}
	assertTrue(itlist.size() == n);

	// all elements of the content set are in the iterated set
	for (Object o : vlist) {
		assertTrue("content element not iterated: ".concat((String)o), itlist.contains(o));
	}
}

@Test
public void union () {
	// sets
	int n1 = 25;
	int n2 = 12;
	SortedArraySet<String> set1 = preloadedStr(n1);
	SortedArraySet<String> set2 = preloadedStr(n2);
	OperatingSet<String> set3;
	List<Object> vlist1 = Arrays.asList(set1.toArray());
	List<Object> vlist2 = Arrays.asList(set2.toArray());
	List<Object> vlistUnited = new ArrayList<Object>(vlist1);
	for (Object o : vlist2) {
		if (!vlistUnited.contains(o)) {
			vlistUnited.add(o);
		}
	}
	int n3 = vlistUnited.size();
	
	// test external result
	set3 = set1.united(set2);
	assertTrue(set3.size() == n3);
	assertTrue(set3.containsAll(vlist1));
	assertTrue(set3.containsAll(vlist2));
	for (Object o : vlistUnited) {
		assertTrue("not contained in result set: ".concat((String)o), set3.contains(o));
	}
	assertTrue(Util.isSortedSet(set3, null));
	assertTrue("type mismatch", set3 instanceof SortedArraySet);
	
	// test source sets unmodified
	assertTrue(set1.size() == n1);
	assertTrue(set1.containsAll(vlist1));
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
	
	// test internal result
	set1.uniteWith(set2);
	assertTrue(set1.size() == n3);
	for (Object o : vlistUnited) {
		assertTrue("not contained in result set: ".concat((String)o), set1.contains(o));
	}
	assertTrue(Util.isSortedSet(set1, null));
	
	// test argument set unmodified
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
}

@Test
public void intersection () {
	// sets
	int n1 = 25;
	SortedArraySet<String> set1 = preloadedStr(n1);
	SortedArraySet<String> set2 = preloadedStr(12);
	Iterator<String> it = set1.iterator();
	for (int i = 0; i < 10; i++) {
		String o = it.next();
		if (i % 2 == 0) {
			set2.add(o);
		}
	}
	int n2 = set2.size();
	
	OperatingSet<String> set3;
	List<Object> vlist1 = Arrays.asList(set1.toArray());
	List<Object> vlist2 = Arrays.asList(set2.toArray());
	List<Object> vlistIntersected = new ArrayList<Object>(vlist1);
	vlistIntersected.retainAll(vlist2);
	int n3 = vlistIntersected.size();
	
	// test external result
	set3 = set1.intersected(set2);
	assertTrue(set3.size() == n3);
	assertTrue(set3.containsAll(vlistIntersected));
	assertTrue(vlistIntersected.containsAll(set3));
	assertTrue(Util.isSortedSet(set3, null));
	assertTrue("type mismatch", set3 instanceof SortedArraySet);

	// test source sets unmodified
	assertTrue(set1.size() == n1);
	assertTrue(set1.containsAll(vlist1));
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
	
	// test internal result
	set1.intersectWith(set2);
	assertTrue(set1.size() == n3);
	assertTrue(set1.containsAll(vlistIntersected));
	assertTrue(vlistIntersected.containsAll(set1));
	assertTrue(Util.isSortedSet(set1, null));
	
	// test argument set unmodified
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
}

@Test
public void negation () {
	// sets
	int n1 = 25;
	SortedArraySet<String> set1 = preloadedStr(n1);
	SortedArraySet<String> set2 = preloadedStr(12);
	Iterator<String> it = set1.iterator();
	for (int i = 0; i < 7; i++) {
		String o = it.next();
		set2.add(o);
	}
	int n2 = set2.size();
	
	OperatingSet<String> set3;
	List<Object> vlist1 = Arrays.asList(set1.toArray());
	List<Object> vlist2 = Arrays.asList(set2.toArray());
	List<Object> vlistResult = new ArrayList<Object>(vlist1);
	vlistResult.removeAll(vlist2);
	int n3 = vlistResult.size();
	System.out.println("negation: n1=" + n1 + " n2=" + n2 + " n3=" + n3);
	
	// test external result
	set3 = set1.without(set2);
	assertTrue(set3.size() == n3);
	assertTrue(set3.containsAll(vlistResult));
	assertTrue(vlistResult.containsAll(set3));
	assertTrue(Util.isSortedSet(set3, null));
	assertTrue("type mismatch", set3 instanceof SortedArraySet);

	// test source sets unmodified
	assertTrue(set1.size() == n1);
	assertTrue(set1.containsAll(vlist1));
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
	
	// test internal result
	set1.exclude(set2);
	assertTrue(set1.size() == n3);
	assertTrue(set1.containsAll(vlistResult));
	assertTrue(vlistResult.containsAll(set1));
	assertTrue(Util.isSortedSet(set1, null));
	
	// test argument set unmodified
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
}

@Test
public void xoring () {
	// sets
	int n1 = 25;
	SortedArraySet<String> set1 = preloadedStr(n1);
	SortedArraySet<String> set2 = preloadedStr(12);
	Iterator<String> it = set1.iterator();
	for (int i = 0; i < 7; i++) {
		String o = it.next();
		set2.add(o);
	}
	int n2 = set2.size();
	List<Object> vlist1 = Arrays.asList(set1.toArray());
	List<Object> vlist2 = Arrays.asList(set2.toArray());
	
	// build control set (union(set1, set2) w/o intersection)
	OperatingSet<String> set3 = new SortedArraySet<String>(set1);
	set3.retainAll(set2);
	SortedArraySet<String> ctrl = new SortedArraySet<String>(set1);
	ctrl.addAll(set2);
	ctrl.removeAll(set3);
	int n3 = ctrl.size();
	
	// test external result
	set3 = set1.xored(set2);
	assertTrue("size error, size=" + set3.size() + "/" + n3, set3.size() == n3);
	assertTrue(set3.containsAll(ctrl));
	assertTrue(ctrl.containsAll(set3));
	assertTrue(Util.isSortedSet(set3, null));
	assertTrue("type mismatch", set3 instanceof SortedArraySet);

	// test source sets unmodified
	assertTrue(set1.size() == n1);
	assertTrue(set1.containsAll(vlist1));
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
	
	// test internal result
	set1.xorWith(set2);
	assertTrue(set1.size() == n3);
	assertTrue(set1.containsAll(ctrl));
	assertTrue(ctrl.containsAll(set1));
	assertTrue(Util.isSortedSet(set1, null));
	
	// test argument set unmodified
	assertTrue(set2.size() == n2);
	assertTrue(set2.containsAll(vlist2));
}

@SuppressWarnings("unchecked")
@Test
public void serialisation () throws IOException {
	int n1 = 12;
	SortedArraySet<String> set1 = preloadedStr(n1);
	SortedArraySet<String> set2 = preloadedStr(12);
	SortedArraySet<String> set3, set4, set5, set6;
	Serialiser sss = new Serialiser();
	
	// serialise
	byte[] ser1 = sss.serialise(set1);
	assertNotNull("serialisation is null", ser1);
	assertTrue("serialisation is empty, len = " + ser1.length, ser1.length > 100);
	byte[] ser2 = sss.serialise(set2);
	assertNotNull("serialisation is null", ser2);
	assertTrue("serialisation is empty", ser2.length > 100);
	
	// de-serialise
	set3 = (SortedArraySet<String>) sss.deserialiseObject(ser1);
	assertNotNull("de-serialisation is null", set3);
	assertTrue("serialisation is invalid", set3.equals(set1));
	assertTrue("unsortiert", Util.isSortedSet(set3, null));

	set4 = (SortedArraySet<String>) sss.deserialiseObject(ser2);
	assertNotNull("de-serialisation is null", set4);
	assertTrue("serialisation is invalid", set4.equals(set2));
	
	// empty value
	set5 = new SortedArraySet<String>();
	assertTrue(set5.isEmpty());
	byte[] ser3 = sss.serialise(set5);
	assertNotNull("serialisation is null", ser3);
	assertTrue("serialisation is empty", ser3.length > 10);
	set6 = (SortedArraySet<String>) sss.deserialiseObject(ser3);
	assertTrue(set6.isEmpty());
	assertTrue("serialisation is invalid", set5.equals(set6));
	
	// set w/ non-serialisable comparator
	try {
		set3.setComparator(new ReverseComparator<String>());
		ser1 = sss.serialise(set3);
		fail("expected NotSerializableException");
	} catch (NotSerializableException e) {
		System.err.println(e);
	}
	
	// set w/ serialisable comparator
	Comparator<String> comp = new ReverseComparator2<String>();
	set3.setComparator(comp);
	assertTrue("unsortiert", Util.isSortedSet(set3, comp));
	ser1 = sss.serialise(set3);
	set4 = (SortedArraySet<String>) sss.deserialiseObject(ser1);
	assertTrue("serialisation w/ comparator is invalid", set3.equals(set4));
	assertNotNull("comparator missing after restore", set4.comparator());
	assertNotNull("illegal comparator after restore", set4.comparator() instanceof ReverseComparator2);
	assertTrue("unsortiert", Util.isSortedSet(set4, comp));
}

private static class ReverseComparator<E extends Comparable<E>> implements Comparator<E> {

	@Override
	public int compare (E o1, E o2) {
		if (o1 == null | o2 == null)
			throw new NullPointerException();
		int c = o1.compareTo(o2);
		return -c;
	}
}

private static class ReverseComparator2<E extends Comparable<E>> implements Comparator<E>, Serializable {
    private static final long serialVersionUID = 988276557223200L;

	@Override
	public int compare (E o1, E o2) {
		if (o1 == null | o2 == null)
			throw new NullPointerException();
		int c = o1.compareTo(o2);
		return -c;
	}
}
}

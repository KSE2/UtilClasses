package kse.utilclass.sets;

/*
*  File: TestC_SetStack.java
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import kse.utilclass.io.Serialiser;

public class TestC_SetStack {

	private Random random = new Random();
	
	public TestC_SetStack() {
	}

private int countIterator (Iterator<?> it) {
	int ct = 0;
	while (it.hasNext()) {
		ct++; 
		it.next();
	}
	return ct;
}

private String randomString () {
	int n = random.nextInt(50);
	StringBuffer buf = new StringBuffer(n);
	for (int i = 0; i < n; i++) {
		char c = (char) (random.nextInt(26) + 'a');
		buf.append(c);
	}
	return buf.toString();
}

private SetStack<String> preloadedStr (int n) {
	SetStack<String> set = new SetStack<>(n);
	while (set.size() < n) {
		set.add(randomString());
	}
	return set;
}

@Test
public void initial () {
	// empty constructor
	SetStack<Object> s1 = new SetStack<Object>();
	assertTrue(s1.size() == 0);
	assertTrue(s1.empty());
	assertTrue(s1.isEmpty());
	assertFalse(s1.contains(null));
	assertFalse(s1.contains(new Object()));
	assertNotNull(s1.iterator());
	assertTrue(countIterator(s1.iterator()) == 0);
	assertTrue(s1.getList().isEmpty());
	assertTrue(s1.getStackList().isEmpty());
	assertTrue(s1.search(null) == -1);
	assertNull(s1.peek());
	assertNull(s1.pop());

	// initial capacity constructor
	int capacity = 100;
	SetStack<Object> s2 = new SetStack<Object>(capacity);
	assertTrue(s2.size() == 0);
	assertTrue(s2.empty());
	assertNull(s2.peek());
	assertNull(s2.pop());
	
	// constructor w/ zero collection
	ArrayList<String> sarr = new ArrayList<>();
	SetStack<String> s3 = new SetStack<>(sarr);
	assertTrue(s3.size() == 0);
	assertTrue(s3.empty());
	assertFalse(s3.contains(null));
	assertFalse(s3.contains(new Object()));
	assertTrue(countIterator(s3.iterator()) == 0);
	
	// constructor w/ string collection (size 4) incl. 1 duplicate
	sarr.add("Bernd das Brot");
	sarr.add("Sendung mit der Maus");
	sarr.add("Karina");
	sarr.add("Bernd das Brot");
	s3 = new SetStack<>(sarr);
	assertTrue(s3.size() == 3);
	assertFalse(s3.empty());
	assertFalse(s3.contains(null));
	assertFalse(s3.contains(new Object()));
	assertTrue(s3.contains("Karina"));
	assertTrue(s3.contains("Bernd das Brot"));
	assertTrue(s3.contains("Sendung mit der Maus"));
	assertTrue(countIterator(s3.iterator()) == 3);
	
	// rendered lists control
	List<String> list = sarr.subList(1, 4);
	assertTrue(s3.getList().equals(list));
	list = s3.getStackList();
	Collections.reverse(list);
	assertTrue(list.equals(s3.getList()));
	
	// order control
	String hs1 = s3.peek();
	assertTrue(hs1.equals("Bernd das Brot"));
	assertTrue(s3.search("Karina") == 2);
	assertTrue(s3.search("Sendung mit der Maus") == 3);
	String hs2 = s3.pop();
	assertTrue(hs2 == hs1);
	
	// fails: constructor w/ negative capacity
	try {
		s1 = new SetStack<Object>(-1);
		fail("expected IllegalArgumentException on negative capacity");
	} catch (IllegalArgumentException e) {
	}
	
	// fails: constructor w/ overflow capacity
	try {
		s1 = new SetStack<Object>(Integer.MAX_VALUE);
		fail("expected OutOfMemoryError on oversized capacity");
	} catch (OutOfMemoryError e) {
	}
}

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
	SetStack<Object> s1 = new SetStack<>();
	SetStack<Object> s2;
	
	// add one
	ok = s1.add(st1);
	assertTrue("object not added", ok);
	assertTrue(s1.size() == 1);
	assertTrue(s1.contains(st1));
	assertFalse(s1.contains(null));
	assertFalse(s1.contains(new Object()));
	assertTrue(countIterator(s1.iterator()) == 1);
	
	// remove nonsense
	s1.remove(st4);
	assertTrue(s1.size() == 1);
	try {
		s1.remove(2);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
	try {
		s1.remove(-1);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
	
	// remove one
	ok = s1.remove(st1);
	assertTrue("object not removed", ok);
	assertTrue(s1.size() == 0);
	assertFalse(s1.contains(st1));
	assertTrue(countIterator(s1.iterator()) == 0);
	
	// add three
	List<Object> list = new ArrayList<>();
	assertTrue("object not added", s1.add(st1));
	list.add(st1);
	assertTrue("object not added", s1.add(st2));
	list.add(st2);
	assertTrue("object not added", s1.add(st3));
	list.add(st3);
	assertTrue(s1.size() == 3);
	assertTrue(s1.contains(st1));
	assertTrue(s1.contains(st2));
	assertTrue(s1.contains(st3));
	
	// control stack
	assertTrue("false stack order", s1.peek().equals(st3));
	assertTrue("false stack order", s1.search(st1) == 3);
	assertTrue("false stack order", s1.search(st2) == 2);
	assertTrue("false stack order", s1.search(st3) == 1);
	assertTrue("false entry list", s1.getList().equals(list));
	Collections.reverse(list);
	assertTrue("false stack list", s1.getStackList().equals(list));

	// non-extending push operation (repetition)
	s1.push(st1);
	assertTrue(s1.size() == 3);
	assertTrue(s1.contains(st1));
	assertTrue(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue("stack order error", s1.search(st1) == 1);
	assertTrue("false stack order", s1.search(st2) == 3);
	assertTrue("false stack order", s1.search(st3) == 2);
	assertTrue("stack order error", s1.peek() == st1);
	
	
	// pop top of the stack
	Object en = s1.pop();
	assertTrue("stack pop error", en.equals(st1));
	assertTrue(s1.size() == 2);
	assertFalse(s1.contains(st1));
	assertTrue(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue("false stack order", s1.search(st2) == 2);
	assertTrue("false stack order", s1.search(st3) == 1);
	assertTrue(countIterator(s1.iterator()) == 2);
	assertTrue("peek error after pop", s1.peek().equals(st3));
	
	// remove one
	ok = s1.remove(st2);
	assertTrue("object not removed", ok);
	assertTrue(s1.size() == 1);
	assertFalse(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue("false stack order", s1.search(st3) == 1);
	assertTrue(s1.peek().equals(st3));
	assertTrue(countIterator(s1.iterator()) == 1);
	
	// pop last
	en = s1.pop();
	assertTrue("stack pop error", en.equals(st3));
	assertTrue(s1.size() == 0);
	assertTrue(s1.empty());
	assertFalse(s1.contains(st3));
	assertTrue(countIterator(s1.iterator()) == 0);
	
	// add bulk
	String[] blk1 = {st1, null, st2, st3, st3, st4, st5, st4, null};
	ArrayList<Object> lst1 = new ArrayList<>();
	for (String s : blk1) lst1.add(s);
	assertTrue(lst1.size() == 9);
	
	s1 = new SetStack<Object>(blk1);
	s2 = new SetStack<Object>(lst1);
	assertTrue("list initialisation error", s1.equals(s2));
//	ok = s1.addAll(lst1);
//	assertTrue("bulk not added", ok);
	assertTrue(s1.size() == 6);
	assertTrue(s1.contains(st1));
	assertTrue(s1.contains(st2));
	assertTrue(s1.contains(st3));
	assertTrue(s1.contains(st4));
	assertTrue(s1.contains(st5));
	assertTrue(s1.contains(null));
	assertFalse(s1.contains(new Object()));
	assertTrue(countIterator(s1.iterator()) == 6);
	
	// remove bulk
	ok = s1.removeAll(lst1);
	assertTrue("bulk not removed", ok);
	assertTrue(s1.size() == 0);
	assertTrue(s1.empty());
	assertTrue(countIterator(s1.iterator()) == 0);
}

@Test
public void set () {
	// objects
	String st1 = "Hometrucker Loadplane";
	String st2 = "Carry a pencil";
	String st3 = "Architecture of Sightseeing";
	String st4 = "Segelsetzer der Regattakanäle";
	String st5 = "";
	String[] initArr = new String[] {st1, st2, st3, st4, st5, null};

	// constructor
	SetStack<String> s1 = new SetStack<>(initArr);
	assertTrue(s1.size() == 6);
	assertNull(s1.peek());
	
	// replace item index 2 (unknown item)
	String st6 = "Im Knäuel der Friedrichskatzen";
	String str = s1.set(2, st6);
	assertTrue("set error, false replacement", str == st3);
	assertTrue("list size error", s1.size() == 6);
	assertTrue("error in replacement position", s1.get(2) == st6);
	
	// replace item already existing at the given index
	String st7 = new String(st2);
	str = s1.set(1, st7);
	assertTrue("set error, false replacement", str == st2);
	assertTrue("list size error", s1.size() == 6);
	assertTrue("error in replacement position", s1.get(1) == st7);
	assertTrue("error in list", s1.get(4) == st5);
	
	// replace item already existing (decrease of list size, decreasing position)
	String st8 = new String(st7);
	str = s1.set(4, st8);
	assertTrue("set error, false replacement", str == st5);
	assertTrue("list size error", s1.size() == 5);
	assertTrue("error in replacement position", s1.get(1) == st6);
	assertTrue("error in replacement position", s1.get(3) == st8);

	// replace item already existing (decrease of list size, stable position)
	s1 = new SetStack<>(initArr);
	st7 = new String(st5);
	str = s1.set(2, st7);
	assertTrue("set error, false replacement", str == st3);
	assertTrue("list size error", s1.size() == 5);
	assertTrue("error in replacement position", s1.get(2) == st7);
	assertTrue("error in replacement position", s1.get(4) == null);

	// set out-of-bounds
	try {
		s1.set(-1, st2);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
	try {
		s1.set(s1.size(), st2);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
}

@Test
public void insert () {
	// objects
	String st1 = "Hometrucker Loadplane";
	String st2 = "Carry a pencil";
	String st3 = "Architecture of Sightseeing";
	String st4 = "Segelsetzer der Regattakanäle";
	String st5 = "";
	String[] initArr = new String[] {st1, st5, null};

	// constructor
	SetStack<String> s1 = new SetStack<>(initArr);
	assertTrue(s1.size() == 3);
	assertNull(s1.peek());
	
	// insert unknown item at index 1	{st1, st2, st5, null}
	s1.add(1, st2);
	assertTrue("list size error", s1.size() == 4);
	assertTrue(s1.search(st2) == 3);
	assertTrue("error of insert placement", s1.get(1) == st2);
	assertTrue("error of insert placement", s1.get(2) == st5);
	assertNull(s1.peek());
	
	// insert known item at equal position	{st1, st7, st5, null}
	String st7 = new String(st2);
	s1.add(1, st7);
	assertTrue("list size error", s1.size() == 4);
	assertTrue(s1.search(st7) == 3);
	assertTrue("search error", s1.search(st2) == 3);
	assertTrue("error of insert placement", s1.get(1) == st7);
	assertTrue("error of insert placement", s1.get(2) == st5);
	assertNull(s1.peek());
	
	// insert known item at lower position	{st1, st8, st7, null}
	String st8 = new String(st5);
	s1.add(1, st8);
	assertTrue("list size error", s1.size() == 4);
	assertTrue(s1.search(st8) == 3);
	assertTrue(s1.search(st5) == 3);
	assertTrue("error of insert placement", s1.get(1) == st8);
	assertTrue("error of insert placement", s1.get(2) == st7);
	assertNull(s1.peek());
	
	// insert known item at higher position	{st8, st3, st7, null}
	st3 = new String(st1);
	s1.add(2, st3);
	assertTrue("list size error", s1.size() == 4);
	assertTrue(s1.search(st3) == 3);
	assertTrue(s1.search(st8) == 4);
	assertTrue("error of insert placement", s1.get(1) == st3);
	assertTrue("error of insert placement", s1.get(2) == st7);
	assertNull(s1.peek());
	
	// insert on top 	{st8, st3, st7, null, st4}
	int index = s1.size();
	s1.add(index, st4);
	assertTrue("list size error", s1.size() == 5);
	assertTrue(s1.search(st4) == 1);
	assertTrue(s1.search(st8) == 5);
	assertTrue("error of insert placement", s1.get(4) == st4);
	assertTrue(s1.peek() == st4);
	
	// insert out-of-bounds
	try {
		s1.add(-1, st2);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
	try {
		s1.add(s1.size()+1, st2);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
}

@Test
public void clear () {
	// objects
	String st1 = "Hometrucker Loadplane";
	String st2 = "Carry a pencil";
	String st3 = null;
	String st4 = "Segelsetzer der Regattakanäle";
	String st5 = "";

	// load structure
	SetStack<Object> s1 = new SetStack<>();
	s1.add(st1); s1.add(st2); s1.add(st3); s1.add(st4); s1.add(st5);
	assertTrue(s1.size() == 5);
	
	s1.clear();
	assertTrue(s1.size() == 0);
	assertFalse(s1.contains(st1));
	assertFalse(s1.contains(null));
	assertFalse(s1.contains(new Object()));
	assertFalse(s1.iterator().hasNext());
	
	// stack methods
	assertTrue(s1.empty());
	assertNull(s1.peek());
	assertNull(s1.pop());
	assertNull(s1.peek());
	assertTrue(s1.search(st2) == -1);
	assertTrue(s1.getList().isEmpty());
	assertTrue(s1.getStackList().isEmpty());
	try {
		s1.getReverseIndex(0);
		fail("expected IndexOutOfBoundsException");
	} catch (IndexOutOfBoundsException e) {
	}
	
	// can populate after clear
	s1.add(st1); s1.add(st2);
	assertTrue(s1.size() == 2);
	assertTrue(s1.peek() == st2);
}

@Test
public void clone_test () {
	int n = 50;
	SetStack<String> s1 = preloadedStr(n);
	
	assertTrue(s1.size() == n);
	
	@SuppressWarnings("unchecked")
	SetStack<String> s2 = (SetStack<String>) s1.clone();
	assertFalse(s2 == s1);
	assertTrue(s2.size() == n);
	assertTrue(s2.equals(s1));
	
	// both structures contain identical elements
	Iterator<String> it1 = s1.iterator();
	Iterator<String> it2 = s2.iterator();
	for (int i = 0; i < n; i++) {
		assertTrue(it1.next() == it2.next());
	}

	// test for independence
	Object[] content = s2.toArray();
	s1.clear();
	assertTrue(Arrays.equals(content, s2.toArray()));
}

@Test
public void iterator () {
	int n = 20;
	SetStack<String> s1 = preloadedStr(n);
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
	
	// sort order of the iterated set
	assertTrue(itlist.equals(s1.getStackList()));
}

@SuppressWarnings("unchecked")
@Test
public void serialisation () throws IOException {
	int n1 = 12;
	SetStack<String> set1 = preloadedStr(n1);
	SetStack<String> set2 = preloadedStr(12);
	SetStack<String> set3, set4, set5, set6;
	Serialiser sss = new Serialiser();
	
	// serialise
	byte[] ser1 = sss.serialise(set1);
	assertNotNull("serialisation is null", ser1);
	assertTrue("serialisation is empty, len = " + ser1.length, ser1.length > 100);
	byte[] ser2 = sss.serialise(set2);
	assertNotNull("serialisation is null", ser2);
	assertTrue("serialisation is empty", ser2.length > 100);
	
	// de-serialise
	set3 = (SetStack<String>) sss.deserialiseObject(ser1);
	assertNotNull("de-serialisation is null", set3);
	assertTrue("serialisation is invalid", set3.equals(set1));

	set4 = (SetStack<String>) sss.deserialiseObject(ser2);
	assertNotNull("de-serialisation is null", set4);
	assertTrue("serialisation is invalid", set4.equals(set2));
	
	// empty value
	set5 = new SetStack<>();
	assertTrue(set5.isEmpty());
	byte[] ser3 = sss.serialise(set5);
	assertNotNull("serialisation is null", ser3);
	assertTrue("serialisation is empty", ser3.length > 10);
	set6 = (SetStack<String>) sss.deserialiseObject(ser3);
	assertTrue(set6.isEmpty());
	assertTrue("serialisation is invalid", set5.equals(set6));
}

}

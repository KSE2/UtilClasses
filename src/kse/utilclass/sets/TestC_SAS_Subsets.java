package kse.utilclass.sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Test;

import kse.utilclass.misc.Util;

public class TestC_SAS_Subsets {


	
private SortedArraySet<String> preloadedStr (int n) {
	SortedArraySet<String> set = new SortedArraySet<String>(n);
	while (set.size() < n) {
		set.add(Util.randomString(50));
	}
	return set;
}

@Test
public void tailSet_basic () {
	SortedArraySet<String> sa1 = new SortedArraySet<>();
	SortedSet<String> sub1, sub2;
	
	// ON EMPTY PARENT
	// failure w/ null parameter
	try {
		sub1 = sa1.tailSet(null);
		fail("expected NullPointerException");
	} catch (NullPointerException e) {
	}

	sub1 = sa1.tailSet("bear");
	assertNotNull(sub1);
	assertTrue(sub1.size() == 0);
	assertTrue(Util.countIterator(sub1.iterator()) == 0);
	assertTrue(sub1.isEmpty());
	assertNull(sub1.first());
	assertNull(sub1.last());
	assertTrue(sub1.toArray().length == 0);
	assertTrue(sub1.toArray(new Object[0]).length == 0);
	
	// ON PARENT SIZE 10
	SortedArraySet<String> sa2 = preloadedStr(10);
	String bLow = Util.getIterValue(sa2.iterator(), 3);
	sub1 = sa2.tailSet(bLow);
	assertNotNull(sub1);
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	assertTrue(Util.countIterator(sub1.iterator()) == 7);
	assertFalse(sub1.isEmpty());
	assertTrue(sub1.toArray().length == 7);
	assertTrue(sub1.toArray(new Object[0]).length == 7);

	// first and last value
	assertNotNull(sub1.first());
	assertNotNull(sub1.last());
	assertEquals(bLow, sub1.first());
	assertEquals(sa2.last(), sub1.last());

	// contains values
	assertTrue(sub1.contains(bLow));
	assertTrue(sub1.contains(sa2.last()));
	assertTrue(Util.isSortedSet(sub1, null));
	assertFalse(sub1.contains(sa2.first()));
	assertFalse(sub1.contains(sa2.getElement(2)));

	// fail: add outbound value
	String v = Util.getRandomValueBelow(bLow, 25);
	try {
		sub1.add(v);
		fail("expected IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	}

	// add inbound value (subset)
	v = Util.getRandomValueAbove(bLow, 25);
	assertTrue(sub1.add(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 8);
	assertTrue(Util.countIterator(sub1.iterator()) == 8);
	assertTrue(sub1.contains(v));
	
	// included in parent
	assertTrue("size detected = " + sa2.size(), sa2.size() == 11);
	assertTrue(Util.countIterator(sa2.iterator()) == 11);
	assertTrue(sa2.contains(v));
	
	// add inbound value (parent)
	v = Util.getRandomValueAbove(bLow, 25);
	assertTrue(sa2.add(v));
	assertTrue(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 12);
	assertTrue(Util.countIterator(sa2.iterator()) == 12);
	
	// included in subset
	assertTrue(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 9);
	assertTrue(Util.countIterator(sub1.iterator()) == 9);
	
	// add outbound value (parent)
	v = Util.getRandomValueBelow(bLow, 25);
	assertTrue(sa2.add(v));
	assertTrue(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 13);
	assertTrue(Util.countIterator(sa2.iterator()) == 13);
	
	// not included in subset
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 9);
	assertTrue(Util.countIterator(sub1.iterator()) == 9);
	
	// remove inbound value (subset)
	v = Util.getIterValue(sub1.iterator(), 3);
	assertTrue(sub1.remove(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 8);
	assertTrue(Util.countIterator(sub1.iterator()) == 8);
	assertFalse(sub1.contains(v));
	
	// removed from parent
	assertTrue("size detected = " + sa2.size(), sa2.size() == 12);
	assertTrue(Util.countIterator(sa2.iterator()) == 12);
	assertFalse(sa2.contains(v));
	
	// remove inbound value (parent)
	v = sub1.last();
	assertTrue(sa2.remove(v));
	assertFalse(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 11);
	assertTrue(Util.countIterator(sa2.iterator()) == 11);
	
	// removed from subset
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	assertTrue(Util.countIterator(sub1.iterator()) == 7);
	
	// remove outbound value (parent)
	v = sa2.first();
	assertTrue(sa2.remove(v));
	assertFalse(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 10);
	assertTrue(Util.countIterator(sa2.iterator()) == 10);
	
	// removed from subset
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	assertTrue(Util.countIterator(sub1.iterator()) == 7);
	
	// fails: remove outbound value (subset)
	v = sa2.first();
	assertFalse(sub1.contains(v));
	assertFalse(sub1.remove(v));
	assertTrue(sa2.contains(v));
	
	// clear subset
	sub1.clear();
	assertTrue("size detected = " + sub1.size(), sub1.size() == 0);
	assertTrue(Util.countIterator(sub1.iterator()) == 0);
	assertTrue(sub1.isEmpty());
	assertTrue("size detected = " + sa2.size(), sa2.size() == 3);
	assertTrue(Util.countIterator(sa2.iterator()) == 3);

	// tailset zero
	String sepV = Util.getRandomValueAbove(sa2.last(), 30);
	sub2 = sa2.tailSet(sepV);
	assertTrue(sub2.size() == 0);
	assertTrue(sub2.isEmpty());
	assertTrue(Util.countIterator(sub2.iterator()) == 0);
	
	// zero: fail to add value below
	try {
		sub2.add(Util.getRandomValueBelow(sepV, 30));
		fail("expected IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	}

	// zero: succeed to add value above limit and transitive in sa2
	v = Util.getRandomValueAbove(sepV, 30);
	sub2.add(v);
	assertTrue(sub2.size() == 1);
	assertTrue(sa2.last().equals(v));
}

@Test
public void headSet_basic () {
	SortedArraySet<String> sa1 = new SortedArraySet<>();
	SortedSet<String> sub1, sub2;
	
	// ON EMPTY PARENT
	// failure w/ null parameter
	try {
		sub1 = sa1.headSet(null);
		fail("expected NullPointerException");
	} catch (NullPointerException e) {
	}

	sub1 = sa1.headSet("bear");
	assertNotNull(sub1);
	assertTrue(sub1.size() == 0);
	assertTrue(Util.countIterator(sub1.iterator()) == 0);
	assertTrue(sub1.isEmpty());
	assertNull(sub1.first());
	assertNull(sub1.last());
	assertTrue(sub1.toArray().length == 0);
	assertTrue(sub1.toArray(new Object[0]).length == 0);
	
	// ON PARENT SIZE 10
	SortedArraySet<String> sa2 = preloadedStr(10);
	String bHigh = Util.getIterValue(sa2.iterator(), 5);
	sub1 = sa2.headSet(bHigh);
	assertNotNull(sub1);
	assertTrue("size detected = " + sub1.size(), sub1.size() == 5);
	assertTrue(Util.countIterator(sub1.iterator()) == 5);
	assertFalse(sub1.isEmpty());
	assertTrue(sub1.toArray().length == 5);
	assertTrue(sub1.toArray(new Object[0]).length == 5);

	// first and last value
	assertNotNull(sub1.first());
	assertNotNull(sub1.last());
	assertEquals(sa2.first(), sub1.first());
	assertEquals(Util.getIterValue(sa2.iterator(), 4), sub1.last());

	// contains values
	assertTrue(Util.isSortedSet(sub1, null));
	assertFalse(sub1.contains(bHigh));
	assertFalse(sub1.contains(sa2.last()));
	assertTrue(sub1.contains(sa2.first()));
	assertTrue(sub1.contains(sa2.getElement(4)));

	// fail: add outbound value
	String v = Util.getRandomValueAbove(bHigh, 25);
	try {
		sub1.add(v);
		fail("expected IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	}

	// add inbound value (subset)
	v = Util.getRandomValueBelow(bHigh, 25);
	assertTrue(sub1.add(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 6);
	assertTrue(Util.countIterator(sub1.iterator()) == 6);
	assertTrue(sub1.contains(v));
	
	// included in parent
	assertTrue("size detected = " + sa2.size(), sa2.size() == 11);
	assertTrue(Util.countIterator(sa2.iterator()) == 11);
	assertTrue(sa2.contains(v));
	
	// add inbound value (parent)
	v = Util.getRandomValueBelow(bHigh, 25);
	assertTrue(sa2.add(v));
	assertTrue(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 12);
	assertTrue(Util.countIterator(sa2.iterator()) == 12);
	
	// included in subset
	assertTrue(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	assertTrue(Util.countIterator(sub1.iterator()) == 7);
	
	// add outbound value (parent)
	v = Util.getRandomValueAbove(bHigh, 25);
	assertTrue(sa2.add(v));
	assertTrue(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 13);
	assertTrue(Util.countIterator(sa2.iterator()) == 13);
	
	// not included in subset
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	assertTrue(Util.countIterator(sub1.iterator()) == 7);
	
	// remove inbound value (subset)
	v = Util.getIterValue(sub1.iterator(), 3);
	assertTrue(sub1.remove(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 6);
	assertTrue(Util.countIterator(sub1.iterator()) == 6);
	assertFalse(sub1.contains(v));
	
	// removed from parent
	assertTrue("size detected = " + sa2.size(), sa2.size() == 12);
	assertTrue(Util.countIterator(sa2.iterator()) == 12);
	assertFalse(sa2.contains(v));
	
	// remove inbound value (parent)
	v = sub1.last();
	assertTrue(sa2.remove(v));
	assertFalse(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 11);
	assertTrue(Util.countIterator(sa2.iterator()) == 11);
	
	// removed from subset
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 5);
	assertTrue(Util.countIterator(sub1.iterator()) == 5);
	
	// remove outbound value (parent)
	v = sa2.last();
	assertTrue(sa2.remove(v));
	assertFalse(sa2.contains(v));
	assertTrue("size detected = " + sa2.size(), sa2.size() == 10);
	assertTrue(Util.countIterator(sa2.iterator()) == 10);
	
	// subset unchanged
	assertFalse(sub1.contains(v));
	assertTrue("size detected = " + sub1.size(), sub1.size() == 5);
	assertTrue(Util.countIterator(sub1.iterator()) == 5);
	
	// fails: remove outbound value (subset)
	v = sa2.last();
	assertFalse(sub1.contains(v));
	assertFalse(sub1.remove(v));
	assertTrue(sa2.contains(v));
	
	// clear subset
	sub1.clear();
	assertTrue("size detected = " + sub1.size(), sub1.size() == 0);
	assertTrue(Util.countIterator(sub1.iterator()) == 0);
	assertTrue(sub1.isEmpty());
	assertTrue("size detected = " + sa2.size(), sa2.size() == 5);
	assertTrue(Util.countIterator(sa2.iterator()) == 5);

	// headset zero
	String sepV = Util.getRandomValueBelow(sa2.first(), 30);
	sub2 = sa2.headSet(sepV);
	assertTrue(sub2.size() == 0);
	assertTrue(sub2.isEmpty());
	assertTrue(Util.countIterator(sub2.iterator()) == 0);
	
	// zero: fail to add value above
	try {
		sub2.add(Util.getRandomValueAbove(sepV, 30));
		fail("expected IllegalArgumentException");
	} catch (IllegalArgumentException e) {
	}

	// zero: succeed to add value below limit and transitive in sa2
	v = Util.getRandomValueBelow(sepV, 30);
	sub2.add(v);
	assertTrue(sub2.size() == 1);
	assertTrue(sa2.first().equals(v));
}


}

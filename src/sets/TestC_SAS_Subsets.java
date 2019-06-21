package sets;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.SortedSet;

import org.junit.Test;

public class TestC_SAS_Subsets {


	
private SortedArraySet<String> preloadedStr (int n) {
	SortedArraySet<String> set = new SortedArraySet<String>(n);
	while (set.size() < n) {
		set.add(Util.randomString(50));
	}
	return set;
}

private <T> T getIterValue (Iterator<T> it, int index) {
	for (int i = 1; i < index; i++) {
		it.next();
	}
	return it.next();
}

@Test
public void tailSet () {
	SortedArraySet<String> sa1 = new SortedArraySet<>();
	SortedSet<String> sub1;
	
	// ON EMPTY PARENT
	// failure w/ null parameter
	try {
		sub1 = sa1.tailSet(null);
	} catch (NullPointerException e) {
	}

	sub1 = sa1.tailSet("bear");
	assertNotNull(sub1);
	assertTrue(sub1.size() == 0);
	
	// ON PARENT SIZE 10
	SortedArraySet<String> sa2 = preloadedStr(10);
	String bLow = getIterValue(sa2.iterator(), 4);
	sub1 = sa1.tailSet(bLow);
	assertNotNull(sub1);
	assertTrue("size detected = " + sub1.size(), sub1.size() == 7);
	
	
}


}

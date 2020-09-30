package sets;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import javax.management.modelmbean.RequiredModelMBean;

class Util {

	private static Random random = new Random();

	/** Whether the given set is sorted in ascending order via its iterator and
	 * the given comparator. An empty set is regarded sorted.
	 *  
	 * @param set {@code Set<T>}
	 * @param comp {@code Comparator<? super T>}, null for "natural" ordering
	 * @return boolean true = sorted ascending, false = else
	 */
	public static <T> boolean isSortedSet (Set<T> set, Comparator<? super T> comp) {
		T t = null;
		for (T o : set) {
			if (t != null) {
				int c = comp == null ? ((Comparable<T>)o).compareTo(t) : comp.compare(o, t);
				if (c < 0) {
					return false;
				} 
			}
			t = o;
		}
		return true;
	}

	/** Returns the number of elements in the given iterator.
	 * 
	 * @param it {@code Iterator<?>}
	 * @return int number of elements
	 */
	public static int countIterator (Iterator<?> it) {
		int ct = 0;
		while (it.hasNext()) {
			ct++; 
			it.next();
		}
		return ct;
	}

	/** Returns a String consisting of random character values (lower case)
	 * and of random length ranging 0 <= len <= maxLength.
	 * 
	 * @param maxLength int maximum string length
	 * @return String
	 */
	public static String randomString (int maxLength) {
		int n = random.nextInt(maxLength);
		StringBuffer buf = new StringBuffer(n);
		for (int i = 0; i < n; i++) {
			char c = (char) (random.nextInt(26) + 'a');
			buf.append(c);
		}
		return buf.toString();
	}

	/** Returns a random String value which sorts below the given value "a".
	 * 
	 * @param a String reference value
	 * @param maxLength int maximum length of the return value
	 * @return String random value (length 0..maxLength)
	 */
	public static String getRandomValueBelow (String a, int maxLength) {
		String v;
		do {
			v = randomString(maxLength);
		} while (v.compareTo(a) >= 0);
		return v;
	}
	
	/** Returns a random String value which sorts above the given value "a".
	 * 
	 * @param a String reference value
	 * @param maxLength int maximum length of the return value
	 * @return String random value (length 0..maxLength)
	 */
	public static String getRandomValueAbove (String a, int maxLength) {
		String v;
		do {
			v = randomString(maxLength);
		} while (v.compareTo(a) <= 0);
		return v;
	}
	
	public static void requirePositive (int i) {
		if (i < 0)
			throw new IllegalArgumentException("argument is negative");
	}
	
	public static void requirePositive (int i, String argument) {
		if (i < 0) {
			String arg = argument == null ? "argument" : argument;
			throw new IllegalArgumentException(arg.concat(" is negative"));
		}
	}

	/** Returns the value from the given iterator at index position.
	 * 
	 * @param it {@code Iterator<T>}
	 * @param index int counting from 0
	 * @return T or null if iterator has no value
	 * @throws IllegalArgumentException if index is negative
	 */
	public static <T> T getIterValue (Iterator<T> it, int index) {
		requirePositive(index, "index");
		if (!it.hasNext()) {
			return null;
		}
		
		for (int i = 0; i < index; i++) {
			it.next();
		}
		return it.next();
	}


}


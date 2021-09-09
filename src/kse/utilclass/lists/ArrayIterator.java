package kse.utilclass.lists;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {

	private T[] arr;
	private int p;
	
	public ArrayIterator (T[] array) {
		if (array == null)
			throw new NullPointerException();
		
		arr = array;
	}

	@Override
	public boolean hasNext() {
		return p < arr.length;
	}

	@Override
	public T next() {
		if ( p >= arr.length ) 
			throw new NoSuchElementException();
		
		T obj = arr[p++];
		return obj;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}

package kse.utilclass.misc;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import kse.utilclass.io.Serialiser;

public class TestC_serialisable {

	public TestC_serialisable() {
	}

	@Test
	public void serialise_1 () throws IOException {
		Serialiser sdev = new Serialiser();
		byte[] ser;
		
		// BooleanResult
		BooleanResult bres = new BooleanResult(true);
		ser = sdev.serialise(bres);
		assertTrue("no serialisation", ser.length > 0);
		BooleanResult bres1 = (BooleanResult) sdev.deserialiseObject(ser);
		assertTrue(bres.equals(bres1)); 
		System.out.println("BooleanResult = " + bres);
		
		// IntegerResult
		IntResult intRes = new IntResult(1981273192);
		ser = sdev.serialise(intRes);
		assertTrue("no serialisation", ser.length > 0);
		IntResult ires = (IntResult) sdev.deserialiseObject(ser);
		assertTrue(ires.equals(intRes)); 
		System.out.println("IntResult = " + ires);
		
		// LongResult
		LongResult longRes = new LongResult(9982781981273192L);
		ser = sdev.serialise(longRes);
		assertTrue("no serialisation", ser.length > 0);
		LongResult lres = (LongResult) sdev.deserialiseObject(ser);
		assertTrue(lres.equals(longRes)); 
		System.out.println("LongResult = " + lres);
		
		// String-Pair
		String s1 = "Ein Esel ging am Straßenrand, und fand ein Blatt, war allerhand!";
		String s2 = "Kobaltblaue Eishüttenberge";
		StringPair spair = new StringPair(s1, s2);
		ser = sdev.serialise(spair);
		assertTrue("no serialisation", ser.length > 0);
		StringPair rpair = (StringPair) sdev.deserialiseObject(ser);
		assertTrue("deserialise error StringPair", spair.equals(rpair));
		assertTrue(s1.equals(rpair.s1));
		assertTrue(s2.equals(rpair.s2));
		System.out.println("StringPair = " + rpair);
		
		// Two-Tuple
		int iv1 = 490012;
		int iv2 = -8233666;
		TwoTuple tuple = new TwoTuple(iv1, iv2);
		ser = sdev.serialise(tuple);
		assertTrue("no serialisation", ser.length > 0);
		TwoTuple res = (TwoTuple) sdev.deserialiseObject(ser);
		assertTrue("deserialise error TwoTuple", tuple.equals(res)); 
		System.out.println("TwoTuple = " + res);
		
		// UUID
		UUID uid = new UUID();
		ser = sdev.serialise(uid);
		assertTrue("no serialisation", ser.length > 0);
		UUID resUid = (UUID) sdev.deserialiseObject(ser);
		assertTrue("deserialise error UUID", uid.equals(resUid));
		assertTrue(Util.equalArrays(uid.getBytes(), resUid.getBytes()));
		System.out.println("UUID = " + resUid);
		
		
		
	}
	
}

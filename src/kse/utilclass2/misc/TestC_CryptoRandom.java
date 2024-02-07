package kse.utilclass2.misc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import kse.utilclass.misc.Log;
import kse.utilclass.misc.Util;

public class TestC_CryptoRandom {

	
	public TestC_CryptoRandom () {
		Log.setLogging(true);
		Log.setDebug(true);
		Log.setLogLevel(10);
		Log.setDebugLevel(10);
	}
	
	@Test
	public void init () {
		CryptoRandom crn = new CryptoRandom();
	
		int instId = crn.getInstanceID();
		assertTrue(crn.getCounter() == 1);
		assertTrue(crn.getCyclePeriod() == 16);
		assertTrue(crn.getPoolData().length >= 32);
		assertTrue(crn.getSystemSeed().length >= 32);
		assertNull(crn.getUserRandom());
		System.out.println();
		
		byte[] urand = Util.randBytes(50);
		CryptoRandom crn2 = new CryptoRandom(4, urand);
		int instId2 = crn2.getInstanceID();
		
		assertFalse(instId2 == instId);
		assertTrue(crn2.getCounter() == 1);
		assertTrue(crn2.getCyclePeriod() == 4);
		assertTrue(crn2.getPoolData().length >= 32);
		assertTrue(crn2.getSystemSeed().length >= 32);
		assertNotNull(crn2.getUserRandom());
		assertTrue(Util.equalArrays(Util.sha512(urand), crn2.getUserRandom()));
		System.out.println();
		
		crn = new CryptoRandom(null);
		
		assertFalse(crn.getInstanceID() == instId || crn.getInstanceID() == instId2);
		assertTrue(crn.getCounter() == 1);
		assertTrue(crn.getCyclePeriod() == 16);
		assertTrue(crn.getPoolData().length >= 32);
		assertTrue(crn.getSystemSeed().length >= 32);
		assertNull(crn.getUserRandom());
		
		try {
			new CryptoRandom(-1);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}

		try {
			new CryptoRandom(-1, new byte[34]);
			fail("expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			System.out.println(e);
		}
	}
	
	@Test
	public void operations_1 () {
		CryptoRandom crn = new CryptoRandom();
		System.out.println();
		
		byte[] sheet = Util.arraycopy(crn.getSheet());
		int len = 32;
		for (int i = 0; i < 4; i++) {
			byte[] d1 = crn.nextBytes(len);
			System.out.println("-- gen. random: " + Util.bytesToHex(d1));
			assertTrue(Util.equalArrays(d1, Util.arraycopy(sheet, i*len, len)));
		}
		System.out.println();
		
		byte[] d2 = crn.nextBytes(len);
		System.out.println("-- gen. random: " + Util.bytesToHex(d2));
		assertTrue(crn.getCounter() == 2);
		assertFalse(Util.equalArrays(sheet, crn.getSheet()));
		assertTrue(Util.equalArrays(d2, Util.arraycopy(crn.getSheet(), len)));
		System.out.println("\nLarge Random:");

		d2 = crn.nextBytes(512);
		System.out.println("-- gen. random: " + Util.bytesToHex(d2));
		assertTrue(crn.getCounter() == 6);
	}
	
	@Test
	public void gen_numbers () {
		CryptoRandom crn = new CryptoRandom();
		System.out.println();
		
		System.out.println("Generated Byte Values:");
		Map<Byte, Byte> map = new HashMap<>();
		int ct = 0;
		for (int i = 0; i < 10; i++) {
			byte val = crn.nextByte();
			map.put(val, null);
			System.out.print(", " + val);
			if (val < 0) ct++;
		}
		int delta = 10 - map.size();
		assertFalse("too many duplicate byte values: " + delta, delta > 1);
		System.out.println("\nNegative: " + ct);
		
		System.out.println("Generated Integer Values:");
		Map<Integer, Integer> map1 = new HashMap<>();
		ct = 0;
		for (int i = 0; i < 50; i++) {
			int val = crn.nextInt();
			map1.put(val, null);
			System.out.print(", " + val);
			if (val < 0) ct++;
		}
		assertTrue("duplicate integer values: " + (50 - map1.size()), map1.size() == 50);
		System.out.println("\nNegative: " + ct);
		
		System.out.println("\nGenerated Long Values:");
		Map<Long, Long> map2 = new HashMap<>();
		ct = 0;
		for (int i = 0; i < 50; i++) {
			Long val = crn.nextLong();
			map2.put(val, null);
			System.out.print(", " + val);
			if (val < 0) ct++;
		}
		assertTrue("duplicate long values: " + (50 - map2.size()), map2.size() == 50);
		System.out.println("\nNegative: " + ct);
		
		System.out.println("\nGenerated Ranged Integers (Range: 1000):");
		map1.clear();
		ct = 0;
		for (int i = 0; i < 100; i++) {
			int val = crn.nextInt(1000);
			assertTrue("integer out of range", val >= 0 && val < 1000);
			map1.put(val, null);
			System.out.print(", " + val);
			if (val < 0) ct++;
		}
		delta = 100 - map1.size();
		assertFalse("duplicate int values: " + delta, delta > 10);
		System.out.println("\nDelta: " + delta + ", Negative: " + ct);
	}
	
	@Test
	public void gen_booleans () {
		CryptoRandom crn = new CryptoRandom();
		System.out.println();
		
		System.out.println("Generated Boolean Values:");
		int ct = 0;
		for (int i = 0; i < 1000; i++) {
			boolean b = crn.nextBoolean();
			if (b) ct++; 
		}
		System.out.println("\nTrue: " + ct + ", False: " + (1000-ct));
		int delta = 500 - ct;
		assertFalse("boolean delta too large: " + delta, Math.abs(delta) > 49);
	}
	
	@Test
	public void recollect () {
		CryptoRandom crn = new CryptoRandom();
		System.out.println();
		
		byte[] useed = Util.randBytes(500);
		crn.nextBytes(200);
		assertNull(crn.getUserRandom());
		
		byte[] oldSheet = crn.getSheet();
		crn.recollect(useed);
		
		assertNotNull(crn.getUserRandom());
		assertTrue(Util.equalArrays(Util.sha512(useed), crn.getUserRandom()));
		assertTrue(Util.equalArrays(oldSheet, crn.getSheet()));
		
	}
	
	@Test
	public void userSeed () {
		OurCryptoRandom crn = new OurCryptoRandom();
		
		assertNotNull( crn.random );
		assertTrue( crn.getUserSeed() == crn.random );
		assertNull( crn.getUserRandom() );
		
		crn.nextBytes(600);
		byte[] seed = crn.getUserRandom();
		assertNotNull(seed);
		assertTrue( Util.equalArrays(Util.sha512(crn.random), crn.getUserRandom()) );
	}
	
	private static class OurCryptoRandom extends CryptoRandom {
		byte[] random = Util.randBytes(200);
		
		public OurCryptoRandom() {
			super(4);
		}
		
		@Override
		public byte[] getUserSeed() {
			return random;
		}
	}
	
}

package net.orfdev;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

public class RandomNumberTest {

	@Test
	public void testRandomnessOfRandom() {
		
		Random rand = new Random(); // get a new random number generator
		long min = 0, max = 0;
		for(int ix = 0; ix < 100; ix++) {
			long next = rand.nextLong(); // get a random number
			System.out.println(next);
			min = Long.min(next, min);
			max = Long.max(next, max);
		}
			
		// test whether the min and max are bounds are sufficient
		// System.out.println("Min: " + min + " Max:" + max);
		assertTrue(min < Long.MIN_VALUE - (Long.MIN_VALUE / 4));
		assertTrue(max > Long.MAX_VALUE - (Long.MAX_VALUE / 4));
	}
}

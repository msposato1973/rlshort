package net.orfdev;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Base62Test {

	private String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	@Test
	public void testEncodeBase62() {
		
		int n = 0;
		String str = "6JaY2";
		char[] chars = str.toCharArray();
		n += ALPHABET.indexOf(chars[0]) * (int) Math.pow(62, 4);
		n += ALPHABET.indexOf(chars[1]) * (int) Math.pow(62, 3);
		n += ALPHABET.indexOf(chars[2]) * (int) Math.pow(62, 2);
		n += ALPHABET.indexOf(chars[3]) * (int) Math.pow(62, 1);
		n += ALPHABET.indexOf(chars[4]) * (int) Math.pow(62, 0);
		assertEquals(str, Base62.encode(n));
		long result = Base62.decode(str);
		System.out.println(result);
	}
	
	@Test
	public void testDecodeBase62() {
		
		String str = "6JaY2";
		long result = Base62.decode(str);
		assertEquals(99424938, result);
		System.out.println(result);
	}
	
	@Test
	public void testBase62EncodingBounds() {
		
		int cMinChars = -1;
		int cMaxChars = -1;
		
		for(long val = 1; val > 0 && val < Long.MAX_VALUE; val = val * 62) {
			
			int cChars = Base62.encode(val).length();
			
			cMinChars = Math.min(cMinChars == -1 ? cChars : cMinChars, cChars);
			cMaxChars = Math.max(cMaxChars == -1 ? cChars : cMaxChars, cChars);
		}
		
		assertEquals(1, cMinChars);
		assertEquals(11, cMaxChars);
	}

}
package com.ubiquity.ubiquitywebserver;

public class BaseConversion {
	
	/**
	 * Takes an MD5 byte array and returns a hex string
	 * http://stackoverflow.com/
	 * questions/332079/in-java-how-do-i-convert-a-byte-
	 * array-to-a-string-of-hex-digits-while-keeping-le/2197650#2197650
	 * 
	 * @param bytes
	 * @return A hex string representing the MD5 hash
	 */
	public static String toHexString(byte[] bytes) {
		char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v / 16];
			hexChars[j * 2 + 1] = hexArray[v % 16];
		}
		return new String(hexChars);
	}
}

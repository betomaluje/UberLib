package cl.betomaluje.android.uberlib.security;

public class RotNEncryption {

	/**
	 * Performs a Rot-N encryption (rotate by "N" places)
	 * 
	 * @param text
	 *            : the target text to encrypt
	 * @param displacement
	 *            : the number of places ("N")
	 * @return
	 */
	public static String encrpyt(String text, int displacement) {
		StringBuffer buffer = new StringBuffer();
		for (char next : text.toCharArray()) {
			buffer.append(rotN(next, displacement));
		}
		return buffer.toString();
	}

	private static char rotN(char letter, int displacement) {
		if (!Character.isLetter(letter)) {
			// invalid character, leave it alone
			return letter;
		}

		boolean isUpper = Character.isUpperCase(letter);
		letter = Character.toLowerCase(letter);
		letter -= 'a'; // map 'a' to 0, 'z' to 25, etc.

		letter = (char) ((letter + displacement) % 26); // perform the rot(n)
														// operation

		letter += 'a'; // map back to the original ASCII character range
		return isUpper ? Character.toUpperCase(letter) : letter;
	}
}

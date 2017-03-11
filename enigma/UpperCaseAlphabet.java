package enigma;

import static enigma.EnigmaException.*;

/** The standard upper-case alphabet.
 *  @author P. N. Hilfinger
 */
class UpperCaseAlphabet extends Alphabet {

    /** The size of my alphabet. */
    private static final int SIZE = 26;

    /** A new alphabet containing the upper-case characters in order. */
    UpperCaseAlphabet() {
        super("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    @Override
    final int size() {
        return SIZE;
    }

    /** Returns true if C is in this alphabet. */
    @Override
    final boolean contains(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    @Override
    final char toChar(int index) {
        if (index < 0 || index >= SIZE) {
            throw error("character index out of range");
        }
        return (char) ('A' + index);
    }

    /** Returns the index of character C, which must be in the alphabet. */
    @Override
    int toInt(char c) {
        if (c < 'A' || c > 'Z') {
            throw error("character not in alphabet");
        }
        return c - 'A';
    }

}

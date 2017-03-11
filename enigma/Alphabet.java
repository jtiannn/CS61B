package enigma;

import static enigma.EnigmaException.*;

/* Extra Credit Only */

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author
 */
class Alphabet {
    String _chars;

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length(); // FIXME
    }

    /** Returns true if C is in this alphabet. */
    boolean contains(char c) {
        for (int i = 0; i < size(); i++) {
            if (_chars.charAt(i) == c) {
                return true;
            }
        }
        return false; // FIXME
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {
        if (index < 0 || index >= size()) {
            throw error("character index out of range");
        }
        return _chars.charAt(index); // FIXME
    }

    /** Returns the index of character C, which must be in the alphabet. */
    int toInt(char c) {
        for (int i = 0; i < size(); i++) {
            if (_chars.charAt(i) == c) {
                return i;
            }
        }
        throw error("character not in alphabet"); // FIXME
    }

}

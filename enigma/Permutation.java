package enigma;

import static enigma.EnigmaException.*;
import java.lang.Object;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters not
     *  included in any cycle map to themselves. Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String temp = cycles.trim();
        temp = temp.replace("(", "");
        temp = temp.replace(")", "");
        _cycles = temp.split(" ");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        String[] newCycle = new String[_cycles.length+1];
        for (int i = 0; i < _cycles.length; i++) {
            newCycle[i] = _cycles[i];
        }
        newCycle[_cycles.length + 1] = cycle;
        _cycles = newCycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        char c = _alphabet.toChar(wrap(p));
        char newChar = '0';
        for (int i = 0; i < _cycles.length; i++) {
            for (int j = 0; j < _cycles[i].length(); j++) {
                if (_cycles[i].charAt(j) == c) {
                    newChar = _cycles[i].charAt((j+1) % _cycles[i].length());
                    /**if (j+1 == _cycles[i].length() - 1) {
                        newChar = _cycles[i].charAt(1);
                    }*/
                    return _alphabet.toInt(newChar);
                }
            }
        }
        return p;
    }

    /** Return the value of P modulo the input SIZE. */
    int mod(int p, int size) {
        int r = p % size;
        if (r < 0) {
            r += size;
        }
        return r;
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        char n = _alphabet.toChar(wrap(c));
        char newChar = '0';
        for (int i = 0; i < _cycles.length; i++) {
            for (int j = 0; j < _cycles[i].length(); j++) {
                if (_cycles[i].charAt(j) == n) {
                    newChar = _cycles[i].charAt(mod(j-1, _cycles[i].length()));
                    /**if (j-1 == 0) {
                        newChar = _cycles[i].charAt(_cycles[i].length() - 2);
                    }*/
                    return _alphabet.toInt(newChar);
                }
            }
        }
        return c;
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        int index = _alphabet.toInt(p);
        return _alphabet.toChar(permute(index));
    }

    /** Return the result of applying the inverse of this permutation to C. */
    int invert(char c) {
        int index = _alphabet.toInt(c);
        return _alphabet.toChar(invert(index));
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int count = 0;
        for (int i = 0; i < _cycles.length; i++) {
            count += _cycles[i].length();
        }
        if (count == _alphabet.size()) {
            return true;
        } else {
            return false;
        } // FIXME
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** An array of strings, each string representing a cycle that a letter would be converted through. */
    private String[] _cycles;
}


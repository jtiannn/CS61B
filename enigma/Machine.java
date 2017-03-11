package enigma;

import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Jacky Tian
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors.toArray();
        _rotors = new Rotor[numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors; // FIXME
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls; // FIXME
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.length; j++) {
                if ((rotors[i].toString()).equals((((Rotor)_allRotors[j]).name()))) {
                    _rotors[i] = (Rotor) _allRotors[j];
                } 
            }
        }
        if (_rotors.length != rotors.length) {
            throw new EnigmaException("Misnamed rotors");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of four
     *  upper-case letters. The first letter refers to the leftmost
     *  rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        if (setting.length() != 4) {
            throw new EnigmaException("Initial positions string wrong length");
        }
        for (int i = 1; i < 5; i++) {
            if (!_alphabet.contains(setting.charAt(i-1))) {
                throw new EnigmaException("Initial positions string not in alphabet");
            }
            _rotors[i].set(setting.charAt(i-1));
        } // FIXME
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard; // FIXME
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean rotor_4 = false;
        boolean rotor_3 = false;
        if (_rotors[4].atNotch()) {
            rotor_4 = true;
        }
        if (_rotors[3].atNotch()) {
            rotor_3 = true;
        }
        if (rotor_4 == true) {
            _rotors[3].advance();
        }
        if (rotor_3 == true) {
            _rotors[2].advance();
            _rotors[3].advance();
        }
        _rotors[_numRotors-1].advance();

        int result = _plugboard.permute(c);
        for (int i = _numRotors-1; i >= 0; i--) {
            result = _rotors[i].convertForward(result); 
        }
        for (int i = 1; i < _numRotors; i++) {
            result = _rotors[i].convertBackward(result);
        }
        result = _plugboard.permute(result);
        return result;
        // FIXME
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char converted = _alphabet.toChar(convert(_alphabet.toInt(msg.charAt(i))));
            result += converted;
        }
        return result;
        // FIXME
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Total number of rotors. */
    private final int _numRotors;

    /** Total number of pawls. */
    private final int _pawls;

    /** The array of rotors that formats the machine. */
    public Rotor[] _rotors;

    /** The initial plugboard which includes steckered pairs. */
    private Permutation _plugboard;

    /** An ArrayList containing all possible rotors that can be used. */
    private Object[] _allRotors;
}

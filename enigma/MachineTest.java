package enigma;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collection;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;


/** The suite of all JUnit tests for the Machine class.
 *  @author Jacky Tian
 */
public class MachineTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    // private Reflector reflect = new Reflector("B", new Permutation("(AE) (BN) (CK) (DQ) (FU) (GY) (HW) (IJ) (LO) (MP) (RX) (SZ) (TV)", UPPER));
    // private FixedRotor fixed = new FixedRotor("BETA", new Permutation("(ALBEVFCYODJWUGNMQTZSKPR) (HIX)", UPPER));
    // private MovingRotor moving1 = new MovingRotor("I", new Permutation("(AELTPHQXRU) (BKNW) (CMOY) (DFG) (IV) (JZ) (S)", UPPER), "Q");
    // private MovingRotor moving2 = new MovingRotor("II", new Permutation("(FIXVYOMW) (CDKLHUP) (ESZ) (BJ) (GR) (NT) (A) (Q)", UPPER), "E");
    // private MovingRotor moving3 = new MovingRotor("III", new Permutation("(ABDHPEJT) (CFLVMZOYQIRWUKXSG) (N)", UPPER), "V");
    // private MovingRotor moving4 = new MovingRotor("IV", new Permutation("(AEPLIYWCOXMRFZBSTGJQNH) (DV) (KU)", UPPER), "J");
    private ArrayList<Rotor> rotors = ALL_ROTORS;
    private Machine machine;
    private String[] insert = {"B", "BETA", "III", "IV", "I"};

    /** Set the rotor to the one with given NAME and permutation as
     *  specified by the NAME entry in ROTORS, with given NOTCHES. */
    private void setMachine(Alphabet alpha, int numrotors, int pawls, Collection<Rotor> allrotors) {
        machine = new Machine(alpha, numrotors, pawls, allrotors);
    }

    /* ***** TESTS ***** */
    @Test
    public void testInsertRotors() {
    	setMachine(UPPER, 5, 3, rotors);
    	machine.insertRotors(insert);
    	assertEquals("Wrong rotor at 0", rotors.get(0), machine._rotors[0]);
    	assertEquals("Wrong rotor at 4", rotors.get(2), machine._rotors[4]);
    }

    @Test
    public void testSetRotors() {
    	setMachine(UPPER, 5, 3, rotors);
    	machine.insertRotors(insert);
    	machine.setRotors("AXLE");
    	assertEquals("Wrong setting at 1", 0, machine._rotors[1].setting());
    	assertEquals("Wrong setting at 2", 23, machine._rotors[2].setting());
    	assertEquals("Wrong setting at 3", 11, machine._rotors[3].setting());
    	assertEquals("Wrong setting at 4", 4, machine._rotors[4].setting());
    }

    @Test 
    public void testConvert() {
    	setMachine(UPPER, 5, 3, rotors);
    	machine.insertRotors(insert);
    	machine.setRotors("AXLE");
    	machine.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", UPPER));
    	assertEquals("Wrong convert", "QVPQ", machine.convert("FROM"));
    	setMachine(UPPER, 5, 3, rotors);
    	machine.insertRotors(insert);
    	machine.setRotors("AXLE");
    	machine.setPlugboard(new Permutation("(HQ) (EX) (IP) (TR) (BY)", UPPER));
    	assertEquals("Wrong convert", "FROM", machine.convert("QVPQ"));
    }
}
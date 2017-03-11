package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.lang.String;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String next = _input.nextLine();
        while (_input.hasNext()) {
            String setting = next;
            if (!setting.contains("*")) {
                throw new EnigmaException("Wrong setting format");
            }
            setUp(enigma, setting);
            next = (_input.nextLine()).toUpperCase();
            while (next.isEmpty()) {
                next = (_input.nextLine()).toUpperCase();
            }
            while (!(next.contains("*"))) {
                String result = enigma.convert(next.replaceAll(" ", ""));
                if (next.isEmpty()) {
                    _output.println();
                } else {
                    printMessageLine(result);
                }
                if (!_input.hasNext()) {
                    next = "*";
                } else {
                    next = (_input.nextLine()).toUpperCase();
                }
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet = _config.next();
            if (alphabet.contains("(") || alphabet.contains(")") || alphabet.contains("*")) {
                throw new EnigmaException("Wrong config format");
            }
            _alphabet = new Alphabet(alphabet);
            
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong config format");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Wrong config format");
            }
            int pawls = _config.nextInt();
            temp = (_config.next()).toUpperCase();
            while (_config.hasNext()) {
                name = temp;
                notches = (_config.next()).toUpperCase();
                _allTheRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, _allTheRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            perm = "";
            temp = (_config.next()).toUpperCase();
            while (temp.contains("(") && _config.hasNext()) {
                perm = perm.concat(temp + " ");
                temp = (_config.next()).toUpperCase();
            }
            if (!_config.hasNext()) {
                perm = perm.concat(temp + " ");
            }

            if (notches.charAt(0) == 'M') {
                return new MovingRotor(name, new Permutation(perm, _alphabet), notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                return new FixedRotor(name, new Permutation(perm, _alphabet));
            } else {
                return new Reflector(name, new Permutation(perm, _alphabet));
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] set = settings.split(" ");
        if (set.length - 1 < M.numRotors()) {
            throw new EnigmaException("Not enough arguments in setting");
        }

        String[] rotors = new String[M.numRotors()];
        for (int i = 1; i < M.numRotors()+1; i++) {
            rotors[i-1] = set[i];
        }

        for (int i = 0; i < rotors.length - 1; i++) {
            for (int j = i + 1; j < rotors.length; j++) {
                if (rotors[i].equals(rotors[j])) {
                    throw new EnigmaException("Repeated Rotor");
                }
            }
        }

        String steckered = "";
        for (int i = 7; i < set.length; i++) {
            steckered = steckered.concat(set[i] + " ");
        }
        M.insertRotors(rotors);
        if (M._rotors[0].reflecting() != true) {
            throw new EnigmaException("First Rotor should be a reflector");
        }
        M.setRotors(set[M.numRotors()+1]);
        M.setPlugboard(new Permutation(steckered, _alphabet));

    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            int cap = msg.length() - i;
            if (cap <= 5) {
                _output.println(msg.substring(i, i+cap));
            } else {
                _output.print(msg.substring(i, i+5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** An ArrayList containing all rotors that can be used. */
    private ArrayList<Rotor> _allTheRotors = new ArrayList<>();

    /** A String containing cycles which readRotor() appends to. */
    private String perm;

    /** Name of current rotor. */
    private String name;

    /** Temporary string that is set to NEXT token of _config. */
    private String temp;

    /** Type and notches of current rotor. */
    private String notches;
}

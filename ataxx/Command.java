package ataxx;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static ataxx.PieceColor.*;

/** All things to do with parsing commands.
 *  @author Jacky Tian
 */
class Command {

    /** Command types.  PIECEMOVE indicates a move of the form
     *  c0r0-c1r1.  ERROR indicates a parse error in the command.
     *  All other commands are upper-case versions of what the
     *  programmer writes. */
    static enum Type {
        /* Start-up state only. */
        AUTO("(?i)auto\\s+(red|blue)"),
        BLOCK("(?i)block\\s+([a-g][1-7])"),
        MANUAL("(?i)manual\\s+(red|blue)"),
        SEED("(?i)seed\\s+([0-9]+)"),
        START,
        /* Regular moves (set-up or play) */
        PASS("pass|-"),
        PIECEMOVE("([a-g][1-7]-[a-g][1-7])"),
        /* Valid at any time. */
        LOAD("(?i)load\\s+(.+)"),
        QUIT, CLEAR, DUMP, HELP,
        /* Special "commands" internally generated. */
        /** Syntax error in command. */
        ERROR(".*"),
        /** End of input stream. */
        EOF;

        /** PATTERN is a regular expression string giving the syntax of
         *  a command of the given type.  It matches the entire command,
         *  assuming no leading or trailing whitespace.  The groups in
         *  the pattern capture the operands (if any). */
        Type(String pattern) {
            _pattern = Pattern.compile(pattern + "$");
        }

        /** A Type whose pattern is the lower-case version of its name
         *  (with no arguments). */
        Type() {
            _pattern = Pattern.compile(this.toString().toLowerCase() + "$");
        }

        /** The Pattern descrbing syntactically correct versions of this
         *  type of command. */
        private final Pattern _pattern;

    }

    /** A new Command of type TYPE with OPERANDS as its operands. */
    Command(Type type, String... operands) {
        _type = type;
        _operands = operands;
    }

    /** Return the type of this Command. */
    Type commandType() {
        return _type;
    }

    /** Returns this Command's operands. */
    String[] operands() {
        return _operands;
    }

    /** Parse COMMAND, returning the command and its operands.
     *  COMMAND is assumed to be trimmed of all leading and
     *  trailing whitespace.  */
    static Command parseCommand(String command) {
        if (command == null) {
            return new Command(Type.EOF);
        }
        command = command.trim();
        for (Type type : Type.values()) {
            Matcher mat = type._pattern.matcher(command);
            if (mat.matches()) {
                String[] operands = new String [mat.groupCount()];
                for (int i = 1; i <= operands.length; i += 1) {
                    operands[i - 1] = mat.group(i);
                }
                return new Command(type, operands);
            }
        }
        throw new Error("Internal failure: error command did not match.");
    }

    /** The command name. */
    private final Type _type;
    /** Command arguments. */
    private final String[] _operands;
}

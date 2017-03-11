package ataxx;

/* Author: P. N. Hilfinger */

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

import static ataxx.PieceColor.*;
import static ataxx.Game.State.*;
import static ataxx.Command.Type.*;
import static ataxx.GameException.error;

/** Controls the play of the game.
 *  @author Jacky Tian
 */
class Game {

    /** States of play. */
    static enum State {
        SETUP, PLAYING, FINISHED;
    }

    /** A new Game, using BOARD to play on, reading initially from
     *  BASESOURCE and using REPORTER for error and informational messages. */
    Game(Board board, CommandSource baseSource, Reporter reporter) {
        _inputs.addSource(baseSource);
        _board = board;
        _reporter = reporter;
        began = false;
    }

    /** Run a session of Ataxx gaming.  Use an AtaxxGUI iff USEGUI. */
    void process(boolean useGUI) {
        Player red, blue;

        red = blue = null;

        GameLoop:
        while (true) {
            doClear(null);

            SetupLoop:
            while (_state == SETUP) {
                doCommand();
            }

            _state = PLAYING;

            while (_state != SETUP && !_board.gameOver()) {
                Move move;
                if (_board.whoseMove() == RED) {
                    move = _red.myMove();
                } else {
                    move = _blue.myMove();
                }
                if (_state == PLAYING) {
                    if (move.isPass() && _board.canMove(_board.whoseMove())) {
                        reportError("that move is illegal.");
                    } else if (_board.legalMove(move)) {
                        _board.makeMove(move);
                    } else {
                        reportError("that move is illegal.");
                    }
                }
            }

            if (_state != SETUP) {
                reportWinner();
            }

            if (_state == PLAYING) {
                _state = FINISHED;
            }

            while (_state == FINISHED) {
                doCommand();
            }
        }

    }

    /** Return a view of my game board that should not be modified by
     *  the caller. */
    Board board() {
        return _board;
    }

    /** Perform the next command from our input source. */
    void doCommand() {
        try {
            Command cmnd =
                Command.parseCommand(_inputs.getLine("ataxx: "));
            _commands.get(cmnd.commandType()).accept(cmnd.operands());
        } catch (GameException excp) {
            _reporter.errMsg(excp.getMessage());
        }
    }

    /** Read and execute commands until encountering a move or until
     *  the game leaves playing state due to one of the commands. Return
     *  the terminating move command, or null if the game first drops out
     *  of playing mode. If appropriate to the current input source, use
     *  PROMPT to prompt for input. */
    Command getMoveCmnd(String prompt) {
        while (_state == PLAYING) {
            try {
                Command cmnd = Command.parseCommand(_inputs.getLine(prompt));
                if (cmnd.commandType() == AUTO) {
                    doAuto(cmnd.operands());
                } else if (cmnd.commandType() == MANUAL) {
                    doManual(cmnd.operands());
                } else if (cmnd.commandType() == BLOCK) {
                    doBlock(cmnd.operands());
                } else if (cmnd.commandType() == LOAD) {
                    doLoad(cmnd.operands());
                } else if (cmnd.commandType() == HELP) {
                    doHelp(cmnd.operands());
                } else if (cmnd.commandType() == QUIT) {
                    doQuit(cmnd.operands());
                } else if (cmnd.commandType() == START) {
                    doStart(cmnd.operands());
                } else if (cmnd.commandType() == CLEAR) {
                    doClear(cmnd.operands());
                } else if (cmnd.commandType() == DUMP) {
                    doDump(cmnd.operands());
                } else if (cmnd.commandType() == SEED) {
                    doSeed(cmnd.operands());
                } else if (cmnd.commandType() == ERROR) {
                    doError(cmnd.operands());
                } else if (cmnd.commandType() == AUTO) {
                    doAuto(cmnd.operands());
                } else if (cmnd.commandType() == PIECEMOVE) {
                    return cmnd;
                } else if (cmnd.commandType() == PASS) {
                    return cmnd;
                }
            } catch (GameException excp) {
                _reporter.errMsg(excp.getMessage());
            }
        }
        return null;
    }

    /** Return random integer between 0 (inclusive) and MAX>0 (exclusive). */
    int nextRandom(int max) {
        return _randoms.nextInt(max);
    }

    /** Report a move, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportMove(String format, Object... args) {
        _reporter.moveMsg(format, args);
    }

    /** Report an error, using a message formed from FORMAT and ARGS as
     *  for String.format. */
    void reportError(String format, Object... args) {
        _reporter.errMsg(format, args);
    }

    /* Command Processors */

    /** Perform the command 'auto OPERANDS[0]'. */
    void doAuto(String[] operands) {
        checkState("auto", SETUP);
        if (operands[0].equals("red")) {
            _red = new AI(this, RED);
        } else if (operands[0].equals("blue")) {
            _blue = new AI(this, BLUE);
        }
    }

    /** Perform a 'help' command. */
    void doHelp(String[] unused) {
        InputStream helpIn =
            Game.class.getClassLoader().getResourceAsStream("ataxx/help.txt");
        if (helpIn == null) {
            System.err.println("No help available.");
        } else {
            try {
                BufferedReader r
                    = new BufferedReader(new InputStreamReader(helpIn));
                while (true) {
                    String line = r.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);
                }
                r.close();
            } catch (IOException e) {
                /* Ignore IOException */
            }
        }
    }

    /** Perform the command 'load OPERANDS[0]'. */
    void doLoad(String[] operands) {
        try {
            FileReader reader = new FileReader(operands[0]);
            _inputs.addSource(new ReaderSource(reader, false));
        } catch (IOException e) {
            throw error("Cannot open file %s", operands[0]);
        }
    }

    /** Perform the command 'manual OPERANDS[0]'. */
    void doManual(String[] operands) {
        checkState("manual", SETUP);
        if (operands[0].equals("red")) {
            _red = new Manual(this, RED);
        } else if (operands[0].equals("blue")) {
            _blue = new Manual(this, BLUE);
        }
    }

    /** Exit the program. */
    void doQuit(String[] unused) {
        System.exit(0);
    }

    /** Perform the command 'start'. */
    void doStart(String[] unused) {
        checkState("start", SETUP);
        _state = PLAYING;
    }

    /** Perform the move OPERANDS[0]. */
    void doMove(String[] operands) {
        checkState("move", SETUP);
        began = true;
        String move = operands[0];
        char c0 = move.charAt(0);
        char r0 = move.charAt(1);
        char c1 = move.charAt(3);
        char r1 = move.charAt(4);
        Move make = Move.move(c0, r0, c1, r1);
        if (_board.legalMove(make)) {
            _board.makeMove(make);
        } else {
            reportError("Illegal move.");
        }
    }

    /** Cause current player to pass. */
    void doPass(String[] unused) {
        checkState("pass", SETUP);
        began = true;
        if (_board.canMove(_board.whoseMove())) {
            reportError("Player can move, so may not pass.");
        } else {
            _board.makeMove('-', '-', '-', '-');
        }
    }

    /** Perform the command 'clear'. */
    void doClear(String[] unused) {
        _board.clear();
        _state = SETUP;
        began = false;
        _red = new Manual(this, RED);
        _blue = new AI(this, BLUE);
    }

    /** Perform the command 'dump'. */
    void doDump(String[] unused) {
        _reporter.moveMsg("===");
        _reporter.moveMsg(_board.toString(false));
        _reporter.moveMsg("===");
    }

    /** Execute 'seed OPERANDS[0]' command, where the operand is a string
     *  of decimal digits. Silently substitutes another value if
     *  too large. */
    void doSeed(String[] operands) {
        checkState("block", SETUP);
    }

    /** Execute the command 'block OPERANDS[0]'. */
    void doBlock(String[] operands) {
        checkState("block", SETUP);
        if (began) {
            reportError("Can only add blocks to initial configuration.");
        } else {
            _board.setBlock(operands[0]);
        }
    }

    /** Execute the artificial 'error' command. */
    void doError(String[] unused) {
        throw error("Command not understood");
    }

    /** Report the outcome of the current game. */
    void reportWinner() {
        String msg;
        if (_board.numPieces(RED) > _board.numPieces(BLUE)) {
            msg = "Red wins.";
        } else if (_board.numPieces(RED) < _board.numPieces(BLUE)) {
            msg = "Blue wins.";
        } else {
            msg = "Draw.";
        }
        _reporter.outcomeMsg(msg);
    }

    /** Check that game is currently in one of the states STATES, assuming
     *  CMND is the command to be executed. */
    private void checkState(Command cmnd, State... states) {
        for (State s : states) {
            if (s == _state) {
                return;
            }
        }
        throw error("'%s' command is not allowed now.", cmnd.commandType());
    }

    /** Check that game is currently in one of the states STATES, using
     *  CMND in error messages as the name of the command to be executed. */
    private void checkState(String cmnd, State... states) {
        for (State s : states) {
            if (s == _state) {
                return;
            }
        }
        throw error("'%s' command is not allowed now.", cmnd);
    }

    /** Mapping of command types to methods that process them. */
    private final HashMap<Command.Type, Consumer<String[]>> _commands =
        new HashMap<>();

    {
        _commands.put(AUTO, this::doAuto);
        _commands.put(BLOCK, this::doBlock);
        _commands.put(CLEAR, this::doClear);
        _commands.put(DUMP, this::doDump);
        _commands.put(HELP, this::doHelp);
        _commands.put(MANUAL, this::doManual);
        _commands.put(PASS, this::doPass);
        _commands.put(PIECEMOVE, this::doMove);
        _commands.put(SEED, this::doSeed);
        _commands.put(START, this::doStart);
        _commands.put(LOAD, this::doLoad);
        _commands.put(QUIT, this::doQuit);
        _commands.put(ERROR, this::doError);
        _commands.put(EOF, this::doQuit);
    }

    /** Input source. */
    private final CommandSources _inputs = new CommandSources();

    /** My board. */
    private Board _board;
    /** Current game state. */
    private State _state;
    /** Used to send messages to the user. */
    private Reporter _reporter;
    /** Source of pseudo-random numbers (used by AIs). */
    private Random _randoms = new Random();
    /** Player red. */
    private Player _red;
    /** Player blue. */
    private Player _blue;
    /** True if SETUP state has began moving pieces. */
    private boolean began;
}

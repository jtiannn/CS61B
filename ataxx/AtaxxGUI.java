package ataxx;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.Observable;
import java.util.Observer;

import java.io.Writer;
import java.io.PrintWriter;

/** The GUI for the Ataxx game.
 *  @author Jacky Tian
 */
class AtaxxGUI extends TopLevel implements Observer, Reporter {

    /* The implementation strategy applied here is to make it as
     * unnecessary as possible for the rest of the program to know that it
     * is interacting with a GUI as opposed to a terminal.
     *
     * To this end, we first have made Board observable, so that the
     * GUI gets notified of changes to a Game's board and can interrogate
     * it as needed, while the Game and Board themselves need not be aware
     * that it is being watched.
     *
     * Second, instead of creating a new API by which the GUI communicates
     * with a Game, we instead simply arrange to make the GUI's input look
     * like that from a terminal, so that we can reuse all the machinery
     * in the rest of the program to interpret and execute commands.  The
     * AtaxxGUI simply composes commands (such as "start" or "clear") and
     * writes them to a Writer that (using the Java library's PipedReader
     * and PipedWriter classes) provides input to the Game using exactly the
     * same API as would be used to read from a terminal. Thus, a simple
     * Manual player can handle all commands and moves from the GUI.
     *
     * See also Main.java for how this might get set up.
     */

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 300;

    /** A new display observing MODEL, with TITLE as its window title.
     *  It uses OUTCOMMANDS to send commands to a game instance, using the
     *  same commands as the text format for Ataxx. */
    AtaxxGUI(String title, Board model, Writer outCommands) {
        super(title, true);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Options->Seed...", this::setSeed);
        _model = model;
        _widget = new AtaxxBoardWidget(model);
        _out = new PrintWriter(outCommands, true);
        add(_widget,
            new LayoutSpec("height", "1",
                           "width", "REMAINDER",
                           "ileft", 5, "itop", 5, "iright", 5,
                           "ibottom", 5));
        setMinimumSize(MIN_SIZE, MIN_SIZE);
        _widget.addObserver(this);
        _model.addObserver(this);
    }

    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        _out.printf("quit%n");
    }

    /** Execute Seed... command. */
    private synchronized void setSeed(String unused) {
        String resp =
            getTextInput("Random Seed", "Get Seed", "question", "");
        if (resp == null) {
            return;
        }
        try {
            long s = Long.parseLong(resp);
            _out.printf("seed %d%n", s);
        } catch (NumberFormatException excp) {
            return;
        }
    }

    @Override
    public void errMsg(String format, Object... args) {
    }

    @Override
    public void outcomeMsg(String format, Object... args) {
    }

    @Override
    public void moveMsg(String format, Object... args) {
    }

    @Override
    public void update(Observable obs, Object arg) {
    }

    /** Respond to a click on SQ while in "play" mode. */
    private void movePiece(String sq) {
    }

    /** Contains the drawing logic for the Ataxx model. */
    private AtaxxBoardWidget _widget;
    /** The model of the game. */
    private Board _model;
    /** Output sink for sending commands to a game. */
    private PrintWriter _out;

}

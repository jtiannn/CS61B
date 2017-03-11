package cube;

import ucb.gui2.TopLevel;
import ucb.gui2.LayoutSpec;

import java.util.Observable;
import java.util.Observer;


/** The GUI for the cube puzzle.
 *  @author P. N. Hilfinger
 */
class CubeGUI extends TopLevel implements Observer {

    /** Minimum size of board in pixels. */
    private static final int MIN_SIZE = 300;

    /** A new display observing CUBE, with TITLE as its window title. */
    CubeGUI(String title, CubeModel cube) {
        super(title, true);
        addMenuButton("Game->New", this::newGame);
        addMenuButton("Game->Quit", this::quit);
        addMenuButton("Type->Size...", this::setSide);
        addMenuButton("Type->Seed...", this::setSeed);
        _cube = cube;
        _widget = new CubeBoardWidget(cube);
        add(_widget,
            new LayoutSpec("height", "REMAINDER",
                           "width", "REMAINDER",
                           "ileft", 5, "itop", 5, "iright", 5,
                           "ibottom", 5));
        setMinimumSize(MIN_SIZE, MIN_SIZE);
        _widget.addObserver(this);
    }

    /** Execute the "Quit" button function. */
    private synchronized void quit(String unused) {
        setChanged();
        notifyObservers("Quit");
    }

    /** Execute the "New Game" button function. */
    private synchronized void newGame(String unused) {
        setChanged();
        notifyObservers("New");
    }

    /** Execute Size... command. */
    private synchronized void setSide(String unused) {
        String resp =
            getTextInput("Squares per side", "Get Board Size", "question",
                         String.format("%d", _cube.side()));
        if (resp == null) {
            return;
        }
        try {
            int s = Integer.parseInt(resp);
            if (s >= 3) {
                _param = (Integer) s;
                setChanged();
                notifyObservers("Size...");
                display(true);
            }
        } catch (NumberFormatException excp) {
            return;
        }
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
            _param = (Long) s;
            setChanged();
            notifyObservers("Seed...");
        } catch (NumberFormatException excp) {
            return;
        }
    }

    /** Display informational message MSG.  ARGS give arguments as for
     *  printf. TITLE is the title of the message box. */
    void message(String title, String msg, Object... args) {
        showMessage(String.format(msg, args), title, "information");
    }

    /** Return mouse's row at last click (may be out of range if mouse off
     *  the board). */
    int mouseRow() {
        return _widget.mouseRow();
    }

    /** Return mouse's column at last click (may be out of range if mouse off
     *  the board). */
    int mouseCol() {
        return _widget.mouseCol();
    }

    /** Return the single parameter provided to menu command (such as
     *  'Seed'. */
    Object param() {
        return _param;
    }

    @Override
    public void update(Observable obs, Object unused) {
        setChanged();
        notifyObservers("click");
    }

    /** Contains the drawing logic for the cube model. */
    private CubeBoardWidget _widget;
    /** The model of the puzzle. */
    private CubeModel _cube;
    /** Parameter provided to user command. */
    private Object _param;

}

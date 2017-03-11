package ataxx;

import java.io.InputStreamReader;

/** The main program for Ataxx.
 *  @author Jacky Tian
 */
public class Main {

    /** Run Ataxx game.  Use display if ARGS[k] is '--display'. */
    public static void main(String[] args) {
        boolean useGUI;
        useGUI = false;
        for (int i = 0; i < args.length; i += 1) {
            switch (args[i]) {
            case "--display":
                useGUI = true;
                break;
            default:
                usage();
                break;
            }
        }

        Game game;
        Board board = new Board();

        game = null;
        if (useGUI) {
            System.err.printf("GUI not implemented.%n");
            System.exit(1);  System.exit(1);
        } else {
            game = new Game(board,
                            new ReaderSource(new InputStreamReader(System.in),
                                             true),
                            new TextReporter());
        }
        game.process(false);
    }

    /** Give usage message and exit. */
    static void usage() {
        System.err.println("Usage: java ataxx.Main [--display] [--timing]"
                           + " [--strict]");
        System.exit(1);
    }

    /** Size of the buffer for reading commands from a GUI (bytes). */
    private static final int BUFFER_LEN = 128;

}

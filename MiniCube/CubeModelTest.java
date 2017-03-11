package cube;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the CubeModel class.
 *  @author P. N. Hilfinger
 */
public class CubeModelTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = new Timeout(5000);

    /* ***** TESTING UTILITIES ***** */

    /** Return a setup containing a cube painted as in FACES on a board
     *  painted as in ROWS.  FACES is a string of asterisks and blanks
     *  of length <= 6.  The corresponding face (numbered as in
     *  CubeModel.isPaintedFace) to each position in FACES that
     *  contains an asterisk is painted.  ROWS contains one string for
     *  each row of the board, with ROWS[0] corresponding to row 0
     *  (the front).  Column j of row i is painted iff character j
     *  of ROWS[i] is an asterisk or capital 'C'.  Furthermore,
     *  if character c of ROWS[i] is 'c' or 'C', the cube is positioned
     *  at that row and column. */
    private CubeModel make(String faces, String... rows) {
        int side = rows.length;
        boolean[][] b = new boolean[side][side];
        boolean[] f = new boolean[6];
        int row0, col0;
        row0 = col0 = -1;
        for (int i = 0; i < side; i += 1) {
            for (int j = 0; j < side; j += 1) {
                if (j < rows[i].length()) {
                    char ch = rows[i].charAt(j);
                    switch (ch) {
                    case '*':
                        b[i][j] = true;
                        break;
                    case 'C': case 'c':
                        row0 = i;
                        col0 = j;
                        b[i][j] = ch == 'C';
                        break;
                    default:
                        break;
                    }
                }
            }
        }
        for (int i = 0; i < 6; i += 1) {
            f[i] = i < faces.length() && faces.charAt(i) == '*';
        }
        CubeModel result = new CubeModel();
        result.initialize(side, row0, col0, b, f);
        return result;
    }

    /** Returns make(DATA[0], DATA[1], DATA[2], ...). */
    private CubeModel make(String[] data) {
        return make(data[0], Arrays.copyOfRange(data, 1, data.length));
    }

    /** Check that cube is as specified by FACES and ROWS (whose meaning
     *  is as for make(FACES, ROWS). ID identifies any errors
     *  in error messages.  If there are discrepencies, throws
     *  AssertionError.
     */
    private void checkCube(String id, String faces, String... rows) {
        int side = rows.length;
        assertEquals(id + ": side()", side, cube.side());
        for (int r = 0; r < side; r += 1) {
            for (int c = 0; c < side; c += 1) {
                char ch = rows[r].charAt(c);
                switch (ch) {
                case '*':
                    assertTrue(String.format("%s: wrong color at (%d, %d)",
                                             id, r, c),
                               cube.isPaintedSquare(r, c));
                    break;
                case 'C': case 'c':
                    assertEquals(String.format("%s: wrong color at (%d, %d)",
                                               id, r, c),
                                 ch == 'C', cube.isPaintedSquare(r, c));
                    assertEquals(id + ": Wrong cube row", r, cube.cubeRow());
                    assertEquals(id + ": Wrong cube column",
                                 c, cube.cubeCol());
                    break;
                default:
                    assertFalse(String.format("%s: wrong color at (%d, %d)",
                                              id, r, c),
                                cube.isPaintedSquare(r, c));

                }
            }
        }
        for (int f = 0; f < 6; f += 1) {
            assertEquals(String.format("%s: isPaintedFace(%d)", id, f),
                         f < faces.length() && faces.charAt(f) == '*',
                         cube.isPaintedFace(f));
        }
    }

    /** Same as checkCube(ID, DATA[0], DATA[1], ...). */
    private void checkCube(String id, String[] data) {
        checkCube(id, data[0], Arrays.copyOfRange(data, 1, data.length));
    }

    /** Check that we correctly move to (ROW, COL) in the test cube. */
    private void checkMove(int row, int col) {
        int move0 = cube.moves();
        cube.move(row, col);
        assertTrue(String.format("wrong move -> %d, %d", row, col),
                   cube.cubeRow() == row && cube.cubeCol() == col);
        assertEquals("move count not incremented", move0 + 1, cube.moves());
    }

    /** Check that an invalid move to (ROW, COL) throws right
     *  exception and does not change state. */
    private void checkBadMove(int row, int col) {
        int move0 = cube.moves();
        int row0 = cube.cubeRow();
        int col0 = cube.cubeCol();

        try {
            cube.move(row, col);
            assertFalse("Missing exception", true);
        } catch (IllegalArgumentException excp) {
            assertTrue("cube moved on illegal move",
                       row0 == cube.cubeRow() && col0 == cube.cubeCol());
            assertTrue("moves changed on illegal move",
                       move0 == cube.moves());
        }
    }

    /* ***** TESTS ***** */

    /** Check that a blank cube is correctly represented. */
    @Test
    public void checkCons0() {
        cube = new CubeModel();
        checkCube("new CubeModel()", BLANK_CUBE);
    }


    /** Check that a blank cube created by .initialize is correct. */
    @Test
    public void checkinit4() {
        cube = make(BLANK_CUBE);
        checkCube("initialize(,,,)", BLANK_CUBE);
    }

    /** Check initialization by copying. Make sure structure is not
     *  shared. */
    @Test
    public void checkCopyCons() {
        CubeModel cube0 = make("**...*",
                               "....", ".c..", "***.", "....");
        cube0.move(1, 2);
        cube0.move(1, 1);
        cube = new CubeModel(cube0);
        checkCube("1", "**...*",
                  "....", ".c..", "***.", "....");
        assertEquals("# of moves not copied", 2, cube.moves());
        cube0.move(2, 1);
        checkCube("shared structure", "**...*",
                  "....", ".c..", "***.", "....");
    }

    /** Check that a sequence of proper moves of the cube on an empty board
     *  goes to the right squares. */
    @Test
    public void checkGoodMoves() {
        cube = new CubeModel();
        checkMove(0, 1);
        checkMove(0, 2);
        checkMove(0, 3);
        checkMove(0, 2);
        checkMove(1, 2);
        checkMove(2, 2);
        checkMove(1, 2);
    }

    /** Check that a moves off the square get the proper exception. */
    @Test
    public void checkOffSquare() {
        cube = new CubeModel();
        checkBadMove(0, -1);
        checkBadMove(-1, 0);
        checkBadMove(-1, -1);
        cube.move(0, 1);
        cube.move(0, 2);
        cube.move(0, 3);
        checkBadMove(0, 4);
    }

    /** Check that moves to non-adjacent square get the proper exception. */
    @Test
    public void checkNonAdjacent() {
        cube = new CubeModel();
        checkBadMove(0, 2);
        checkBadMove(2, 0);
        checkBadMove(1, 1);
    }

    /** Check that painted face changes correctly as cube moves. */
    @Test
    public void checkFaceMoves() {
        cube = make(TEST_CUBE2);
        cube.move(2, 1);
        checkCube("1", "...*..",
                  "....", "....", ".c..", "....");
        cube.move(3, 1);
        checkCube("2", "...*..",
                  "....", "....", "....", ".c..");
        cube.move(2, 1);
        cube.move(1, 1);
        checkCube("3", "...*..",
                  "....", ".c..", "....", "....");
        cube.move(0, 1);
        checkCube("4", "...*..",
                  ".c..", "....", "....", "....");
        cube.move(0, 0);
        checkCube("5", ".....*",
                  "c...", "....", "....", "....");
        cube.move(1, 0);
        checkCube("6", ".*....",
                  "....", "c...", "....", "....");
        cube.move(1, 1);
        checkCube("7", ".*....",
                  "....", ".c..", "....", "....");

        cube = make(TEST_CUBE2);
        cube.move(3, 2);
        checkCube("8", "*.....",
                  "....", "....", "....", "..c.");

        cube = make(TEST_CUBE2);
        cube.move(2, 3);
        checkCube("9", "..*....",
                  "....", "....", "...c", "....");
        cube.move(2, 2);
        checkCube("10", "",
                  "....", "....", "..C.", "....");
        cube.move(2, 1);
        cube.move(2, 2);
        checkCube("11", TEST_CUBE2);
    }

    /** Check that allFacedPainted works. */
    @Test
    public void checkAllFacesPainted() {
        cube = make("", ".**c", ".*..", ".*..", "**..");
        assertFalse("none painted", cube.allFacesPainted());
        cube.move(0, 2);
        assertFalse("only one painted", cube.allFacesPainted());
        cube.move(0, 1);
        assertFalse("only two painted", cube.allFacesPainted());
        cube.move(1, 1);
        assertFalse("only three painted", cube.allFacesPainted());
        cube.move(2, 1);
        assertFalse("only four painted", cube.allFacesPainted());
        cube.move(3, 1);
        assertFalse("only five painted", cube.allFacesPainted());
        cube.move(3, 0);
        assertTrue("all should be painted", cube.allFacesPainted());
    }

    /** Check that a blank face exchanges paint with a painted square and
     *  vice-versa. */
    @Test
    public void checkBlankFacePainted() {
        cube = make(TEST_CUBE1);
        cube.move(2, 1);
        checkCube("1", "....*.",
                  "**..", ".*..", ".c.*", ".*..");
        cube.move(2, 2);
        checkCube("2", "..*...",
                  "**..", ".*..", "..c*", ".*..");
        cube.move(2, 1);
        checkCube("3", "",
                  "**..", ".*..", ".C.*", ".*..");
        cube.move(2, 2);
        checkCube("4", TEST_CUBE1);
    }


    /** Holds test cube in tests. */
    private CubeModel cube;

    /** An empty cube puzzle as created by new CubeModel(). */
    private static final String[] BLANK_CUBE =
    { "",
      "c...", "....", "....", "...."
    };

    /** A test cube where the cube is initially blank. */
    private static final String[] TEST_CUBE1 =  {
        "",
        "**..", ".*..", ".*c*", ".*.."
    };

    /** A test cube where the cube is initially blank. */
    private static final String[] TEST_CUBE2 =  {
        "....*.",
        "....", "....", "..c.", "...."
    };

}

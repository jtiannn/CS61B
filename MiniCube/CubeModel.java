package cube;

import java.util.Observable;

import static java.lang.System.arraycopy;

/** Models an instance of the Cube puzzle: a cube with color on some sides
 *  sitting on a cell of a square grid, some of whose cells are colored.
 *  Any object may register to observe this model, using the (inherited)
 *  addObserver method.  The model notifies observers whenever it is modified.
 *  @author P. N. Hilfinger
 */
class CubeModel extends Observable {

    boolean[][] board;
    boolean[] game_cube;
    int cube_row;
    int cube_col;
    int sides;
    int moves_count;

    /** A blank cube puzzle of size 4. */
    CubeModel() {
        initialize(4, this.cube_row, this.cube_col, new boolean[4][4]);
    }

    /** A copy of CUBE. */
    CubeModel(CubeModel cube) {
        initialize(cube);
    }

    /** Initialize puzzle of size SIDExSIDE with the cube initially at
     *  ROW0 and COL0, with square r, c painted iff PAINTED[r][c], and
     *  with face k painted iff FACEPAINTED[k] (see isPaintedFace).
     *  Assumes that
     *    * SIDE > 2.
     *    * PAINTED is SIDExSIDE.
     *    * 0 <= ROW0, COL0 < SIDE.
     *    * FACEPAINTED has length 6.
     */
    void initialize(int side, int row0, int col0, boolean[][] painted,
                    boolean[] facePainted) {
        this.sides = side;
        this.board = painted;
        this.cube_row = row0;
        this.cube_col = col0;
        this.game_cube = facePainted;
        this.moves_count = 0;
        setChanged();
        notifyObservers();
    }

    /** Initialize puzzle of size SIDExSIDE with the cube initially at
     *  ROW0 and COL0, with square r, c painted iff PAINTED[r][c].
     *  The cube is initially blank.
     *  Assumes that
     *    * SIDE > 2.
     *    * PAINTED is SIDExSIDE.
     *    * 0 <= ROW0, COL0 < SIDE.
     */
    void initialize(int side, int row0, int col0, boolean[][] painted) {
        initialize(side, row0, col0, painted, new boolean[6]);
    }

    /** Initialize puzzle to be a copy of CUBE. */
    void initialize(CubeModel cube) {
        boolean[] copy = new boolean[6];
        for (int i = 0; i < 6; i++) {
            copy[i] = cube.game_cube[i];
        }
        this.sides = cube.sides;
        this.board = cube.board;
        this.cube_row = cube.cube_row;
        this.cube_col = cube.cube_col;
        this.game_cube = copy;
        this.moves_count = cube.moves_count;
        setChanged();
        notifyObservers();
    }

    /** Return absolute value of NUMBER. */
    int abs(int number) {
        if (number < 0) {
            return -number;
        }
        return number;
    }

    /** Move the cube to (ROW, COL), if that position is on the board and
     *  vertically or horizontally adjacent to the current cube position.
     *  Transfers colors as specified by the rules.
     *  Throws IllegalArgumentException if preconditions are not met.
     */
    void move(int row, int col) {
        if (row < 0 || row > this.sides - 1 || col < 0 || col > this.sides - 1) {
            throw new IllegalArgumentException("Cube movement out of range.");
        } else if ((row == this.cube_row && abs(col - this.cube_col) == 1) || (col == this.cube_col && abs(row - this.cube_row) == 1)) {
            boolean temp0 = this.game_cube[0];
            boolean temp1 = this.game_cube[1];
            boolean temp2 = this.game_cube[2];
            boolean temp3 = this.game_cube[3];
            boolean temp4 = this.game_cube[4];
            boolean temp5 = this.game_cube[5];
            if (row - this.cube_row == 1) {
                this.game_cube[5] = temp0;
                this.game_cube[4] = temp1;
                this.game_cube[1] = temp5;
                this.game_cube[0] = temp4;
                this.cube_row = row;
            } else if (row - this.cube_row == -1) {
                this.game_cube[5] = temp1;
                this.game_cube[4] = temp0;
                this.game_cube[1] = temp4;
                this.game_cube[0] = temp5;
                this.cube_row = row;
            } else if (col - this.cube_col == 1) {
                this.game_cube[5] = temp2;
                this.game_cube[4] = temp3;
                this.game_cube[3] = temp5;
                this.game_cube[2] = temp4;
                this.cube_col = col;
            } else if (col - this.cube_col == -1) {
                this.game_cube[5] = temp3;
                this.game_cube[4] = temp2;
                this.game_cube[3] = temp4;
                this.game_cube[2] = temp5;
                this.cube_col = col;
            }

            if (this.game_cube[4] == true && this.board[row][col] != true) {
                this.board[row][col] = true;
                this.game_cube[4] = false;
            } else if (this.game_cube[4] != true && this.board[row][col] == true) {
                this.board[row][col] = false;
                this.game_cube[4] = true;
            }

            this.moves_count += 1;

        } else {
            throw new IllegalArgumentException("Preconditions not met");
        }
        setChanged();
        notifyObservers();
    }

    /** Return the number of squares on a side. */
    int side() {
        return this.sides; // FIXME
    }

    /** Return true iff square ROW, COL is painted.
     *  Requires 0 <= ROW, COL < board size. */
    boolean isPaintedSquare(int row, int col) {
        return this.board[row][col]; // FIXME
    }

    /** Return current row of cube. */
    int cubeRow() {
        return this.cube_row; // FIXME
    }

    /** Return current column of cube. */
    int cubeCol() {
        return this.cube_col; // FIXME
    }

    /** Return the number of moves made on current puzzle. */
    int moves() {
        return this.moves_count; // FIXME
    }

    /** Return true iff face #FACE, 0 <= FACE < 6, of the cube is painted.
     *  Faces are numbered as follows:
     *    0: Vertical in the direction of row 0 (nearest row to player).
     *    1: Vertical in the direction of last row.
     *    2: Vertical in the direction of column 0 (left column).
     *    3: Vertical in the direction of last column.
     *    4: Bottom face.
     *    5: Top face.
     */
    boolean isPaintedFace(int face) {
        return this.game_cube[face]; // FIXME
    }

    /** Return true iff all faces are painted. */
    boolean allFacesPainted() {
        for (int i = 0; i < 6; i++) {
            if (this.game_cube[i] != true) {
                return false;
            }
        }
        return true;
    }

    // ADDITIONAL FIELDS AND PRIVATE METHODS HERE, AS NEEDED.

}

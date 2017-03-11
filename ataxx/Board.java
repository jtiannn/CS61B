package ataxx;

/* Author: P. N. Hilfinger, (C) 2008. */

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Observable;

import static ataxx.PieceColor.*;
import static ataxx.GameException.error;

/** An Ataxx board.   The squares are labeled by column (a char value between
 *  'a' - 2 and 'g' + 2) and row (a char value between '1' - 2 and '7'
 *  + 2) or by linearized index, an integer described below.  Values of
 *  the column outside 'a' and 'g' and of the row outside '1' to '7' denote
 *  two layers of border squares, which are always blocked.
 *  This artificial border (which is never actually printed) is a common
 *  trick that allows one to avoid testing for edge conditions.
 *  For example, to look at all the possible moves from a square, sq,
 *  on the normal board (i.e., not in the border region), one can simply
 *  look at all squares within two rows and columns of sq without worrying
 *  about going off the board. Since squares in the border region are
 *  blocked, the normal logic that prevents moving to a blocked square
 *  will apply.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Jacky Tian
 */
class Board extends Observable {

    /** Number of squares on a side of the board. */
    static final int SIDE = 7;
    /** Length of a side + an artificial 2-deep border region. */
    static final int EXTENDED_SIDE = SIDE + 4;

    /** Number of non-extending moves before game ends. */
    static final int JUMP_LIMIT = 25;

    /** Number of set blocks on the board. */
    static final int SETBLOCKS = 72;

    /** A new, cleared board at the start of the game. */
    Board() {
        _board = new PieceColor[EXTENDED_SIDE * EXTENDED_SIDE];
        clear();
    }

    /** A copy of B. */
    @SuppressWarnings("unchecked")
    Board(Board b) {
        _board = b._board.clone();
        _whoseMove = b._whoseMove;
        jumpCounter = b.jumpCounter;
        _moves = b._moves;
        _allMoves = (ArrayList<Move>) b._allMoves.clone();
        _redPieces = b._redPieces;
        _bluePieces = b._bluePieces;
        _undo = (Stack) b._undo.clone();
    }

    /** Return the linearized index of square COL ROW. */
    static int index(char col, char row) {
        return (row - '1' + 2) * EXTENDED_SIDE + (col - 'a' + 2);
    }

    /** Return the linearized index of the square that is DC columns and DR
     *  rows away from the square with index SQ. */
    static int neighbor(int sq, int dc, int dr) {
        return sq + dc + dr * EXTENDED_SIDE;
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions and no blocks. */
    void clear() {
        _whoseMove = RED;
        _moves = 0;
        _allMoves = new ArrayList<Move>();
        _undo = new Stack<ArrayList<Integer>>();
        jumpCounter = 0;
        _redPieces = 2;
        _bluePieces = 2;
        for (int i = 0; i < _board.length; i += 1) {
            if (i <= EXTENDED_SIDE * 2 - 1
                || i >= EXTENDED_SIDE * 9) {
                _board[i] = BLOCKED;
            } else if (i % 11 == 0 || i % 11 == 1
                || i % 11 == 9 || i % 11 == 10) {
                _board[i] = BLOCKED;
            } else if (i == EXTENDED_SIDE * 2 + 8
                || i == EXTENDED_SIDE * 8 + 2) {
                _board[i] = RED;
            } else if (i == EXTENDED_SIDE * 8 + 8
                || i == EXTENDED_SIDE * 2 + 2) {
                _board[i] = BLUE;
            } else {
                _board[i] = EMPTY;
            }
        }

        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if neither side has
     *  any moves, if one side has no pieces, or if there have been
     *  MAX_JUMPS consecutive jumps without intervening extends. */
    boolean gameOver() {
        if (jumpCounter >= JUMP_LIMIT) {
            return true;
        } else if (_redPieces == 0 || _bluePieces == 0) {
            return true;
        } else if (!canMove(RED) && !canMove(BLUE)) {
            return true;
        }
        return false;
    }

    /** Return number of red pieces on the board. */
    int redPieces() {
        int count = 0;
        for (PieceColor piece : _board) {
            if (piece == RED) {
                count += 1;
            }
        }
        return count;
    }

    /** Return number of blue pieces on the board. */
    int bluePieces() {
        int count = 0;
        for (PieceColor piece : _board) {
            if (piece == BLUE) {
                count += 1;
            }
        }
        return count;
    }

    /** Return number of COLOR pieces on the board. */
    int numPieces(PieceColor color) {
        if (color == RED) {
            return redPieces();
        } else if (color == BLUE) {
            return bluePieces();
        } else if (color == BLOCKED) {
            int count = 0;
            for (PieceColor piece : _board) {
                if (piece == BLOCKED) {
                    count += 1;
                }
            }
            return count - SETBLOCKS;
        } else if (color == EMPTY) {
            int count = 0;
            for (PieceColor piece : _board) {
                if (piece == EMPTY) {
                    count += 1;
                }
            }
            return count;
        }
        return -1;
    }

    /** Increment numPieces(COLOR) by K. */
    private void incrPieces(PieceColor color, int k) {
        if (color == RED) {
            _redPieces += k;
        } else if (color == BLUE) {
            _bluePieces += k;
        }
    }

    /** The current contents of square CR, where 'a'-2 <= C <= 'g'+2, and
     *  '1'-2 <= R <= '7'+2.  Squares outside the range a1-g7 are all
     *  BLOCKED.  Returns the same value as get(index(C, R)). */
    PieceColor get(char c, char r) {
        return _board[index(c, r)];
    }

    /** Return the current contents of square with linearized index SQ. */
    PieceColor get(int sq) {
        return _board[sq];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'g', and
     *  '1' <= R <= '7'. */
    private void set(char c, char r, PieceColor v) {
        set(index(c, r), v);
    }

    /** Set square with linearized index SQ to V.  This operation is
     *  undoable. */
    private void set(int sq, PieceColor v) {
        if (v == RED) {
            incrPieces(RED, 1);
        } else if (v == BLUE) {
            incrPieces(BLUE, 1);
        }
        _board[sq] = v;

    }

    /** Set square at C R to V (not undoable). */
    private void unrecordedSet(char c, char r, PieceColor v) {
        _board[index(c, r)] = v;
    }

    /** Set square at linearized index SQ to V (not undoable). */
    private void unrecordedSet(int sq, PieceColor v) {
        _board[sq] = v;
    }

    /** Return true iff MOVE is legal on the current board. */
    boolean legalMove(Move move) {
        if (move == null) {
            return false;
        } else if (move.isPass()) {
            return !canMove(_whoseMove);
        } else {
            if (_board[move.fromIndex()] != _whoseMove) {
                return false;
            } else {
                if (move.isJump() || move.isExtend()) {
                    if (_board[move.toIndex()] == EMPTY) {
                        return true;
                    }
                }
                return false;
            }
        }
    }

    /** Return true iff player WHO can move, ignoring whether it is
     *  that player's move and whether the game is over. */
    boolean canMove(PieceColor who) {
        for (int i = 0; i < _board.length; i += 1) {
            if (_board[i] == who) {
                for (int j = i - 2; j <= i + 2; j += 1) {
                    for (int k = j - EXTENDED_SIDE * 2;
                        k <= j + EXTENDED_SIDE * 2; k += 11) {
                        if (_board[k] == EMPTY) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Return total number of moves and passes since the last
     *  clear or the creation of the board. */
    int numMoves() {
        return _moves;
    }

    /** Return number of non-pass moves made in the current game since the
     *  last extend move added a piece to the board (or since the
     *  start of the game). Used to detect end-of-game. */
    int numJumps() {
        return jumpCounter;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        if (c0 == '-') {
            makeMove(Move.pass());
        } else {
            makeMove(Move.move(c0, r0, c1, r1));
        }
    }

    /** Make the MOVE on this Board, assuming it is legal. */
    void makeMove(Move move) {
        assert legalMove(move);
        if (move.isPass()) {
            pass();
            return;
        }

        ArrayList<Integer> flips = new ArrayList<Integer>();
        _board[move.toIndex()] = _whoseMove;
        for (int i = move.toIndex() - 12; i <= move.toIndex() - 10; i += 1) {
            for (int j = 0; j <= EXTENDED_SIDE * 2; j += 11) {
                if (_board[i + j] == _whoseMove.opposite()) {
                    _board[i + j] = _whoseMove;
                    flips.add(i + j);
                    incrPieces(_whoseMove, 1);
                    incrPieces(_whoseMove.opposite(), -1);
                }
            }
        }
        _moves += 1;
        if (move.isJump()) {
            jumpCounter += 1;
            _board[move.fromIndex()] = EMPTY;
        } else {
            jumpCounter = 0;
            incrPieces(_whoseMove, 1);
        }
        _allMoves.add(move);
        _undo.push(flips);
        PieceColor opponent = _whoseMove.opposite();
        _whoseMove = opponent;
        setChanged();
        notifyObservers();
    }

    /** Update to indicate that the current player passes, assuming it
     *  is legal to do so.  The only effect is to change whoseMove(). */
    void pass() {
        assert !canMove(_whoseMove);
        _whoseMove = _whoseMove.opposite();
        _moves += 1;
        _allMoves.add(Move.pass());
        setChanged();
        notifyObservers();
    }

    /** Undo the last move. */
    @SuppressWarnings("unchecked")
    void undo() {
        if (_undo.empty()) {
            return;
        }
        Move last = _allMoves.get(_allMoves.size() - 1);
        if (last.isPass()) {
            _whoseMove = _whoseMove.opposite();
            _moves -= 1;
            _allMoves.remove(_allMoves.size() - 1);
        } else {
            ArrayList<Integer> recent = _undo.pop();
            for (int i = 0; i < recent.size(); i += 1) {
                _board[recent.get(i)] = _board[recent.get(i)].opposite();
                incrPieces(_board[recent.get(i)].opposite(), -1);
                incrPieces(_board[recent.get(i)], 1);
            }
            if (last.isJump()) {
                _board[last.fromIndex()] = _whoseMove.opposite();
                jumpCounter -= 1;
            } else {
                incrPieces(_whoseMove, -1);
            }
            _board[last.toIndex()] = EMPTY;
            _whoseMove = _whoseMove.opposite();
            _moves -= 1;
            _allMoves.remove(_allMoves.size() - 1);
        }
        setChanged();
        notifyObservers();
    }

    /** Return true iff it is legal to place a block at C R. */
    boolean legalBlock(char c, char r) {
        if (_board[index(c, r)] == EMPTY) {
            return true;
        }
        return false;
    }

    /** Return true iff it is legal to place a block at CR. */
    boolean legalBlock(String cr) {
        return legalBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Set a block on the square C R and its reflections across the middle
     *  row and/or column, if that square is unoccupied and not
     *  in one of the corners. Has no effect if any of the squares is
     *  already occupied by a block.  It is an error to place a block on a
     *  piece. */
    void setBlock(char c, char r) {
        if (_board[index(c, r)] != BLOCKED) {
            int cInt = 'a' + 'g' - c;
            char cMirror = (char) cInt;
            int rInt = '1' + '7' - r;
            char rMirror = (char) rInt;
            if (!legalBlock(c, r) || !legalBlock(cMirror, r)
                || !legalBlock(c, rMirror) || !legalBlock(cMirror, rMirror)) {
                throw error("illegal block placement");
            } else {
                _board[index(c, r)] = BLOCKED;
                _board[index(cMirror, r)] = BLOCKED;
                _board[index(c, rMirror)] = BLOCKED;
                _board[index(cMirror, rMirror)] = BLOCKED;
            }
            setChanged();
            notifyObservers();
        }
    }

    /** Place a block at CR. */
    void setBlock(String cr) {
        setBlock(cr.charAt(0), cr.charAt(1));
    }

    /** Return a list of all moves made since the last clear (or start of
     *  game). */
    List<Move> allMoves() {
        return _allMoves;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    /* .equals used only for testing purposes. */
    @Override
    public boolean equals(Object obj) {
        Board other = (Board) obj;
        return Arrays.equals(_board, other._board);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(_board);
    }

    /** Return a text depiction of the board (not a dump).  If LEGEND,
     *  supply row and column numbers around the edges. */
    String toString(boolean legend) {
        String board = "";
        for (int i = EXTENDED_SIDE * 8 + 2;
            i >= EXTENDED_SIDE * 2 + 2; i -= 11) {
            for (int j = i; j <= i + 6; j += 1) {
                char piece;
                if (_board[j] == RED) {
                    piece = 'r';
                } else if (_board[j] == BLUE) {
                    piece = 'b';
                } else if (_board[j] == BLOCKED) {
                    piece = 'X';
                } else {
                    piece = '-';
                }

                if (j % 11 == 2) {
                    board = board + "  " + piece + ' ';
                } else if (j % 11 != 8) {
                    board = board + piece + ' ';
                } else if (j == EXTENDED_SIDE * 2 + 8) {
                    board = board + piece;
                } else {
                    board = board + piece + '\n';
                }
            }
        }
        return board;
    }

    /** For reasons of efficiency in copying the board,
     *  we use a 1D array to represent it, using the usual access
     *  algorithm: row r, column c => index(r, c).
     *
     *  Next, instead of using a 7x7 board, we use an 11x11 board in
     *  which the outer two rows and columns are blocks, and
     *  row 2, column 2 actually represents row 0, column 0
     *  of the real board.  As a result of this trick, there is no
     *  need to special-case being near the edge: we don't move
     *  off the edge because it looks blocked.
     *
     *  Using characters as indices, it follows that if 'a' <= c <= 'g'
     *  and '1' <= r <= '7', then row c, column r of the board corresponds
     *  to board[(c -'a' + 2) + 11 (r - '1' + 2) ], or by a little
     *  re-grouping of terms, board[c + 11 * r + SQUARE_CORRECTION]. */
    private final PieceColor[] _board;

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Counts the number of consecutive jump moves. */
    private int jumpCounter;

    /** Counts the number of moves since the last clear. */
    private int _moves;

    /** A list of all moves that have been made. */
    private ArrayList<Move> _allMoves;

    /** Number of red pieces on the board. */
    private int _redPieces;

    /** Number of blue pieces on the board. */
    private int _bluePieces;

    /** Stack to keep track of undos. */
    private Stack<ArrayList<Integer>> _undo;


}

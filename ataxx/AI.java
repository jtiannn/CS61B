package ataxx;

import static ataxx.PieceColor.*;
import static ataxx.Board.*;

import java.util.ArrayList;

/** A Player that computes its own moves.
 *  @author Jacky Tian
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;
    /** A string storing the Player's color. */
    private String _color;

    /** A new AI for GAME that will play MYCOLOR. */
    public AI(Game game, PieceColor myColor) {
        super(game, myColor);
        if (myColor == RED) {
            _color = "Red";
        } else  {
            _color = "Blue";
        }
    }

    @Override
    Move myMove() {
        if (!board().canMove(myColor())) {
            game().reportMove(_color + " passes.");
            return Move.pass();
        }
        Move move = findMove();
        game().reportMove(_color + " moves " + move.toString() + ".");
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == RED) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** Used to communicate best moves found by findMove, when asked for. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value >= BETA if SENSE==1,
     *  and minimal value or value <= ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels before using a static estimate. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0) {
            return staticScore(board);
        } else {
            ArrayList<Move> moves = new ArrayList<Move>();
            for (int a = 0; a < EXTENDED_SIDE * EXTENDED_SIDE; a += 1) {
                if (board.get(a) == board.whoseMove()) {
                    for (int i = -2; i <= 2; i += 1) {
                        for (int j = -2; j <= 2; j += 1) {
                            int toIndex = board.neighbor(a, i, j);
                            if (board.get(toIndex) == EMPTY) {
                                char c0 = (char) ((a - (EXTENDED_SIDE * 2 + 2))
                                    % EXTENDED_SIDE + 'a');
                                char r0 = (char) ((a - (EXTENDED_SIDE * 2 + 2))
                                    / EXTENDED_SIDE + '1');
                                char c1 = (char)
                                    ((toIndex - (EXTENDED_SIDE * 2 + 2))
                                    % EXTENDED_SIDE + 'a');
                                char r1 = (char)
                                    ((toIndex - (EXTENDED_SIDE * 2 + 2))
                                    / EXTENDED_SIDE + '1');
                                moves.add(Move.move(c0, r0, c1, r1));
                            }
                        }
                    }
                }
            }
            if (board.legalMove(Move.pass())) {
                moves.add(Move.pass());
            }
            int alphascore = alpha;
            int betascore = beta;
            for (int i = 0; i < moves.size(); i += 1) {
                if (sense == 1) {
                    board.makeMove(moves.get(i));
                    int best = findMove(board, depth - 1,
                        false, -1, alphascore, betascore);
                    if (best > alphascore) {
                        alphascore = best;
                        if (saveMove) {
                            _lastFoundMove = moves.get(i);
                        }
                    }
                    board.undo();
                } else {
                    board.makeMove(moves.get(i));
                    int best = findMove(board, depth - 1,
                        false, 1, alphascore, betascore);
                    if (best < betascore) {
                        betascore = best;
                        if (saveMove) {
                            _lastFoundMove = moves.get(i);
                        }
                    }
                    board.undo();
                }
            }
        }
        return staticScore(board);
    }

    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        return board.redPieces() - board.bluePieces();
    }
}

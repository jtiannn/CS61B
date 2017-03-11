package ataxx;

import org.junit.Test;
import static org.junit.Assert.*;

/** Tests of the Board class.
 *  @author
 */
public class BoardTest {

    private static final String[]
        GAME1 = { "a7-b7", "a1-a2",
                  "a7-a6", "a2-a3",
                  "a6-a5", "a3-a4" };

    private static void makeMoves(Board b, String[] moves) {
        for (String s : moves) {
            b.makeMove(s.charAt(0), s.charAt(1),
                       s.charAt(3), s.charAt(4));
        }
    }

    @Test public void testUndo() {
        Board b0 = new Board();
        Board b1 = new Board(b0);
        makeMoves(b0, GAME1);
        Board b2 = new Board(b0);
        for (int i = 0; i < GAME1.length; i += 1) {
            b2.undo();
        }
        assertEquals("failed to return to start", b1, b2);
        assertEquals(b1.redPieces(), b2.redPieces());
        assertEquals(b1.bluePieces(), b2.bluePieces());
        assertEquals(b1.numMoves(), b2.numMoves());
        assertEquals(b1.allMoves(), b2.allMoves());
        makeMoves(b2, GAME1);
        assertEquals("second pass failed to reach same position", b2, b0);
    }

    @Test public void testBoard() {
        String[] jumpMove = new String[2];
        jumpMove[0] = "a7-a6";
        jumpMove[1] = "a1-a3";
        Board b = new Board();
        assertEquals(2, b.redPieces());
        assertEquals(2, b.bluePieces());
        b.setBlock("d2");
        assertEquals(2, b.numPieces(PieceColor.BLOCKED));

        makeMoves(b, GAME1);
        assertEquals(4, b.redPieces());
        assertEquals(6, b.bluePieces());
        assertEquals(37, b.numPieces(PieceColor.EMPTY));
        assertEquals(0, b.numJumps());
        assertEquals(6, b.numMoves());

        b.clear();
        assertEquals(2, b.redPieces());
        assertEquals(2, b.bluePieces());
        assertEquals(45, b.numPieces(PieceColor.EMPTY));
        assertEquals(0, b.numPieces(PieceColor.BLOCKED));

        makeMoves(b, jumpMove);
        assertEquals(3, b.redPieces());
        assertEquals(2, b.bluePieces());
        assertEquals(1, b.numJumps());
        assertEquals(2, b.numMoves());
    }

    @Test public void testLegalMove() {
        Board b = new Board();
        Move move = Move.move('a', '1', 'b', '1');
        assertFalse(b.legalMove(move));
        assertFalse(b.legalMove(Move.pass()));
    }
}

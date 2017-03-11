package ataxx;

/** Describes the classes of Piece on an Ataxx board.
 *  @author P. N. Hilfinger
 */
enum PieceColor {

    /** EMPTY: no piece.
     *  BLOCKED: square contains a block.
     *  RED, BLUE: piece colors. */
    EMPTY, BLOCKED,
    RED {
        @Override
        PieceColor opposite() {
            return BLUE;
        }

        @Override
        boolean isPiece() {
            return true;
        }
    },
    BLUE {
        @Override
        PieceColor opposite() {
            return RED;
        }

        @Override
        boolean isPiece() {
            return true;
        }
    };

    /** Return the piece color of my opponent, if defined. */
    PieceColor opposite() {
        throw new UnsupportedOperationException();
    }

    /** Return true iff I denote a piece rather than an empty square or
     *  block. */
    boolean isPiece() {
        return false;
    }

    @Override
    public String toString() {
        return capitalize(super.toString().toLowerCase());
    }

    /** Return WORD with first letter capitalized. */
    static String capitalize(String word) {
        return Character.toUpperCase(word.charAt(0)) + word.substring(1);
    }

}

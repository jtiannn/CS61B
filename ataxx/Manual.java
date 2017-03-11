package ataxx;

import static ataxx.PieceColor.*;
import static ataxx.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Jacky Tian
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Command cmnd = game().getMoveCmnd(myColor().toString() + ": ");
        return createMove(cmnd);
    }

    /** Return a move based on the CMND given. */
    Move createMove(Command cmnd) {
        if (cmnd.commandType() == PIECEMOVE) {
            return Move.move((cmnd.operands())[0].charAt(0),
                        (cmnd.operands())[0].charAt(1),
                        (cmnd.operands())[0].charAt(3),
                        (cmnd.operands())[0].charAt(4));
        }
        return Move.pass();
    }

}


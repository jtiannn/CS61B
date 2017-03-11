package ataxx;

/** An object that reports errors or messages.
 *  @author P. N. Hilfinger
 */
interface Reporter {

    /** Display an error message formed from FORMAT and OPERANDS as
     *  for String.format. */
    void errMsg(String format, Object... operands);

    /** Display a message intended to announce the outcome of a game,
     *  formed from FORMAT and OPERANDS as for String.format. */
    void outcomeMsg(String format, Object... operands);

    /** Display a message intended to announce a move or print a board
     *  formed from FORMAT and OPERANDS as for String.format. */
    void moveMsg(String format, Object... operands);

}

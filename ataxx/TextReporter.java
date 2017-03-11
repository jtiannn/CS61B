package ataxx;

/** A Reporter that uses System.out for messages.
 *  @author P. N. Hilfinger
 */
class TextReporter implements Reporter {

    @Override
    public void errMsg(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }

    @Override
    public void outcomeMsg(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }

    @Override
    public void moveMsg(String format, Object... args) {
        System.out.printf(format, args);
        System.out.println();
    }

}


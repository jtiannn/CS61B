package ataxx;

/** Provides command input, one line at a time.
 *  @author P. N. Hilfinger
 */
interface CommandSource {

    /** Read and return a line of input from the input stream,
     *  removing comments and leading and trailing whitespace,
     *  and skipping blank lines. Returns null when input exhausted.
     *  PROMPT suggests a prompt string that might be used, if
     *  appropriate to the input method. */
    String getLine(String prompt);
}

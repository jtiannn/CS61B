
import re, sys
from subprocess import Popen, DEVNULL, PIPE, STDOUT
from threading import Thread
from queue import Queue, Empty, Full
from getopt import getopt, GetoptError
from io import StringIO
from os.path import basename

EOF = object()

CLEANUP_TIME = 2
DEFAULT_TOTAL_TIME = 120
MOVE_FORMAT = '[a-g][1-7]-[a-g][1-7]'
RED_MOVE = re.compile(r'(?:((?:Red|Blue) wins|Draw)|Red (passes)|Red moves ('
                      + MOVE_FORMAT + '))\.$')
BLUE_MOVE = re.compile(r'(?:((?:Red|Blue) wins|Draw)|Blue (passes)|Blue moves ('
                       + MOVE_FORMAT + '))\.$')

class test_err(BaseException):
    pass
class test_fail(BaseException):
    pass

def prog_runner(prog, msg_queue, script_start):
    try:
        while True:
            cmnd = prog.next_cmnd()
            if cmnd is None:
                break
            for patn, func in program.COMMAND_PATTERNS:
                if match(patn, cmnd):
                    func(prog)
                    break
            else:
                prog.send(cmnd + "\n")
        prog.finish()
        end_msg = (prog.title, "OK", None, None)
    except test_err as excp:
        prog.stop()
        end_msg = (prog.title, "ERROR", excp.args[0], prog.k + script_start)
    except test_fail as excp:
        prog.stop()
        end_msg = (prog.title, "FAIL", excp.args[0], prog.k + script_start)
    except BaseException as excp:
        prog.stop()
        end_msg = (prog.title, "FAIL", repr(excp), prog.k + script_start)
    msg_queue.put(end_msg)
    prog.log("T {}", str(end_msg))
        
class program:
    def __init__(self, title, script_input, terminate_msgs,
                 script_start_line):
        if verbose:
            self.log_file = StringIO()
        else:
            self.log_file = None

        self.title = title
        self.eof = False
        self.script = script_input
        self.k = 0
        command = self.next_cmnd()
        if command is None:
            terminate_msgs.put((title, "FAIL", "No command found",
                                script_start + self.k))
        self.proc = Popen(re.split(r'\s+', command), universal_newlines=True,
                          stdin=PIPE, stdout=PIPE, stderr=STDOUT)
        self.input, _ = get_input_queue(title+"-in", self.proc.stdin)
        self.output, _ = get_output_queue(title+"-out", self.proc.stdout)
        self.op_limit = 10
        self.other_input = self.other_output = None
        self.thread = Thread(target=prog_runner, name=title + "-runner",
                             daemon=True,
                             args=(self, terminate_msgs, script_start_line))
    def log(self, msg, *args):
        if verbose:
            print(msg.format(*args).rstrip(), file=self.log_file)

    def print_log(self):
        print("\n-----\nLog for {}\n-----\n".format(self.title),
              self.log_file.getvalue(), "-----\n", file=sys.stderr)

    def start(self):
        self.thread.start()

    def connect(self, other):
        if self.other_input:
            return
        self.other_input = Queue(4)
        other.connect(self)
        self.other_output = other.other_input

    def error(self, msg, *args):
        raise test_err(msg.format(*args))

    def fail(self, msg, *args):
        raise test_fail(msg.format(*args))

    def send(self, msg):
        self.log("> {}", "<EOF>" if msg is EOF else msg.rstrip())
        self.input.put(msg)

    def send_other(self, msg, ignore=False):
        if self.other_input:
            try:
                self.log("R> {}", "<EOF>" if msg is EOF else msg)
                self.other_output.put(msg, block=False)
                return
            except Full:
                pass
        if not ignore:
            self.error("other program blocked")

    def get(self, timeout=None):
        timeout = timeout or self.op_limit
        try:
            if self.eof:
                return EOF
            v = self.output.get(timeout=timeout)
            if v is EOF:
                self.eof = True
            self.log("< {}", "<EOF>" if v is EOF else v.rstrip())
            return v
        except Empty:
            return None
    
    def get_other(self, timeout=None):
        timeout = timeout or self.op_limit
        try:
            if not self.other_output:
                error("no other program")
            move = self.other_input.get(timeout)
            if move is EOF:
                self.other_input = None
                self.log("R< <EOF>")
                return EOF
            self.log("R< {}", move)
            return move
        except Empty:
            return None

    def check_move(self, who, move):
        """Checks that MOVE is a move for WHO."""
        patn = RED_MOVE if who == "red" else BLUE_MOVE
        if not match(patn, move):
            self.error("invalid move for {} ({})", who, move)
        if group(1):
            return move, None
        elif group(2):
            return move, "-"
        else:
            return move, group(3)

    def check_patn(self, pattern):
        if re.match(r' *$', pattern):
            return None
        try:
            return re.compile(pattern)
        except re.error:
            self.fail("bad test pattern: {}", pattern)

    def get_move(self, who):
        """Get a move or win by WHO from this player."""
        move = self.get()
        if move is EOF:
            self.error("game output truncated")
        elif move is None:
            self.error("timed out waiting for my {} move", who)
        else:
            return self.check_move(who, move)

    def get_other_move(self, who):
        """Receive a move or win by WHO from the other player."""
        move = self.get_other(self)
        if move is EOF:
            self.error("game output truncated")
        elif move is None:
            self.error("timed out waiting for other's {} move", who)
        else:
            return self.check_move(who, move)

    def finish(self, timeout=None):
        self.send(EOF)
        self.send_other(EOF, ignore=True)
        line = self.get()
        if line is None:
            self.stop()
            self.error("program did not terminate properly")
        elif line is not EOF:
            self.stop()
            self.error("program produced extra output")
        self.proc.wait(timeout=timeout or self.op_limit)
        rc = self.proc.returncode
        if rc is None:
            self.stop()
            self.error("program did not terminate properly")
        elif rc != 0:
            self.error("program terminated with error exit")

    def stop(self):
        try:
            self.proc.kill()
            self.proc.wait()
        except:
            pass
        
    def is_game_end(self, move):
        if move is EOF:
            self.error("game output truncated")
        return search(r'wins|Draw', move)

    # Script-command handlers

    def do_time(self):
        try:
            self.op_limit = float(group(1))
        except ValueError:
            self.fail("bad number")

    def bad_command(self):
        self.fail("bad command in script")

    def play_self(self):
        to_move = group(1)
        end_patn = self.check_patn(group(2))
        while True:
            move, tag = self.get_move(to_move)
            if tag is None:
                break
            to_move = "blue" if to_move == "red" else "red"
        if end_patn and not match(end_patn, move):
            self.error("outcome does not match end pattern")

    def send_recv_moves(self, send_first, first_mover, end_patn):
        end_patn = self.check_patn(end_patn)
        second_mover = "blue" if first_mover == "red" else "red"
        if send_first:
            send, send_mover, recv_mover = True, first_mover, second_mover
        else:
            send, send_mover, recv_mover = False, second_mover, first_mover
        while True:
            if send:
                msg, move = self.get_move(send_mover)
                self.send_other(msg)
                if move is None:
                    other_msg, _ = self.get_other_move(recv_mover)
                    if msg != other_msg:
                        self.error("game outcomes don't agree")
                    break
            send = True
            other_msg, other_move = self.get_other_move(recv_mover)
            if other_move is None:
                msg, _ = self.get_move(send_mover)
                self.send_other(msg)
                if msg != other_msg:
                    self.error("game outcomes don't agree")
                break
            self.send(other_move)
        if end_patn and not end_patn.match(msg):
            self.error("outcome does not match end pattern")

    def send_moves(self):
        self.send_recv_moves(True, group(1), group(2))

    def receive_moves(self):
        self.send_recv_moves(False, group(1), group(2))

    def check_output(self):
        typ = group(1)
        patn_str = group(2)
        patn = self.check_patn(patn_str) if typ == '?' else patn_str
        line = self.get(timeout=self.op_limit)
        if line is EOF:
            self.error("premature end of output")
        if line is None:
            self.error("timed out waiting for output")
        if search('Exception', line):
            self.error("uncaught exception occurred: {}", line.rstrip())
        line = re.sub(r'\t', ' ', line.rstrip())
        line = re.sub('  +', ' ', line)
        if (typ == '<' and line == patn) or \
           (typ == '?' and match(patn, line)):
            return True
        else:
            self.error("output mismatch ({} / {})", line, patn_str)
            
    # End of handlers

    def next_cmnd(self, default=None):
        while True:
            if self.k >= len(self.script):
                self.log("* <EOF>")
                return default
            else:
                cmnd = clean(self.script[self.k])
                self.log("* {}", cmnd)
                self.k += 1
                if cmnd != '':
                    return cmnd

    COMMAND_PATTERNS = (
        (r"@time ([\d.]+)", do_time),
        (r"@total-time (\d+)", lambda x: None),
        (r"@([<?])(.*)", check_output),
        (r"@(red)\.\.\.\s*(.*)", play_self),
        (r"@(blue)\.\.\.\s*(.*)", play_self),
        (r"@send (red|blue)\.\.\.\s*(.*)", send_moves),
        (r"@recv (red|blue)\.\.\.\s*(.*)", receive_moves),
        (r"@", bad_command)
        )
        

def get_output_queue(title, stream):
    queue = Queue(500)
    def reader():
        while True:
            line = stream.readline()
            if line == '':
                queue.put(EOF)
                return
            line = re.sub(r'^.*:\s*', '', line)
            line = re.sub(r'\s+\n', '\n', line)
            line = re.sub('\t', ' ', line)
            line = re.sub('  +', ' ', line)
            if match(r'\s*===', line):
                queue.put(line)
                while True:
                    line = stream.readline()
                    if line == '':
                        queue.put(EOF)
                        return
                    queue.put(line)
                    if match(r'\s*===', line):
                        break
            elif search(r'(?i)wins|passes|moves|draw|Exception in thread', line):
                queue.put(line)
    th = Thread(target=reader, name=title, daemon=True)
    th.start()
    return queue, th

def get_input_queue(title, stream):
    queue = Queue(500)
    def writer():
        while True:
            line = queue.get()
            if line is EOF:
                stream.close()
                return
            print(line.rstrip(), file=stream)
            stream.flush()
    th = Thread(target=writer, name=title, daemon=True)
    th.start()
    return queue, th

def match(patn, text):
    global last_match
    if type(patn) is str:
        last_match = re.match(patn, text)
    else:
        last_match = patn.match(text)
    return last_match

def search(patn, text):
    global last_match
    if type(patn) is str:
        last_match = re.search(patn, text)
    else:
        last_match = patn.search(text)
    return last_match

def group(k):
    return last_match and last_match.group(k)

def breakup(text):
    text = text.rstrip()
    if text == '':
        return []
    return re.split(r'\n', text)

def clean(text):
    text = re.sub('\t', ' ', text)
    text = re.sub(r'^#.*', '', text.strip())
    text = re.sub(r'  +', ' ', text)
    return text

def log(format, *args, end="\n"):
    print(format.format(*args), file=sys.stderr, end=end)

def run_test(text):
    def print_logs():
        if verbose:
            prog1.print_log()
            if prog2:
                prog2.print_log()

    def make_msg(msg):
        title, typ, text, line = msg
        if typ == "OK":
            return ""
        else:
            return "{} near line {}".format(text, line)
        

    total_limit = DEFAULT_TOTAL_TIME
    if search(r'(?m)^\s+total-time\s+(\d+)', text):
        total_limit = int(group(1))
    terminate_queue = Queue(8)
    if match(r'(?sm)(.*?)^ *----------+ *\n(.*)', text):
        sect1, sect2 = breakup(group(1)), breakup(group(2))
        prog1 = program("Prog1", sect1, terminate_queue, 0)
        prog2 = program("Prog2", sect2, terminate_queue, len(sect1)+1)
        prog1.connect(prog2)
        prog2.connect(prog1)
        prog1.start()
        prog2.start()
    else:
        sect1 = breakup(text)
        prog1 = program("Prog1", sect1, terminate_queue, 0)
        prog1.start()
        prog2 = None
        
    msgs = []
    try:
        msgs.append(terminate_queue.get(timeout=total_limit))
    except Empty:
        prog1.stop()
        if prog2:
            prog2.stop()
        print_logs()
        return "ERROR", "test time exceeded {} seconds".format(total_limit)
    if prog2 is not None:
        try:
            msgs.append(terminate_queue.get(timeout=CLEANUP_TIME))
        except:
            prog1.stop()
            prog2.stop()
            print_logs()
            return "ERROR", "other program fails to finish"
    msgs = sorted(msgs)

    print_logs()

    if all(map(lambda x: x[1] == "OK", msgs)):
        return "OK", None
    if any(map(lambda x: x[1] == "FAIL", msgs)):
        return "FAIL", '/'.join(map(make_msg, msgs))
    else:
        return "ERROR", '/'.join(map(make_msg, msgs))
        
try:
    opts, args = getopt(sys.argv[1:], 'v', ['verbose'])
except GetoptError:
    print("Usage: python3 test-ataxx.py [ --verbose | -v ] SCRIPT ...",
          file=sys.stderr)
    sys.exit(1)


verbose= False
for opt, val in opts:
    if opt == '-v' or opt == '--verbose':
        verbose = True

test_count = len(args)
err_count = 0
problem_count = 0

for f in args:
    log("{}: ", basename(f), end="")
    try:
        with open(f) as inp:
            typ, msg = run_test(inp.read())
    except IOError as excp:
        typ, msg = "FAIL", str(excp)
    
    if typ == "OK":
        log("OK")
    else:
        log("{} ({})", typ, msg)
        if typ == "ERROR":
            err_count += 1
        else:
            problem_count += 1

log("\nSummary:\n   {:3d} tests\n   {:3d} passed\n   {:3d} errors\n"
    "   {:3d} problematic tests", 
    test_count, test_count - err_count - problem_count, err_count,
    problem_count)

sys.exit(0 if err_count + problem_count == 0 else 1)

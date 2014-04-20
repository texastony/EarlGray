/**
 * This package will one day be a working FTP Server, with a caffinated theme.
 * We are using FTP_Java_Server_Example by Abhishek K. Dhote as a guide, in conjungtion with our
 * echo telnet server, TelTexas. 
 * as example Telnet Servers to guide us.
 * @author Tony Knapp
 * @author Teagan Atwater
 * @author Jake Junda
 * @version Alpha
 * @category FTP server
 */

//Site for protocol specs:
// http://www.ietf.org/rfc/rfc959.txt

//Site for Abhishek K. Dhote's FTP server
//http://www.codemiles.com/finished-projects/ftp-server-and-ftp-client-t1179.html

//In general, it is the server's responsibility to maintain the data connection--to initiate it and to close it.

/*An FTP reply consists of a three digit number (transmitted as
    three alphanumeric characters) followed by some text.  The number
    is intended for use by automata to determine what state to enter
    next; the text is intended for the human user.  It is intended
      that the three digits contain enough encoded information that the
      user-process (the User-PI) will not need to examine the text and
      may either discard it or pass it on to the user, as appropriate.
      */

/* 5.1.  MINIMUM IMPLEMENTATION

      In order to make FTP workable without needless error messages, the
      following minimum implementation is required for all servers:

         TYPE - ASCII Non-print
         MODE - Stream
         STRUCTURE - File, Record
         COMMANDS - USER,
          					QUIT,
          					PORT,
                    TYPE,
                    MODE,
                    STRU,
                      for the default values
                    RETR,
                    STOR,
                    NOOP.

      The default values for transfer parameters are:

         TYPE - ASCII Non-print
         MODE - Stream
         STRU - File

      All hosts must accept the above as the standard defaults.
*/

/*
 *  5.3.1.  FTP COMMANDS

         The following are the FTP commands:

            USER <SP> <username> <CRLF>
            PASS <SP> <password> <CRLF>
            QUIT <CRLF>
            PORT <SP> <host-port> <CRLF>
            PASV <CRLF>
            TYPE <SP> <type-code> <CRLF>
            STRU <SP> <structure-code> <CRLF>
            MODE <SP> <mode-code> <CRLF>
            RETR <SP> <pathname> <CRLF>
            STOR <SP> <pathname> <CRLF>
            PWD  <CRLF>
            LIST [<SP> <pathname>] <CRLF>
            HELP [<SP> <string>] <CRLF>
            NOOP <CRLF>

*/

/*
 * 5.3.2.  FTP COMMAND ARGUMENTS

         The syntax of the above argument fields (using BNF notation
         where applicable) is:

            <username> ::= <string>
            <password> ::= <string>
            <string> ::= <char> | <char><string>
            <char> ::= any of the 128 ASCII characters except <CR> and
            <host-port> ::= <host-number>,<port-number>
            <host-number> ::= <number>,<number>,<number>,<number>
            <port-number> ::= <number>,<number>
            <number> ::= any decimal integer 1 through 255
            <form-code> ::= N | T | C
            <type-code> ::= A [<sp> <form-code>]
                          | E [<sp> <form-code>]
                          | I
                          | L <sp> <byte-size>
            <structure-code> ::= F | R | P
            <mode-code> ::= S | B | C
            <pathname> ::= <string>
 */

/*
 * 5.4.  SEQUENCING OF COMMANDS AND REPLIES

      The communication between the user and server is intended to be an
      alternating dialogue.  As such, the user issues an FTP command and
      the server responds with a prompt primary reply.  The user should
      wait for this initial primary success or failure response before
      sending further commands.

      Certain commands require a second reply for which the user should
      also wait.  These replies may, for example, report on the progress
      or completion of file transfer or the closing of the data
      connection.  They are secondary replies to file transfer commands.

      One important group of informational replies is the connection
      greetings.  Under normal circumstances, a server will send a 220
      reply, "awaiting input", when the connection is completed.  The
      user should wait for this greeting message before sending any
      commands.  If the server is unable to accept input right away, a
      120 "expected delay" reply should be sent immediately and a 220
      reply when ready.  The user will then know not to hang up if there
      is a delay.

      Spontaneous Replies

         Sometimes "the system" spontaneously has a message to be sent
         to a user (usually all users).  For example, "System going down
         in 15 minutes".  There is no provision in FTP for such
         spontaneous information to be sent from the server to the user.
         It is recommended that such information be queued in the
         server-PI and delivered to the user-PI in the next reply
         (possibly making it a multi-line reply).

      The table below lists alternative success and failure replies for
      each command.  These must be strictly adhered to; a server may
      substitute text in the replies, but the meaning and action implied
      by the code numbers and by the specific command reply sequence
      cannot be altered.

      Command-Reply Sequences

         In this section, the command-reply sequence is presented.  Each
         command is listed with its possible replies; command groups are
         listed together.  Preliminary replies are listed first (with
         their succeeding replies indented and under them), then
         positive and negative completion, and finally intermediary
          replies with the remaining commands from the sequence
         following.  This listing forms the basis for the state
         diagrams, which will be presented separately.

            Connection Establishment
               120
                  220
               220
               421
            Login
               USER
                  230
                  530
                  500, 501, 421
                  331, 332
               PASS
                  230
                  202
                  530
                  500, 501, 503, 421
                  332
            Logout
               QUIT
                  221
                  500
                  Transfer parameters
               PORT
                  200
                  500, 501, 421, 530
               PASV
                  227
                  500, 501, 502, 421, 530
               MODE
                  200
                  500, 501, 504, 421, 530
               TYPE
                  200
                  500, 501, 504, 421, 530
               STRU
                  200
                  500, 501, 504, 421, 530
            File action commands
               RETR
                  125, 150
                     (110)
                     226, 250
                     425, 426, 451
                  450, 550
                  500, 501, 421, 530
                  LIST
                  125, 150
                     226, 250
                     425, 426, 451
                  450
                  500, 501, 502, 421, 530
               PWD
                  257
                  500, 501, 502, 421, 550
               HELP
                  211, 214
                  500, 501, 502, 421
            Miscellaneous commands
               NOOP
                  200
                  500 421
 */

/*
 * 6.  STATE DIAGRAMS

   Here we present state diagrams for a very simple minded FTP
   implementation.  Only the first digit of the reply codes is used.
   There is one state diagram for each group of FTP commands or command
   sequences.

   The command groupings were determined by constructing a model for
   each command then collecting together the commands with structurally
   identical models.

   For each command or command sequence there are three possible
   outcomes: success (S), failure (F), and error (E).  In the state
   diagrams below we use the symbol B for "begin", and the symbol W for
   "wait for reply".

   We first present the diagram that represents the largest group of FTP
   commands:

      
                               1,3    +---+
                          ----------->| E |
                         |            +---+
                         |
      +---+    cmd    +---+    2      +---+
      | B |---------->| W |---------->| S |
      +---+           +---+           +---+
                         |
                         |     4,5    +---+
                          ----------->| F |
                                      +---+
      

      This diagram models the commands:

         ABOR, ALLO, DELE, CWD, CDUP, SMNT, HELP, MODE, NOOP, PASV,
         QUIT, SITE, PORT, SYST, STAT, RMD, MKD, PWD, STRU, and TYPE.

   The other large group of commands is represented by a very similar
   diagram:

      
                               3      +---+
                          ----------->| E |
                         |            +---+
                         |
      +---+    cmd    +---+    2      +---+
      | B |---------->| W |---------->| S |
      +---+       --->+---+           +---+
                 |     | |
                 |     | |     4,5    +---+
                 |  1  |  ----------->| F |
                  -----               +---+
      

      This diagram models the commands:

         APPE, LIST, NLST, REIN, RETR, STOR, and STOU.

   Note that this second model could also be used to represent the first
   group of commands, the only difference being that in the first group
   the 100 series replies are unexpected and therefore treated as error,
   while the second group expects (some may require) 100 series replies.
   Remember that at most, one 100 series reply is allowed per command.

   The remaining diagrams model command sequences, perhaps the simplest
   of these is the rename sequence:

      
      +---+   RNFR    +---+    1,2    +---+
      | B |---------->| W |---------->| E |
      +---+           +---+        -->+---+
                       | |        |
                3      | | 4,5    |
         --------------  ------   |
        |                      |  |   +---+
        |               ------------->| S |
        |              |   1,3 |  |   +---+
        |             2|  --------
        |              | |     |
        V              | |     |
      +---+   RNTO    +---+ 4,5 ----->+---+
      |   |---------->| W |---------->| F |
      +---+           +---+           +---+  
      The next diagram is a simple model of the Restart command:

      
      +---+   REST    +---+    1,2    +---+
      | B |---------->| W |---------->| E |
      +---+           +---+        -->+---+
                       | |        |
                3      | | 4,5    |
         --------------  ------   |
        |                      |  |   +---+
        |               ------------->| S |
        |              |   3   |  |   +---+
        |             2|  --------
        |              | |     |
        V              | |     |
      +---+   cmd     +---+ 4,5 ----->+---+
      |   |---------->| W |---------->| F |
      +---+        -->+---+           +---+
                  |      |
                  |  1   |
                   ------
      

         Where "cmd" is APPE, STOR, or RETR.

   We note that the above three models are similar.  The Restart differs
   from the Rename two only in the treatment of 100 series replies at
   the second stage, while the second group expects (some may require)
   100 series replies.  Remember that at most, one 100 series reply is
   allowed per command.
   
    The most complicated diagram is for the Login sequence:

      
                            1
      +---+   USER    +---+------------->+---+
      | B |---------->| W | 2       ---->| E |
      +---+           +---+------  |  -->+---+
                       | |       | | |
                     3 | | 4,5   | | |
         --------------   -----  | | |
        |                      | | | |
        |                      | | | |
        |                 ---------  |
        |               1|     | |   |
        V                |     | |   |
      +---+   PASS    +---+ 2  |  ------>+---+
      |   |---------->| W |------------->| S |
      +---+           +---+   ---------->+---+
                       | |   | |     |
                     3 | |4,5| |     |
         --------------   --------   |
        |                    | |  |  |
        |                    | |  |  |
        |                 -----------
        |             1,3|   | |  |
        V                |  2| |  |
      +---+   ACCT    +---+--  |   ----->+---+
      |   |---------->| W | 4,5 -------->| F |
      +---+           +---+------------->+---+
      
Finally, we present a generalized diagram that could be used to model
   the command and reply interchange:

      
               ------------------------------------
              |                                    |
      Begin   |                                    |
        |     V                                    |
        |   +---+  cmd   +---+ 2         +---+     |
         -->|   |------->|   |---------->|   |     |
            |   |        | W |           | S |-----|
         -->|   |     -->|   |-----      |   |     |
        |   +---+    |   +---+ 4,5 |     +---+     |
        |     |      |    | |      |               |
        |     |      |   1| |3     |     +---+     |
        |     |      |    | |      |     |   |     |
        |     |       ----  |       ---->| F |-----
        |     |             |            |   |
        |     |             |            +---+
         -------------------
              |
              |
              V
             End
      
 */
package us.texastony.EarlGray;
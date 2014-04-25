/**
 * This package will one day be a working FTP Server, with a caffinated theme.
 * We are using FTP_Java_Server_Example by Abhishek K. Dhote as a guide, in conjungtion with our
 * echo telnet server, TelTexas. 
 * as example Telnet Servers to guide us.
 * @author Tony Knapp
 * @author Teagan Atwater
 * @author Jake Junda
 * @version 1.0 (04/24/2014)
 * @category FTP server
 */

//Site for protocol clarification
//http://slacksite.com/other/ftp.html

//Site for protocol specs:
// http://www.ietf.org/rfc/rfc959.txt

//Site for Abhishek K. Dhote's FTP server
//http://www.codemiles.com/finished-projects/ftp-server-and-ftp-client-t1179.html

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
 */
package EarlGray;
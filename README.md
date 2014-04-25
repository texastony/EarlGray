EarlGray
========

This a very simple FTP server implementation for a project.

###Installation Requirements###
Requires Java 1.6+

Runs Completely on Linux

Runs on Max OS X, but PWD will not return unless FTP is run with arguments: -dv

##Server Side##

###Start the server:###
`java -jar EarlGray.jar [-d <directory>] [-p <port number>]`

###Commands:###
`PORT <port number>` - Allows you to set the server's port number

`QUIT` - Disconnects all client sessions and shuts down the server

##Client Side##

###Commands:###

`USER <username>` - Submit username to server

`PASS <password>`- Submit password to server

`LIST` - List all files and folders in current directory

`PWD` - Print current directory

`RETR <filename>` - Get a file from the server

`Port <port number>` - Send local port number to server

`TYPE <type>` - Set transmission type (must be legth 8 bytes `L 8` or `tenex`)

`MODE <mode>` - Set transmission mode (must be Stream `S`)

`STRU <structure>` - Set transmission structure (must be File `F`)

`NOOP` - Keep the server active

###To Compile Source Code###
Any advanced Development Platform such as Eclipse or Xcode will compile this with no fuss.

Compiling with the command line is a bit of a pain, as you have to set the package enviormental variable.

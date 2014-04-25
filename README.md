EarlGray
========

This a very simple FTP server implementation for a project.

##Server Side##

###Start the server:###
`Java EarlGray [-d <directory>] [-p <port number>]`

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

`TYPE <type>` - Set transmission type (must be L 8)

`MODE <mode>` - Set transmission mode (must be Stream)

`STRU <structure>` - Set transmission structure (must be File)

`NOOP` - Keep the server active

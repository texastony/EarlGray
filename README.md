EarlGray
========

This a very simple FTP server implementation for a project.

##Server Side##

###Start the server:###
`Java EarlGray [-d <directory>] [-p <port number>]`

###Commands:###
`PORT` - Allows you to set the server's port number

`QUIT` - Disconnects all client sessions and shuts down the server

##Client Side##

###Commands:###

`USER` - Submit username to server

`PASS`- Submit password to server

`LIST` - List all files and folders in current directory

`PWD` - Print current directory

`RETR <filename>` - Get a file from the server

`Port` - Send local port number to server

`TYPE` - Set transmission type (must be L 8)

`MODE` - Set transmission mode (must be Stream)

`STRU` - Set transmission structure (must be File)

`NOOP` - Keep the server active

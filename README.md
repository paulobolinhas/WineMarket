# CONTENTS
    * Introduction
    * Usage
    * Contributors

# INTRODUCTION

The work consists of implementing the Tintolmarket system, which is a client-server type system offering a similar service
to that of Vivino, but allowing the purchase and sale of wines by system users. Exists
a server application "Tintolmarket server" that maintains information about registered users and wines
listed, their value, classification and quantity made available by each user. The users
can add wines, indicate the quantity they supervised, rate each wine, and even send
private messages to other users. Registered users must use an application
client to interact with the server and use these features.

# USAGE

In order to run the project in IDE you must run TintolMarket class after run TintolMarketServer class. 

You can run multiple clients since its a multi threaded application.

In order to run the project in command prompt you can "cd" to the open the project folder 
and then run TintolMarket after run TintolMarketServer. 

- Server: 

  TintolmarketServerJar <port>

	• <port> identifies the port (TCP) to accept connections from clients. By default the server
	    	 must use port 12345.

	run Server: java -jar server.jar <port> 

- Client:

  TintolmarketJar <serverAddress> <userID> [password]

	• <serverAddress> identifies the server. The format of serverAddress is as follows:
  			  <IP/hostname>[:Port]. IP address or server hostname is mandatory and the port
  			  it is optional. By default, the client should connect to server port 12345.
	• <clientID> identifies the local user.
	• [password] – password used to authenticate the local user. If the password does not
	       	       given on the command line, it must be prompted by the application (Optional).

	run Client: java -jar client.jar <serverAddress> <clientID> [password]


# LIMITATIONS

Client must follow the actions menu input rules:

---
Actions:
add <wine> <image>
sell <wine> <value> <quantity>
view <wine>
buy <wine> <seller> <quantity>
wallet 
classify <wine> <stars>
talk <user> <message>
read
exit
---

For client to run the add function he must have the wine image in the local repository.

# CONTRIBUTORS

Group SD-050
 João Santos nº 57103
 Paulo Bolinhas nº 56300
 Rui Martins nº 56283

-------------------------------------------------------------------------------------------------# WineMarket
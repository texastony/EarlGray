package us.texastony.EarlGray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**This class spawns a Thread that
 * handles the clients connections.
 * <p>
 * There may be a bit of southern dialect
 * in our server responses. Ain't anything to be 
 * alarmed about it. Gives the server somethin' of 
 * a character. 
 * 
 * @author Tony Knapp
 * @since Alpha (04/04/2014)
 * @version Alpha (04/04/2014)
 */
public class EGClientInst extends Thread {
	private Socket controlSoc;
	private Socket dataSoc; 
	private BufferedReader controlIn; 
	private DataOutputStream controlOut; 
	private String handle;
	private Date loginTime;
	private EarlGray kettle;
	private String curDirName = "Server/";
	private File parentDir;
	public boolean acceptence = false;
	private boolean running = true;
	private boolean dataConnection = false;
	private boolean type = true; //True for ASCII, False for Bytes (L 8)
	private boolean[] mode = {false, false}; // 0 0: Stream, 0 1: Block, 1 0: Compressed
	
	/**This constructs a Thread that
	 * handles the client's connections. 
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 * @param Socket, Server
	 */
	public EGClientInst(Socket cSoc, EarlGray server, File parentFolder ) throws IOException {
		this.controlSoc = cSoc; // attach to client socket
		this.controlIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.controlOut = new DataOutputStream(controlSoc.getOutputStream());
		this.kettle = server;
		this.parentDir = parentFolder;
		System.out.println("A new guest has Conncected\rAwaiting Username and Password");
	}
	
	/**
	 * First, this function forces the user to login, providing both a user name,
	 * and a valid password. Then, the server allows the client to send commands.
	 * run() then calls other functions to handle all the commands.
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha (04/04/2014)
	 */
	public void run() {
 		try {
			this.loginTime = new Date();
			if (this.running) {
				greetGuest();
			}
			if (this.running) {
				String text = controlIn.readLine(); // takes user input and logs it
				while (text != null && !text.trim().equalsIgnoreCase("QUIT")	&& this.running) {
					if (text.trim().startsWith("LIST")) {
						list();
					}
					else if (text.trim().startsWith("PWD")){
						controlOut.writeChars("257 Directory Path: " + curDirName + "\r");
						controlOut.flush();
					}
					else if (text.trim().startsWith("RETR")) {
						retr(text);
					}
					else if (text.trim().startsWith("PORT")){
						port(text);
					}
					else if (text.trim().startsWith("TYPE")){
						type(text);
					}
					else if (text.trim().startsWith("MODE")){
						mode(text);
					}
					else if (text.trim().startsWith("STRU")){
						stru(text);
					}
					else if (text.trim().startsWith("NOOP")){
						//TODO TEAGAN: NOOP -- Do nothing but send an OK reply
//						noop();
					}
					else if (text.trim().startsWith("PASV")){
						pasv();
					}
					else {
						controlOut.writeChars("502 Command not implemented");
						controlOut.flush();
					}
					text = controlIn.readLine();
				}
			}
			if (this.running) {
				controlOut.writeChars("221, Have a wonderful day.\r");
				controlOut.flush();
				quit();
			}
		} catch (IOException e) {
			e.printStackTrace(); // print error stack
		}
	}
	
	/**
	 * Sets the transmission structure.
	 * EarlGray only supports File (F), though
	 * a more complete FTP server would support
	 * Page (P) and Record (R).
	 * 
	 * @author Tony Knapp
	 * @param input
	 * @throws IOException
	 * @since Alpha (04/22/2014)
	 */
	private void stru(String input) throws IOException {
		input = input.substring(3).trim();
		if (input == "F") {
			this.controlOut.writeChars("200 Transmission Structure set to File.");
			this.controlOut.flush();
			return;
		}
		else {
			this.controlOut.writeChars("504 EarlGray only supports Transmission Structure set to File.");
			this.controlOut.flush();
			return;
		}
	}
	
	/**
	 * Sets the transmissions mode for
	 * files. The mode determines how 
	 * files are sent to the client. The
	 * options are Stream, Block, or Compressed.
	 * EarlGray will definately support Stream, and hopefully
	 * support Block.
	 * 
	 * @author Tony Knapp
	 * @param input
	 * @throws IOException 
	 * @since Alpha (04/22/2014)
	 */
	private void mode(String input) throws IOException {
		input = input.substring(3).trim();
		if (input == "S") {
			this.mode[0] = false;
			this.mode[1] = false;
			this.controlOut.writeChars("200 Transmission Mode is Stream.");
			this.controlOut.flush();
			return;
		}
//		else if (input == "B") {
//			this.mode[0] = false;
//			this.mode[1] = true;
//			this.controlOut.writeChars("200 Transmission Mode is Block.");
//			this.controlOut.flush();
//		}
		else {
			this.controlOut.writeChars("504 EarlGray only supports Stream");
			this.controlOut.flush();
			return;
		}
	}
	
	private void sendBlock() {
		//I really want to get block mode, but time may mean we blow it off 
		
		//So Block Mode:
		//Data sent in blocks
		//(Some or all?) data blocks are preceded by a header block.
		//A header block contains a count field and descriptor code(s?)
		//
	}
	
	/**
	 * For EarlGray, if this not A T, or L 8,
	 * it is not supported. Otherwise, we are good.
	 * The function sets the transmission data type.
	 * Only data being sent on the data port is in
	 * this type. Everything done on the control port
	 * is over ASCII Telnet. 
	 * 
	 * @author Tony Knapp
	 * @param input
	 * @throws IOException 
	 * @since alpha (04/22/2014)
	 */
	private void type(String input) throws IOException {
		input = input.substring(3).trim();
		if (input != "A T" || input != "A" || input != "L 8"){
			this.controlOut.writeChars("504 EarlGray only supports A T, A, or L 8.");
			this.controlOut.flush();
			return;
		}
		else {
			if (input == "A T" || input == "A") {
				this.type = true;
				this.controlOut.writeChars("200 Type set to ASCII");
				this.controlOut.flush();
				return;
			}
			else if (input == "L 8") {
				this.type = false;
				this.controlOut.writeChars("200 Type set to Bytes with length 8");
				this.controlOut.flush();
				return;
			}
		}
	}
	
	/** Allows the server to establish a connection to the client
	 * PASSIVELY. This means that the server will open a server socket
	 * and send the address of it to the client, who will then connect to 
	 * it.
	 * 
	 * @author Tony Knapp
	 * @param input
	 * @throws IOException
	 * @since alpha (04/22/2014)
	 */	
	private void pasv() throws IOException {
		this.controlOut.writeChars("100 creating data connection");
		this.controlOut.flush();
		if (this.dataConnection){
			this.dataSoc.close();
			this.dataConnection = false;
		}
		final ServerSocket server = new ServerSocket(0);
		String ipAddress = server.getInetAddress().toString();
		int portNumber = server.getLocalPort();
		ipAddress = ipAddress.replace('.', ',');
		ipAddress = ipAddress.concat(Integer.toString(portNumber/256) + "," + Integer.toString(portNumber%256));
		this.controlOut.writeChars("227 Entering Passive Mode (" + ipAddress +")");
		this.controlOut.flush();
		//TODO The connection should be in another thread, so that server thread can keep running...
		this.dataSoc = server.accept();
		server.close();
		this.dataConnection = true;
		return;
	}
	
	/**
	 * This function allows the server to make an
	 * ACTIVE connection to the client. This is when client
	 * tells the server what port to connect to it. Then, our server
	 * attempts connects to that port.
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/21/2014)
	 */
	private void port(String input) throws IOException {
		if (this.running) {
			this.controlOut.writeChars("100 establishing data connection");
			this.controlOut.flush();
			String[] args = input.split(" ");
			String[] hostPort = args[1].split(",");
			String hostNumber = hostPort[0] + "." + hostPort[1] + "." + hostPort[2] + "." + hostPort[3];
			int portNumber = Integer.parseInt(hostPort[4])*256 + Integer.parseInt(hostPort[5]);
			try {
				this.dataSoc = new Socket(hostNumber, portNumber);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				this.controlOut.writeChars("501 Failed to connect to specified address.");
				this.controlOut.flush();
			}
			this.dataConnection = true;
			this.controlOut.writeChars("200 Data connection established");
			this.controlOut.flush();
		}		
			
	}

	/**
	 * Function finds the file the user
	 * requests and sends it to the desired port.
	 * 
	 * @author Tony Knapp
	 * @throws IOException
	 * @since alpha 04/21/2014
	 */
	private void retr(String input) throws IOException {
		//TODO  RETR -- Command specifies a file to be sent to the connect data port
		int startOfPath = 5;
		String reqFile = input.substring(startOfPath);
		try {
			File sendFile = new File(reqFile);
			if (!sendFile.isFile()) {
				controlOut.writeChars("450 RETR aborted");
				controlOut.flush();
				controlOut.writeChars("550 File not avaliable");
				return;
			}
			if (!dataConnection){
				controlOut.writeChars("150 File Ok, about to open data connection");
				pasv();
			}
			if (this.mode[0] == true) {
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO send file
		//TODO log file transfer to kettle
	}
	
	/**
	 * This Function communicates with the client user, Receives their input,
	 * stores them to the Session's class attributes. It ensures that the User
	 * sends the USER command, and then the PASS command. The function and those that
	 * it calls (user(), pass()) log everything through the kettle. 
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 */
	private void greetGuest() throws IOException {
		if (this.running) {
			controlOut.writeChars("220 Good day! Before you have some tea, \r we must know if you are on the guest list. \r May I have your name?\r");
			controlOut.flush();
			String text = controlIn.readLine();
			while (!text.startsWith("USER")) {
				controlOut.writeChars("530 First command must be USER <username>");
				controlOut.flush();
				text = controlIn.readLine();
			}
			user(text);			
			if (!this.acceptence) {
				controlOut.writeChars("421 Failed Login three times. You have been kicked.\r");
				while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence));
				quit();
				return;
			}
		}
	}
	
	/**
	 * Sets the user's name to the given argurment.
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/21/2014)
	 * @param input
	 * @throws IOException 
	 */
	private void user(String input) throws IOException {
		this.handle = input.replace("USER", "");	
		controlOut.writeChars("331 Username Logged, please provide password now via \"PASS <sp> <username>\"");
		controlOut.flush();
		System.out.println(this.handle + " has connected but not provided a password");
		tryAgain(3);
	}
	
	/**
	 * This function checks the password provided by the user. If the count is
	 * less than or equal to zero, it reutrns false. If the user provides the
	 * correct password, then it returns true. If the user provides the wrong
	 * password, it returns <code>tryAgain(count-1)</code>
	 * 
	 * @author Tony Knapp
	 * @param
	 * @return boolean
	 * @since Alpha (04/04/2014)
	 */
	private boolean tryAgain(int count) throws IOException {
		if (count <= 0 && this.running) {
			while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence));
			return false;
		} 
		else if (this.running) {
			controlOut.flush();
			String text = controlIn.readLine();
			if (text.startsWith("PASS")) {
				pass(text);
				if (this.acceptence) {
					return true;
				}
				else {
					return tryAgain(count - 1);
				}
			} 
			else if (this.running && count > 1) {
				controlOut.flush();
				controlOut.writeChars("530 The next command must be \"PASS <sp> <password>\"");
				controlOut.flush();
				System.out.println(this.handle + " don't know the password!");
				return tryAgain(count - 1);
			}
		}
		return false;
	}

	
	/**
	 * This function checks the server's password via the kettle.
	 * It also logs the user as logged in if the user is successful.
	 * 
	 * @author Tony Knapp
	 * @throws IOException 
	 * @since Alpha (04/21/2014)
	 * 
	 */
	private void pass(String input) throws IOException{
		if (kettle.running && this.running && kettle.checkPassword(input.substring(5))) {
			this.acceptence = true;
			this.loginTime = new Date();
			while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence));
			controlOut.writeChars("230 Password Accepted. User logged in at" + this.loginTime);
			controlOut.flush();
			System.out.println(this.handle + " has gained attmidentence " + this.loginTime);
		}
		else {
			controlOut.writeChars("530 That is not the password");
			controlOut.flush();
			System.out.println(this.handle + " does not know the password!");
		}
	}
	
	/**
	 * This function lists the contents of the current dirName
	 * 
	 * 
	 * @author Jake Junda
	 * @author Teagan Atwater
	 * @author Tony Knapp
	 * @throws IOException 
	 * @since Alpha (04/04/2014)
	 */
	private void list() throws IOException {
		//TODO  LIST -- List files & directories in current dirName
		controlOut.writeChars("100");
		controlOut.flush();
		//TODO check file status
		if (!this.dataConnection && parentDir.isDirectory()) {
			controlOut.writeChars("150 File status okay; about to open data connection \r");
			controlOut.flush();
			pasv();
		}
    controlOut.writeChars("Directory Name "+this.curDirName);
		controlOut.flush();
    ArrayList<String> files = new ArrayList<String>(Arrays.asList(curDir.list()));
    for (int i =0 ; i < files.size(); i++) {
    	controlOut.writeChars(files.get(i));
    	controlOut.flush();
    }
	}


	/**
	 * This function removes client session from server's active sessions and
	 * alerts server's terminal
	 * 
	 * @author Teagan Atwater
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 * @exception IOException
	 */
	private void quit() throws IOException {
		//TODO  Ensure there is not file being sent before quitting!
		if (kettle.terminateSession(this)) {
			if (this.acceptence) {
				System.out.println(this.handle + " has left the tea party.");
			} 
			else {
				System.out.println(this.handle + " has been driven off.");
			}
			shutThingsDown(0);
		}
	}

	/**
	 * This function closes down everything related to the client session
	 * 
	 * @author Teagan Atwater
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 * @exception IOException
	 */
	public boolean shutThingsDown(int printKick) throws IOException {
		this.running = false;
		if (printKick == 1) {
			controlOut.writeChars("221 I regret to inform you that this tea pary has come to an end. \r Safe travels, and please come again.");
			controlOut.flush();
		}
		if (printKick == 2) {
			controlOut.writeChars("10068 I regret to inform you that this tea pary is full. \r There is no room for other users.");
			controlOut.flush();
		}
		this.controlSoc.shutdownInput(); // closes client inputStream, allows this.run() to end
		controlIn.close();                       // closes input reader
		controlOut.close();                      // closes output writer
		controlSoc.close();              // closes socket on port
		return true;
	}
}

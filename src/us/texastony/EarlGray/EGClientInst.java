package us.texastony.EarlGray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
//import java.util.ArrayList;
//import java.util.Arrays;
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
 * @version Beta (04/23/2014)
 */
public class EGClientInst extends Thread {
	Socket controlSoc;
	Socket dataSoc;
	BufferedReader controlIn;
	DataOutputStream controlOut;
	String handle = "";
	Date loginTime;
	EarlGray kettle;
	String curDirName = "Server/";
	File curDir;
	File parentDir;
	boolean acceptence = false;
	boolean running = true;
	boolean dataConnection = false;
	boolean isSending = false;
	/**
	 * True for ASCII, False for Bytes (L 8)
	 */
	boolean type = true;
	/**
	 * 0 0: Stream, 0 1: Block, 1 0: Compressed
	 */
	boolean[] mode = {false, false};
	
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
		this.curDir = this.parentDir;
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
 			controlOut.writeChars("220 Good day!\rMay I have your name?\n");
			controlOut.flush();
			this.loginTime = new Date();
			if (this.running) {
				String text = controlIn.readLine(); // takes user input and logs it
				System.out.println(text);
				while (!text.trim().equalsIgnoreCase("QUIT")	&& this.running) {
					if (!text.equals(null)) {
						kettle.logTransfer(handle, new Date(), text);						
					}
					if (text.equals(null));
					else if (this.handle.isEmpty()) {
						if (text.trim().startsWith("USER")) {
							user(text);
						}
						else {
							controlOut.writeChars("530 First command must be USER <username>\n");
							controlOut.flush();							
						}
					}
					else if (text.trim().startsWith("USER")) {
						user(text);
					}
					else if (!this.acceptence) {
						if (text.trim().startsWith("PASS")) {
							pass(text);
						}
						else {
							controlOut.writeChars("530 The next command must be PASS <sp> <password>\n");
							controlOut.flush();
						}
					}
					else if (text.trim().startsWith("PASS")) {
						pass(text);
					}
					else if (text.trim().startsWith("LIST")) {
						list();
					}
					else if (text.trim().startsWith("PWD")){
						pwd(text);
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
						noop();
					}
					else {
						controlOut.writeChars("502 Command not implemented\n");
						controlOut.flush();
					}
					text = controlIn.readLine();
					System.out.println(text);
				}
			}
			if (this.running) {
				controlOut.writeChars("221, Have a wonderful day\n");
				controlOut.flush();
				quit();
			}
		} catch (IOException e) {
			shutThingsDown(0);
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
		if (input.equals("F")) {
			this.controlOut.writeChars("200 Transmission Structure set to File\n");
			this.controlOut.flush();
			return;
		}
		else {
			this.controlOut.writeChars("504 EarlGray only supports Transmission Structure set to File\n");
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
		if (input.equals("S")) {
			this.mode[0] = false;
			this.mode[1] = false;
			this.controlOut.writeChars("200 Transmission Mode is Stream\n");
			this.controlOut.flush();
			return;
		}
		else {
			this.controlOut.writeChars("504 EarlGray only supports Stream\n");
			this.controlOut.flush();
			return;
		}
	}
	
	/**
	 * For EarlGray, only L 8,
	 * is supported.
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
		input = input.substring(4).trim();
		if (input.equals("L 8")) {
				this.type = false;
				this.controlOut.writeChars("200 Type set to Bytes with length 8\n");
				this.controlOut.flush();
				return;
		}
		else{
			this.controlOut.writeChars("504 EarlGray only supports A T, A, or L 8\n");
			this.controlOut.flush();
			return;
		}
	}
	
	/** Responds with 200.
	 * 
	 * @author Teagan Atwater
	 * @throws IOException
	 * @since alpha (04/22/2014)
	 */	
	private void noop() throws IOException {
        this.controlOut.writeChars("200 We'll wait for you\n");
        this.controlOut.flush();
        return;
	}
	
	/** Allows any function in the package EarlGray to send
	 * a message down the Control port.
	 * 
	 * @author Tony Knapp
	 * @param msg
	 * @return int
	 * @throws IOException
	 * @since Beta (04/23/2014)
	 */
	int sendControlMessage(String msg) throws IOException {
		if (this.running) {
			try {
				this.controlOut.writeChars(msg);
				this.controlOut.flush();
				return 0;
			} catch (IOException e){
				return 1;
			}
		}			
		return 1;
	}
	
	/**
	 * Allows a seperate thread to set the data connection
	 * if there is no data connection set currently.
	 * 
	 * @author Tony Knapp
	 * @param newData
	 * @since Beta (04/23/2014)
	 */
	int setData(Socket newData) {
		if (this.dataConnection == false) {
			this.dataSoc = newData;
			this.dataConnection = true;
			return 0;
		}
		else {
			return 1;
		}
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
			String[] args = input.split(" ");
			String[] hostPort = args[1].split(",");
			String hostNumber = hostPort[0] + "." + hostPort[1] + "." + hostPort[2] + "." + hostPort[3];
			int portNumber = Integer.parseInt(hostPort[4])*256 + Integer.parseInt(hostPort[5]);
			try {
				this.dataSoc = new Socket(hostNumber, portNumber);
			} catch (UnknownHostException e) {
				this.controlOut.writeChars("501 Failed to connect to specified address\n");
				this.controlOut.flush();
			}
			this.dataConnection = true;
			this.controlOut.writeChars("200 Data connection established\n");
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
	void retr(String input) throws IOException {
		int startOfPath = 4;
		String reqFile = input.substring(startOfPath).trim();
		try {
			final File sendFile = new File(curDir.getPath() + "/" + reqFile);
			if (!sendFile.isFile() || !sendFile.canRead()) {
				controlOut.writeChars("550 File not avaliable\n");
				controlOut.flush();
				return;
			}
			if (!dataConnection){
				controlOut.writeChars("150 File Ok, about to open data connection\n");
				controlOut.flush();
			}
			else {
				controlOut.writeChars("125	Data connection already open; transfer starting.\n");
				controlOut.flush();
			}
			new Thread (new Runnable() {
				public void run() {
					try {
						isSending = true;
						byte[] buffer = new byte[1];
						OutputStream byteWriter = dataSoc.getOutputStream();				
						FileInputStream in = new FileInputStream(sendFile);
						while (in.available() > 0){
							in.read(buffer);
							byteWriter.write(buffer);
						}
						in.close();
						byteWriter.close();
						sendControlMessage("226 Closing data connection, transfer complete\n");
						dataSoc.close();
						isSending = false;
					} 
					catch (IOException e) {
						isSending = false;
					}						
				}
			}).start();
		} catch (Exception e) {
			isSending=false;
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
		this.handle = input.replace("USER", "").trim();	
		controlOut.writeChars("331 Username Logged, please provide password now via \"PASS <sp> <username>\"\n");
		controlOut.flush();
		System.out.println(this.handle + " has connected but not provided a password");
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
		if (kettle.running && this.running && kettle.checkPassword(input.substring(4).trim())) {
			this.acceptence = true;
			this.loginTime = new Date();
			while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence));
			controlOut.writeChars("230 Password Accepted. User logged in at" + this.loginTime + "\n");
			controlOut.flush();
			System.out.println(this.handle + " has gained attmidentence " + this.loginTime);
			return;
		}
		else {
			controlOut.writeChars("530 That is not the password\n");
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
			controlOut.writeChars("150 File status okay; about to open data connection \n");
			controlOut.flush();
		}
    controlOut.writeChars("Directory Name "+this.curDirName);
		controlOut.flush();
//    ArrayList<String> files = new ArrayList<String>(Arrays.asList(curDir.list()));
//    for (int i =0 ; i < files.size(); i++) {
//    	controlOut.writeChars(files.get(i));
    	controlOut.flush();
    }
//	}

	/**
	 * This function prints the address for the current directory
	 * 
	 * @author Teagan Atwater
	 * @since Alpha (04/24/14)
	 * @exception IOException
	 */
	private void pwd(String input) throws IOException {
		controlOut.writeChars("257 \"" + this.parentDir.getName() + "\" is the current directory.\n");
		controlOut.flush();
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
	public boolean shutThingsDown(int printKick) {
			this.running = false;
			try {
				if (printKick == 1) {
					sendControlMessage("221 I regret to inform you that this tea "
						+ "pary has come to an end.\rSafe travels, and please come again\n");
				}
				else if (printKick == 2) {
					sendControlMessage("10068 I regret to inform you that this tea pary is "
							+ "full.\rThere is no room for other users\n");
				}
				this.controlSoc.shutdownInput(); // closes client inputStream, allows this.run() to end
				controlIn.close();                       // closes input reader
				controlOut.close();                      // closes output writer
				controlSoc.close();              // closes socket on port
				if (this.isSending) {
					new Thread (new Runnable(){
						public void run() {
							while(isSending);
							try {
								dataSoc.close();
							} catch (IOException e) {
							}
						}
					}).start();
				}
				return true;
			}
			catch (IOException e) {
				return false;
			}
		}
}
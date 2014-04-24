package us.texastony.EarlGray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import us.texastony.EarlGray.EGClientInst;
 

/*Authors: Tony Knapp, Teagan Atwater, Jake Junda
//Started on: April  3, 2014
//A caffeinated FTP server.
//
//This FTP server allows multiple clients to connect to the server by providing
//a user handle and the server's universal password (Earl Gray). The server uses 
//multi-threading to handle multiple clients connecting at one time

//To login from a local machine: 	ftp 127.0.0.1 [port]
 * [port] is outputed to the console at start of server.

//To view the process, run it, and then: ps -A |grep EarlGray
//To view the resources it is taking up, take the PID from the above, 
//and check it out on some proccess manager
 * 
 */

public class EarlGray extends Thread {

	/**
	 * A very simple FTP server with a caffinated theme!
	 * 
	 * This class creates a FTP server. It allows multiple
	 * clients to connect to the server by providing a user handle and the server's
	 * universal password (EarlGray). The server uses multi-threading to handle
	 * multiple clients connecting at one time. User's Handle (teacup) and login time are
	 * recorded to the log. The class cleanly quits all client instances and itself
	 * via the <code>.stopServer()</code> method.
	 * 
	 * @author Tony Knapp
	 * @version Beta (4/24/2014)
	 * @since Alpha (4/03/2014)
	 */
	private static int CNT_FTP_PORT;               		// server port number
	private ArrayList<EGClientInst> clientInstList; // store running sessions
	private ServerSocket incoming;                  // socket that the server listens to
	final private String password = "EarlGray";
	private File inFile = new File("log.txt");
	private File directory;
	private PrintWriter out;
	public boolean running = true;
	private static int USER_LIMIT = 10;
	
	/**
	 * This function takes in the port number
	 *  and directory to be shared
	 *  and creates an instance of
	 * EarlGray on the given port
	 * 
	 * @author Tony Knapp
	 * @param
	 * @since Alpha (04/03/14)
	 * @exception IOException
	 */
	public EarlGray(int port, String directoryPath) throws IOException {
		this.clientInstList = new ArrayList<EGClientInst>();
		this.directory=new File(directoryPath);
		if(!this.directory.isDirectory()) {
			this.directory.mkdir();
		}

		try {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter(inFile, true)));
			this.incoming = new ServerSocket(port); // create server socket on designated port
			CNT_FTP_PORT = incoming.getLocalPort();
		} catch (IOException e) {
			e.printStackTrace();                  // print error stack
		}
	}
	
	/**
	 * This function waits for clients to
	 *  connect to the server, then creates a
	 * socket and stores the new client session
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/03/2014)
	 */
	public void run() {
		System.out.println("The kettle is hot on: " + CNT_FTP_PORT + "!");
		System.out.println("Type \"quit\" to exit, or \"port\" to display the Server's port.");
		boolean running = true;
		try {
			while (running) {
				Socket clientSoc = incoming.accept();                            // wait for new connection
				EGClientInst clientInst = new EGClientInst(clientSoc, this, this.directory); // create new session on socket
				if (clientInstList.size() > USER_LIMIT){
					clientInst.shutThingsDown(2);
				}
				else {
					clientInstList.add(clientInst);                                      // store new client session
					clientInst.start();                                              // starts client thread
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();                                             // print error stack
		}
	}
	
	/**
	 * This function takes a string and returns true if the string is the server's
	 * password, false otherwise
	 * 
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @param
	 * @return boolean
	 * @since Alpha (03/04/2014)
	 */
	public boolean checkPassword(String attempt) {
		if (attempt.equals(this.password) && this.running) {
			return true;
		} 
		else {
			return false;
		}
	}
	
	/**
	 * This Function writes a client's details to the log file.
	 * 
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @param
	 * @return true
	 * @since Alpha (04/04/2014)
	 */
	public boolean logLogIn(String handle, Date date, boolean acceptance) {
		out.print(handle + "\t" + date + "\t" + acceptance +"\n");
		return true;
	}
		
	/**
	 * This Function writes a client's 
	 * file transaction details to a log.
	 * 
	 * @author Tony Knapp
	 * @param
	 * @return true
	 * @since Alpha (04/21/2014)
	 */
	public boolean logTransfer(String handle, Date date, String command) {
		out.print(handle + "\t" + date + "\t" + command +"\n");
		return true;
	}

	/**
	 * This function removes the client from the sessions
	 * 
	 * @author Teagan Atwater
	 * @author Tony Knapp
	 * @author Jake Junda
	 * @
	 * @param
	 * @return true
	 * @since Alpha
	 */
	public boolean terminateSession(EGClientInst session) {
		clientInstList.remove(session); // remove the session from the active session list
		return true;
	}
	
	/**
	 * This function closes down all user sessions and the whole server
	 * 
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha
	 * @exception IOException
	 */
	private boolean stopServer() throws IOException {
		this.running = false;
		for (EGClientInst client : clientInstList) {
			while (!client.shutThingsDown(1)); // wait for the client to shut down before proceeding
		}
		out.close();
		System.out.println("All client sessions have been terminated.\nStopping server.");
		try {
			this.join(100);                    // let the thread die
		} catch (InterruptedException e) {
			e.printStackTrace();               // print error stack
		}
		return true;
	}
	
	/**
	 * This function gets the port number
	 * stored in the int CNT_FTP_PORT
	 * 
	 * @author Jake Junda
	 * @param int
	 * @return int
	 * @since Alpha(4/23/2014)
	 */
	protected static int getPortNum(){
			return CNT_FTP_PORT;
	}
	
		/**
		 * The main function intiates the server
		 * and handles the server user's input.
		 * The main function will close the server, and then
		 * JVM before exiting.
		 * 
		 * @author Tony Knapp
		 * @author Jake Junda
		 * @param
		 * @since Alpha
		 * @throws Exception
		 */
		public static void main(String[] args) throws Exception {
			boolean portFlag = false;
			int portNumber = 20;
			boolean directoryFlag = false;
			String directoryName = "/Users/" + System.getProperty("user.name") + "/Desktop/Share";
			Scanner in = new Scanner(System.in);                               // initialize scanner
			String text;                                 // read user input 
			if (args.length > 0) {
				for (int i = 0; i <= args.length; i++) {
					if (args[i].trim().equals("-p")){
						if (args[i+1].matches("^([-+] ?)?[0-9]+(,[0-9]+)?$")){
							if (Integer.parseInt(args[i+1]) <= 65535) {
								portFlag = true;
								portNumber = Integer.parseInt(args[i+1]);
								i = i + 2;
							}
							else{
								System.out.println("Bad port argument. Must be less than 65535.");
								System.exit(2);
							}						
						}
					}
					else if (args[i].trim().equals("-d")){
						if (args[i + 1].startsWith("/") || args[i + 1].startsWith("C://")) {
							directoryFlag = true;
							directoryName = args[i + 1];
						}
						else {
							System.out.println("Bad directory argument. Must be an absolute path");
							System.exit(2);
						}
					}
				}
			}
			if (portFlag == false) {
				System.out.println("Missing port argument.\n"
						+ "Default is 20, return nothing for default.\n"
						+ "Or Return 0 for to have a port automically assigned.\n"
						+ "What port would like the control on?");
				text = in.nextLine();
				if (text.isEmpty()) {
					portFlag = true;
					portNumber = 20;
				}
				else if (Integer.parseInt(text) <= 65535) {
					portFlag = true;
					portNumber = Integer.parseInt(text);
				}
				else{
					System.out.println("Bad port argument. Must be less than 65535.");
					System.exit(2);
				}
			}
			if (directoryFlag == false){
				System.out.println("Missing directory argument!\n"
						+ "The default folder is ~/Desktop/Share. Return nothing for default.\n"
						+ "Please provide the absolute path to the directory "
						+ "you would like to share:");
				text = in.nextLine();
				if (text.startsWith("/") || text.startsWith("C://")) {
					directoryFlag = true;
					directoryName = text;
				}
				else if (text.isEmpty()) {
					directoryFlag = true;
				}
				else {
					System.out.println("Bad directory argument. Must be an absolute path");
					System.exit(2);
				}
			}
			if (directoryFlag == true && portFlag == true) {
				EarlGray server = new EarlGray(portNumber, directoryName);           // creates an instance server class
				server.start();                                                    // starts the server 
				text = in.nextLine();      
				while (text != null && !text.trim().equalsIgnoreCase("quit")) { // if the server user does NOT quit
					if (text.trim().equalsIgnoreCase("port")) {
							int pNum = getPortNum();
							System.out.println("The port number you should connect to is "+pNum);
					}
					text = in.nextLine();                                          // let the server user type again
				}                                                                  // else begin closing things
				while (!server.stopServer());                                      // wait for the server.stopServer() to return true
				in.close();      // close the Scanner
			}                                                  
			System.exit(0);                                                    // shutdown the JVM
		}                                                                    
	}
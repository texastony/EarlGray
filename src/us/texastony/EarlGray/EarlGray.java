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

//TODO 10068 - too many users, server is full             


/*Authors: Tony Knapp, Teagan Atwater, Jake Junda
//Started on: April  3, 2014
//A caffeinated FTP server.
//
//This FTP server allows multiple clients to connect to the server by providing
//a user handle and the server's universal password (Earl Gray). The server uses 
//multi-threading to handle multiple clients connecting at one time

//To login from a local machine: 	ftp 127.0.0.1 5217

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
	 * @version Alpha (4/03/2014)
	 * @since Alpha (4/03/2014)
	 */
	private static int CNT_FTP_PORT;               		// server port number
	private static int DATA_FTP_PORT;
	private ArrayList<EGClientInst> clientInstList; // store running sessions
	private ServerSocket incoming;                  // socket that the server listens to
	final private String password = "EarlGray";
	private File inFile = new File("log.txt");
	private String directoryPath;
	private File directory;
	private PrintWriter out;
	public boolean running = true;
	
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
		CNT_FTP_PORT = port;
		DATA_FTP_PORT = port + 1;
		this.clientInstList = new ArrayList<EGClientInst>();
		this.directory=new File(directoryPath);
		
		if(!this.directory.exists()) {
			this.directory.mkdir();
		}

		try {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter(inFile, true)));
			this.incoming = new ServerSocket(CNT_FTP_PORT); // create server socket on designated port
		} catch (IOException e) {
			e.printStackTrace();                  // print error stack
		}
	}
	
	/**
	 * This function waits for clients to connect to the server, then creates a
	 * socket and stores the new client session
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/03/2014)
	 */
	public void run() {
		System.out.println("The kettle is hot on: " + CNT_FTP_PORT + "!");
		System.out.println("Type \"This tea is cold...\" to quit, or just relax and let these good people enjoy their tea.");
		boolean running = true;

		try {
			while (running) {
				Socket clientSoc = incoming.accept();                            // wait for new connection
				EGClientInst clientInst = new EGClientInst(clientSoc, this, this.directory); // create new session on socket
				clientInstList.add(clientInst);                                      // store new client session
				clientInst.start();                                              // starts client thread
			}
		} catch (IOException e) {
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
	
	//TODO NEED TO ADD LOGGING FOR FILE HANDLING. IE: they downloaded x file, or they uploaded y file...

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
			EarlGray server = new EarlGray(5217, "~/Desktop/Share");           // creates an instance server class
			//TODO add parsing of args for server parameters
			//TODO if args are not given, provide text prompts
			server.start();                                                    // starts the server 
			Scanner in = new Scanner(System.in);                               // initialize scanner
			String text = in.nextLine();                                       // read user input 
			while (text != null &&
					!((text.trim().equalsIgnoreCase("quit")) || (text.trim().equalsIgnoreCase("This tea is cold")))) { // if the server user does NOT quit
				if (text.trim().equalsIgnoreCase("help") || text.trim().equalsIgnoreCase("?")) {
					//TODO add help menu
				}
				text = in.nextLine();                                          // let the server user type again
			}                                                                  // else begin closing things
			while (!server.stopServer());                                      // wait for the server.stopServer() to return true
			in.close();                                                        // close the Scanner
			System.exit(0);                                                    // shutdown the JVM
		}                                                                    
	}
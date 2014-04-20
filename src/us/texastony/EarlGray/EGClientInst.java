package us.texastony.EarlGray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
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
	private Socket dataSoc; //After looking at other examples, it appears that nobody creates another port for data besides rfc...
	private BufferedReader dIn; //Because it does not make sense to me, how will the client know which socket to connect to unless I tell it
	private DataOutputStream dOut; //I would have to have complete access to the client, or implement more of the FTP protocol than Bo Du indicated...
	private String handle;
	private String password;
	private Date loginTime;
	private EarlGray kettle;
	private String curDirName = "Server/";
	private File parentDir;
	private File curDir;
	public boolean acceptence = false;
	private boolean running = true;
	
	/**This constructs a Thread that
	 * handles the client's connections. 
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 * @param Socket, Server
	 */
	public EGClientInst(Socket cSoc, EarlGray server, File parentFolder ) throws IOException {
		this.controlSoc = cSoc; // attach to client socket
		this.dIn = new BufferedReader(new InputStreamReader(controlSoc.getInputStream()));
		this.dOut = new DataOutputStream(controlSoc.getOutputStream());
		this.kettle = server;
		this.parentDir = parentFolder;
		this.curDir = this.parentDir;

		System.out.println("A new guest has Conncected\nAwaiting Username and Password");
	}
	
	/**
	 * This function handles the clients connection. The client user is given
	 * three attempts to enter the server's password. If the client user fails, he
	 * is booted. In either case, the data is logged. </P> If this thread was
	 * constructed using a separate Runnable run object, then that Runnable
	 * object's run method is called; otherwise, this method does nothing and
	 * returns.
	 * 
	 * @author Tony Knapp
	 * @author Teagan Atwater
	 * @author Jake Junda
	 * @since Alpha (04/04/2014)
	 */
	public void run() {
	//TODO add File Transfer features...	
		
	//Server return codes:                                    
	//TODO 200 - boolean True                                 
	//TODO 550 - requested action not taken           
		
	//FTP commands that need to be implemented   
  //TODO  USER -- First command sent by the client
	//TODO  QUIT -- closes the control connection IF there is no file being sent
	//TODO  PORT -- There are defaults, but this command will change where the data is being sent, this is going to be a challenge
	//TODO  TYPE -- For EarlGray, if this is not A T (ASCII Telnet), or L followed by any int, we don't support it
	//TODO  MODE -- We could do any of the three, Stream would be the easiet
	//TODO  STRU -- We only handle File, or F, so if they follow up with anything else, we say no
	//TODO  RETR -- Command specifies a file to be sent to the connect data port
	//TODO  STOR -- This is how a client stores data to the server, we don't have to do this one
	//TODO  NOOP -- Do nothing but send an OK reply
	//TODO  GET  -- Retrieve a file, this is actually RETR                            
	//TODO  LIST -- or LSList files & directories in current dirName
  //TODO  PASS -- the password for a user, Likely the second command from the lcient

		try {
			this.loginTime = new Date();
			greetGuest();

			if (this.running) {
				dOut.writeChars("This is a private Tea Party. Do you know the password?\n");
			}

			if (tryAgain(3) && this.running) {
				this.acceptence = true;
				while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence)) {
				}
			} else if (this.running) {
				dOut.writeChars("Ah... you do not know the password. Please leave.\n");
				while (!kettle.logLogIn(this.handle, this.loginTime, this.acceptence));
				quit();
				return;
			}

			if (this.running) {
				String text = dIn.readLine(); // takes user input and logs it
				while (text != null && !((text.trim().equalsIgnoreCase("EXIT")) || (text.trim().equalsIgnoreCase("this tea is cold")))	&& this.running) {
					text = dIn.readLine();
					if (text.trim().equalsIgnoreCase("LS") || text.trim().equalsIgnoreCase("list")) {
						dOut.writeChars("[200]");
						dOut.flush();
						menu();
					}
					else if (text.trim().equalsIgnoreCase("PWD") || text.trim().equalsIgnoreCase("path")) {
						dOut.writeChars("[200] Directory Path: " + curDirName + "\n");
						dOut.flush();
					}
					else if (text.trim().equalsIgnoreCase("GET")) {
						dOut.writeChars("[200] Please enter the file name you wish to download: ");
						String reqFile = dIn.readLine();
						try {
							//TODO locate file if it exists
						} catch (Exception e) {
							e.printStackTrace();
						}
						//TODO send file
						//TODO log file transfer to kettle
					}
					else {
						dOut.writeChars("[550]");
						dOut.flush();
						dOut.writeChars("[502] Command not implemented");
						dOut.flush();
					}
				}
			}
			if (this.running) {
				dOut.writeChars("[221], Have a wonderful day.\n");
				dOut.flush();
				quit();
			}
		} catch (IOException e) {
			e.printStackTrace(); // print error stack
		}
	}

	
	/**
	 * This Function communicates with the client user, Receives their input,
	 * stores them to the Session's class attributes
	 * 
	 * @author Tony Knapp
	 * @since Alpha (04/04/2014)
	 */
	private void greetGuest() throws IOException {
		if (this.running) {
			dOut.writeChars("Good day! Before you have some tea, \n we must know if you are on the guest list. \n May I have your name?\n");
			dOut.flush();
			this.handle = dIn.readLine();
			if (!this.handle.contains("USER")) {
				dOut.writeChars("First command must be USER <username>");
				dOut.flush();				
			}
			else {
				this.handle = this.handle.replace("USER", "");
				
			}
			System.out.println("Is " + this.handle + " on the guest list?");
		}
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
			return false;
		} 
		else if (this.running) {
			dOut.flush();
			this.password = dIn.readLine();
			if (kettle.running && kettle.checkPassword(password) && this.running) {
				dOut.flush();
				this.loginTime = new Date();
				dOut.writeChars("[230] Excellent, " + this.handle + "! " + this.loginTime + "\n I will fetch the tea trolly.\n");
				dOut.flush();
				System.out.println(this.handle + " has gained attmidentence " + this.loginTime);
				return true;
			} 
			else if (this.running) {
				if (count>1){
					dOut.flush();
					dOut.writeChars("Ah, you see, that is not the password. Please try again: \n");
					dOut.flush();
					System.out.println(this.handle + " don't know the password!");
				}
				return tryAgain(count - 1);
			}
		}
		return false;
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
	private void menu() throws IOException {
	    dOut.writeChars("Directory Name "+this.curDirName);
			dOut.flush();
	    ArrayList<String> files = new ArrayList<String>(Arrays.asList(curDir.list()));
	    for (int i =0 ; i < files.size(); i++) {
	    	dOut.writeChars(files.get(i));
	    	dOut.flush();
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
			dOut.writeChars("[221] I regret to inform you that this tea pary has come to an end. \n Safe travels, and please come again.");
			dOut.flush();
		}
		this.controlSoc.shutdownInput(); // closes client inputStream, allows this.run() to end
		dIn.close();                       // closes input reader
		dOut.close();                      // closes output writer
		controlSoc.close();              // closes socket on port
		return true;
	}
}

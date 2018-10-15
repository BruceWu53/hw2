package mail;

import java.util.Scanner;

/**
   Connects a phone to the mail system. The purpose of this
   class is to keep track of the state of a connection, since
   the phone itself is just a source of individual key presses.
*/
public class Connection
{
   /**
      Construct a Connection object.
      @param s a MailSystem object
      @param p a Telephone object
   */
   public Connection(MailSystem s, Telephone p)
   {
      system = s;
      phone = p;
      resetConnection();
   }

   /**
      Respond to the user's pressing a key on the phone touchpad
      @param key the phone key pressed by the user
   */
   public void dial(String key)
   {
      if (state == CONNECTED)
         connect(key);
      else if (state == RECORDING)
         login(key);
      else if (state == CHANGE_PASSCODE)
         changePasscode(key);
      else if (state == CHANGE_GREETING)
         changeGreeting(key);
      else if (state == MAILBOX_MENU)
         mailboxMenu(key);
      else if (state == MESSAGE_MENU)
         messageMenu(key);
   }

   /**
      Record voice.
      @param voice voice spoken by the user
   */
   public void record(String voice)
   {
      if (state == RECORDING || state == CHANGE_GREETING)
         currentRecording += voice;
   }

   /**
      The user hangs up the phone.
   */
   public void hangup()
   {
      if (state == RECORDING)
         currentMailbox.addMessage(new Message(currentRecording));
      resetConnection();
   }

   /**
      Reset the connection to the initial state and prompt
      for mailbox number
   */
	public int selectFirstMenu() {
		Scanner input = new Scanner(System.in);
		int key = 0;
		phone.speak("To leave a message, press (1), to access your mailbox, press (2)");
		while (input.hasNext()) {
			if (input.hasNextInt() ) {
				key = input.nextInt();
				break;
			} else {
				input.next();
				phone.speak("To leave a message, press (1), to access your mailbox, press (2)");
			}
		}
		if (key == 1) {
			key = 0;
			return 1;
		} else if (key == 2) {
			key = 0;
			return 2;
		} else if (key != 1 && key != 2) {
			key = 0;
			selectFirstMenu();
		}
		key = 0;
		return 0;

	}

	void resetConnection()
   {
      currentRecording = "";
      accumulatedKeys = "";
      int choice = selectFirstMenu();
      if (choice == 1) {
      connect("hello");
      }
      else if (choice == 2) {
    	  state = MAILBOX_MENU;
    	  phone.speak(MAILBOX_MENU_TEXT);
      }
      else if (choice == 0) {
    	  System.out.println("Invalid Inputs");
    	  resetConnection();
      }
      else {
    	  System.out.println("result of selectFirstMenu(): "+ choice);
    	  resetConnection();
      }
   }

   /**
      Try to connect the user with the specified mailbox.
      @param key the phone key pressed by the user
   */
	   private void connect(String keys)
	   {
			Scanner input = new Scanner(System.in);
			int key = 0;
			phone.speak("Enter mailbox number");
			while (input.hasNext()) {
				if (input.hasNextInt() ) {
					key = input.nextInt();
					break;
				} else {
					input.next();
					phone.speak("Invalid Mailbox number, please enter a valid mailbox number: ");
				}
			}
			currentMailbox = system.findMailbox(String.valueOf(key));
	         if (currentMailbox != null)
	         {
	            state = RECORDING;
	            phone.speak(currentMailbox.getGreeting());
	         }
	         else
	            phone.speak("Incorrect mailbox number. Try again!");
	      }
	   

   /**
      Try to log in the user.
      @param key the phone key pressed by the user
   */
   private int check = 0;
   private void login(String key)
   {
	   if (check == 0) {phone.speak("Please enter the passcode, then press the # key"); }
      if (key.equals("#") && check >= 1)
      {
         if (currentMailbox.checkPasscode(accumulatedKeys))
         {
            state = MAILBOX_MENU;
            phone.speak(MAILBOX_MENU_TEXT);
         }
         else {
            phone.speak("Incorrect passcode. Try again!"); }
         accumulatedKeys = "";
      }
      else
         accumulatedKeys += key;
      	if (check == 0) { accumulatedKeys = ""; } 
      	 check++;
   }

   /**
      Change passcode.
      @param key the phone key pressed by the user
   */
   private void changePasscode(String key)
   {
      if (key.equals("#"))
      {
         currentMailbox.setPasscode(accumulatedKeys);
         state = MAILBOX_MENU;
         phone.speak(MAILBOX_MENU_TEXT);
         accumulatedKeys = "";
      }
      else
         accumulatedKeys += key;
   }

   /**
      Change greeting.
      @param key the phone key pressed by the user
   */
   private void changeGreeting(String key)
   {
      if (key.equals("#"))
      {
         currentMailbox.setGreeting(currentRecording);
         currentRecording = "";
         state = MAILBOX_MENU;
         phone.speak(MAILBOX_MENU_TEXT);
      }
   }

   /**
      Respond to the user's selection from mailbox menu.
      @param key the phone key pressed by the user
   */
   private void mailboxMenu(String key)
   {
      if (key.equals("1"))
      {
         state = MESSAGE_MENU;
         phone.speak(MESSAGE_MENU_TEXT);
      }
      else if (key.equals("2"))
      {
         state = CHANGE_PASSCODE;
         phone.speak("Enter new passcode followed by the # key");
      }
      else if (key.equals("3"))
      {
         state = CHANGE_GREETING;
         phone.speak("Record your greeting, then press the # key");
      }
   }

   /**
      Respond to the user's selection from message menu.
      @param key the phone key pressed by the user
   */
   private void messageMenu(String key)
   {
      if (key.equals("1"))
      {
         String output = "";
         Message m = currentMailbox.getCurrentMessage();
         if (m == null) output += "No messages." + "\n";
         else output += m.getText() + "\n";
         output += MESSAGE_MENU_TEXT;
         phone.speak(output);
      }
      else if (key.equals("2"))
      {
         currentMailbox.saveCurrentMessage();
         phone.speak(MESSAGE_MENU_TEXT);
      }
      else if (key.equals("3"))
      {
         currentMailbox.removeCurrentMessage();
         phone.speak(MESSAGE_MENU_TEXT);
      }
      else if (key.equals("4"))
      {
         state = MAILBOX_MENU;
         phone.speak(MAILBOX_MENU_TEXT);
      }
   }

   private MailSystem system;
   private Mailbox currentMailbox;
   private String currentRecording;
   private String accumulatedKeys;
   private Telephone phone;
   private int state;

   private static final int DISCONNECTED = 0;
   private static final int CONNECTED = 1;
   private static final int RECORDING = 2;
   private static final int MAILBOX_MENU = 3;
   private static final int MESSAGE_MENU = 4;
   private static final int CHANGE_PASSCODE = 5;
   private static final int CHANGE_GREETING = 6;

   private static final String INITIAL_PROMPT = 
         "Enter mailbox number followed by #";      
   private static final String MAILBOX_MENU_TEXT = 
         "Enter 1 to listen to your messages\n"
         + "Enter 2 to change your passcode\n"
         + "Enter 3 to change your greeting";
   private static final String MESSAGE_MENU_TEXT = 
         "Enter 1 to listen to the current message\n"
         + "Enter 2 to save the current message\n"
         + "Enter 3 to delete the current message\n"
         + "Enter 4 to return to the main menu";
}

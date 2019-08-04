package appServer;

import java.io.*;
import data.Data;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultListModel;
import AppClient.Main;

/**
 * The server for the chat application. It allows multiple users to connect and text each other using 
 * Sockets and ThreadPools.
 * @author Siddharth
 *
 */
public class appMain{
  
  /**
   * The parameter that stores the port for the server.
   */
  public static final int PORT = 59008;
  
  /**
   * A HashSet which stores the names of all the users registered to use the app.
   */
  public static HashSet<String> names = new HashSet<String>();
  
  /**
   * A HashMap that stores the Socket of each user along with their username as a key value.
   */
  public static HashMap<String, Socket> clients = new HashMap<String, Socket>();
  
  /**
   * A HashMap that stores each users OutputStream along with their username as a key value.
   */
  public static HashMap<String, ObjectOutputStream> map = new HashMap<>();
  
  /**
   * This class Handles the sockets connection to the Server.
   * @author Siddharth
   *
   */
  public static class Handler extends Thread{
    
    public static class Control {
      public volatile static boolean flag = false;
      
      public static void setFlag(boolean flag) {
        Control.flag = flag;
      }
      
      public void updateFlag() {
        if(Main.stopCapture == true) {
          Control.setFlag(true);
        }
        return;
      }
    }
    
    /**
     * Stores the socket of the client connecting to the server
     */
    private Socket socket;
    
    /**
     * Creates an ObjectStream that can read Object inputs sent over the socket from the connected client to the server.
     */
    private ObjectInputStream in;
    
    /**
     * Creates an ObjectStream that can send Objects from the server to the connected client.
     */
    private ObjectOutputStream out;
    
    /**
     * ObjectOutputStream that gets the OutputStream of the receiving client.
     */
    private ObjectOutputStream friendOut;
    
    /**
     * InputStream that carries the voice input from the connected client to the server
     */
    private InputStream voiceIn;
    
    /**
     * OutputStream that carries the voice output from server to clients.
     */
    private OutputStream voiceOut;
    
    /**
     * A line that can store the audio for the target.
     */
    private TargetDataLine targetDataLine;
    
    /**
     * The audio format of the data line for the stream.
     */
    private AudioFormat audioFormat;
    
    /**
     * The source of the audio is stored in this data line.
     */
    private SourceDataLine sourceDataLine;
    
    /**
     * The buffer for the audio stream.
     */
    byte tempBuffer[] = new byte[10000];
    
    //public volatile boolean stopCapture;
    
    Control control = new Control();
    
    /**
     * Stores and access all the mixers installed on the server's system.
     */
    private static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    
    /**
     * Constructor method for the Handler class.
     * @param socket - Client's socket when connecting to server
     */
    public Handler(Socket socket) {
      this.socket = socket;
    }
    
//    public void updateStopCapture() {
//      this.stopCapture = Main.atomicStopCapture.get();
//      System.out.println("updateStopCapture() called: " + stopCapture);
//    }
    
    
    /** (non-Javadoc)
     * @see java.lang.Thread#run()
     * Creates the run() method as the Handler class extends Thread. Handles all of the server functions.
     */
    public void run() {
      String name; //Creates a variable that will store the client user's name
      //Start of a repeating loop in the run method, to keep the server continuously running. 
      while(true) {
        try {
          //Get the outputstream for the connected socket
          out = new ObjectOutputStream(socket.getOutputStream());
          //Get the inputstream for the connect socket
          in = new ObjectInputStream(socket.getInputStream());
          //This loop starts the servers connection with the client
          while(true) {
            //Sends a name submission request to the client
            out.writeObject("SUBMIT");
            name = (String) in.readObject();
            //if name returned is null, keeps prompting
            if (name == null){
              return;
            }
            //inserts name and associated socket into the HashMap
            clients.put(name, socket);
            //inserts name and associated output stream into the HashMap.
            map.put(name, out);
            //Send out a request to get connection success response from the client
            while(true) {
              out.writeObject("CONNECTED");
              String read = (String) in.readObject();
              if(read.equals("SUCCESS")) {
                break; //Break out of connection success loop
              }
            }
            break; //Break out of server-client connection creation loop
          }
          
          //Start of loop that processes requests from the client
          while(true) {
            try {
              //Reads the input send by the client 
              in = new ObjectInputStream(socket.getInputStream());
              Object inputMsg = in.readObject();
              //Re-prompts in null input
              if(inputMsg == null) {
                continue;
              }
              //calls checkMsgType() method to take required action before re-prompting for input
              checkMsgType((DefaultListModel<Object>) inputMsg, name);
            }
            catch(Exception e) {
              //System.err.println("FAILED: Request processing loop.");
              //Logger.getLogger(appMain.class.getName()).log(Level.SEVERE, null, e);
            }
          }
        }
        catch(Exception e) {
          System.err.println("FAILED: run().");
          Logger.getLogger(appMain.class.getName()).log(Level.SEVERE, null, e);
        }
      }
    }
    
    /**
     * Method to check the type of request sent by the client and call the appropriate method to
     * deal with it.
     * @param input - The input sent by the client.
     * @param sender - The sender that the server assumes the request is sent by (checked later)
     */
    private void checkMsgType(DefaultListModel input, String sender) {
      //The client sends data in a ListModel format, where the element at 0 stores the type of the request.
      switch((int) input.elementAt(0)) {
        case RequestType.SEND_MSG:
          sendMessage(input, sender);
          break;
        case RequestType.SEND_FILE:
          sendFile(input, sender);
          break;
        case RequestType.CALL:
          voiceCall();
          break;
        case RequestType.LOGOUT:
          Logout(input, sender);
          break;
        default:
          break;
      }
    }
    
    /**
     * This method processes the clients request to send Files over the connection to another 
     * user.
     * @param input - Sent by the client to the server
     * @param sender - The user who sent the request to the server
     */
    private void sendFile(DefaultListModel input, String sender) {
      //Gets the name of the client user from input
      String user = (String) input.elementAt(2);
      //Gets the data that the client send
      Data data = (Data) input.elementAt(1);
      //Gets the list of the friends that the client wants to send the information too
      DefaultListModel<String> friendList = (DefaultListModel<String>) input.elementAt(3);
      //the loop iterates through all the friends that the user wants to send the data too
      for(int i = 0; i < friendList.size(); i++) {
        //Gets the friends name
        String friendName = friendList.elementAt(i);
        //Gets the outputstream from the HashMap
        friendOut = map.get(friendName);
        //Checks if the request is sent by the user the server thinks it is sent by and
        //that the outputstream of the friends is not null
        if(user.equals(sender) && friendOut != null) {
          try {
            //Creates the new listmodel to send to the receiving users
            DefaultListModel model = new DefaultListModel();
            //At index 0 -> Add the type of request 
            model.addElement(RequestType.SEND_FILE);
            //At index 1 -> Add the data
            model.addElement(data);
            //At index 2 -> Add the name of the sender
            model.addElement(sender);
            //Get the new OutputStream for the friend receiving the Model
            friendOut = new ObjectOutputStream((clients.get(friendName)).getOutputStream());
            //Send the Model
            friendOut.writeObject(model);
            //flush the stream
            friendOut.flush();
            System.out.println("Send file successful!");
          }
          catch (IOException e){
            System.err.println("FAILED TO SEND FILE");
            Logger.getLogger(appMain.class.getName()).log(Level.SEVERE, null, e);
          }
        }
      }
    }
    
    /**
     * This method processes the clients request to send messages to one or more friends over
     * the connection.
     * @param input - Sent by the client to the server
     * @param sender - The user who sent the request to the server
     */
    private void sendMessage(DefaultListModel input, String sender) {
      //From the input extract the user, messages and friends to send message to
      String user = (String) input.elementAt(2);
      String message = (String) input.elementAt(1); 
      DefaultListModel<String> friendList = (DefaultListModel<String>) input.elementAt(3);
      //iterate through the list of friends, sending the message to each one
      for(int i = 0; i < friendList.size(); i++) {
        String friendName = friendList.elementAt(i);
        //From the HashMap get the friend's outputstream
        friendOut = map.get(friendName);
        //If the user that sent the request is same as the user that the server thinks send the request and
        //if the outputstream is not null, send the message to the friend 
        if(user.equals(sender) && friendOut != null) {
          try {
            //Creates a new Model to send to the friend
            DefaultListModel model = new DefaultListModel();
            model.addElement(RequestType.SEND_MSG);
            model.addElement(message);
            model.addElement(sender);
            //Gets a new instance of ObjectOutputStream from the socket and sends the message
            friendOut = new ObjectOutputStream((clients.get(friendName)).getOutputStream());
            friendOut.writeObject(model);
            friendOut.flush();
            System.out.println("Send message successful!");
          }
          catch (IOException e){
            System.out.println("FAILED TO SEND");
            Logger.getLogger(appMain.class.getName()).log(Level.SEVERE, null, e);
          }
        }
      }
    }
    
    /**
     * This method is called when the client wants to start a voice call over the connection.
     */
    private void voiceCall() {
      try {
        //Use the AudioSystem class again to get the mixers installed on this device
        Mixer mixer_ = AudioSystem.getMixer(mixerInfo[0]);
        //Gets the audio format of the voice connection by calling another method that specifies the details
        audioFormat = getaudioformat();
        //Get the specification for the dataline 
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        //Using the information of the specification from above, Create an instance of SourceDataLine
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        //Open the sourceDataLine for getting an input an start it
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        //After starting the source line, call the capture audio method
        captureAudio();
        //Create new instance of the Voice input and output streams
        voiceIn = new BufferedInputStream(socket.getInputStream());
        voiceOut = new BufferedOutputStream(socket.getOutputStream());
        //keeps reading for voice input over the connection until the stopCapture boolean has been set to true
        while (Control.flag == false) {
          control.updateFlag();
          if(Control.flag == true) {
            break;
          }
          else {
            if(voiceIn.read(tempBuffer) != -1) {
              sourceDataLine.write(tempBuffer, 0, tempBuffer.length);
            }
          }
        }
        System.out.println("STOP CALL METHOD");
        voiceIn.close();
        voiceOut.close();
        sourceDataLine.close();
        return;
    } catch (IOException e) {
      //TODO
      e.printStackTrace();
    } catch (LineUnavailableException e) {
      // TODO
      e.printStackTrace();
    }
    }
    
    /**
     * This method handles the clients request to log out of the application
     * @param input - Sent by the client
     * @param sender - The user that sent the request (acc. to the server)
     */
    private void Logout(DefaultListModel input, String sender) {
      System.out.println("Client\"" + sender +"\" just logged out!\r");
      Thread.currentThread().interrupt();
    }
    
    /**
     * This method is responsible for creating an AudioFormat for the voice chat.
     * @return
     */
    private AudioFormat getaudioformat() {
      float sampleRate = 8000.0F;
      int sampleSizeInBits = 8;
      int channel = 2;
      boolean signed = true;
      boolean bigEndian = false;
      return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }
    
    /**
     * 
     */
    private void captureAudio() {
      try {
          audioFormat = getaudioformat();
          DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
          Mixer mixer = null;
          System.out.println("Available mixers:");
          for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
              mixer = AudioSystem.getMixer(mixerInfo[3]);
              if (mixer.isLineSupported(dataLineInfo)) {
                  System.out.println(mixerInfo[cnt].getName());
                  targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
              }
          }
          targetDataLine.open(audioFormat);
          targetDataLine.start();
          Thread captureThread = new CaptureThread();
          captureThread.start();
      } catch (Exception e) {
          System.out.println(e);
      }
    }
      
    class CaptureThread extends Thread {

      byte tempBuffer[] = new byte[10000];
      
      volatile boolean threadStopCapture = Control.flag;
      
      @Override
      public void run() {
          try {
            System.out.println("Capture Thread Starts: " + threadStopCapture);
              while (true) {
                control.updateFlag();
                threadStopCapture = Control.flag;
                if(threadStopCapture == true) {
                  System.out.println("CAPTURE THREAD CLOSES");
                  Thread.currentThread().interrupt();
                  return;
                }
                  int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
                  voiceOut.write(tempBuffer);
                  voiceOut.flush();
              }
          } catch (Exception e) {
            System.out.println("Capture thread error");
              System.out.println(e);
          }
      }
   }
    
  }
  
  /**
   * The main method of the server application which is responsible for accepting socket connections and 
   * opening each one on its own thread.
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("The server is running.....");
    try(ServerSocket listener = new ServerSocket(PORT);){
      while(true) {
        new Handler (listener.accept()).start();
      }
    }
    catch(Exception e) {
      //TODO
    }
  }
  
}

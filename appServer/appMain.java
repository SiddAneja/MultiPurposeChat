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
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.DefaultListModel;

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
  public static final int PORT = 59003;
  
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
   * This class Handles the connection to the Server.
   * @author Siddharth
   *
   */
  private static class Handler extends Thread{
    
    private Socket socket;
    
    private ObjectInputStream in;
    
    private ObjectOutputStream out;
    
    private ObjectOutputStream friendOut;
    
    private InputStream voiceIn;
    
    private OutputStream voiceOut;
    
    private TargetDataLine targetDataLine;
    
    private AudioFormat audioFormat;
    
    private SourceDataLine sourceDataLine;
    
    byte tempBuffer[] = new byte[10000];
    
    private static Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    
    public Handler(Socket socket) {
      this.socket = socket;
    }
    
    public void run() {
      String name;
      while(true) {
        try {
          out = new ObjectOutputStream(socket.getOutputStream());
          in = new ObjectInputStream(socket.getInputStream());
          while(true) {
            out.writeObject("SUBMIT");
            name = (String) in.readObject();
            if (name == null){
              return;
            }
            clients.put(name, socket);
            map.put(name, out);
            while(true) {
              out.writeObject("CONNECTED");
              String read = (String) in.readObject();
              if(read.equals("SUCCESS")) {
                break;
              }
            }
            break;
          }
          
          while(true) {
            try {
              in = new ObjectInputStream(socket.getInputStream());
              Object inputMsg = in.readObject();
              if(inputMsg == null) {
                continue;
              }
              checkMsgType((DefaultListModel<Object>) inputMsg, name);
            }
            catch(Exception e) {
              //TODO
            }
          }
        }
        catch(Exception e) {
          //TODO
        }
      }
    }
    
    private void checkMsgType(DefaultListModel input, String sender) {
      switch((int) input.elementAt(0)) {
        case RequestType.SEND_MSG:
          sendMessage(input, sender);
          break;
        case RequestType.SEND_FILE:
          sendFile(input, sender);
          break;
        case RequestType.CALL:
          System.out.println("CALL DETECTED");
          voiceCall();
          break;
        case RequestType.LOGOUT:
          Logout(input, sender);
          break;
        default:
          break;
      }
    }
    
    private void sendFile(DefaultListModel input, String sender) {
      String user = (String) input.elementAt(2);
      Data data = (Data) input.elementAt(1);
      DefaultListModel<String> friendList = (DefaultListModel<String>) input.elementAt(3);
      for(int i = 0; i < friendList.size(); i++) {
        String friendName = friendList.elementAt(i);
        friendOut = map.get(friendName);
        if(user.equals(sender) && friendOut != null) {
          try {
            DefaultListModel model = new DefaultListModel();
            model.addElement(RequestType.SEND_FILE);
            model.addElement(data);
            model.addElement(sender);
            friendOut = new ObjectOutputStream((clients.get(friendName)).getOutputStream());
            friendOut.writeObject(model);
            friendOut.flush();
            System.out.println("Send file successful!");
          }
          catch (IOException e){
            //TODO
            System.out.println("FAILED TO SEND");
          }
        }
      }
    }
    
    private void sendMessage(DefaultListModel input, String sender) {
      String user = (String) input.elementAt(2);
      String message = (String) input.elementAt(1); 
      DefaultListModel<String> friendList = (DefaultListModel<String>) input.elementAt(3);
      for(int i = 0; i < friendList.size(); i++) {
        String friendName = friendList.elementAt(i);
        friendOut = map.get(friendName);
        if(user.equals(sender) && friendOut != null) {
          try {
            DefaultListModel model = new DefaultListModel();
            model.addElement(RequestType.SEND_MSG);
            model.addElement(message);
            model.addElement(sender);
            friendOut = new ObjectOutputStream((clients.get(friendName)).getOutputStream());
            friendOut.writeObject(model);
            friendOut.flush();
            System.out.println("Send message successful!");
          }
          catch (IOException e){
            //TODO
            System.out.println("FAILED TO SEND");
          }
        }
      }
    }
    
    private void voiceCall() {
      try {
        Mixer mixer_ = AudioSystem.getMixer(mixerInfo[0]);
        audioFormat = getaudioformat();
        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
        sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();
        captureAudio();
        voiceIn = new BufferedInputStream(socket.getInputStream());
        voiceOut = new BufferedOutputStream(socket.getOutputStream());
        while (voiceIn.read(tempBuffer) != -1) {
            sourceDataLine.write(tempBuffer, 0, 10000);
        }
    } catch (IOException e) {
      //TODO
      e.printStackTrace();
    } catch (LineUnavailableException e) {
      // TODO
      e.printStackTrace();
    }
    }
    
    private void Logout(DefaultListModel input, String sender) {
      System.out.println("Client\"" + sender +"\" just logged out!\r");
      Thread.currentThread().stop();
    }
    
    private AudioFormat getaudioformat() {
      float sampleRate = 8000.0F;
      int sampleSizeInBits = 8;
      int channel = 1;
      boolean signed = true;
      boolean bigEndian = false;
      return new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
    }
    
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
          System.out.println("REACH THREAD");
          Thread captureThread = new CaptureThread();
          captureThread.start();
      } catch (Exception e) {
          System.out.println(e);
      }
    }
    
    class CaptureThread extends Thread {

      byte tempBuffer[] = new byte[10000];

      @Override
      public void run() {
          try {
              while (true) {
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
  
  public static void main(String[] args) {
    System.out.println("The server is running.....");
    ExecutorService threadPool = Executors.newFixedThreadPool(500);
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

package appServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import AppClient.LoginApp;
import AppClient.Main;

public class appMain{
  public static final int PORT = 59001;
  public static HashSet<String> names = new HashSet<String>();
  public static HashMap<String, Socket> clients = new HashMap<String, Socket>();
  public static HashMap<String, ObjectOutputStream> map = new HashMap<>();

  private static class Handler extends Thread{
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    
    public Handler(Socket socket) {
      this.socket = socket;
    }
    
    public void run() {
      while(true) {
        try {
          in = new ObjectInputStream(socket.getInputStream());
          out = new ObjectOutputStream(socket.getOutputStream());
          DefaultListModel model = (DefaultListModel) in.readObject();
          int type = (int) model.elementAt(0);
          String name = (String) model.elementAt(1);
          String password = (String) model.elementAt(2);
          String email = (String) model.elementAt(3);
          
          if(type == RequestType.LOGIN) {
            try {
              Class.forName("com.mysql.cj.jdbc.Driver");
              Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
              Statement stmt = con.createStatement();
              String qry = "select * from login where Username="+" '"+name+"';";
              ResultSet rs = stmt.executeQuery(qry);
              if(rs.next() && names.contains(name)) {
                if(password.equals(rs.getString("Password"))) {
                  DefaultListModel resModel = new DefaultListModel();
                  resModel.addElement(RequestType.SUCCESSFUL);
                  resModel.addElement(new ArrayList<String>(names));
                  out.writeObject(resModel);
                }
                else {
                  JOptionPane.showMessageDialog(null, "Incorrect Password");
                  continue;
                }
              }
              else {
                JOptionPane.showMessageDialog(null, "User does not exist!");
                continue;
              }
            }
            catch(Exception e) {
              JOptionPane.showMessageDialog(null, "No connection!");
            }
          }
          else if(type == RequestType.REGISTER) {
            if(!names.contains(name)) {
              try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
                Statement stmt = con.createStatement();
                String qry = "insert into login values ('"+name+"', '"+password+"', '"+email+"');";
                stmt.executeUpdate(qry);
                JOptionPane.showMessageDialog(null, "New User Registered!");
                names.add(name);
                DefaultListModel resModel = new DefaultListModel();
                resModel.addElement(RequestType.SUCCESSFUL);
                resModel.addElement(new ArrayList<String>(names));
                out.writeObject(resModel);
              }
              catch(Exception e) {
                JOptionPane.showMessageDialog(null, "No Connection");
              }
            }else {
              JOptionPane.showMessageDialog(null, "User already exists!");
              continue;
            }
          }
          clients.put(name, socket);
          map.put(name, out);
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
      
    }
  }

  public static void main(String[] args) {
    System.out.println("The server is running.....");
    try{
      ServerSocket listener = new ServerSocket(PORT);
      appMain server = new appMain();
      while(true) {
        new Handler (listener.accept()).start();
      }
    }
    catch(Exception e) {
      //TODO
    }
  }
  
}

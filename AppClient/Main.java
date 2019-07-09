package AppClient;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import appServer.RequestType;
import appServer.appMain;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Scanner;
import java.awt.event.ActionEvent;

public class Main extends JFrame {
  private Socket socket;
  private String user;
  private String serverAddress;
  private ObjectInputStream in;
  private ObjectOutputStream out;
  private JPanel contentPane;
  private JTextField input;
  private JTextField add;
  JButton sendBtn;
  JList list;
  
  private void running() throws IOException{
    
    try {
      socket = new Socket(serverAddress, 59001);
      in = new ObjectInputStream(socket.getInputStream());
      out = new ObjectOutputStream(socket.getOutputStream());
      DefaultListModel resModel = new DefaultListModel();
      resModel.addElement(RequestType.START);
      resModel.addElement(user);
      out.writeObject(resModel);
      
      try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
        Statement stmt = con.createStatement();
        String qry = "update " + user + " set Socket = '"+socket+"' where Username = '"+user+"';";
      }catch(Exception e) {
        //TODO
      }
      
      while(true) {
        sendBtn.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
              try {
                out = new ObjectOutputStream(socket.getOutputStream());
                DefaultListModel sendModel = new DefaultListModel();
                sendModel.addElement(RequestType.SEND_MSG);
                sendModel.addElement(input.getText());
                sendModel.addElement(user);
                DefaultListModel<String> friends = new DefaultListModel<String>();
                friends.addElement((String)list.getSelectedValue());
                System.out.println(list.getSelectedValue());
                sendModel.addElement(friends);
                out.writeObject(sendModel);
                input.setText("");
              } catch (IOException e1) {
                // TODO 
              }
            }
        });
      }
    } finally {
      
    }
  }

  /**
   * Launch the application.
   */
  public static void main(String[] args) throws Exception{
    if (args.length != 1) {
      System.err.println("Pass the server IP as the sole command line argument");
      return;
    }
    else if(args.length == 1){
      LoginApp login = new LoginApp(args[0]);
      login.setVisible(true);
    }
    
  }

  /**
   * Create the frame.
   * @param username 
   */
  public Main(String username, String serverAddress) {
    user = username;
    this.serverAddress = serverAddress;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 726, 524);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JPanel panel = new JPanel();
    panel.setBounds(0, 0, 203, 477);
    contentPane.add(panel);
    panel.setLayout(null);
    
    JPanel panel_2 = new JPanel();
    panel_2.setBounds(0, 0, 203, 139);
    panel.add(panel_2);
    panel_2.setLayout(null);
    
    list = refreshList(list, user);
    list.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        input.setEditable(true);
      }
    });
    list.setBounds(12, 177, 179, 240);
    panel.add(list);
    
    add = new JTextField();
    add.setBounds(12, 442, 125, 22);
    panel.add(add);
    add.setColumns(10);
    
    JButton addfriend = new JButton("+");
    addfriend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String friendToAdd = add.getText();
        try {
          Class.forName("com.mysql.cj.jdbc.Driver");
          Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
          Statement stmt = con.createStatement();
          String qry = "select * from login where Username="+" '"+friendToAdd+"';";
          ResultSet rs = stmt.executeQuery(qry);
          if(rs.next()) {
            qry = "insert into " + username + " values('"+friendToAdd+"', '"+appMain.clients.get(friendToAdd)+"');";
            stmt.executeUpdate(qry);
            JOptionPane.showMessageDialog(null, "Friend added!");
          }
          else {
            JOptionPane.showMessageDialog(null,"User does not exist.");
          }
        }
        catch(Exception e) {
          //TODO
        }
        list = refreshList(list, user);
      }
    });
    addfriend.setBounds(150, 441, 41, 25);
    panel.add(addfriend);
    
    JPanel panel_1 = new JPanel();
    panel_1.setBounds(203, 0, 505, 60);
    contentPane.add(panel_1);
    panel_1.setLayout(null);
    
    JPanel panel_3 = new JPanel();
    panel_3.setBounds(215, 404, 481, 60);
    contentPane.add(panel_3);
    panel_3.setLayout(null);
    
    input = new JTextField();
    input.setBounds(12, 13, 367, 34);
    panel_3.add(input);
    input.setColumns(10);
    input.setEditable(false);
    
    sendBtn = new JButton("Send");
    sendBtn.setBounds(397, 13, 72, 34);
    panel_3.add(sendBtn);
    
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setBounds(215, 73, 481, 318);
    contentPane.add(textArea);
    
  }
  
  
  public JList refreshList(JList list, String username) {
    DefaultListModel listModel = new DefaultListModel();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
      Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
      Statement stmt = con.createStatement();
      String qry = "select * from " + username;
      ResultSet rs = stmt.executeQuery(qry);
      while(rs.next()) {
        listModel.addElement(rs.getString("FriendName"));
      }
    }catch(Exception e) {
      //TODO
    }
    return new JList(listModel);
  }
}

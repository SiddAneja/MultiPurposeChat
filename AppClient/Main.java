package AppClient;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import appServer.appMain;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.awt.event.ActionEvent;

public class Main extends JFrame {
  private String user;
  private JPanel contentPane;
  private JTextField input;
  private JTextField add;
  private String serverAddress;
  

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   * @param username 
   */
  public Main(String username) {
    user = username;
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
    
    JList list = new JList();
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent arg0) {
        
      }
    });
    list.setBounds(176, 402, -150, -206);
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
            //TODO
          }
        }
        catch(Exception e) {
          //TODO
        }
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
    
    JButton sendBtn = new JButton("Send");
    sendBtn.setBounds(397, 13, 72, 34);
    panel_3.add(sendBtn);
    
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setBounds(215, 73, 481, 318);
    contentPane.add(textArea);
  }
}

package AppClient;

import java.awt.BorderLayout;
import com.mysql.*;
import appServer.RequestType;
import appServer.appMain;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JFormattedTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

/**
 * This class creates a JFrame which generates a login page for the user to launch the main application.
 * @author Siddharth
 *
 */
public class LoginApp extends JFrame {

  /**
   * 
   */
  private JPanel contentPane;
  
  /**
   * Creates a JTextField called username, where the user enters their username.
   */
  private JTextField username;
  
  /**
   * Creates a JPasswordField with Echo-characters which permits the user to enter their password.
   */
  private JPasswordField password;
  
  /**
   * Stores the name of the user.
   */
  public String user;
  
  /**
   * Launch the application.
   */
  public static void main(String[] args) {
  }
  

  /**
   * Create the frame.
   */
  public LoginApp() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    JPanel panel = new JPanel();
    panel.setBounds(35, 75, 93, 94);
    contentPane.add(panel);
    
    JPanel panel_1 = new JPanel();
    panel_1.setBounds(126, 75, 261, 94);
    contentPane.add(panel_1);
    panel_1.setLayout(null);
    
    username = new JTextField();
    username.setBounds(98, 13, 116, 22);
    panel_1.add(username);
    username.setColumns(10);
    
    JLabel lblNewLabel = new JLabel("Username:");
    lblNewLabel.setBounds(12, 16, 74, 16);
    panel_1.add(lblNewLabel);
    
    JLabel lblNewLabel_1 = new JLabel("Password:");
    lblNewLabel_1.setBounds(12, 62, 74, 16);
    panel_1.add(lblNewLabel_1);
    
    password = new JPasswordField();
    password.setBounds(98, 59, 116, 22);
    panel_1.add(password);
    
       
    JButton loginBtn = new JButton("Login");
    loginBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //When the "login" button is clicked a  request is made to the MySQL database to check if the 
        //user is registered and if their entered passwords match.
        try {
          Class.forName("com.mysql.cj.jdbc.Driver");
          //Create a JDBC connection with the database.
          Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
          //Creates a statement using the connection
          Statement stmt = con.createStatement();
          String name = username.getText();
          String pass = new String(password.getPassword());
          //Create a String which is the query to be run in SQL
          String qry = "select * from login where Username="+" '"+name+"';";
          //Stores the returned value of the query in the ResultSet
          ResultSet rs = stmt.executeQuery(qry);
          //Checks if the ResultSet has values stored for the given query
          if(rs.next()) {
            //If the password and username match, it launches the main application.
            if(pass.equals(rs.getString("Password"))) {
              Main main = new Main(name);
              main.setVisible(true);
              main.names.add(name);
              //Creates a new Thread to keep the running() method always running
              Thread newThread = new Thread(new Runnable() {
                @Override
                public void run() {
                  try {
                    main.running();
                  } catch (IOException e) {
                    // TODO
                  }
                }
              });
              newThread.start();
              setVisible(false);
            }
            else {
              JOptionPane.showMessageDialog(null, "Incorrect Password");
            }
          }
          else {
            JOptionPane.showMessageDialog(null, "User does not exist!");
          }
        }
        catch(Exception ew) {
          JOptionPane.showMessageDialog(null, "No connection!");
        }
      }
    });
    loginBtn.setBounds(92, 182, 97, 25);
    contentPane.add(loginBtn);
    
    JButton registerBtn = new JButton("New User");
    registerBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        RegisterUser registerUser = new RegisterUser();
        registerUser.setVisible(true);
        dispose();
      }
    });
    registerBtn.setBounds(225, 182, 97, 25);
    contentPane.add(registerBtn);
  }
  
  
}

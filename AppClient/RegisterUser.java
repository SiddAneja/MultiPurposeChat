package AppClient;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import appServer.RequestType;
import appServer.appMain;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import javax.swing.JPasswordField;

/**
 * This JFrame is the Register page for the Chat application. Users create their accounts and their information 
 * gets stored in the MySQL database after making a new account.
 * @author Siddharth
 *
 */
public class RegisterUser extends JFrame {

  private JPanel contentPane;
  
  private JTextField username;
  
  private JTextField emailText;
  
  private JPasswordField password;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
  }

  /**
   * Create the frame.
   */
  public RegisterUser() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 450, 300);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);
    
    username = new JTextField();
    username.setBounds(245, 59, 116, 22);
    contentPane.add(username);
    username.setColumns(10);
    
    emailText = new JTextField();
    emailText.setBounds(245, 108, 116, 22);
    contentPane.add(emailText);
    emailText.setColumns(10);
    
    
    JLabel lblNewLabel = new JLabel("Choose your username:");
    lblNewLabel.setBounds(46, 62, 138, 16);
    contentPane.add(lblNewLabel);
    
    JLabel lblEnterYourEmail = new JLabel("Enter your email:");
    lblEnterYourEmail.setBounds(85, 111, 99, 16);
    contentPane.add(lblEnterYourEmail);
    
    JLabel lblPickAPassword = new JLabel("Pick a password:");
    lblPickAPassword.setBounds(86, 158, 98, 16);
    contentPane.add(lblPickAPassword);
    
    password = new JPasswordField();
    password.setBounds(245, 155, 116, 22);
    contentPane.add(password);
    
    
    JButton registerBtn = new JButton("Register");
    registerBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        String name = username.getText();
        if(!appMain.names.contains(name)) {
          try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
            Statement stmt = con.createStatement();

            String pass = new String (password.getPassword());
            String email = emailText.getText();
            String qry = "insert into login values ('"+name+"', '"+pass+"', '"+email+"');";
            stmt.executeUpdate(qry);
            String sql = "create table " + name + "(FriendName varchar(20) primary key, Socket varchar(255));";
            stmt.executeUpdate(sql);
            qry = "insert into " + name + "(FriendName) values ('"+name+"');";
            stmt.executeUpdate(qry);
            JOptionPane.showMessageDialog(null, "New User Registered!");
            appMain.names.add(name);
            LoginApp login = new LoginApp();
            login.setVisible(true);
            dispose();
          }
          catch(Exception e) {
            JOptionPane.showMessageDialog(null, "No Connection");
          }
        }else {
          JOptionPane.showMessageDialog(null, "User already exists!");
        }
      }
    });
    registerBtn.setBounds(151, 204, 97, 25);
    contentPane.add(registerBtn);
  }
}

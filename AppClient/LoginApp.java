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

public class LoginApp extends JFrame {

  private JPanel contentPane;
  private JTextField username;
  private JPasswordField password;
  public String user;
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
  
  public String getName() {
    return username.getText();
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
        try {
          Class.forName("com.mysql.cj.jdbc.Driver");
          Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatapp", "root", "tiger");
          Statement stmt = con.createStatement();
          String name = username.getText();
          String pass = new String(password.getPassword());
          String qry = "select * from login where Username="+" '"+name+"';";
          ResultSet rs = stmt.executeQuery(qry);
          if(rs.next()) {
            if(pass.equals(rs.getString("Password"))) {
              Main main = new Main(name);
              main.setVisible(true);
              main.names.add(name);
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

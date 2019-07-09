import java.lang.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.net.ssl.*;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;

public class appClient extends JFrame {

 	String serverAddress;
    Scanner in;
    PrintWriter out;
    JFrame frame = new JFrame("ChatBox");
    JTextField textField = new JTextField(50);
    JTextArea textArea = new JTextArea(16, 50);

    /**
    * The constructor of the client which creates the Frame and stores the server address.
    */
    public appClient(String serverAddress) {
        this.serverAddress = serverAddress;

        //Create Frame
        textField.setEditable(false);
        textArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    /**
    * This method create a pop-up screen where the user has to input a username.
    */
    public String getName() {
        return JOptionPane.showInputDialog(
            frame,
            "Choose a screen name:",
            "Screen name selection",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
    * This method is the main method that the client uses to send and receive messages from the server.
    */
    private void run() throws IOException {
        try {
            Socket socket = new Socket(serverAddress, 5900); //Creates a socket that connects to the given address and port
            //Scanner to read inputStream from the server
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("SUBMIT_NAME")) {
                    out.println(this.getName());
                } 
                else if (line.startsWith("NAME_ACCEPTED")) {
                    this.frame.setTitle("ChatBox - " + line.substring(14));
                    textField.setEditable(true);
                } 
                else if (line.startsWith("MESSAGE")) {
                    textArea.append(line.substring(8) + "\n");
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    /**
   	*/ 
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Pass the server IP as the sole command line argument");
            return;
        }
        appClient client = new appClient(args[0]);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
  
}

package appServer;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

public class appMain {
  private static final int PORT = 59001;
  private static HashSet<String> names = new HashSet<String>();
  private static HashMap<String, Socket> clients = new HashMap<String, Socket>();
  private static HashMap<String, ObjectOutputStream> map = new HashMap<>();

  
  
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

}

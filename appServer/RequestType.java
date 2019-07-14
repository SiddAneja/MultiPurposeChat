package appServer;

/**
 * This class provides a integer value for the type of request send by the client or server.
 * @author Siddharth
 *
 */
public class RequestType {
  public static final int CHECK = 9;
  
  public static final int START = 33;
  
  public static  final int LOGIN = -1;
  
  public static final int REGISTER = 0;
  
  public static final int SUCCESSFUL = 99;
  
  public static final int SEND_MSG = 11;
  
  public static final int SEND_FILE = 999;
  
  public static final int LOGOUT = 69;
}

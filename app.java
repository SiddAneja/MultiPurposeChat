import java.lang.*;
import java.util.Set;
import java.util.HashSet;
import javax.net.ssl.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.util.concurrent.*;
import java.io.PrintWriter;
import java.io.IOException;

public class app {
	private static Set<String> usernames = new HashSet<>();

	private static Set<PrintWriter> writers = new HashSet<>();

	public static class Handler implements Runnable {
		private String name;
		//private SSLSocket socket;
		private Socket socket;
		private Scanner in;
		private PrintWriter out;

		public Handler(Socket socket) {
			this.socket = socket;
		}

		public void run(){
			try{
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				while(true){
					out.println("SUBMIT_NAME");
					name = in.nextLine();
					if (name == null){
						return;
					}
					synchronized (usernames) {
						if(!name.trim().isEmpty() && !usernames.contains(name)) {
							usernames.add(name);
							break;
						}
					}
				}
				out.println("NAME_ACCEPTED: " + name);
				for(PrintWriter writer : writers) {
					writer.println("MESSAGE " + name + " has joined"); 
				}
				writers.add(out);

				while(true){
					String input = in.nextLine();
					if (input.toLowerCase().startsWith("/quit")) {
						return;
					}
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + name  + ": " +  input);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				if (out != null){
					writers.remove(out);
				}
				if (name != null){
					System.out.println(name + " is leaving");
					usernames.remove(name);
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + name + " has left");
					}
				}
				try {
					socket.close();
				} catch(IOException e) {}
			}
		}
	} 
	public static void main(String[]  args) throws Exception{
		// SSLServerSocketFactory serverFactory = null;
		
		System.out.println("The chat server is running....");
		ExecutorService threadPool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(5900)){
			// serverFactory = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();
			// listener = (SSLServerSocket)serverFactory.createServerSocket(5900);
			while(true){
				threadPool.execute(new Handler(listener.accept()));
				//new Handler(listener.accept());
			}
		}
	}
}
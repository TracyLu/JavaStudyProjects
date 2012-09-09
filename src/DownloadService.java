import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class DownloadService {
   public static void main(String[] args) {
      
      try {
         ServerSocket serverSocket = new ServerSocket(9999);
         Socket socket = serverSocket.accept();
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         String line = null;
         while (null != (line = reader.readLine())) {
            if ("quit".equals(line)) {
               break;
            } else {
               writer.println("echo: " + line);
               writer.flush();
            }
         }
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      
   }
}

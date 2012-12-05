package net.madz.download.telnet;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CreateTaskTest {

    final String url = "https://github-central.s3.amazonaws.com/mac/GitHub%20for%20Mac%2060.zip";
    final String folderName = "/Users/tracy/Downloads/demo";
    final String fileName = "git.zip";

    public static void main(String[] args) {
        CreateTaskTest test = new CreateTaskTest();
        test.createTask();
    }
    public String createTask() {
        // deleteDataAndMetaFiles();
        final StringBuilder sb = new StringBuilder();
        sb.append("create-task").append(" ").append(url).append(" ").append(folderName).append(" ").append(fileName).append(" ").append("--reCreate");
        return executeCommand(sb.toString());
    }

    private String executeCommand(final String sb) {
        final Socket socket = new Socket();
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            socket.connect(new InetSocketAddress("127.0.0.1", 9999));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.println(sb);
            writer.flush();
            return reader.readLine();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            try {
                reader.close();
            } catch (IOException e) {
            }
            writer.close();
        }
        throw new IllegalStateException();
    }
}

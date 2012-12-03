package net.madz.download.telnet;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.Test;

public class ClientTest {

    final String url = "https://github-central.s3.amazonaws.com/mac/GitHub%20for%20Mac%2060.zip";
    final String folderName = "/Users/tracy/Downloads/demo";
    final String fileName = "git.zip";

    public String createTask() {
        // deleteDataAndMetaFiles();
        final StringBuilder sb = new StringBuilder();
        sb.append("create-task").append(" ").append(url).append(" ").append(folderName).append(" ").append(fileName).append(" ").append("--reCreate");
        return executeCommand(sb.toString());
    }

    public String pauseTask(String id) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ignored
            //
        }
        final StringBuilder command = new StringBuilder();
        command.append("pause-task").append(" ").append(id);
        return executeCommand(command.toString());
    }

    @Test
    public void testResumeTask() {
        String id = createTask();
        pauseTask(id);
        final StringBuilder command = new StringBuilder();
        command.append("resume-task").append(" ").append(id);
        executeCommand(command.toString());
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

    public void onTaskItemRead(String line) {
        final String id = line.substring(0, line.indexOf(" "));
        pauseTask(id);
        taskCounts--;
    }

    @SuppressWarnings("unused")
    private volatile int taskCounts;

    public static void main(String[] args) {
        ClientTest test = new ClientTest();
        test.testResumeTask();
    }
}

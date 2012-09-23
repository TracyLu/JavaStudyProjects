package net.madz.download.connector;

import java.net.Socket;

import net.madz.download.ILifeCycle;
import net.madz.download.agent.ITelnetClient;

public interface IServiceEndpoint extends ILifeCycle {

    ITelnetClient createClient(Socket socket);
}

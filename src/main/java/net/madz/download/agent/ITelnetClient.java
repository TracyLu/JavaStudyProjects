package net.madz.download.agent;

import net.madz.download.ILifeCycle;

public interface ITelnetClient extends ILifeCycle {
    String acquireConfirm(String request);
}

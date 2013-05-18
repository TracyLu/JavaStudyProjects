package net.madz.download.agent.protocol.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RawCommand {

    private String name;
    private List<String> options = new LinkedList<String>();
    private List<String> args = new LinkedList<String>();

    public void addArg(String arg) {
        this.args.add(arg);
    }

    public void addOption(String option) {
        options.add(option);
    }

    public List<String> getArgs() {
        return Collections.unmodifiableList(args);
    }

    public String getName() {
        return name;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setName(String name) {
        this.name = name;
    }
}

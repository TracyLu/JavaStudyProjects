package net.madz.download.engine;

import java.io.Serializable;

public class DownloadSegment implements Serializable {
    
    private static final long serialVersionUID = -5065005410835889264L;

    private int id;

    private long startBytes;
    
    private long endBytes;
    
    private long currentBytes;
    
    private Byte state;

    
    public long getCurrentBytes() {
        return currentBytes;
    }


    
    public long getEndBytes() {
        return endBytes;
    }


    public int getId() {
        return id;
    }

    
    public long getStartBytes() {
        return startBytes;
    }

    
    public Byte getState() {
        return state;
    }

    
    public void setCurrentBytes(long currentBytes) {
        this.currentBytes = currentBytes;
    }

    
    public void setEndBytes(long endBytes) {
        this.endBytes = endBytes;
    }

    
    public void setId(int id) {
        this.id = id;
    }

    
    public void setStartBytes(long startBytes) {
        this.startBytes = startBytes;
    }

    
    public void setState(Byte state) {
        this.state = state;
    }



    @Override
    public String toString() {
        return "Segment [id=" + id + ", startBytes=" + startBytes + ", endBytes=" + endBytes + ", currentBytes=" + currentBytes + ", state=" + state + "]";
    }
    
}

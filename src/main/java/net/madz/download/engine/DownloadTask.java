package net.madz.download.engine;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class DownloadTask implements Serializable {

    private static final long serialVersionUID = 4357100059823468260L;
    private int id;
    private URL url;
    private URL referURL;
    private File folder;
    private String fileName;
    private long totalLength;
    private int segmentsNumber;
    private boolean resumable;
    private byte threadNumber;
    private byte state;
    private List<DownloadSegment> segments = new LinkedList<DownloadSegment>();

    public DownloadTask(URL url, URL referURL, File folder, String filename) {
        this.url = url;
        this.referURL = referURL;
        this.folder = folder;
        this.fileName = filename;
    }

    public void addSegment(DownloadSegment segment) {
        this.segments.add(segment);
    }

    public String getFileName() {
        return fileName;
    }

    public File getFolder() {
        return folder;
    }

    public int getId() {
        return id;
    }

    public long getReceivedBytes() {
        long receivedBytes = 0;
        if ( 0 >= this.getSegments().size() ) {
            return 0;
        }
        for ( int i = 0; i < this.segmentsNumber; i++ ) {
            DownloadSegment segment = this.segments.get(i);
            long startBytes = segment.getStartBytes();
            long currentBytes = segment.getCurrentBytes();
            receivedBytes += currentBytes - startBytes + 1;
        }
        return receivedBytes;
    }

    public URL getReferURL() {
        return referURL;
    }

    public List<DownloadSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public int getSegmentsNumber() {
        return segmentsNumber;
    }

    public byte getState() {
        return state;
    }

    public byte getThreadNumber() {
        return threadNumber;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public URL getUrl() {
        return url;
    }

    public boolean isResumable() {
        return resumable;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReferURL(URL referURL) {
        this.referURL = referURL;
    }

    public void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    public void setSegmentsNumber(int segmentsNumber) {
        this.segmentsNumber = segmentsNumber;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public void setThreadNumber(byte threadNumber) {
        this.threadNumber = threadNumber;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public String toString() {
        StringBuilder segmentsInformation = new StringBuilder();
        if ( null != segments ) {
            segmentsInformation.append("(");
            for ( DownloadSegment segment : segments ) {
                segmentsInformation.append("id=" + segment.getId());
                segmentsInformation.append(",start bytes=" + segment.getStartBytes());
                segmentsInformation.append(",end bytes=" + segment.getEndBytes());
                segmentsInformation.append(",current bytes=" + segment.getCurrentBytes());
                segmentsInformation.append(",state=" + segment.getState());
            }
        }
        return "DownloadTask [id=" + id +",url="+ url + ", referURL=" + referURL + ", folder=" + folder + ", fileName=" + fileName + ", totalLength=" + totalLength
                + ", segmentsNumber=" + segmentsNumber + ", resumable=" + resumable + ", threadNumber=" + threadNumber + ", state=" + state + ", segments="
                + segments + "]";
    }
}

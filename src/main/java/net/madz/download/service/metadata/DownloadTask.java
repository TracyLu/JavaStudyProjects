package net.madz.download.service.metadata;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public final class DownloadTask {

    private URL url;
    private URL referURL;
    private File folder;
    private String fileName;
    private long totalLength;
    private int segmentsNumber;
    private boolean resumable;
    private byte threadNumber;
    private byte state;
    private List<Segment> segments;

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public URL getReferURL() {
        return referURL;
    }

    public void setReferURL(URL referURL) {
        this.referURL = referURL;
    }

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }

    public int getSegmentsNumber() {
        return segmentsNumber;
    }

    public void setSegmentsNumber(int segmentsNumber) {
        this.segmentsNumber = segmentsNumber;
    }

    public boolean isResumable() {
        return resumable;
    }

    public void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    public byte getThreadNumber() {
        return threadNumber;
    }

    public void setThreadNumber(byte threadNumber) {
        this.threadNumber = threadNumber;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public void addSegment(Segment segment) {
        this.segments.add(segment);
    }
}

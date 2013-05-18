package net.madz.download.engine.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import net.madz.download.engine.DownloadSegment;
import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.IDownloadProcess;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.utils.LogUtils;

public final class DownloadSegmentWorker implements Runnable {

    private final DownloadTask task;
    private final DownloadSegment segment;
    private IDownloadProcess process;
    private File dataFile;
    private File metadataFile;

    public DownloadSegmentWorker(IDownloadProcess process, DownloadTask task, DownloadSegment segment, File dataFile, File metadataFile) {
        this.process = process;
        this.task = task;
        this.segment = segment;
        this.dataFile = dataFile;
        this.metadataFile = metadataFile;
    }

    public boolean isPauseFlag() {
        return this.process.isPaused();
    }

    @Override
    public void run() {
        URL url = task.getUrl();
        HttpURLConnection openConnection = null;
        InputStream inputStream = null;
        RandomAccessFile randomAccessDataFile = null;
        byte[] buf = new byte[8096];
        int size = 0;
        try {
            openConnection = (HttpURLConnection) url.openConnection();
            long nextByte = 0;
            if ( segment.getStartBytes() == segment.getCurrentBytes() ) {
                nextByte = segment.getStartBytes();
            } else if ( segment.getStartBytes() < segment.getCurrentBytes() ) {
                nextByte = segment.getCurrentBytes() + 1;
            }
            openConnection.setRequestProperty("RANGE", "bytes=" + nextByte + "-" + segment.getEndBytes());
            openConnection.connect();
            inputStream = openConnection.getInputStream();
            randomAccessDataFile = new RandomAccessFile(dataFile, "rw");
            long offset = nextByte;
            long totalReceivedBytes = 0;
            while ( ( !isPauseFlag() ) && ( size = inputStream.read(buf) ) != -1 ) {
                randomAccessDataFile.seek(offset);
                randomAccessDataFile.write(buf, 0, size);
                offset += size;
                totalReceivedBytes += size;
                segment.setCurrentBytes(offset -1);
                MetaManager.updateSegmentDownloadProgress(metadataFile, segment.getId(), offset - 1);
                if ( segment.getCurrentBytes() == segment.getEndBytes() ) {
                    process.receive(totalReceivedBytes);
                }
            }
        } catch (Exception ignored) {
            LogUtils.error(DownloadSegmentWorker.class, ignored);
        } finally {
            if ( null != openConnection ) {
                openConnection.disconnect();
            }
            try {
                if ( null != inputStream ) {
                    inputStream.close();
                }
                if ( null != randomAccessDataFile ) {
                    randomAccessDataFile.close();
                }
            } catch (IOException ignored) {
                LogUtils.error(DownloadSegmentWorker.class, ignored);
            }
        }
    }
}

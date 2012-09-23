package net.madz.download.service.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.madz.download.LogUtils;
import net.madz.download.service.IService;
import net.madz.download.service.IServiceResponse;
import net.madz.download.service.annotations.Arg;
import net.madz.download.service.annotations.Command;
import net.madz.download.service.annotations.Option;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.metadata.Consts;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.metadata.SegmentState;
import net.madz.download.service.metadata.TaskState;
import net.madz.download.service.requests.CreateTaskRequest;
import net.madz.download.service.responses.CreateTaskResponse;

@Command(arguments = { @Arg(name = "url", description = "the address of remote file."), @Arg(name = "folder", description = "where to store the file"),
        @Arg(name = "filename", description = "new file name.") }, commandName = "create-task", options = { @Option(description = "thread number",
        fullName = "--threadNumber", shortName = "-n") }, request = CreateTaskRequest.class,
        description = "This command is responsible for downloding specified url resource.")
public class CreateTaskService implements IService<CreateTaskRequest> {

    private ExecutorService pool; // We use thread pool
    private Lock poolLock = new ReentrantLock();
    private Condition allDone = poolLock.newCondition();
    private int doneNumber = 0;
    private static File file; // Point to the storage file
    private File logFile;
    private DownloadTask task;

    @Override
    public void start() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isStarted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
    }

    @Override
    public IServiceResponse processRequest(CreateTaskRequest request) throws ErrorException {
        URL url = null;
        try {
            url = new URL(request.getUrl());
        } catch (MalformedURLException notExpected) {
            LogUtils.error(CreateTaskService.class, notExpected);
        }
        String folder = request.getFolder();
        String filename = request.getFilename();
        int threadNumber = request.getThreadNumber();
        this.pool = Executors.newFixedThreadPool(threadNumber);
        file = new File(folder, filename);
        logFile = new File("./meta/downloading/" + file.getName() + "_log");
        task = MetaManager.createDownloadTask(request);
        MetaManager.serialize(task, logFile);
        MetaManager.deserializeHeadInformation(logFile);
        download(url, task.getSegmentsNumber());
        CreateTaskResponse downloadResponse = new CreateTaskResponse();
        downloadResponse.setMessage("You task is downloading");
        MetaManager.deserializeHeadInformation(logFile);
        MetaManager.deserializeSegmentsInformation(task, logFile);
        return downloadResponse;
    }

    public boolean download(URL url, int segmentsNumber) {
        long totalLength = 0;
        long partLength = 0;
        totalLength = (int) MetaManager.getTotalLength(url);
        partLength = totalLength / segmentsNumber;
        for ( int i = 0; i < segmentsNumber; i++ ) {
            final int seq = i;
            final long finalPartLength = partLength;
            final URL finalURL = url;
            if ( i < segmentsNumber - 1 ) {
                pool.execute(new Runnable() {

                    @Override
                    public void run() {
                        download(finalURL, finalPartLength * seq, finalPartLength * ( seq + 1 ) - 1, logFile, seq);
                    }
                });
            } else {
                final long finalTotalLength = totalLength;
                pool.execute(new Runnable() {

                    @Override
                    public void run() {
                        download(finalURL, finalPartLength * seq, finalTotalLength - 1, logFile, seq);
                    }
                });
            }
        }
        poolLock.lock();
        try {
            while ( doneNumber < segmentsNumber ) {
                try {
                    allDone.await();
                    if ( doneNumber == segmentsNumber ) {
                        task.setState(TaskState.DONE);
                        MetaManager.updateTaskState(task, logFile);
                        pool.shutdown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            poolLock.unlock();
        }
        return true;
    }

    private void download(URL url, long start, long end, File logfile, int seq) {
        HttpURLConnection openConnection = null;
        InputStream bs = null;
        RandomAccessFile fs = null;
        byte[] buf = new byte[8192];
        int size = 0;
        try {
            openConnection = (HttpURLConnection) url.openConnection();
            openConnection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
            openConnection.connect();
            fs = new RandomAccessFile(file, "rw");
            bs = openConnection.getInputStream();
            long off = start;
            RandomAccessFile fos = new RandomAccessFile(logfile, "rw");
            long position = Consts.FIRST_SEGMENT_POSITION + seq * Consts.SEGMENT_LENGTH;
            fos.seek(position);
            fos.writeInt(seq);
            position += Consts.SEGMENT_ID_LENGTH;
            fos.writeLong(start);
            position += Consts.SEGMENT_START_BYTES_LENGTH;
            fos.writeLong(end);
            position += Consts.SEGMENT_END_BYTES_LENGTH;
            long segment_state_position = position;
            fos.writeByte(SegmentState.STARTED);
            position += Consts.SEGMENT_STATE_LENGTH;
            while ( ( size = bs.read(buf) ) != -1 ) {
                fs.seek(off);
                fs.write(buf, 0, size);
                off += size;
                fos.seek(position);
                fos.writeLong(off - 1);
            }
            poolLock.lock();
            try {
                doneNumber++;
                allDone.signalAll();
            } finally {
                poolLock.unlock();
                fos.seek(segment_state_position);
                fos.writeByte(SegmentState.DONE);
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bs.close();
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

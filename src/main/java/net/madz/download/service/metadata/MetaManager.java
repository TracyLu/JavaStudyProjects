package net.madz.download.service.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import net.madz.download.LogUtils;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.exception.ErrorMessage;
import net.madz.download.service.requests.CreateTaskRequest;

public class MetaManager {

    public static HashMap<URL, DownloadTask> allTasks = new HashMap<URL, DownloadTask>();

    public static void serialize(DownloadTask task, File file) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // randomAccessFile.seek(Consts.URL_POSITION);
            // randomAccessFile.writeChars(task.getUrl().toString());
            //
            // randomAccessFile.seek(Consts.REFER_URL_LENGTH);
            // randomAccessFile.writeChars(task.getReferURL().toString());
            // randomAccessFile.seek(Consts.FOLDER_POSITION);
            // randomAccessFile.writeChars(task.getFolder().getAbsolutePath());
            // randomAccessFile.seek(Consts.FILE_NAME_POSITION);
            // randomAccessFile.writeChars(task.getFileName());
            randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
            randomAccessFile.writeLong(task.getTotalLength());
            randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
            randomAccessFile.writeInt(task.getSegmentsNumber());
            randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
            randomAccessFile.writeBoolean(task.isResumable());
            // randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
            // randomAccessFile.writeByte(task.getThreadNumber());
            randomAccessFile.seek(Consts.STATE_POSTION);
            randomAccessFile.writeByte(task.getState());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static DownloadTask createDownloadTask(CreateTaskRequest request) {
        DownloadTask task = new DownloadTask();
        URL url = null;
        URL referURL = null;
        boolean resumable = false;
        try {
            url = new URL(request.getUrl());
            referURL = new URL(request.getReferURL());
        } catch (MalformedURLException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        }
        task.setUrl(url);
        task.setReferURL(referURL);
        task.setFolder(new File(request.getFilename()));
        task.setFileName(request.getFilename());
        task.setThreadNumber(new Integer(request.getThreadNumber()).byteValue());
        try {
            resumable = checkResumable(url);
            task.setResumable(resumable);
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        }
        task.setTotalLength(getTotalLength(url));
        task.setState((byte) TaskState.STARTED);
        if ( resumable ) {
            task.setSegmentsNumber((int) ( task.getTotalLength() / Consts.ONE_SEGEMENT ));
        } else {
            task.setSegmentsNumber(1);
        }
        return task;
    }

    public static long getTotalLength(URL url) {
        URLConnection openConnection = null;
        int totalLength = 0;
        try {
            openConnection = url.openConnection();
            openConnection.connect();
            totalLength = openConnection.getContentLength();
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        }
        return totalLength;
    }

    public static void deserializeHeadInformation(File file) {
        RandomAccessFile randomAccessFile = null;
        StringBuilder headInfo = new StringBuilder();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            byte[] result = new byte[Consts.URL_LENGTH];
            randomAccessFile.readFully(result, 0, Consts.URL_LENGTH);
            headInfo.append(" URL:");
            headInfo.append(new String(result));
            result = new byte[Consts.REFER_URL_LENGTH];
            randomAccessFile.seek(Consts.REFER_URL_POSITION);
            randomAccessFile.readFully(result, 0, Consts.REFER_URL_LENGTH);
            headInfo.append(" Refer URL:");
            headInfo.append(new String(result));
            result = new byte[Consts.FOLDER_LENGTH];
            randomAccessFile.seek(Consts.FOLDER_POSITION);
            randomAccessFile.readFully(result, 0, Consts.FOLDER_LENGTH);
            headInfo.append(" Folder:");
            headInfo.append(new String(result));
            result = new byte[Consts.FILE_NAME_LENGTH];
            randomAccessFile.seek(Consts.FILE_NAME_POSITION);
            randomAccessFile.readFully(result, 0, Consts.FILE_NAME_LENGTH);
            headInfo.append(" File name:");
            headInfo.append(new String(result));
            randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
            headInfo.append(" Total Length:");
            headInfo.append(randomAccessFile.readLong());
            randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
            headInfo.append(" Segements Number:");
            headInfo.append(randomAccessFile.readInt());
            randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
            headInfo.append(" Resumable:");
            headInfo.append(randomAccessFile.readByte());
            randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
            headInfo.append(" Thread Number:");
            headInfo.append(randomAccessFile.readByte());
            randomAccessFile.seek(Consts.STATE_POSTION);
            headInfo.append(" State:");
            headInfo.append(randomAccessFile.readByte());
            System.out.println("Task header Information:");
            System.out.println(headInfo.toString());
        } catch (FileNotFoundException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } catch (IOException ignored2) {
            LogUtils.error(MetaManager.class, ignored2);
        }
    }

    private static boolean checkResumable(URL url) throws IOException {
        boolean resumable = false;
        URLConnection openConnection;
        openConnection = url.openConnection();
        openConnection.setRequestProperty("RANGE", "bytes=" + 0 + "-" + 0);
        openConnection.connect();
        String acceptRanges = openConnection.getHeaderField("Accept-Ranges");
        if ( "bytes".equalsIgnoreCase(acceptRanges) ) {
            resumable = true;
        }
        return resumable;
    }

    public static void updateTaskState(DownloadTask task, File logFile) {
        try {
            RandomAccessFile raf = new RandomAccessFile(logFile, "rw");
            raf.seek(Consts.STATE_POSTION);
            raf.writeByte(task.getState());
        } catch (Exception e) {
            LogUtils.error(MetaManager.class, e); // ignored the exception
        }
    }

    public static void deserializeSegmentsInformation(DownloadTask task, File logFile) throws ErrorException {
        int segments = task.getSegmentsNumber();
        StringBuilder segmentsInformation = new StringBuilder();
        try {
            RandomAccessFile raf = new RandomAccessFile(logFile, "rw");
            long position = 0;
            for ( int i = 0; i < segments; i++ ) {
                position = Consts.FIRST_SEGMENT_POSITION + i * Consts.SEGMENT_LENGTH;
                raf.seek(position);
                segmentsInformation.append("Segment Id:");
                segmentsInformation.append(raf.readInt());
                position += Consts.SEGMENT_ID_LENGTH;
                raf.seek(position);
                segmentsInformation.append("Segment start bytes:");
                segmentsInformation.append(raf.readLong());
                position += Consts.SEGMENT_START_BYTES_LENGTH;
                segmentsInformation.append("Segment end bytes:");
                segmentsInformation.append(raf.readLong());
                position += Consts.SEGMENT_END_BYTES_LENGTH;
                segmentsInformation.append("Segment state:");
                segmentsInformation.append(raf.readByte());
                position += Consts.SEGMENT_STATE_LENGTH;
                segmentsInformation.append("Segment current bytes:");
                segmentsInformation.append(raf.readLong());
            }
        } catch (FileNotFoundException e) {
            LogUtils.error(MetaManager.class, e);
            throw new ErrorException(ErrorMessage.LOG_FILE_WAS_NOT_FOUND + logFile.getName());
        } catch (IOException e) {
            LogUtils.error(MetaManager.class, e);
            throw new ErrorException(ErrorMessage.LOG_FILE_IS_NOT_COMPLETE + logFile.getName());
        }
        System.out.println("Segements Information:" + segmentsInformation.toString());
    }

    public static void serialize(CreateTaskRequest request, File file) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(Consts.URL_POSITION);
            randomAccessFile.writeChars(request.getUrl());
            randomAccessFile.seek(Consts.REFER_URL_LENGTH);
            randomAccessFile.writeChars(request.getReferURL());
            randomAccessFile.seek(Consts.FOLDER_POSITION);
            randomAccessFile.writeChars(request.getFolder());
            randomAccessFile.seek(Consts.FILE_NAME_POSITION);
            randomAccessFile.writeChars(request.getFilename());
            randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
            randomAccessFile.writeByte(request.getThreadNumber());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void recover() {
        File downloadingFolder = new File("./meta/downloading");
        File finishedFolder = new File("./meta/finished");
        new File("./meta/deleted").mkdirs();
        new File("./meta/paused").mkdirs();
    }
}

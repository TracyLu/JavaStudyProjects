package net.madz.download.service.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import net.madz.download.LogUtils;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.exception.ErrorMessage;
import net.madz.download.service.requests.CreateTaskRequest;

public class MetaManager {

    public static HashMap<URL, DownloadTask> allTasks = new HashMap<URL, DownloadTask>();

    public static void serializeForPreparedState(DownloadTask task, File file) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
            randomAccessFile.writeLong(task.getTotalLength());
            randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
            randomAccessFile.writeInt(task.getSegmentsNumber());
            randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
            randomAccessFile.writeBoolean(task.isResumable());
            randomAccessFile.seek(Consts.STATE_POSTION);
            randomAccessFile.writeByte(StateEnum.Prepared.ordinal());
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
        task.setState((byte) StateEnum.Prepared.ordinal());
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

    public static void serializeForNewState(CreateTaskRequest request, File file) {
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
            randomAccessFile.seek(Consts.STATE_POSTION);
            randomAccessFile.writeByte(StateEnum.New.ordinal());
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

    public static void serializeSegmentsInformation(DownloadTask task, File metadataFile) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(metadataFile, "rw");
            for ( Segment segment : task.getSegments() ) {
                int seq = segment.getId();
                long position = Consts.FIRST_SEGMENT_POSITION + seq * Consts.SEGMENT_LENGTH;
                raf.seek(position);
                raf.writeInt(seq);
                position += Consts.SEGMENT_ID_LENGTH;
                raf.writeLong(segment.getStartBytes());
                position += Consts.SEGMENT_START_BYTES_LENGTH;
                raf.writeLong(segment.getEndBytes());
                position += Consts.SEGMENT_END_BYTES_LENGTH;
                raf.writeByte(StateEnum.Prepared.ordinal());
                position += Consts.SEGMENT_STATE_LENGTH;
                raf.writeLong(segment.getStartBytes());
            }
        } catch (FileNotFoundException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        }
    }

    public static void computeSegmentsInformation(DownloadTask task) {
        int segmentsNumber = task.getSegmentsNumber();
        long partLength = 0L;
        long totalLength = task.getTotalLength();
        partLength = totalLength / segmentsNumber;
        for ( int i = 0; i < segmentsNumber; i++ ) {
            Segment segment = new Segment();
            segment.setId(i);
            final int seq = i;
            final long finalPartLength = partLength;
            if ( i < segmentsNumber - 1 ) {
                segment.setStartBytes(finalPartLength * seq);
                segment.setEndBytes(finalPartLength * ( seq - 1 ) - 1);
            } else {
                final long finalTotalLength = totalLength;
                segment.setStartBytes(finalPartLength * seq);
                segment.setEndBytes(finalTotalLength - 1);
            }
            task.addSegment(segment);
        }
    }

    private static void copy(File resFile, File objFolderFile) {
        if ( !resFile.exists() ) return;
        if ( !objFolderFile.exists() ) objFolderFile.mkdirs();
        if ( resFile.isFile() ) {
            File objFile = new File(objFolderFile.getPath() + File.separator + resFile.getName());
            InputStream ins = null;
            FileOutputStream outs = null;
            try {
                ins = new FileInputStream(resFile);
                outs = new FileOutputStream(objFile);
                byte[] buffer = new byte[1024 * 512];
                int length;
                while ( ( length = ins.read(buffer) ) != -1 ) {
                    outs.write(buffer, 0, length);
                }
            } catch (FileNotFoundException ignored) {
                LogUtils.error(MetaManager.class, ignored);
            } catch (IOException ignored) {
                LogUtils.error(MetaManager.class, ignored);
            } finally {
                try {
                    ins.close();
                    outs.flush();
                    outs.close();
                } catch (IOException ignored) {
                    LogUtils.error(MetaManager.class, ignored);
                }
            }
        } else {
            String objFolder = objFolderFile.getPath() + File.separator + resFile.getName();
            File _objFolderFile = new File(objFolder);
            _objFolderFile.mkdirs();
            for ( File sf : resFile.listFiles() ) {
                copy(sf, new File(objFolder));
            }
        }
    }

    public static void move(File srcFile, File objFolderFile) {
        copy(srcFile, objFolderFile);
        delete(srcFile);
    }

    private static void delete(File file) {
        if ( !file.exists() ) return;
        if ( file.isFile() ) {
            file.delete();
        } else {
            for ( File f : file.listFiles() ) {
                delete(f);
            }
            file.delete();
        }
    }
}

package net.madz.download.engine.impl.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.madz.download.LogUtils;
import net.madz.download.engine.IDownloadProcess.StateEnum;
import net.madz.download.service.exception.ServiceException;
import net.madz.download.service.exception.ExceptionMessage;
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
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if ( null != randomAccessFile ) {
                    randomAccessFile.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public static DownloadTask createDownloadTask(CreateTaskRequest request) {
        DownloadTask task = new DownloadTask();
        task.setId(IdFactory.getInstance().generate());
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
        task.setFolder(new File(request.getFolder()));
        task.setFileName(request.getFilename());
        task.setThreadNumber(new Integer(request.getThreadNumber()).byteValue());
        try {
            resumable = checkResumable(url);
            task.setResumable(resumable);
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        }
        task.setTotalLength(getTotalLength(url));
        task.setState((byte) StateEnum.New.ordinal());
        if ( resumable ) {
            int segmentsNumber = (int) ( task.getTotalLength() / Consts.ONE_SEGEMENT );
            task.setSegmentsNumber(segmentsNumber <= 0 ? 1 : segmentsNumber);
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

    public static DownloadTask deserializeHeadInformation(File file) {
        DownloadTask task = new DownloadTask();
        RandomAccessFile randomAccessFile = null;
        StringBuilder headInfo = new StringBuilder();
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // Task ID
            //
            randomAccessFile.seek(Consts.ID_POSITION);
            final int id = randomAccessFile.readInt();
            task.setId(id);
            headInfo.append(" ID:" + id);
            // URL
            //
            randomAccessFile.seek(Consts.URL_SIZE_POSITION);
            final int urlLength = randomAccessFile.readInt();
            byte[] result = new byte[urlLength];
            randomAccessFile.seek(Consts.URL_POSITION);
            randomAccessFile.readFully(result, 0, urlLength);
            headInfo.append(" URL:");
            String urlStr = new String(result, "utf8");
            headInfo.append(urlStr);
            URL url = new URL(urlStr);
            task.setUrl(url);
            // Refer URL
            //
            randomAccessFile.seek(Consts.REFER_URL_SIZE_POSITION);
            final int referUrlLength = randomAccessFile.readInt();
            if ( 0 < referUrlLength ) {
                result = new byte[referUrlLength];
                randomAccessFile.seek(Consts.REFER_URL_POSITION);
                randomAccessFile.readFully(result, 0, referUrlLength);
                headInfo.append(" Refer URL:");
                String referUrl = new String(result, "utf8");
                headInfo.append(referUrl);
                task.setReferURL(new URL(referUrl));
            }
            // Folder
            //
            randomAccessFile.seek(Consts.FOLDER_SIZE_POSITION);
            final int folderLength = randomAccessFile.readInt();
            if ( 0 < folderLength ) {
                result = new byte[folderLength];
                randomAccessFile.seek(Consts.FOLDER_POSITION);
                randomAccessFile.readFully(result, 0, folderLength);
                headInfo.append(" Folder:");
                String folder = new String(result, "utf8");
                headInfo.append(new String(result));
                task.setFolder(new File(folder));
            }
            // File name
            //
            randomAccessFile.seek(Consts.FILE_NAME_SIZE_POSITION);
            final int filenameLength = randomAccessFile.readInt();
            if ( 0 < filenameLength ) {
                result = new byte[filenameLength];
                randomAccessFile.seek(Consts.FILE_NAME_POSITION);
                randomAccessFile.readFully(result, 0, filenameLength);
                headInfo.append(" File name:");
                String filename = new String(result, "utf8");
                headInfo.append(new String(result));
                task.setFileName(filename);
            }
            // Total length
            //
            randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
            headInfo.append(" Total Length:");
            long totalLength = randomAccessFile.readLong();
            headInfo.append(totalLength);
            task.setTotalLength(totalLength);
            // Segments number
            //
            randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
            headInfo.append(" Segements Number:");
            int segmentNumber = randomAccessFile.readInt();
            headInfo.append(segmentNumber);
            task.setSegmentsNumber(segmentNumber);
            // Resumable
            //
            randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
            headInfo.append(" Resumable:");
            byte resumableValue = randomAccessFile.readByte();
            headInfo.append(resumableValue);
            if ( resumableValue == 0 ) {
                task.setResumable(false);
            } else {
                task.setResumable(true);
            }
            // Thread number
            //
            randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
            headInfo.append(" Thread Number:");
            byte threadNumber = randomAccessFile.readByte();
            headInfo.append(threadNumber);
            task.setThreadNumber(threadNumber);
            // State
            //
            randomAccessFile.seek(Consts.STATE_POSTION);
            headInfo.append(" State:");
            byte stateValue = randomAccessFile.readByte();
            headInfo.append(stateValue);
            task.setState(stateValue);
            System.out.println("Task header Information:");
            System.out.println(headInfo.toString());
        } catch (FileNotFoundException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } finally {
            if ( null != randomAccessFile ) {
                try {
                    randomAccessFile.close();
                } catch (IOException ignored) {
                    LogUtils.error(MetaManager.class, ignored);
                }
            }
        }
        return task;
    }

    public static boolean checkResumable(URL url) throws IOException {
        boolean resumable = false;
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("RANGE", "bytes=" + 0 + "-" + 0);
        conn.connect();
        final int statusCode = conn.getResponseCode();
        if ( 206 == statusCode ) {
            resumable = true;
            return resumable;
        }
        final String acceptRanges = conn.getHeaderField("Accept-Ranges");
        if ( "bytes".equalsIgnoreCase(acceptRanges) ) {
            resumable = true;
        } else {
            System.out.println("Accept-Ranges" + acceptRanges);
        }
        return resumable;
    }

    public static void updateTaskState(DownloadTask task, File logFile) {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(logFile, "rw");
            raf.seek(Consts.STATE_POSTION);
            raf.writeByte(task.getState());
        } catch (Exception e) {
            LogUtils.error(MetaManager.class, e); // ignored the exception
        } finally {
            if ( null != raf ) {
                try {
                    raf.close();
                } catch (IOException ignored) {
                    LogUtils.error(MetaManager.class, ignored); // ignored the
                                                                // exception
                }
            }
        }
    }

    public static void deserializeSegmentsInformation(DownloadTask task, File logFile) throws ServiceException {
        int segments = task.getSegmentsNumber();
        StringBuilder segmentsInformation = new StringBuilder();
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(logFile, "rw");
            long position = 0;
            for ( int i = 0; i < segments; i++ ) {
                Segment item = new Segment();
                // Segment Id
                //
                position = Consts.FIRST_SEGMENT_POSITION + i * Consts.SEGMENT_LENGTH;
                raf.seek(position);
                segmentsInformation.append("Segment Id:");
                final int id = raf.readInt();
                segmentsInformation.append(id);
                item.setId(id);
                // Segment Start bytes
                //
                position += Consts.SEGMENT_ID_LENGTH;
                raf.seek(position);
                segmentsInformation.append("Segment start bytes:");
                final long startBytes = raf.readLong();
                segmentsInformation.append(startBytes);
                item.setStartBytes(startBytes);
                // Segment End bytes
                //
                position += Consts.SEGMENT_START_BYTES_LENGTH;
                segmentsInformation.append("Segment end bytes:");
                final long endBytes = raf.readLong();
                segmentsInformation.append(endBytes);
                item.setEndBytes(endBytes);
                // Segment state
                //
                position += Consts.SEGMENT_END_BYTES_LENGTH;
                segmentsInformation.append("Segment current bytes:");
                final long currentBytes = raf.readLong();
                segmentsInformation.append(currentBytes);
                item.setCurrentBytes(currentBytes);
                // Segment current bytes
                //
                position += Consts.SEGMENT_CURRENT_BYTES_LENGTH;
                segmentsInformation.append("Segment state:");
                final byte state = raf.readByte();
                segmentsInformation.append(state);
                item.setState(state);
                task.addSegment(item);
            }
        } catch (FileNotFoundException e) {
            LogUtils.error(MetaManager.class, e);
            throw new ServiceException(ExceptionMessage.LOG_FILE_WAS_NOT_FOUND + logFile.getName());
        } catch (IOException e) {
            LogUtils.error(MetaManager.class, e);
            throw new ServiceException(ExceptionMessage.LOG_FILE_IS_NOT_COMPLETE + logFile.getName());
        } finally {
            if ( null != raf ) {
                try {
                    raf.close();
                } catch (IOException ignored) {
                    LogUtils.error(MetaManager.class, ignored);
                }
            }
        }
        System.out.println("Segements Information:" + segmentsInformation.toString());
    }

    public static void serializeForNewState(DownloadTask task, File file) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // Task Id
            //
            int id = task.getId();
            randomAccessFile.seek(Consts.ID_POSITION);
            randomAccessFile.writeInt(id);
            // URL
            //
            byte[] urlBytes = task.getUrl().toString().getBytes("utf8");
            randomAccessFile.seek(Consts.URL_SIZE_POSITION);
            randomAccessFile.writeInt(urlBytes.length);
            randomAccessFile.seek(Consts.URL_POSITION);
            randomAccessFile.write(urlBytes);
            // REFER URL
            //
            randomAccessFile.seek(Consts.REFER_URL_SIZE_POSITION);
            byte[] referUrlBytes = task.getReferURL().toString().getBytes("utf8");
            randomAccessFile.writeInt(referUrlBytes.length);
            randomAccessFile.seek(Consts.REFER_URL_POSITION);
            randomAccessFile.write(referUrlBytes);
            // FOLDER
            //
            randomAccessFile.seek(Consts.FOLDER_SIZE_POSITION);
            byte[] folderBytes = task.getFolder().toString().getBytes("utf8");
            randomAccessFile.writeInt(folderBytes.length);
            randomAccessFile.seek(Consts.FOLDER_POSITION);
            randomAccessFile.write(folderBytes);
            // File name
            //
            randomAccessFile.seek(Consts.FILE_NAME_SIZE_POSITION);
            byte[] filenameBytes = task.getFileName().getBytes("utf8");
            randomAccessFile.writeInt(filenameBytes.length);
            randomAccessFile.seek(Consts.FILE_NAME_POSITION);
            randomAccessFile.write(filenameBytes);
            // Thread number
            //
            randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
            randomAccessFile.writeByte(task.getThreadNumber());
            // State
            //
            randomAccessFile.seek(Consts.STATE_POSTION);
            randomAccessFile.writeByte(StateEnum.New.ordinal());
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if ( null != randomAccessFile ) {
                    randomAccessFile.close();
                }
            } catch (IOException ignored) {
            }
        }
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
                raf.writeLong(segment.getCurrentBytes());
                position += Consts.SEGMENT_CURRENT_BYTES_LENGTH;
                raf.writeByte(StateEnum.Prepared.ordinal());
            }
        } catch (FileNotFoundException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } catch (IOException ignored) {
            LogUtils.error(MetaManager.class, ignored);
        } finally {
            if ( null != raf ) {
                try {
                    raf.close();
                } catch (IOException ignored) {
                    LogUtils.error(MetaManager.class, ignored);
                }
            }
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
                segment.setEndBytes(finalPartLength * ( seq + 1 ) - 1);
                segment.setCurrentBytes(finalPartLength * seq);
                System.out.println("segment id:" + i);
                System.out.println("segment start bytes:" + segment.getStartBytes());
                System.out.println("segment end bytes:" + segment.getEndBytes());
            } else {
                final long finalTotalLength = totalLength;
                segment.setStartBytes(finalPartLength * seq);
                segment.setCurrentBytes(finalPartLength * seq);
                segment.setEndBytes(finalTotalLength - 1);
                System.out.println("segment id:" + i);
                System.out.println("segment start bytes:" + segment.getStartBytes());
                System.out.println("segment end bytes:" + segment.getEndBytes());
            }
            task.addSegment(segment);
        }
    }

    private static File copy(File resFile, File objFolderFile) {
        if ( !resFile.exists() || !resFile.isFile() ) throw new IllegalStateException("resFile only accept file existed.");
        if ( !objFolderFile.exists() ) objFolderFile.mkdirs();
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
                if ( null != ins ) {
                    ins.close();
                }
                if ( null != outs ) {
                    outs.flush();
                    outs.close();
                }
            } catch (IOException ignored) {
                LogUtils.error(MetaManager.class, ignored);
            }
        }
        return objFile;
    }

    public static File move(File srcFile, File objFolderFile) {
        File targetFile = copy(srcFile, objFolderFile);
        delete(srcFile);
        return targetFile;
    }

    public static void delete(File file) {
        if ( !file.exists() ) return;
        if ( file.isFile() ) {
            int count = 0;
            while ( !file.delete() ) {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    LogUtils.error(MetaManager.class, ignored);
                }
                if ( count > 10 ) {
                    break;
                }
            }
        } else {
            for ( File f : file.listFiles() ) {
                delete(f);
            }
            file.delete();
        }
    }

    public static void updateTaskState(File metadataFile, StateEnum state) throws FileNotFoundException, IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(metadataFile, "rw");
            raf.seek(Consts.STATE_POSTION);
            raf.writeByte(state.ordinal());
        } finally {
            if ( null != raf ) {
                raf.close();
            }
        }
    }

    public static void updateSegmentDownloadProgress(File metadataFile, int segmentId, long currentBytes) throws FileNotFoundException, IOException {
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(metadataFile, "rw");
            final long segmentStartPosition = Consts.FIRST_SEGMENT_POSITION + Consts.SEGMENT_LENGTH * segmentId;
            final long endBytesPosition = segmentStartPosition + Consts.SEGMENT_ID_LENGTH + Consts.SEGMENT_START_BYTES_LENGTH;
            final long currentBytesPosition = segmentStartPosition + Consts.SEGMENT_ID_LENGTH + Consts.SEGMENT_START_BYTES_LENGTH
                    + Consts.SEGMENT_END_BYTES_LENGTH;
            final long statePosition = currentBytesPosition + Consts.SEGMENT_CURRENT_BYTES_LENGTH;
            // Log current bytes
            //
            raf.seek(currentBytesPosition);
            raf.writeLong(currentBytes);
            // Get end bytes
            //
            raf.seek(endBytesPosition);
            final long endBytes = raf.readLong();
            // Update segment state
            //
            if ( currentBytes < endBytes ) {
                raf.seek(statePosition);
                raf.writeByte(StateEnum.Started.ordinal());
            } else {
                raf.seek(statePosition);
                raf.writeByte(StateEnum.Finished.ordinal());
            }
        } finally {
            if ( null != raf ) {
                raf.close();
            }
        }
    }

    public static void initiateMetadataDirs() {
        createFolder("./meta/new");
        createFolder("./meta/prepared");
        createFolder("./meta/started");
        createFolder("./meta/failed");
        createFolder("./meta/finished");
        createFolder("./meta/paused");
    }

    private static void createFolder(String path) {
        File folder = new File(path);
        if ( !folder.exists() ) {
            folder.mkdir();
        }
    }

    public synchronized static List<DownloadTask> load(String root) {
        List<DownloadTask> results = new LinkedList<DownloadTask>();
        List<File> files = new LinkedList<File>();
        files = parseFolder(files, new File(root));
        for ( File file : files ) {
            DownloadTask task = MetaManager.deserializeHeadInformation(file);
            try {
                MetaManager.deserializeSegmentsInformation(task, file);
            } catch (ServiceException ignored) {
                LogUtils.error(MetaManager.class, ignored);
            }
            task.toString();
            results.add(task);
        }
        return results;
    }
    public synchronized static DownloadTask load(String root, int taskId) {
        DownloadTask result = null;
        List<File> files = new LinkedList<File>();
        files = parseFolder(files, new File(root));
        for ( File file : files ) {
            DownloadTask task = MetaManager.deserializeHeadInformation(file);
            try {
                MetaManager.deserializeSegmentsInformation(task, file);
            } catch (ServiceException ignored) {
                LogUtils.error(MetaManager.class, ignored);
            }
            if (taskId == task.getId()) {
                result = task;
            }
        }
        return result;
    }
    private static List<File> parseFolder(List<File> result, File root) {
        File[] listFiles = root.listFiles();
        if ( null == listFiles ) {
            return null;
        }
        for ( File file : listFiles ) {
            if ( file.isFile() && file.getName().contains("_log") ) {
                result.add(file);
            } else {
                parseFolder(result, file);
            }
        }
        return result;
    }

    public static void updateSegmentState(File metadataFile, DownloadTask task, StateEnum state) throws FileNotFoundException, IOException {
        RandomAccessFile raf = null;
        List<Segment> segments = task.getSegments();
        try {
            raf = new RandomAccessFile(metadataFile, "rw");
            for ( int i = 0; i < task.getSegmentsNumber(); i++ ) {
                final long segmentStartPosition = Consts.FIRST_SEGMENT_POSITION + Consts.SEGMENT_LENGTH * i;
                final long statePosition = segmentStartPosition + Consts.SEGMENT_ID_LENGTH + Consts.SEGMENT_START_BYTES_LENGTH
                        + Consts.SEGMENT_END_BYTES_LENGTH + Consts.SEGMENT_CURRENT_BYTES_LENGTH;
                // Update segment state
                //
                raf.seek(statePosition);
                byte lastState = raf.readByte();
                //
                //
                if ( StateEnum.Prepared.ordinal() == lastState || StateEnum.Started.ordinal() == lastState ) {
                    raf.seek(statePosition);
                    raf.writeByte(state.ordinal());
                }
                if ( StateEnum.Paused.ordinal() == lastState ) {
                    raf.seek(statePosition);
                    raf.writeByte(state.ordinal());
                    segments.get(i).setState((byte) state.ordinal());
                }
            }
        } finally {
            if ( null != raf ) {
                raf.close();
            }
        }
    }
}

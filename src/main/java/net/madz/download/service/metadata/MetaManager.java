package net.madz.download.service.metadata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MetaManager {

	public static void serialize(DownloadTask task, File file) {
		RandomAccessFile randomAccessFile = null;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.seek(Consts.URL_POSITION);
			randomAccessFile.writeChars(task.getUrl().toString());

			randomAccessFile.seek(Consts.REFER_URL_LENGTH);
			randomAccessFile.writeChars(task.getReferURL().toString());

			randomAccessFile.seek(Consts.FOLDER_POSITION);
			randomAccessFile.writeChars(task.getFolder().getAbsolutePath());

			randomAccessFile.seek(Consts.FILE_NAME_POSITION);
			randomAccessFile.writeChars(task.getFileName());

			randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
			randomAccessFile.writeLong(task.getTotalLength());

			randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
			randomAccessFile.writeInt(task.getSegmentsNumber());

			randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
			randomAccessFile.writeBoolean(task.isResumable());

			randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
			randomAccessFile.writeByte(task.getThreadNumber());

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

	public static void deserialize(File file) {
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

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

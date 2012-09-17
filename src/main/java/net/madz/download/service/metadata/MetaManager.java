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
			int position = 0;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getUrl().toString());

			position = position + Consts.URL_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getReferURL().toString());

			position = position + Consts.REFER_URL_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getFolder().getAbsolutePath());

			position = position + Consts.FOLDER_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getFileName());

			position = position + Consts.FILE_NAME_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeLong(task.getTotalLength());

			position = position + Consts.TOTAL_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeInt(task.getSegmentsNumber());

			position = position + Consts.SEGMENTS_NUMBER_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeBoolean(task.isResumable());

			position = position + Consts.RESUMABLE_FLAG_LENGTH;
			randomAccessFile.seek(position);
			randomAccessFile.writeByte(task.getThreadNumber());

			position = position + Consts.THREAD_NUMBER_LENGTH;
			randomAccessFile.seek(position);
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
		int pointer = 0;
		try {
			randomAccessFile = new RandomAccessFile(file, "rw");
			byte[] result = new byte[Consts.URL_LENGTH];
			randomAccessFile.readFully(result, 0, Consts.URL_LENGTH);
			headInfo.append(" URL:");
			headInfo.append(new String(result));
			pointer = pointer + Consts.URL_LENGTH;
			
			result = new byte[Consts.REFER_URL_LENGTH];
			randomAccessFile.seek(pointer);
			randomAccessFile.readFully(result, 0, Consts.REFER_URL_LENGTH);
			headInfo.append(" Refer URL:");
			headInfo.append(new String(result));
			pointer += Consts.REFER_URL_LENGTH;
			
			result = new byte[Consts.FOLDER_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.FOLDER_LENGTH);
			headInfo.append(" Folder:");
			headInfo.append(new String(result));
			pointer += Consts.FOLDER_LENGTH;
			
			result = new byte[Consts.FILE_NAME_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.FILE_NAME_LENGTH);
			headInfo.append(" File name:");
			headInfo.append(new String(result));
			pointer += Consts.FILE_NAME_LENGTH;
			
			randomAccessFile.seek(pointer); 
			headInfo.append(" Total Length:");
			headInfo.append(randomAccessFile.readLong());
			pointer += Consts.TOTAL_LENGTH;
			
			result = new byte[Consts.SEGMENTS_NUMBER_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.SEGMENTS_NUMBER_LENGTH);
			headInfo.append(" Segements Number:");
			headInfo.append(new String(result));
			pointer += Consts.SEGMENTS_NUMBER_LENGTH;
			
			result = new byte[Consts.RESUMABLE_FLAG_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.RESUMABLE_FLAG_LENGTH);
			headInfo.append(" Resumable:");
			headInfo.append(new String(result));
			pointer += Consts.RESUMABLE_FLAG_LENGTH;
			
			result = new byte[Consts.THREAD_NUMBER_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.THREAD_NUMBER_LENGTH);
			headInfo.append(" Thread Number:");
			headInfo.append(new String(result));
			pointer += Consts.THREAD_NUMBER_LENGTH;
			
			result = new byte[Consts.STATE_LENGTH];
			randomAccessFile.seek(pointer); 
			randomAccessFile.readFully(result, 0, Consts.STATE_LENGTH);
			headInfo.append(" State:");
			headInfo.append(new String(result));
			pointer += Consts.STATE_LENGTH;
			
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

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

			position = position + 3000;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getReferURL().toString());

			position = position + 3000;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getFolder().getAbsolutePath());

			position = position + 3000;
			randomAccessFile.seek(position);
			randomAccessFile.writeChars(task.getFileName());

			position = position + 128 * 3;
			randomAccessFile.seek(position);
			randomAccessFile.writeLong(task.getTotalLength());

			position = position + 8;
			randomAccessFile.seek(position);
			randomAccessFile.writeInt(task.getSegmentsNumber());

			position = position + 4;
			randomAccessFile.seek(position);
			randomAccessFile.writeBoolean(task.isResumable());

			position = position + 1;
			randomAccessFile.seek(position);
			randomAccessFile.writeByte(task.getThreadNumber());

			position = position + 1;
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

}

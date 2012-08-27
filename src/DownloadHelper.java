import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;


public class DownloadHelper {

//	public DownloadHelper(DownloadTask task) {
//		
//	}
	public static void main(String[] args) {
		File file  = new File("./test");
		try {
			RandomAccessFile rFile = new RandomAccessFile(file, "rw");
			rFile.write(128);
			rFile.seek(256);
			rFile.write(256);
			rFile.seek(512);
			rFile.write(512);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

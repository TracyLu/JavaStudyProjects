import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 
 * @author tracy
 *
 * The class describes download task meta information.
 * See download_task_meta_file.pptx
 */
public class DownloadHelper {

   private RandomAccessFile logfile;
   // public DownloadHelper(DownloadTask task) {
   //
   // }
   public static void main(String[] args) {
      File file = new File("./test");
      try {
         RandomAccessFile rFile = new RandomAccessFile(file, "rw");
         rFile.writeLong(128);
         rFile.seek(8);
         rFile.writeLong(256);
         rFile.seek(16);
         rFile.writeLong(512);
         rFile.seek(0);
         rFile.writeLong(234);
         rFile.seek(0);
         long readLong = rFile.readLong();
         System.out.println(readLong);
         rFile.seek(8);
         readLong = rFile.readLong();
         System.out.println(readLong);

         rFile.seek(16);
         readLong = rFile.readLong();
         System.out.println(readLong);
      }
      catch (FileNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static long readOffset(int seq) {
      // TODO Auto-generated method stub
      return 0;
   }

   public static void writeOffSet(int seq, long count) {
      // TODO Auto-generated method stub
      
   }

}

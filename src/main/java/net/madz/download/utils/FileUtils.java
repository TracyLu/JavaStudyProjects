package net.madz.download.utils;

import java.io.File;


public abstract class FileUtils {

    public static void delete(File file) {
        if ( !file.exists() ) return;
        if ( file.isFile() ) {
            int count = 0;
            while ( !file.delete() ) {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    LogUtils.error(FileUtils.class, ignored);
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
    
}

package net.madz.download.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.madz.download.engine.impl.metadata.MetaManager;


public abstract class FileUtils {

    public static File copy(File resFile, File objFolderFile) {
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

    public static File move(File srcFile, File objFolderFile) {
        File targetFile = copy(srcFile, objFolderFile);
        delete(srcFile);
        return targetFile;
    }
    
}

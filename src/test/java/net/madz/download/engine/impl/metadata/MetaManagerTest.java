package net.madz.download.engine.impl.metadata;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import net.madz.download.engine.DownloadTask;
import net.madz.download.engine.DownloadSegment;
import net.madz.download.engine.impl.metadata.MetaManager;
import net.madz.download.service.exception.ServiceException;

public class MetaManagerTest {

    public static void main(String[] args) {
        testDeserializeHeadInformation();
        testDeserializeSegmentsInformation();
    }

    public static void testCheckResumable() {
        String url = "http://dlc.sun.com.edgesuite.net/netbeans/6.7/final/bundles/netbeans-6.7-ml-macosx.dmg";
        String url2 = "http://dl_dir.qq.com/qqfile/qq/QQ2012/QQ2012Beta3.exe";
        try {
            boolean checkResumable = MetaManager.checkResumable(new URL(url));
            Assert.assertEquals(true, checkResumable);
            checkResumable = MetaManager.checkResumable(new URL(url2));
            Assert.assertEquals(true, checkResumable);
        } catch (MalformedURLException e) {
            fail("MalformedURLException occurred.");
        } catch (IOException e) {
            fail("IOException occurred.");
        }
    }

    public static void testDeserializeHeadInformation() {
        File file = new File("./meta/git.zip.meta");
        DownloadTask task = MetaManager.deserializeHeadInformation(file);
        System.out.println(task.toString());
    }

    public static void testDeserializeSegmentsInformation() {
        File file = new File("./meta/git.zip.meta");
        DownloadTask task = MetaManager.deserializeHeadInformation(file);
        try {
            MetaManager.deserializeSegmentsInformation(task, file);
        } catch (ServiceException e) {
            fail("Error occurred.");
        }
        for ( DownloadSegment segment : task.getSegments() ) {
            System.out.println(segment.toString());
        }
    }
}

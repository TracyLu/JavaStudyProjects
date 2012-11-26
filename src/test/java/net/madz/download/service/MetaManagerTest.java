package net.madz.download.service;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Assert;

import net.madz.download.service.exception.ErrorException;
import net.madz.download.service.metadata.DownloadTask;
import net.madz.download.service.metadata.MetaManager;
import net.madz.download.service.metadata.Segment;

import org.junit.Test;

public class MetaManagerTest {

    @Test
    public void testSerializeForPreparedState() {
        fail("Not yet implemented");
    }

    @Test
    public void testCreateDownloadTask() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetTotalLength() {
        fail("Not yet implemented");
    }

    @Test
    public void testCheckResumable() {
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

    @Test
    public void testDeserializeHeadInformation() {
        File file = null;
        file = new File("./meta/started/nt.dmg_log");
        DownloadTask task = MetaManager.deserializeHeadInformation(file);
        System.out.println(task.toString());
    }

    @Test
    public void testUpdateTaskStateDownloadTaskFile() {
        fail("Not yet implemented");
    }

    @Test
    public void testDeserializeSegmentsInformation() {
        File file = null;
        file = new File("./meta/started/nt.dmg_log");
        DownloadTask task = MetaManager.deserializeHeadInformation(file);
        try {
            MetaManager.deserializeSegmentsInformation(task, file);
        } catch (ErrorException e) {
            fail("Error occurred.");
        }
        for ( Segment segment : task.getSegments() ) {
            System.out.println(segment.toString());
        }
    }

    @Test
    public void testSerializeForNewState() {
        fail("Not yet implemented");
    }

    @Test
    public void testSerializeSegmentsInformation() {
        fail("Not yet implemented");
    }

    @Test
    public void testComputeSegmentsInformation() {
        fail("Not yet implemented");
    }

    @Test
    public void testMove() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateTaskStateFileStateEnum() {
        fail("Not yet implemented");
    }

    @Test
    public void testUpdateSegmentDownloadProgress() {
        fail("Not yet implemented");
    }

    @Test
    public void testInitiateMetadataDirs() {
        fail("Not yet implemented");
    }

    @Test
    public void testLoad() {
        fail("Not yet implemented");
    }
}

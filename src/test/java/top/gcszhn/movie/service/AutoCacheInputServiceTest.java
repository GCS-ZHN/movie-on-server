package top.gcszhn.movie.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.movie.AppTest;

public class AutoCacheInputServiceTest extends AppTest {
    @Autowired
    AutoCacheInputService autoCacheInputService;

    @After
    public void after() throws Exception {
        Thread.sleep(7000);
    }

    @Test
    public void testCrateInputStream() throws Exception {
        FileInputStream fis = new FileInputStream("test_params.json");
        AutoCacheInputStream acis = autoCacheInputService.createCacheInputStream(
            fis, "test_cache_params.json", 5000);
        try (acis) {
            System.out.println(new String(acis.readAllBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetInputStream() throws Exception {
        FileInputStream fis = new FileInputStream("test_params.json");
        AutoCacheInputStream acis = autoCacheInputService.createCacheInputStream(
            fis, "test_cache_params.json", 5000);
        byte[] data;
        try (acis) {
            data = acis.readAllBytes();
            System.out.println("Cache created with size: " + data.length);
        }
        InputStream is =  autoCacheInputService.getCacheInputStream("test_cache_params.json");
        assertEquals(data.length, autoCacheInputService.getCacheSize("test_cache_params.json"));
        assertNotNull(is);
        try (is) {
            System.out.println(new String(is.readAllBytes()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

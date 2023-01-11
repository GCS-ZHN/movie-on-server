package top.gcszhn.movie.service;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import top.gcszhn.movie.AppTest;
import top.gcszhn.movie.utils.LogUtils;

public class AsyncServiceTest extends AppTest {
    
    @Autowired
    AsyncService asyncService;

    @After
    public void waited() throws InterruptedException {
        Thread.sleep(10000);
    }

    @Test
    public void testRun() {
        asyncService.run(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.printMessage("Hello World");
        });
    }
}

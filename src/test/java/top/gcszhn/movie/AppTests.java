package top.gcszhn.movie;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppTests {

    @Test
    void contextLoads() {
    }
    @Test
    void utils() {
        Assertions.assertEquals(AppUtils.readableFileSize(-1), "0.00B");
        Assertions.assertEquals(AppUtils.readableFileSize(1), "1.00B");
        Assertions.assertEquals(AppUtils.readableFileSize(1<<10), "1.00KB");
        Assertions.assertEquals(AppUtils.readableFileSize(1<<20), "1.00MB");
        Assertions.assertEquals(AppUtils.readableFileSize(1<<30), "1.00GB");
    }
}

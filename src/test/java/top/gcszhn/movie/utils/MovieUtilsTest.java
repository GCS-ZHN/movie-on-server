package top.gcszhn.movie.utils;

import java.io.FileInputStream;

import org.junit.Before;
import org.junit.Test;

public class MovieUtilsTest {
    private String m3u8Content;

    @Before
    public void loadM3U8() throws Exception {
        try (FileInputStream in = new FileInputStream("test.m3u8") ) {
            this.m3u8Content = new String(in.readAllBytes());
        }
    }


    @Test
    public void testParseHlsList() throws Exception {
        MovieUtils.parseHlsList("http://localhost:8080/", this.m3u8Content).forEach(
            System.out::println
        );
    }
}

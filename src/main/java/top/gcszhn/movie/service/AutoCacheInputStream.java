package top.gcszhn.movie.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class AutoCacheInputStream extends InputStream {
    /**缓存输出 */
    FileOutputStream fos;
    /**输入 */
    InputStream in;
    /**流是否被关闭 */
    private @Getter boolean closed = false;
    /**流结束是的回调 */
    private List<Runnable> runWhenCloses = new ArrayList<>();

    public AutoCacheInputStream(InputStream in, String path, boolean reserved) throws IOException {
        super();
        File cacheFile = new File(path);
        if (!reserved) cacheFile.deleteOnExit();
        fos = new FileOutputStream(cacheFile);
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b != -1) {
            fos.write(b);
        }
        return b;
    }

    
    @Override
    public void close() throws IOException {
        super.close();
        fos.close();
        in.close();
        closed = true;
        for (Runnable runWhenClosed : runWhenCloses) {
            runWhenClosed.run();
        }
    }

    public AutoCacheInputStream whenClosed(Runnable runnable) {
        this.runWhenCloses.add(runnable);
        return this;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }
}

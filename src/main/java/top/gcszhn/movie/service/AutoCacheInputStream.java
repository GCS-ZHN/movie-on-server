package top.gcszhn.movie.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;

public class AutoCacheInputStream extends InputStream {

    FileOutputStream fos;
    InputStream in;
    private @Getter boolean closed = false;
    private Runnable runWhenClosed = null;

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
        if (runWhenClosed != null) {
            runWhenClosed.run();
        }
    }

    public AutoCacheInputStream whenClosed(Runnable runnable) {
        this.runWhenClosed = runnable;
        return this;
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }
}

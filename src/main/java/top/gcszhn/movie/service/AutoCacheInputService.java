package top.gcszhn.movie.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import top.gcszhn.movie.AppConfig;

@Service
public class AutoCacheInputService {
    @Value("${cache.reserved:true}")
    private boolean reserved;

    @Autowired
    private AppConfig appConfig;
    private Cache cache;
    @Autowired
    public AutoCacheInputService(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("autoCacheInput");
        if (this.cache == null) {
            throw new RuntimeException("Cache 'autoCacheInput' not found or created");
        }
    }
    public AutoCacheInputStream createCacheInputStream(InputStream in, String path, long expired) throws IOException {
        AutoCacheInputStream inputStream = new AutoCacheInputStream(in, path, reserved);
        final String streamKey = "stream:" + path;
        inputStream.whenClosed(()->{
            cache.put(streamKey, path);
            appConfig.cleanCache(cache.getName(), streamKey, expired);
        });
        return inputStream;
    }

    public InputStream getCacheInputStream(String key) throws IOException {
        String path = cache.get("stream:" + key, String.class);
        if (path == null) {
            return null;
        }
        return new FileInputStream(path);
    }

    public long getCacheSize(String key) {
        String path = cache.get("stream:" + key, String.class);
        if (path == null) {
            return 0;
        }
        return new File(path).length();
    }
}

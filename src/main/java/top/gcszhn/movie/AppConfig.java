/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.movie;

import lombok.Getter;
import lombok.Setter;
import top.gcszhn.movie.service.AuthService;
import top.gcszhn.movie.service.BaiduPanAuthService;
import top.gcszhn.movie.service.BaiduPanResourceService;
import top.gcszhn.movie.service.LocalAuthService;
import top.gcszhn.movie.service.LocalResourceService;
import top.gcszhn.movie.service.ResourceService;
import top.gcszhn.movie.utils.LogUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class AppConfig implements WebMvcConfigurer, EnvironmentAware {
    /** 统一字符集 */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /** 资源路径 */
    private @Getter @Setter String resourcePath;

    /** 缓存管理器 */
    @Autowired
    private @Getter CacheManager cacheManager;

    /** 资源类型 */
    @Value("#{'${resource.type:mp4}'.split(',')}")
    private @Getter List<String> resourceType;

    /** 资源后端 */
    @Value("${resource.backend:local}")
    private @Getter String resourceBackend;

    /** 服务器端口 */
    @Value("${server.port:8080}")
    private @Getter int port;

    /** 临时文件夹 */
    private @Getter File tmpDir;

    public AppConfig() {
        LogUtils.printMessage("AppConfig init", LogUtils.Level.DEBUG);
        tmpDir = new File(System.getProperty("java.io.tmpdir"), "movie");
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        if (!tmpDir.isDirectory()) {
            throw new RuntimeException("Failed to create tmp dir: " + tmpDir.getAbsolutePath());
        }
        LogUtils.printMessage("App tmpdir: " + tmpDir.getAbsolutePath(), LogUtils.Level.DEBUG);
    }

    @Override
    public void setEnvironment(Environment environment) {
        switch (getResourceBackend()) {
            case "local":
                setResourcePath(environment.getProperty("resource.path", "."));
                break;
            case "baidupan":
                setResourcePath(environment.getProperty("resource.path", "/"));
                break;
            default:
                throw new RuntimeException("Unknown resource backend");
        }
        LogUtils.printMessage("Resource path: " + getResourcePath());
        LogUtils.printMessage("Resource Backend: " + getResourceBackend());
    }

    @Bean
    public ResourceService movieService() {
        switch (getResourceBackend()) {
            case "local":
                return new LocalResourceService();
            case "baidupan":
                return new BaiduPanResourceService();
            default:
                throw new RuntimeException("Unknown resource backend");
        }
    }

    @Bean
    public AuthService authService() {
        switch (getResourceBackend()) {
            case "local":
                return new LocalAuthService();
            case "baidupan":
                return new BaiduPanAuthService();
            default:
                throw new RuntimeException("Unknown resource backend");
        }
    }

    /**
     * 配置tomcat的web服务器, 允许RFC 3986的路径中包含特殊字符
     * 
     * @return
     */
    @Bean
    public TomcatServletWebServerFactory webServerFactoryCustomizer() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("relaxedPathChars", "\"<>[\\]^`{|}");
            connector.setProperty("relaxedQueryChars", "\"<>[\\]^`{|}");
            connector.setProperty("rejectIllegalHeader", "false");
        });
        return factory;
    }

    @Async
    public void cleanCache(String cacheName, String key, long delay) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            LogUtils.printMessage(
                String.format("Cache [%s]/[%s] not found", cacheName, key), LogUtils.Level.INFO);
            return;
        }
        LogUtils.printMessage(
            String.format("Cache [%s]/[%s] will be cleaned after %dms", cacheName, key, delay), 
            LogUtils.Level.DEBUG);
        try {
            Thread.sleep(delay);
            if (key.startsWith("stream:")) {
                String path = cache.get(key, String.class);
                new File(path).delete();
            }
            cache.evict(key);
            LogUtils.printMessage(
                String.format("Cache [%s]/[%s] cleaned", cacheName, key),
                LogUtils.Level.DEBUG);
        } catch (Exception e) {
            LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
        }
    }
}

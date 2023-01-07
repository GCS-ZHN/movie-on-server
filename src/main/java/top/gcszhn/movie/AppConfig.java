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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class AppConfig implements WebMvcConfigurer, EnvironmentAware {
    /**统一字符集 */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**资源路径 */
    private @Getter @Setter String resourcePath;

    /**资源类型 */
    @Value("#{'${resource.type:mp4}'.split(',')}")
    private @Getter List<String> resourceType;

    /**资源后端 */
    @Value("${resource.backend:local}")
    private @Getter String resourceBackend;

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
                throw new RuntimeException("未知的资源后端");
        }
    }

    @Bean
    public ResourceService movieService() {
        switch (getResourceBackend()) {
            case "local":
                return new LocalResourceService();
            case "baidupan":
                return new BaiduPanResourceService();
            default:
                throw new RuntimeException("未知的资源后端");
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
                throw new RuntimeException("未知的资源后端");
        }
    }

    /**
     * 配置tomcat的web服务器, 允许RFC 3986的路径中包含特殊字符
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
}

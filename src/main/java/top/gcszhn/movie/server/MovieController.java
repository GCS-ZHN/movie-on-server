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
package top.gcszhn.movie.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.service.ResourceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;


@RestController
public class MovieController {
    @Autowired
    AppConfig config;

    @Autowired  
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    ResourceService movieService;

    /**
     * 获取电影列表
     * @param target 目标目录
     * @return 电影列表
     * @throws IOException IO异常
     */
    @GetMapping("query/movies")
    public List<Map<String, String>> getMovies(@RequestParam String target) throws IOException {
        if (config.getResourceBackend().equals("baidupan")) {
            String token = "121.abdd85946ef7cd33afe4b2bf8e911c11.YgJH9i9PdKPKDxaL9GWYhfvwtZOOi1nu4Qrj0s8.T7KB9g";
            target = token + ":" + target;
        }
        return movieService.getResourceList(target, config.getResourcePath(), config.getResourceType());
    }

    /**
     * 返回资源流
     * @param target 目标文件
     * @throws IOException IO异常
     * @throws URISyntaxException
     */
    @GetMapping("/stream/**")
    public ResponseEntity<InputStreamResource> fetchResourceStream() throws IOException, URISyntaxException {
        String target = URLDecoder.decode(request.getRequestURI(), AppConfig.DEFAULT_CHARSET).replace("/stream", "");
        if (config.getResourceBackend().equals("baidupan")) {
            String token = "121.abdd85946ef7cd33afe4b2bf8e911c11.YgJH9i9PdKPKDxaL9GWYhfvwtZOOi1nu4Qrj0s8.T7KB9g";
            target = token + ":" + target;
        }
        return movieService.getResourceStream(target, config.getResourcePath());
    }
}

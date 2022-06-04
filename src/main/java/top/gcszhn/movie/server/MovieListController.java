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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.AppUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;


@RestController
public class MovieListController {
    @Autowired
    AppConfig config;

    @Autowired
    HttpServletResponse response;

    /**
     * 获取电影列表
     * @param target 目标目录
     * @return 电影列表
     * @throws IOException IO异常
     */
    @GetMapping("query/movies")
    public List<Map<String, String>> getMovies(@RequestParam String target) throws IOException {
        List<Map<String, String>> movies = new ArrayList<>();

        final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:sss");
        final File dir = new File(config.getResourcePath(), target);
        if (!dir.exists() || !dir.isDirectory()) {
            return movies;
        }
        for (File pathname: dir.listFiles()) {
            boolean hidden = pathname.getName().startsWith(".");
            boolean flag = pathname.isDirectory() && !hidden;
            for (String type: config.getResourceType()) {
                if (pathname.getName().toLowerCase().endsWith("." + type) && !hidden) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                Map<String, String> fileInfo = new HashMap<>();
                fileInfo.put("name", pathname.getName());
                fileInfo.put("type", pathname.isDirectory() ? "directory": "file");
                fileInfo.put("create", dateFormat.format(Files.readAttributes(pathname.toPath(), BasicFileAttributes.class).creationTime().toMillis()));
                fileInfo.put("size", AppUtils.readableFileSize(pathname.length()));
                movies.add(fileInfo);
            }
        }
        return movies;
    }
}

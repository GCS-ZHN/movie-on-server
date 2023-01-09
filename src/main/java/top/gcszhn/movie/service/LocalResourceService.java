package top.gcszhn.movie.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

import top.gcszhn.movie.AppUtils;


public class LocalResourceService implements ResourceService {


    public List<Map<String, String>> getResourceList(String target, String resourcePath, List<String> resoureType) throws IOException {
        List<Map<String, String>> movies = new ArrayList<>();

        final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:sss");
        final File dir = new File(resourcePath, target);
        if (!dir.exists() || !dir.isDirectory()) {
            return movies;
        }
        for (File pathname: dir.listFiles()) {
            boolean hidden = pathname.getName().startsWith(".");
            boolean flag = pathname.isDirectory() && !hidden;
            for (String type: resoureType) {
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

    @Override
    public ResponseEntity<InputStreamResource> getResourceStream(String target, String resourcePath)
            throws IOException, URISyntaxException {
            File file = new File(resourcePath, target);
            if (!file.exists() || file.isDirectory()) {
                return null;
            }
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .header("Content-Type", "application/octet-stream")
                .header("Content-Length", String.valueOf(file.length()))
                .body(new InputStreamResource(Files.newInputStream(file.toPath())));
    }



    
}

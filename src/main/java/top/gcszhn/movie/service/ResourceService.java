package top.gcszhn.movie.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;

public interface ResourceService {
    /**
     * 获取电影列表
     * @param target 目标目录
     * @param resourcePath 资源根路径
     * @param resoureType 资源类型
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> getResourceList(String target, String resourcePath, List<String> resoureType) 
        throws IOException;
    
     /**
      * 拉取资源流
      * @param target 目标文件
      * @param resourcePath 资源根路径
      * @return
      * @throws IOException
      * @throws URISyntaxException
      */
    public ResponseEntity<InputStreamResource> getResourceStream(String target, String resourcePath) 
        throws IOException, URISyntaxException;
}

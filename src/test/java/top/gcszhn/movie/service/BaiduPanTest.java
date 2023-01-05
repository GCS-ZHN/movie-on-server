package top.gcszhn.movie.service;

import java.io.FileWriter;
import java.io.IOException;

import org.apache.http.client.utils.HttpClientUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


@RunWith(SpringRunner.class)
@SpringBootTest
public class BaiduPanTest {
    @Autowired
    BaiduPanService baiduPanService;

    private String code = "3e0e08eb995081d6d8ecbd31ad853fda";
    private String accessToken = "121.a256dc8e81e06d159eac183d9b8ea5ea.YGwCNdp6X-oIHOAGICjgiF8z7PcRDQ6NGe5fN-p.0CfZBg";
    private String refreshToken = "122.a83ccb47216e71e371d34a7b3965f274.Y5_YLWZrIHhAXh8jV3q36JjxquZGbC468Y02UNw.IxYNkA";
    private String fsIds = "[424916916046646]";
    private String dlink = "https://d.pcs.baidu.com/file/6d8400216t84ffd76ebd769f58cefc59?fid=1193395696-250528-906289219587990&rt=pr&sign=FDtAERV-DCb740ccc5511e5e8fedcff06b081203-obGk3FazFZGk8QuneqyZPxd27%2Fw%3D&expires=8h&chkbd=0&chkv=3&dp-logid=3691441786451200670&dp-callid=0&dstime=1672926667&r=465493077&origin_appid=29530371&file_type=0";
    @Test
    public void testAuthorize() {
        try {
            baiduPanService.authorize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAccessToken() throws IOException {
        JSONObject response = baiduPanService.getAccessToken(code);
        System.out.println(response.getString("access_token"));
        System.out.println(response.getString("refresh_token"));
    }

    @Test
    public void testRefreshToken() throws IOException {
        JSONObject response = baiduPanService.refreshToken(refreshToken);
        System.out.println(response.getString("access_token"));
    }

    @Test
    public void testGetUserInfo() throws IOException {
        JSONObject response = baiduPanService.getUserInfo(accessToken);
        System.out.println(response.getString("baidu_name"));
    }

    @Test
    public void testGetFileList() throws IOException {
        JSONArray FileList = baiduPanService.getFileList(
            accessToken,
            "/我的资源/movie",
            false);

        FileList.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.println(fileJson.getString("server_filename"));
            System.out.println(fileJson.getString("fs_id"));
        });
    }

    @Test
    public void testGetFileMetaInfoByIds() throws IOException {
        JSONArray response = baiduPanService.getFileMetaInfoByIds(
            accessToken,
            JSONArray.parseArray(fsIds),
            true);
        response.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.println(fileJson.getString("filename"));
            System.out.print((fileJson.getIntValue("isdir")==1? "dir" : "file") + " ");
            System.out.println(fileJson.getString("dlink"));
        });
    }

    @Test
    public void testGetFileMetaInfoByDir() throws IOException {
        JSONArray response = baiduPanService.getFileMetaInfoByDir(
            accessToken,
            "/我的资源/movie/",
            true);
        response.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("filename") + " ");
            System.out.print((fileJson.getIntValue("isdir")==1? "dir" : "file") + " ");
            System.out.println(fileJson.getString("dlink"));
        });
    }

    @Test
    public void testDownloadFile() throws IOException {
        baiduPanService.downloadFile(dlink, accessToken, "68756b_0000.ts");
    }

    @Test
    public void testGetOnlyFiles() throws IOException {
        JSONArray files = baiduPanService.getOnlyFiles(
            accessToken,
            "/我的资源/movie/舒淇-洗澡.hls");
        files.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("server_filename") + " ");
            System.out.println(fileJson.getString("fs_id"));
        });
    }

    @Test
    public void convertHLSFile() throws IOException {
        JSONArray response = baiduPanService.getFileMetaInfoByDir(
            accessToken,
            "/我的资源/movie/v.hls",
            true);
        JSONObject fileDlinks = new JSONObject();
        String m3u8file= null;
        for (Object file : response) {
            JSONObject fileJson = (JSONObject) file;
            if (fileJson.getIntValue("isdir") == 1) {
                continue;
            }
            fileDlinks.put(fileJson.getString("filename"), fileJson.getString("dlink"));
            if (fileJson.getString("filename").endsWith(".m3u8")) {
                m3u8file = fileJson.getString("filename");
            }
        }
        if (m3u8file != null) {
            String m3u8 = baiduPanService.getFileText(fileDlinks.getString(m3u8file), accessToken);
            String[] lines = m3u8.split("\n");
            try (FileWriter writer = new FileWriter("v.m3u8")) {
                for (String line : lines) {
                    if (line.startsWith("#")) {
                        writer.write(line + "\n");
                        continue;
                    }
                    String link = fileDlinks.getString(line) + "&access_token=" + accessToken;
                    writer.write(link + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

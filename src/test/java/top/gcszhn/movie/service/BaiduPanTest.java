package top.gcszhn.movie.service;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.utils.HttpClientUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.gcszhn.movie.utils.LogUtils;


@RunWith(SpringRunner.class)
@SpringBootTest
public class BaiduPanTest {
    @Autowired
    BaiduPanService baiduPanService;

    static class Param {
        public String code = "";
        public String accessToken = "";
        public String refreshToken = "";
        public JSONArray fsIds = new JSONArray();
        public String dlink = "";
        public String dir = "";
        public String hlsDir = "";
    }

    Param param;

    @Before
    public void loadParams() {
        LogUtils.printMessage("Loading params...");
        // load param from test_params.json file
        try (FileInputStream fis = new FileInputStream("test_params.json")) {
            this.param = JSONObject.parseObject(fis, Param.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void saveParams() {
        LogUtils.printMessage("Saving params...");
        // save param to test_params.json file, include null value
        try (FileWriter fw = new FileWriter("test_params.json")) {
            fw.write(JSONObject.toJSONString(param, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        if (param.code == null) {
            LogUtils.printMessage("Please set code!");
        } else {
            JSONObject response = baiduPanService.getAccessToken(param.code);
            System.out.println("access_token: " + response.getString("access_token"));
            System.out.println("refresh_token: " + response.getString("refresh_token"));
            param.accessToken = response.getString("access_token");
            param.refreshToken = response.getString("refresh_token");
            param.code = null;
        }
    }

    @Test
    public void testRefreshToken() throws IOException {
        JSONObject response = baiduPanService.refreshToken(param.refreshToken);
        System.out.println("access_token: " + response.getString("access_token"));
        System.out.println("refresh_token: " + response.getString("refresh_token"));
        param.accessToken = response.getString("access_token");
        param.refreshToken = response.getString("refresh_token");
    }

    @Test
    public void testGetUserInfo() throws IOException {
        JSONObject response = baiduPanService.getUserInfo(param.accessToken);
        response.forEach((key, value) -> {
            System.out.println(key + ": " + value);
        });
    }

    @Test
    public void testGetFileList() throws IOException {
        JSONArray FileList = baiduPanService.getFileList(
            param.accessToken,
            param.dir,
            false);

        FileList.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("server_filename") + "\t");
            System.out.println(fileJson.getString("fs_id"));
        });
    }

    @Test
    public void testGetFileMetaInfoByIds() throws IOException {
        JSONArray response = baiduPanService.getFileMetaInfoByIds(
            param.accessToken,
            param.fsIds,
            true);
        response.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("filename") + "\t");
            System.out.print((fileJson.getIntValue("isdir")==1? "dir" : "file") + "\t");
            System.out.println(fileJson.getString("dlink"));
        });
    }

    @Test
    public void testGetFileMetaInfoByDir() throws IOException {
        JSONArray response = baiduPanService.getFileMetaInfoByDir(
            param.accessToken,
            param.dir,
            true);
        response.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("filename") + "\t");
            System.out.print((fileJson.getIntValue("isdir")==1? "dir" : "file") + "\t");
            System.out.println(fileJson.getString("dlink"));
        });
    }

    @Test
    public void testDownloadFile() throws IOException, URISyntaxException {
        baiduPanService.downloadFile(param.accessToken, param.dlink, "v.m3u8");
    }

    @Test
    public void testGetOnlyFiles() throws IOException {
        JSONArray files = baiduPanService.getOnlyFiles(
            param.accessToken,
            param.dir);
        files.forEach(file -> {
            JSONObject fileJson = (JSONObject) file;
            System.out.print(fileJson.getString("server_filename") + "\t");
            System.out.println(fileJson.getString("fs_id"));
        });
    }

    @Test
    public void convertHLSFile() throws IOException, URISyntaxException {
        JSONArray response = baiduPanService.getFileMetaInfoByDir(
            param.accessToken,
            param.hlsDir,
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
            LogUtils.printMessage("m3u8 file: " + m3u8file);
            String m3u8 = baiduPanService.getFileText(param.accessToken, fileDlinks.getString(m3u8file));
            if (m3u8 == null) {
                LogUtils.printMessage("Fetch m3u8 file failed!");
                return;
            }
            String[] lines = m3u8.split("\n");
            try (FileWriter writer = new FileWriter("converted.m3u8")) {
                for (String line : lines) {
                    if (line.startsWith("#")) {
                        writer.write(line + "\n");
                        continue;
                    }
                    String link = fileDlinks.getString(line) + "&access_token=" + param.accessToken;
                    writer.write(link + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package top.gcszhn.movie.service;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.gcszhn.movie.utils.HttpClientUtils;
import top.gcszhn.movie.utils.LogUtils;

@Service
public class BaiduPanService {
    @Value("${baidu.client_id}")
    private String clientId;
    @Value("${baidu.client_secret}")
    private String clientSecret;
    @Value("${baidu.redirect_uri}")
    private String redirectUri;
    private static final String AUTHORIZE_URL = "http://openapi.baidu.com/oauth/2.0/authorize";
    private static final String ACCESS_TOKEN_URL = "https://openapi.baidu.com/oauth/2.0/token";
    private static final String NAS_URL = "https://pan.baidu.com/rest/2.0/xpan/nas";
    private static final String FILE_URL = "https://pan.baidu.com/rest/2.0/xpan/file";
    private static final String MULTIMEDIA_URL = "https://pan.baidu.com/rest/2.0/xpan/multimedia";
    public void authorize() throws IOException {
        String target = AUTHORIZE_URL + "?response_type=code&scope=basic,netdisk&qrcode=1&client_id=" + clientId + "&redirect_uri=" + redirectUri;
        LogUtils.printMessage("target: " + target);
        System. setProperty("java.awt.headless", "false");
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(URI.create(target));
    }

    public JSONObject getAccessToken(String code) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = ACCESS_TOKEN_URL + "?grant_type=authorization_code&code=" + code + "&client_id=" + clientId + "&client_secret=" + clientSecret + "&redirect_uri=" + redirectUri;
            LogUtils.printMessage("target: " + target);
            String response = client.doGetText(target);
            return JSONObject.parseObject(response);
        }
    }

    public JSONObject refreshToken(String refreshToken) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = ACCESS_TOKEN_URL + "?grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id=" + clientId + "&client_secret=" + clientSecret;
            LogUtils.printMessage("target: " + target);
            String response = client.doGetText(target);
            return JSONObject.parseObject(response);
        }
    }

    public JSONObject getUserInfo(String accessToken) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = NAS_URL + "?method=uINFO&access_token=" + accessToken;
            LogUtils.printMessage("target: " + target);
            String response = client.doGetText(target);
            return JSONObject.parseObject(response);
        }
    }

    public JSONArray getFileList(
        String accessToken, 
        String dir,
        boolean folder) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = FILE_URL + "?method=list&access_token=" + accessToken + "&dir=" + dir + "&folder=" + (folder?1:0);
            LogUtils.printMessage("target: " + target);
            String response = client.doGetText(target);
            return JSONObject.parseObject(response).getJSONArray("list");
        }
    }

    public JSONArray getFileMetaInfoByIds(
        String accessToken,
        JSONArray fsids,
        boolean dlink) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = MULTIMEDIA_URL + "?method=filemetas&access_token=" + accessToken + "&fsids=" + fsids + "&dlink=" + (dlink?1:0);
            LogUtils.printMessage("target: " + target);
            String response = client.doGetText(target);
            return JSONObject.parseObject(response).getJSONArray("list");
        }
    }

    public JSONArray getFileMetaInfoByDir(
        String accessToken,
        String dir,
        boolean dlink) throws IOException {
        JSONArray list = getFileList(accessToken, dir, false);
        JSONArray fsids = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject file = list.getJSONObject(i);
            fsids.add(file.getLongValue("fs_id"));

        }
        return getFileMetaInfoByIds(accessToken, fsids, dlink);
    }

    public void downloadFile(String dlink, String accessToken, String path) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = dlink + "&access_token=" + accessToken;
            LogUtils.printMessage("target: " + target);
            client.doDownload(path, target);
        }
    }

    public String getFileText(String dlink, String accessToken) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            String target = dlink + "&access_token=" + accessToken;
            LogUtils.printMessage("target: " + target);
            return client.doGetText(target);
        }
    }

    public JSONArray getOnlyFiles(String accessToken, String dir) throws IOException {
        JSONArray list = getFileList(accessToken, dir, false);
        JSONArray files = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject file = list.getJSONObject(i);
            if (file.getIntValue("isdir") == 0) {
                files.add(file);
            }
        }
        return files;
    }
}

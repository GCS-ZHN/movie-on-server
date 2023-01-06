package top.gcszhn.movie.service;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.utils.HttpClientUtils;
import top.gcszhn.movie.utils.HttpDataPair;
import top.gcszhn.movie.utils.LogUtils;

@Service
public class BaiduPanService {
    /**
     * 百度云盘第三方应用AppKey
     */
    @Value("${baidu.client_id}")
    private String clientId;
    /**
     * 百度云盘第三方应用AppSecret
     */
    @Value("${baidu.client_secret}")
    private String clientSecret;
    /**
     * 百度云盘第三方应用回调地址
     */
    @Value("${baidu.redirect_uri}")
    private String redirectUri;
    /**
     * 百度云盘第三方应用授权地址
     */
    private static final String AUTHORIZE_URL = "http://openapi.baidu.com/oauth/2.0/authorize";
    /**
     * 百度云盘第三方应用获取AccessToken地址
     */
    private static final String ACCESS_TOKEN_URL = "https://openapi.baidu.com/oauth/2.0/token";
    /**
     * 百度云盘第三方应用获取用户信息地址
     */
    private static final String NAS_URL = "https://pan.baidu.com/rest/2.0/xpan/nas";
    /**
     * 百度云盘第三方应用获取文件列表地址
     */
    private static final String FILE_URL = "https://pan.baidu.com/rest/2.0/xpan/file";
    /**
     * 百度云盘第三方应用获取文件元信息地址
     */
    private static final String MULTIMEDIA_URL = "https://pan.baidu.com/rest/2.0/xpan/multimedia";
    /**
     * 调用本地浏览器进行校验码授权，获取授权码
     * @throws IOException
     * @throws URISyntaxException
     */
    public void authorize() throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(AUTHORIZE_URL, AppConfig.DEFAULT_CHARSET)
            .addParameter("response_type", "code")
            .addParameter("scope", "basic,netdisk")
            .addParameter("qrcode", "1")
            .addParameter("client_id", clientId)
            .addParameter("redirect_uri", redirectUri);
        String target = builder.build().toString();
        System. setProperty("java.awt.headless", "false");
        Desktop desktop = Desktop.getDesktop();
        LogUtils.printMessage("Open the following URL and grant access to your account:");
        LogUtils.printMessage(target);
        desktop.browse(URI.create(target));
    }

    public JSONObject getAccessToken(String code) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("code", code),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("redirect_uri", redirectUri)
            );
            String response = client.doGetText(ACCESS_TOKEN_URL, param);
            return JSONObject.parseObject(response);
        }
    }

    public JSONObject refreshToken(String refreshToken) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("grant_type", "refresh_token"),
                new BasicNameValuePair("refresh_token", refreshToken),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret)
            );
            String response = client.doGetText(ACCESS_TOKEN_URL, param);
            return JSONObject.parseObject(response);
        }
    }

    public JSONObject getUserInfo(String accessToken) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "uinfo"),
                new BasicNameValuePair("access_token", accessToken)
            );
            String response = client.doGetText(NAS_URL, param);
            return JSONObject.parseObject(response);
        }
    }

    public JSONArray getFileList(String accessToken, String dir, boolean folder) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "list"),
                new BasicNameValuePair("access_token", accessToken),
                new BasicNameValuePair("dir", dir),
                new BasicNameValuePair("folder", folder?"1":"0")
            );
            String response = client.doGetText(FILE_URL, param);
            return JSONObject.parseObject(response).getJSONArray("list");
        }
    }

    public JSONArray getFileMetaInfoByIds(String accessToken, JSONArray fsids, boolean dlink) throws IOException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "filemetas"),
                new BasicNameValuePair("access_token", accessToken),
                new BasicNameValuePair("fsids", fsids.toJSONString()),
                new BasicNameValuePair("dlink", dlink?"1":"0")
            );
            String response = client.doGetText(MULTIMEDIA_URL, param);
            return JSONObject.parseObject(response).getJSONArray("list");
        }
    }

    public JSONArray getFileMetaInfoByDir(String accessToken, String dir, boolean dlink) throws IOException {
        JSONArray list = getFileList(accessToken, dir, false);
        JSONArray fsids = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject file = list.getJSONObject(i);
            fsids.add(file.getLongValue("fs_id"));

        }
        return getFileMetaInfoByIds(accessToken, fsids, dlink);
    }

    public void downloadFile(String accessToken, String dlink, String path) throws IOException, URISyntaxException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("access_token", accessToken)
            );
            client.doDownload(
                path, 
                new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString());
        }
    }

    public HttpDataPair getFile(String accessToken, String dlink) throws IOException, URISyntaxException {
        HttpClientUtils client = new HttpClientUtils();
        List<NameValuePair> param = List.of(
            new BasicNameValuePair("access_token", accessToken)
        );
        String uri = new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString();
        HttpDataPair dataPair = client.doGet(uri); 
        dataPair.setCloseClient(true);
        return dataPair;
    }

    public HttpDataPair getFileByPath(String accessToken, String path) throws IOException, URISyntaxException {
        String dir = path.substring(0, path.lastIndexOf("/") + 1);
        String basename = path.substring(path.lastIndexOf("/") + 1);
        JSONArray list = getFileMetaInfoByDir(accessToken, dir, true);
        for (int i = 0; i < list.size(); i++) {
            JSONObject file = list.getJSONObject(i);
            if (file.getString("filename").equals(basename)) {
                return getFile(accessToken, file.getString("dlink"));
            }
        }
        return null;
    }

    public String getFileText(String accessToken, String dlink) throws IOException, URISyntaxException {
        try (HttpClientUtils client = new HttpClientUtils()) {
            List<NameValuePair> param = List.of(
                new BasicNameValuePair("access_token", accessToken)
            );
            return client.doGetText(new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString());
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

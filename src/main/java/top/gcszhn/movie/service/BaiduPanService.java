package top.gcszhn.movie.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.Getter;
import lombok.Setter;
import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.utils.HttpClientUtils;
import top.gcszhn.movie.utils.HttpDataPair;
import top.gcszhn.movie.utils.LogUtils;
import top.gcszhn.movie.utils.WebBrowerUtils;

@Service
public class BaiduPanService implements AutoCloseable {
    /**
     * 百度云盘第三方应用AppKey
     */
    @Value("${baidupan.client_id}")
    private @Getter @Setter String clientId;
    /**
     * 百度云盘第三方应用AppSecret
     */
    @Value("${baidupan.client_secret}")
    private @Getter @Setter String clientSecret;
    /**
     * 百度云盘第三方应用回调地址
     */
    @Value("${baidupan.redirect_url}")
    private @Getter @Setter String redirectUrl;

    private HttpClientUtils client = new HttpClientUtils();
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
     * 
     * @throws IOException
     */
    public void authorize() throws IOException {
        String target = getAuthURL();
        LogUtils.printMessage("Open the following URL and grant access to your account:");
        LogUtils.printMessage(target);
        WebBrowerUtils.openUrl(target);
    }

    public String getAuthURL() {
        try {
            URIBuilder builder = new URIBuilder(AUTHORIZE_URL, AppConfig.DEFAULT_CHARSET)
                    .addParameter("response_type", "code")
                    .addParameter("scope", "basic,netdisk")
                    .addParameter("qrcode", "1")
                    .addParameter("client_id", clientId)
                    .addParameter("redirect_uri", redirectUrl);
            return builder.toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public JSONObject getAccessToken(String code) throws IOException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("grant_type", "authorization_code"),
                new BasicNameValuePair("code", code),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret),
                new BasicNameValuePair("redirect_uri", redirectUrl));
        Optional<String> response = Optional.ofNullable(client.doGetText(ACCESS_TOKEN_URL, param));
        return response.map(s -> JSONObject.parseObject(s))
                .orElseGet(() -> {
                    LogUtils.printMessage("Failed to get access token.");
                    return null;
                });

    }

    public JSONObject refreshToken(String refreshToken) throws IOException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("grant_type", "refresh_token"),
                new BasicNameValuePair("refresh_token", refreshToken),
                new BasicNameValuePair("client_id", clientId),
                new BasicNameValuePair("client_secret", clientSecret));
        Optional<String> response = Optional.ofNullable(client.doGetText(ACCESS_TOKEN_URL, param));
        return response.map(s -> JSONObject.parseObject(s))
                .orElseGet(() -> {
                    LogUtils.printMessage("Failed to refresh token.");
                    return null;
                });
    }

    public JSONObject getUserInfo(String accessToken) throws IOException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "uinfo"),
                new BasicNameValuePair("access_token", accessToken));
        Optional<String> response = Optional.ofNullable(client.doGetText(NAS_URL, param));
        return response.map(s -> JSONObject.parseObject(s))
                .orElseGet(() -> {
                    LogUtils.printMessage("Failed to get user info.");
                    return null;
                });
    }

    /**
     * 获取文件列表，包括文件和文件夹，最多返回1000条记录，超过1000条记录需要分页获取
     * @param accessToken 授权码
     * @param dir 目录路径，根目录为"/"
     * @param folder 是否只返回文件夹
     * @param start 分页起始位置
     * @param limit 分页获取条数
     * @return
     * @throws IOException
     */
    public JSONArray getFileList(String accessToken, String dir, boolean folder, int start, int limit) throws IOException {
        if (limit > 1000) {
            limit = 1000;
        }
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "list"),
                new BasicNameValuePair("access_token", accessToken),
                new BasicNameValuePair("dir", dir),
                new BasicNameValuePair("folder", folder ? "1" : "0"),
                new BasicNameValuePair("start", String.valueOf(start)),
                new BasicNameValuePair("limit", String.valueOf(limit)));
        Optional<String> response = Optional.ofNullable(client.doGetText(FILE_URL, param));
        return response.map(s -> JSONObject.parseObject(s).getJSONArray("list"))
                .orElseGet(() -> {
                    LogUtils.printMessage("Failed to get file list.");
                    return null;
                });
    }

    /**
     * 获取文件列表，如果文件太多，会分批次获取，直到获取完毕
     * @param accessToken 授权码
     * @param dir 目录路径，根目录为"/"
     * @param folder 是否只返回文件夹
     * @return
     * @throws IOException
     */
    public JSONArray getFileList(String accessToken, String dir, boolean folder) throws IOException {
        JSONArray list = new JSONArray();
        while (true) {
            JSONArray temp = getFileList(accessToken, dir, folder, list.size(), 100);
            if (temp == null || temp.isEmpty()) {
                break;
            }
            list.addAll(temp);
        }
        return list;
    }

    /**
     * 获取文件元信息，包括文件和文件夹，最多返回100条记录，超过100条记录需要分页获取
     * @param accessToken 授权码
     * @param fsids 文件id列表
     * @param dlink 是否返回下载链接
     * @return
     * @throws IOException
     */
    public JSONArray getFileMetaInfoByIds(String accessToken, List<Object> fsids, boolean dlink) throws IOException {
        if (fsids == null || fsids.isEmpty()) {
            return null;
        }
        if (fsids.size() > 100) {
            LogUtils.printMessage("The number of fsids should not exceed 100. please split the fsids into multiple requests.");
            return null;
        }
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("method", "filemetas"),
                new BasicNameValuePair("access_token", accessToken),
                new BasicNameValuePair("fsids", JSONArray.toJSONString(fsids)),
                new BasicNameValuePair("dlink", dlink ? "1" : "0"));
        Optional<String> response = Optional.ofNullable(client.doGetText(MULTIMEDIA_URL, param));
        return response.map(s -> JSONObject.parseObject(s).getJSONArray("list"))
                .orElseGet(() -> {
                    LogUtils.printMessage("Failed to get file meta info.");
                    return null;
                });
    }

    /**
     * 获取文件元信息，如果文件太多，会分批次获取，直到获取完毕
     * @param accessToken 授权码
     * @param dir 目录路径，根目录为"/"
     * @param dlink 是否返回下载链接
     * @return
     * @throws IOException
     */
    public JSONArray getFileMetaInfoByDir(String accessToken, String dir, boolean dlink) throws IOException {
        Optional<JSONArray> list = Optional.ofNullable(getFileList(accessToken, dir, false));
        return list.map(l -> {
            JSONArray fsids = new JSONArray();
            for (int i = 0; i < l.size(); i++) {
                JSONObject file = l.getJSONObject(i);
                fsids.add(file.getLongValue("fs_id"));
            }
            try {
                JSONArray metaInfo = new JSONArray();
                for (int i = 0; i < fsids.size(); i += 100) {
                    int toIndex = i + 100 > fsids.size() ? fsids.size() : i + 100;
                    JSONArray temp = getFileMetaInfoByIds(accessToken, fsids.subList(i, toIndex), dlink);
                    if (temp != null) {
                        metaInfo.addAll(temp);
                    }
                }
                return metaInfo;
            } catch (IOException e) {
                LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                return null;
            }
        }).orElseGet(() -> {
            LogUtils.printMessage("Failed to get file meta info.");
            return null;
        });
    }

    /**
    * 下载文件
    * @param accessToken 授权码
    * @param dlink 下载链接
    * @param path 保存路径
    * @throws IOException
    * @throws URISyntaxException
     */
    public void downloadFile(String accessToken, String dlink, String path) throws IOException, URISyntaxException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("access_token", accessToken));
        client.doDownload(
                path,
                new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString());
    }

    /**
     * 获取文件下载流
     * @param accessToken 授权码
     * @param dlink 下载链接
     * @return HTTP响应数据对，包括响应头和响应体
     * @throws IOException
     * @throws URISyntaxException
     */
    public HttpDataPair getFile(String accessToken, String dlink) throws IOException, URISyntaxException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("access_token", accessToken));
        String uri = new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString();
        HttpDataPair dataPair = client.doGet(uri);
        return dataPair;
    }

    /**
     * 获取文件下载流
     * @param accessToken 授权码
     * @param path 文件路径
     * @return HTTP响应数据对，包括响应头和响应体
     * @throws IOException
     * @throws URISyntaxException
     */
    public HttpDataPair getFileByPath(String accessToken, String path) throws IOException, URISyntaxException {
        String dir = path.substring(0, path.lastIndexOf("/") + 1);
        String basename = path.substring(path.lastIndexOf("/") + 1);
        Optional<JSONArray> list = Optional.ofNullable(getFileMetaInfoByDir(accessToken, dir, true));
        return list.map(l -> {
            for (int i = 0; i < l.size(); i++) {
                JSONObject file = l.getJSONObject(i);
                if (file.getString("filename").equals(basename)) {
                    try {
                        return getFile(accessToken, file.getString("dlink"));
                    } catch (Exception e) {
                        LogUtils.printMessage(null, e, LogUtils.Level.ERROR);
                        return null;
                    }
                }
            }
            return null;
        }).orElseGet(() -> {
            LogUtils.printMessage("Failed to get file.");
            return null;
        });
    }

    /**
     * 获取文本文件内容
     * @param accessToken 授权码
     * @param dlink 下载链接
     * @return 文本文件内容
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getFileText(String accessToken, String dlink) throws IOException, URISyntaxException {
        List<NameValuePair> param = List.of(
                new BasicNameValuePair("access_token", accessToken));
        return client.doGetText(
            new URIBuilder(dlink, AppConfig.DEFAULT_CHARSET).addParameters(param).build().toString());
    }

    /**
     * 只获取文件列表，排除文件夹
     * @param accessToken 授权码
     * @param dir 目录
     * @return 文件列表
     * @throws IOException
     */
    public JSONArray getOnlyFiles(String accessToken, String dir) throws IOException {
        Optional<JSONArray> list = Optional.ofNullable(getFileList(accessToken, dir, false));
        return list.map(l -> {
            JSONArray files = new JSONArray();
            for (int i = 0; i < l.size(); i++) {
                JSONObject file = l.getJSONObject(i);
                if (file.getIntValue("isdir") == 0) {
                    files.add(file);
                }
            }
            return files;
        }).orElseGet(() -> {
            LogUtils.printMessage("Failed to get file list.");
            return null;
        });
    }

    @Override
    public void close() throws Exception {
        LogUtils.printMessage("Closing BaiduPan Service");
        client.close();
    }
}

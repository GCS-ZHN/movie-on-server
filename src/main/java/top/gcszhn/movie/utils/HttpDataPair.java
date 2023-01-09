/*
 * Copyright © 2022 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 * Release under GPL License
 */
package top.gcszhn.movie.utils;

import lombok.Data;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLDecoder;

@Data
public class HttpDataPair implements Closeable {
    HttpRequestBase request;
    CloseableHttpResponse response;
    HttpClientUtils client;
    boolean closeClient = false;

    @Override
    public void close() throws IOException {
        if (response != null) {
            EntityUtils.consumeQuietly(response.getEntity());
            response.close();
        }
        if (request != null) {
            request.releaseConnection();
        }
        if (client != null && closeClient) {
            client.close();
        }
        LogUtils.printMessage(
            "Close " + URLDecoder.decode(request.getURI().toString(), "UTF-8"), 
            LogUtils.Level.DEBUG);
    }
}

package top.gcszhn.movie.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;

public class BaiduPanAuthService implements AuthService {

    @Autowired
    BaiduPanService baiduPanService;

    @Override
    public Map<String, String> auth(HttpSession session, Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty() || session == null) {
            return Map.of("status", "2", "message", "非法请求");
        }
        try {
            JSONObject response = baiduPanService.getAccessToken(code);
            if (response==null) {
                return Map.of("status", "2", "message", "无效授权码");
            }
            // access_token有效期720小时，refresh_token有效期10年
            // 暂时不考虑access_token过期的情况，会话有效期为24小时
            String access_token = response.getString("access_token");
            String refresh_token = response.getString("refresh_token");
            session.setAttribute("access_token", access_token);
            session.setAttribute("refresh_token", refresh_token);
            session.setAttribute("online", true);
            return Map.of("status", "0", "message", "登录成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> preauth(HttpSession session) {
        if (session.getAttribute("online") != null) {
            return Map.of("status", "1", "message", "已登录");
        } else {
            return Map.of(
                "status", "0", 
                "message", "未登录",
                "auth_url", baiduPanService.getAuthURL());
        }
    }
    
}

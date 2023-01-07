package top.gcszhn.movie.service;

import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;

import top.gcszhn.movie.security.RSAEncrypt;
import top.gcszhn.movie.security.ShaEncrypt;

public class LocalAuthService implements AuthService {
    @Value("#{'${passwd:}'.split(':')}")
    private String[] passwd;
    @Override
    public Map<String, String> auth(HttpSession session, Map<String, String> params) {
        try {
            if (session.getAttribute("online") != null) {
                return Map.of("status", "0", "message", "已经登录或不用密码");
            } else if (session.getAttribute("key") != null) {
                final String token = params.get("token");
                if (token == null) {
                    return Map.of("status", "3", "message", "没有提供token");
                }
                String pwd = RSAEncrypt.decryptToString(token, ((String[]) session.getAttribute("key"))[0]);
                if (ShaEncrypt.encrypt(pwd, passwd[1]).equals(passwd[2])) {
                    session.setAttribute("online", true);
                    session.removeAttribute("key");
                    return Map.of("status", "0", "message", "登录成功");
                } else {
                    return Map.of("status", "1", "message", "无效token");
                }
            } {
                return Map.of("status", "2", "message", "非法请求");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Map.of("status", "1", "message", "登录失败，原因不明");
    }

    @Override
    public Map<String, String> preauth(HttpSession session) {
        if (passwd[0].equals("")) {
            session.setAttribute("online", true);
        }
        
        if (session.getAttribute("online")!=null) {
            return Map.of("status", "1", "message", "已经登录或不用密码");
        } else {
            String[] keys = new String[2];
            RSAEncrypt.generateKeyPair(keys);
            session.setAttribute("key", keys);
            return Map.of("status", "0", "message", keys[1]);
        }
    }
}

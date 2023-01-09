package top.gcszhn.movie.service;

import java.util.Map;

import javax.servlet.http.HttpSession;

public interface AuthService {
    /**
     * 登录验证
     * @param params 登录token
     * @return 登录结果
     */
    Map<String, String> auth(HttpSession session, Map<String, String> params);

    /**
     * 获取RSA加密公钥，同时也是对登录状态的验证
     * @return 验证结果
     */
    Map<String, String> preauth(HttpSession session);
}

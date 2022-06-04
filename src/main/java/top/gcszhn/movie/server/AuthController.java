/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package top.gcszhn.movie.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import top.gcszhn.movie.security.RSAEncrypt;
import top.gcszhn.movie.security.ShaEncrypt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
public class AuthController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Value("#{'${passwd:}'.split(':')}")
    private String[] passwd;

    /**
     * 登录验证
     * @param params 登录token
     * @return 登录结果
     */
    @PostMapping("auth")
    Map<String, String> auth(@RequestBody Map<String, String> params) {
        try {
            final HttpSession session = request.getSession(false);
            if (session == null) {
                request.getSession().setAttribute("online", true);
                return Map.of("status", "2", "message", "非法请求");
            } else if (session.getAttribute("online") != null) {
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

    /**
     * 获取RSA加密公钥，同时也是对免密码登录的验证
     * @return 公钥
     */
    @GetMapping("key")
    Map<String, String> getKey() {
        final HttpSession session = request.getSession();
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

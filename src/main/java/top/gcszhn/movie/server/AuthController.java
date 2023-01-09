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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import top.gcszhn.movie.AppConfig;
import top.gcszhn.movie.service.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {
    @Autowired
    HttpServletRequest request;

    @Autowired
    HttpServletResponse response;

    @Autowired
    AuthService authService;

    @Autowired
    AppConfig appConfig;

    /**
     * 登录POST验证，用于Local登录
     * @param params 登录token
     * @return 登录结果
     */
    @PostMapping("auth")
    Map<String, String> auth(@RequestBody Map<String, String> params) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            Map.of("status", "2", "message", "非法请求");
        }
        return authService.auth(session, params);
    }

    /**
     * 登录GET验证，用于百度网盘登录
     * @param code 百度网盘授权码
     * @return 登录结果
     * @throws IOException
     */
    @GetMapping("auth")
    Map<String, String> auth(String code) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null||code==null||code.isEmpty()
            ||appConfig.getResourceBackend().equals("local")) {
            Map.of("status", "2", "message", "非法请求");
        }
        Map<String, String> res = authService.auth(session, Map.of("code", code));
        if (res.get("status").equals("0")) {
            response.sendRedirect("/home.html");
        }
        return res;
    }

    /**
     * 获取RSA加密公钥，同时也是对免密码登录的验证
     * @return 公钥
     */
    @GetMapping("preauth")
    Map<String, String> preauth() {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(24 * 60 * 60);
        Map<String, String> res = authService.preauth(session);
        Map<String, String> update = new HashMap<>(res);
        update.put("backend", appConfig.getResourceBackend());;
        return update;
    }
}

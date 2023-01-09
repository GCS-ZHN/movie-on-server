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
package top.gcszhn.movie.filter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import top.gcszhn.movie.utils.LogUtils;

import java.io.IOException;

/**
 * 过滤未经授权登录的请求
 */
@WebFilter(urlPatterns = {"/query/*", "/stream/*", "/", "/index.html"})
public class F1_AuthFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = request.getSession(false);
        boolean isOnline = session != null && session.getAttribute("online") != null;
        switch (request.getRequestURI()) {
            case "/":
            case "/index.html":
                if (isOnline) {
                    LogUtils.printMessage("Redirecting to /home.html");
                    response.sendRedirect("/home.html");
                    return;
                }
                break;
            default:
                if (!isOnline) {
                    LogUtils.printMessage("Sending 403");
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "请先登录");
                    return;
                }
                break;
        }
        super.doFilter(request, response, chain);
    }
}

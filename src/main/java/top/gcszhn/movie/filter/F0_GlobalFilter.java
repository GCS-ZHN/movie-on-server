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

import top.gcszhn.movie.utils.LogUtils;

import java.io.IOException;

/**
 * 全局过滤器
 */
@WebFilter(urlPatterns = {"/*"})
public class F0_GlobalFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        // response.addHeader("Access-Control-Allow-Credentials", "true");
        // response.setHeader("Access-Control-Allow-Methods", "POST, GET, PATCH, DELETE, PUT");
        // response.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With, X-XSRF-TOKEN, Authorization");
        String realIP = request.getHeader("X-Real-IP");
        if (realIP == null || realIP.length() == 0 || "unknown".equalsIgnoreCase(realIP)) {
            realIP = request.getRemoteAddr();
        }
        LogUtils.printMessage(String.format("Request for %s from %s [%s]", 
            request.getRequestURI(), request.getRemoteHost(), realIP));
        super.doFilter(request, response, chain);
    }
}

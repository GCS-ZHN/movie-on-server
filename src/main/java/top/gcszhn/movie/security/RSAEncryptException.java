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
package top.gcszhn.movie.security;

/**
 * RSA加密解密的异常
 * @author Zhang.H.N
 * @version 1.0
 */
public class RSAEncryptException extends RuntimeException {
    /**序列化ID */
    public static final long serialVersionUID = 202105041631L;
    /**
     * RSA加密异常的构造方法
     * @param message 描述信息
     * @param thr 堆栈信息
     */
    public RSAEncryptException(String message, Throwable thr) {
        super(message, thr);
    }
}
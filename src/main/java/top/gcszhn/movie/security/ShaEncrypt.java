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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

import top.gcszhn.movie.AppConfig;

/**
 * SHA单向加密算法
 * @author Zhang.H.N
 * @version 1.0
 */
public class ShaEncrypt {
    /**
     * SHA-256的加盐加密算法
     * @param message UTF-8编码的明文
     * @param salt UTF-8编码的Base64字符串盐
     * @return UTF-8编码的Base64密文
     */
    public static String encrypt(String message, String salt) {
        byte[] saltArray = Base64.getDecoder().decode(salt.getBytes(AppConfig.DEFAULT_CHARSET));
        byte[] messArray = message.getBytes(AppConfig.DEFAULT_CHARSET);
        byte[] mixsArray = new byte[Math.max(saltArray.length, messArray.length)];
        for (int i = 0; i < mixsArray.length; i++) {
            byte s = i < saltArray.length?saltArray[i]:0;
            byte m = i < messArray.length?messArray[i]:0;
            mixsArray[i] = (byte)((s + m)/2);
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(mixsArray);
            return new String(Base64.getEncoder().encode(md.digest()), AppConfig.DEFAULT_CHARSET);
        } catch (NoSuchAlgorithmException nsae) {
            nsae.printStackTrace();
        }
        return null;
    }
    /**
     * 指定盐的字节长度，返回随机生成的Base64字符串盐
     * @param length Base64加密的字节数组长度
     * @return UTF-8编码的Base64字符串盐
     */
    public static String getSalt(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Salt string's length must be positive");
        }
        byte[] bit = new byte[length];
        new Random().nextBytes(bit);;
        return new String(Base64.getEncoder().encode(bit), AppConfig.DEFAULT_CHARSET);
    }
}
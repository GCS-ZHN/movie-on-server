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

import top.gcszhn.movie.AppConfig;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * RSA不对称加密的工具
 * @author Zhang.H.N
 */
public class RSAEncrypt {
    /**
     * 对字符串加密解密时用的字符编码方式，RSA加密解密针对的是字节数组，不同编码方式的字节数组不同
     */

    /**
     * 生成RSA密钥对，并以Base64编码保存在字符串组中，0为密钥，1为公钥
     * @param pairMap 用于保存生成的RSA密钥对Base64编码的字符串组
     */
    public static void generateKeyPair (String[] pairMap) {
        try {
            if (pairMap.length != 2) throw new IllegalArgumentException("参数数组长度必须为2");
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");//公密钥对生成器，选用RSA算法
            keyPairGenerator.initialize(1024, new SecureRandom());// 初始化密钥对生成器，密钥大小为96-1024位
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            pairMap[0] = new String(Base64.getEncoder().encode(privateKey.getEncoded()), AppConfig.DEFAULT_CHARSET);
            pairMap[1] = new String(Base64.getEncoder().encode(publicKey.getEncoded()), AppConfig.DEFAULT_CHARSET);
        } catch (NoSuchAlgorithmException e) {
            throw new RSAEncryptException(e.getMessage(), e);
        }
    }
    /**
     * 将字符串信息用指定RSA公钥加密为Base64编码字符串，字符串编码方式由类变量charset指定
     * @param info 待加密字符串信息
     * @param pubilcKeyString Base64编码的公钥字符串
     * @return RSA加密并Base64编码的字符串
     */
    public static String encrypt (String info, String pubilcKeyString) {
        return encrypt(info.getBytes(AppConfig.DEFAULT_CHARSET), pubilcKeyString);
    }
    /**
     * 将字节数组用指定RSA公钥加密为Base64编码字符串，可用于非文本加密
     * @param info 待加密字节数组
     * @param pubilcKeyString Base64编码的公钥字符串
     * @return RSA加密并Base64编码的字符串
     */
    public static String encrypt (byte[] info, String pubilcKeyString) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(pubilcKeyString.getBytes(AppConfig.DEFAULT_CHARSET));
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return new String(Base64.getEncoder().encode(cipher.doFinal(info)), AppConfig.DEFAULT_CHARSET);
        } catch (InvalidKeySpecException|NoSuchAlgorithmException|NoSuchPaddingException|InvalidKeyException|
            IllegalBlockSizeException|BadPaddingException|IllegalArgumentException e) {
                throw new RSAEncryptException(e.getMessage(), e);
        }
    }
    /**
     * RSA加密并Base64编码的字符串用指定密钥解密为字符串，解密后字符串编码方式由类变量charset指定
     * @param encryptinfo RSA加密并Base64编码的字符串
     * @param privateKeyString Base64编码的密钥字符串
     * @return 解密后的字符串
     */
    public static String decryptToString (String encryptinfo, String privateKeyString) {
        return new String(decryptToBytes(encryptinfo, privateKeyString), AppConfig.DEFAULT_CHARSET);
    }
    /**
     * RSA加密并Base64编码的字符串用指定密钥解密为字节数组，可用于非文本信息解密
     * @param encryptinfo RSA加密并Base64编码的字符串
     * @param privateKeyString Base64编码的密钥字符串
     * @return 解密后的字节数组
     */
    public static byte[] decryptToBytes(String encryptinfo, String privateKeyString) {
        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString.getBytes(AppConfig.DEFAULT_CHARSET));
            RSAPrivateKey privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(Base64.getDecoder().decode(encryptinfo.getBytes(AppConfig.DEFAULT_CHARSET)));
        } catch (InvalidKeySpecException|NoSuchAlgorithmException|NoSuchPaddingException
            |InvalidKeyException|IllegalBlockSizeException|BadPaddingException|IllegalArgumentException e) {
            throw new RSAEncryptException(e.getMessage(), e);
        }
    }
}
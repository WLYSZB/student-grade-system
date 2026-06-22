package com.example.gradesystem.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 加密工具类
 * 与成员1数据库初始化脚本中的密码加密方式保持一致
 */
public class MD5Util {

    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    /**
     * 验证明文密码是否与 MD5 密文匹配
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        return md5(rawPassword).equals(encryptedPassword);
    }
}

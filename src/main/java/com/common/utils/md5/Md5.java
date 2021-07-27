package com.common.utils.md5;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class Md5 {
    public static HashCode encrypt(String value, Object salt) {
        return Hashing.hmacMd5(String.valueOf(salt).getBytes()).hashString(value, UTF_8);
    }

    public static String encryptToString(String value, String salt) {
        return encrypt(value, salt).toString();
    }

    public static void main(String[] args) {
        log.debug("加密内容：" + Md5.encryptToString("admin123456", "1"));
    }
}

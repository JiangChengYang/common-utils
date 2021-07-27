package com.common.utils.secure;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class AES {
    // 密钥算法
    private static final String KEY_ALGORITHM = "AES";
    // 加解密算法/工作模式/填充方式,Java6.0支持PKCS5Padding填充方式,BouncyCastle支持PKCS7Padding填充方式
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static SecureProperties secureProperties;

    /**
     * 生成密钥
     */
    public static String initkey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(128);
        SecretKey secretKey = kg.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    /**
     * 转换密钥
     */
    public static Key toKey(byte[] key) throws Exception {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    /**
     * 加密数据
     *
     * @param data 待加密数据
     * @return 加密后的数据
     */
    public static String encrypt(String data) throws Exception {
        Key k = toKey(Base64.getDecoder().decode(secureProperties.getAesKey()));
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, k);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    /**
     * 解密数据
     *
     * @param data 待解密数据
     * @return 解密后的数据
     */
    public static String decrypt(String data) throws Exception {
        Key k = toKey(Base64.getDecoder().decode(secureProperties.getAesKey()));
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, k);
        return new String(cipher.doFinal(Base64.getDecoder().decode(data)));
    }

    public static void main(String[] args) throws Exception {
        System.out.println("key: " + initkey());
        String data = "123456";

        String encrypt = encrypt(data);
        System.out.println(encrypt);
        System.out.println(decrypt(encrypt));

    }
}

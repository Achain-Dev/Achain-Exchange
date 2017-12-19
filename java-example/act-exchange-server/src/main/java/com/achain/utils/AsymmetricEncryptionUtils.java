package com.achain.utils;


import javax.crypto.Cipher;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;


public class AsymmetricEncryptionUtils {

    private static String ALGORITHM = "RSA";

    private static Integer KEYSIZE = 4096;

    private static String PUBLIC_KEY_FILE = "PublicKey";

    private static String PRIVATE_KEY_FILE = "PrivateKey";

    /**
     * 根据传入字符串产生一对公钥和私钥，并保存到文件
     *
     * @param key
     */
    public static void generateKeyPair(String key) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEYSIZE, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();
            ObjectOutputStream publicKeyOutputStream = new ObjectOutputStream(new FileOutputStream(PUBLIC_KEY_FILE));
            ObjectOutputStream privateKeyOutputStream = new ObjectOutputStream(new FileOutputStream(PRIVATE_KEY_FILE));
            publicKeyOutputStream.writeObject(publicKey);
            privateKeyOutputStream.writeObject(privateKey);
            publicKeyOutputStream.close();
            privateKeyOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用公钥解密
     *
     * @param content
     * @return
     */
    public static String encrypt(Key publicKey, String content) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encodeData = cipher.doFinal(content.getBytes());
            return Base64.getEncoder().encodeToString(encodeData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 使用私钥解密
     *
     * @param content
     * @return
     */
    public static String decrypt(Key publicKey, String content) {
        try {
            byte[] decodeContent = Base64.getDecoder().decode(content);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] decodeData = cipher.doFinal(decodeContent);
            return new String(decodeData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

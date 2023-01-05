package com.octskyout.users.aes;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class Aes256Test {
    String alg = "AES/CBC/PKCS5Padding";
    String key = "9d6bf7b53696697eb36e1b05f25fad18";
    String iv = key.substring(0,16);

    Aes256 aes256 = new Aes256(alg, key, iv);

    @Test
    void 암호화를_수행한다()
        throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
        BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String testStr = "anyString";
        String encryptStr = aes256.encrypt(testStr);

        assertThat(encryptStr).isNotEqualTo(testStr);
        log.debug(encryptStr);
    }

    @Test
    void 복호화_및_같은_값인지_비교에_성공한다()
        throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
        BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String encryptedStr = "ot+IKrZopKsjixS8LIbWFA==";
        String realStr = "anyString";
        String decryptStr = aes256.decrypt(encryptedStr);

        assertThat(decryptStr).isEqualTo(realStr);
    }
}

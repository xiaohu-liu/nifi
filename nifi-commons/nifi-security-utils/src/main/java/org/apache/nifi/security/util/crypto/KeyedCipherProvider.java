/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.security.util.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.security.util.EncryptionMethod;

public abstract class KeyedCipherProvider implements CipherProvider {
    static final byte[] IV_DELIMITER = "NiFiIV".getBytes(StandardCharsets.UTF_8);
    // This is 16 bytes for AES but can vary for other ciphers
    static final int MAX_IV_LIMIT = 16;

    /**
     * Returns an initialized cipher for the specified algorithm. The IV is provided externally to allow for non-deterministic IVs, as IVs
     * deterministically derived from the password are a potential vulnerability and compromise semantic security. See
     * <a href="http://crypto.stackexchange.com/a/3970/12569">Ilmari Karonen's answer on Crypto Stack Exchange</a>
     *
     * @param encryptionMethod the {@link EncryptionMethod}
     * @param key              the key
     * @param iv               the IV or nonce
     * @param encryptMode      true for encrypt, false for decrypt
     * @return the initialized cipher
     * @throws Exception if there is a problem initializing the cipher
     */
    public abstract Cipher getCipher(EncryptionMethod encryptionMethod, SecretKey key, byte[] iv, boolean encryptMode) throws Exception;

    /**
     * Returns an initialized cipher for the specified algorithm. The IV will be generated internally (for encryption). If decryption is requested, it will throw an exception.
     *
     * @param encryptionMethod the {@link EncryptionMethod}
     * @param key              the key
     * @param encryptMode      true for encrypt, false for decrypt
     * @return the initialized cipher
     * @throws Exception if there is a problem initializing the cipher or if decryption is requested
     */
    public abstract Cipher getCipher(EncryptionMethod encryptionMethod, SecretKey key, boolean encryptMode) throws Exception;

    /**
     * Generates a new random IV of the correct length.
     *
     * @return the IV
     */
    abstract byte[] generateIV();

    public byte[] readIV(InputStream in) throws IOException, ProcessException {
        return CipherUtility.readBytesFromInputStream(in, "IV", MAX_IV_LIMIT, IV_DELIMITER);
    }

    public void writeIV(byte[] iv, OutputStream out) throws IOException {
        CipherUtility.writeBytesToOutputStream(out, iv, "IV", IV_DELIMITER);
    }
}

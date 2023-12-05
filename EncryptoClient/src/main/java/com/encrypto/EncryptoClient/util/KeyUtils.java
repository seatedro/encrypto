package com.encrypto.EncryptoClient.util;

import static java.lang.System.arraycopy;

import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class KeyUtils {
    private static final String KEYSTORE_SUBDIR = ".config/Encrypto";
    private static final String KEYSTORE_FILENAME = "user_keystore.p12";

    public static KeyPair generateECDHKeyPair() {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("EC");
            var ecSpec = new ECGenParameterSpec("secp384r1");
            keyPairGenerator.initialize(ecSpec);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public static String exportPublicKey(PublicKey publicKey) {
        try {
            var keyFactory = KeyFactory.getInstance("EC");
            var x509EncodedKeySpec =
                    keyFactory.getKeySpec(publicKey, java.security.spec.X509EncodedKeySpec.class);
            return Base64.getEncoder().encodeToString(x509EncodedKeySpec.getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PublicKey importPublicKey(String publicKeyString) {
        try {
            var keyFactory = KeyFactory.getInstance("EC");
            var x509EncodedKeySpec =
                    new java.security.spec.X509EncodedKeySpec(
                            Base64.getDecoder().decode(publicKeyString));
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static void storePrivateKey(KeyPair keyPair, String alias, char[] password) {
        try {
            var keyStore = KeyStore.getInstance("PKCS12");
            var keyStorePath = getKeyStorePath();

            // Create the subdirectory if it doesn't exist
            var keyStoreDir = keyStorePath.getParent().toFile();
            if (!keyStoreDir.exists()) {
                keyStoreDir.mkdirs();
            }

            // Load or initialize the keystore
            if (keyStorePath.toFile().exists()) {
                keyStore.load(new FileInputStream(keyStorePath.toFile()), password);
            } else {
                keyStore.load(null, null);
            }

            var privateKey = keyPair.getPrivate();

            // Create a self-signed certificate
            var cert = generateSelfSignedCertificate(keyPair);

            // Store the private key
            var privateKeyEntry =
                    new KeyStore.PrivateKeyEntry(privateKey, new Certificate[] {cert});
            var protectionParam = new KeyStore.PasswordProtection(password);
            keyStore.setEntry(alias, privateKeyEntry, protectionParam);

            // Save the keystore
            try (var fos = new FileOutputStream(keyStorePath.toFile())) {
                keyStore.store(fos, password);
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey loadPrivateKey(String alias, char[] password) {
        try {
            var keyStore = KeyStore.getInstance("PKCS12");
            var keyStorePath = getKeyStorePath();

            // Check if the keystore exists
            if (!keyStorePath.toFile().exists()) {
                throw new RuntimeException("Keystore does not exist: " + keyStorePath);
            }

            // Load the keystore
            try (var fis = new FileInputStream(keyStorePath.toFile())) {
                keyStore.load(fis, password);
            }

            // Get the private key
            var privateKeyEntry =
                    (KeyStore.PrivateKeyEntry)
                            keyStore.getEntry(alias, new KeyStore.PasswordProtection(password));
            if (privateKeyEntry == null) {
                throw new RuntimeException("Private key not found under alias: " + alias);
            }

            return privateKeyEntry.getPrivateKey();
        } catch (KeyStoreException
                | IOException
                | NoSuchAlgorithmException
                | CertificateException
                | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }
    }

    private static X509Certificate generateSelfSignedCertificate(KeyPair keyPair)
            throws GeneralSecurityException {
        try {

            var certBuilder = getJcaX509v3CertificateBuilder(keyPair);

            var signer = new JcaContentSignerBuilder("SHA256WithECDSA").build(keyPair.getPrivate());

            return new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider())
                    .getCertificate(certBuilder.build(signer));
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        }
    }

    private static JcaX509v3CertificateBuilder getJcaX509v3CertificateBuilder(KeyPair keyPair) {
        var principal = new X500Principal("CN=Self-Signed, O=My App");
        var notBefore = new Date();
        var notAfter = new Date(notBefore.getTime() + 365 * 86400000L); // 1 year validity
        var serialNumber = new BigInteger(64, new SecureRandom());

        return new JcaX509v3CertificateBuilder(
                principal, serialNumber, notBefore, notAfter, principal, keyPair.getPublic());
    }

    private static Path getKeyStorePath() {
        var userHome = System.getProperty("user.home");
        return Paths.get(userHome, KEYSTORE_SUBDIR, KEYSTORE_FILENAME);
    }

    private SecretKey deriveSharedSecret(PrivateKey privateKey, PublicKey publicKey) {
        try {
            var keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            var secret = keyAgreement.generateSecret();
            return new SecretKeySpec(secret, 0, 16, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] encryptMessage(String message, SecretKey aesKey) {
        try {
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            var iv = new byte[12];
            new SecureRandom().nextBytes(iv);
            var gcmParameterSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmParameterSpec);
            var encryptedData = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return combineAndEncode(iv, encryptedData);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] combineAndEncode(byte[] iv, byte[] encryptedData) {
        var combinedData = new byte[iv.length + encryptedData.length];
        arraycopy(iv, 0, combinedData, 0, iv.length);
        arraycopy(encryptedData, 0, combinedData, iv.length, encryptedData.length);
        return Base64.getEncoder().encode(combinedData);
    }
}

package com.it_nomads.fluttersecurestorage.ciphers;

import android.content.Context;
import android.content.res.Configuration;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Calendar;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.security.auth.x500.X500Principal;

public class RSACipherOAEPImplementation implements KeyCipher {
    private static final String KEYSTORE_PROVIDER_ANDROID = "AndroidKeyStore";
    private static final String TYPE_RSA = "RSA";
    private final String keyAlias;
    private final Context context;

    public RSACipherOAEPImplementation(Context context) throws Exception {
        this.context = context;
        keyAlias = createKeyAlias();
        createRSAKeysIfNeeded();
    }

    @Override
    public byte[] wrap(Key key) throws Exception {
        PublicKey publicKey = getPublicKey();
        Cipher cipher = getRSACipher();
        cipher.init(Cipher.WRAP_MODE, publicKey, getAlgorithmParameterSpec());

        return cipher.wrap(key);
    }

    @Override
    public Key unwrap(byte[] wrappedKey, String algorithm) throws Exception {
        PrivateKey privateKey = getPrivateKey();
        Cipher cipher = getRSACipher();
        cipher.init(Cipher.UNWRAP_MODE, privateKey, getAlgorithmParameterSpec());

        return cipher.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY);
    }

    private String createKeyAlias() {
        return context.getPackageName() + ".FlutterSecureStoragePluginKeyOAEP";
    }

    private PrivateKey getPrivateKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID);
        ks.load(null);

        Key key = ks.getKey(keyAlias, null);
        if (key == null) {
            throw new Exception("No key found under alias: " + keyAlias);
        }

        if (!(key instanceof PrivateKey)) {
            throw new Exception("Not an instance of a PrivateKey");
        }

        return (PrivateKey) key;
    }

    private PublicKey getPublicKey() throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID);
        ks.load(null);

        Certificate cert = ks.getCertificate(keyAlias);
        if (cert == null) {
            throw new Exception("No certificate found under alias: " + keyAlias);
        }

        PublicKey key = cert.getPublicKey();
        if (key == null) {
            throw new Exception("No key found under alias: " + keyAlias);
        }

        return key;
    }

    private void createRSAKeysIfNeeded() throws Exception {
        KeyStore ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID);
        ks.load(null);

        Key privateKey = ks.getKey(keyAlias, null);
        if (privateKey == null) {
            createKeys();
        }
    }

    private void setLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        context.createConfigurationContext(config);
    }

    private void createKeys() throws Exception {
        final Locale localeBeforeFakingEnglishLocale = Locale.getDefault();
        try {
            setLocale(Locale.ENGLISH);
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 25);

            KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID);

            AlgorithmParameterSpec spec;

            spec = makeAlgorithmParameterSpec(start, end);

            kpGenerator.initialize(spec);
            kpGenerator.generateKeyPair();
        } finally {
            setLocale(localeBeforeFakingEnglishLocale);
        }
    }

    private AlgorithmParameterSpec makeAlgorithmParameterSpec(Calendar start, Calendar end) {
        final KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                .setCertificateSubject(new X500Principal("CN=" + keyAlias))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                .setCertificateSerialNumber(BigInteger.valueOf(1))
                .setCertificateNotBefore(start.getTime())
                .setCertificateNotAfter(end.getTime());
        return builder.build();
    }

    private Cipher getRSACipher() throws Exception {
        return Cipher.getInstance("RSA/ECB/OAEPPadding", "AndroidKeyStoreBCWorkaround");
    }

    private AlgorithmParameterSpec getAlgorithmParameterSpec() {
        return new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
    }
}

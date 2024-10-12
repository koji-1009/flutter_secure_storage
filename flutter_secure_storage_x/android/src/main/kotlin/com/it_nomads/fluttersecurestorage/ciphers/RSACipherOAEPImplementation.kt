package com.it_nomads.fluttersecurestorage.ciphers

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.lang.Exception
import java.math.BigInteger
import java.security.Key
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.MGF1ParameterSpec
import java.util.Calendar
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource
import javax.security.auth.x500.X500Principal

internal class RSACipherOAEPImplementation(
  private val context: Context,
) : KeyCipher {
  private val keyAlias = createKeyAlias()

  init {
    createRSAKeysIfNeeded()
  }

  override fun wrap(key: Key): ByteArray {
    val publicKey = getPublicKey()
    val cipher = getRSACipher()
    cipher.init(Cipher.WRAP_MODE, publicKey, getAlgorithmParameterSpec())

    return cipher.wrap(key)
  }

  override fun unwrap(wrappedKey: ByteArray, algorithm: String): Key {
    val privateKey = getPrivateKey()
    val cipher = getRSACipher()
    cipher.init(Cipher.UNWRAP_MODE, privateKey, getAlgorithmParameterSpec())

    return cipher.unwrap(wrappedKey, algorithm, Cipher.SECRET_KEY)
  }

  private fun createKeyAlias(): String {
    return context.packageName + ".FlutterSecureStoragePluginKeyOAEP"
  }

  private fun getPrivateKey(): PrivateKey {
    val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
    ks.load(null)

    val key = ks.getKey(keyAlias, null)
    if (key == null) {
      throw Exception("No key found under alias: $keyAlias")
    }

    if (key !is PrivateKey) {
      throw Exception("Not an instance of a PrivateKey")
    }

    return key
  }

  private fun getPublicKey(): PublicKey {
    val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
    ks.load(null)

    val cert = ks.getCertificate(keyAlias)
    if (cert == null) {
      throw Exception("No certificate found under alias: $keyAlias")
    }

    val key = cert.publicKey
    if (key == null) {
      throw Exception("No key found under alias: $keyAlias")
    }

    return key
  }

  private fun createRSAKeysIfNeeded() {
    val ks = KeyStore.getInstance(KEYSTORE_PROVIDER_ANDROID)
    ks.load(null)

    val privateKey = ks.getKey(keyAlias, null)
    if (privateKey == null) {
      createKeys()
    }
  }

  private fun setLocale(locale: Locale) {
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.createConfigurationContext(config)
  }

  private fun createKeys() {
    val localeBeforeFakingEnglishLocale = Locale.getDefault()
    try {
      setLocale(Locale.ENGLISH)
      val start = Calendar.getInstance()
      val end = Calendar.getInstance()
      end.add(Calendar.YEAR, 25)

      val kpGenerator = KeyPairGenerator.getInstance(TYPE_RSA, KEYSTORE_PROVIDER_ANDROID)
      val spec = makeAlgorithmParameterSpec(start, end)

      kpGenerator.initialize(spec)
      kpGenerator.generateKeyPair()
    } finally {
      setLocale(localeBeforeFakingEnglishLocale)
    }
  }

  private fun makeAlgorithmParameterSpec(start: Calendar, end: Calendar): AlgorithmParameterSpec {
    val builder = KeyGenParameterSpec.Builder(
      keyAlias,
      KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT,
    )
      .setCertificateSubject(X500Principal("CN=$keyAlias"))
      .setDigests(KeyProperties.DIGEST_SHA256)
      .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
      .setCertificateSerialNumber(BigInteger.valueOf(1))
      .setCertificateNotBefore(start.getTime())
      .setCertificateNotAfter(end.getTime())
    return builder.build()
  }

  private fun getRSACipher(): Cipher {
    return Cipher.getInstance("RSA/ECB/OAEPPadding", "AndroidKeyStoreBCWorkaround")
  }

  private fun getAlgorithmParameterSpec(): AlgorithmParameterSpec {
    return OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
  }

  companion object {
    private const val KEYSTORE_PROVIDER_ANDROID = "AndroidKeyStore"
    private const val TYPE_RSA = "RSA"
  }
}

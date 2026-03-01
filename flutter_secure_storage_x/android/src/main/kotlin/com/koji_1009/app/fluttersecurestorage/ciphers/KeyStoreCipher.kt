package com.koji_1009.app.fluttersecurestorage.ciphers

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeyStoreCipher {
  companion object {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "flutter_secure_storage_x"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
  }

  private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
    load(null)
  }

  private val secretKey: SecretKey by lazy {
    getOrCreateSecretKey()
  }

  fun encrypt(plainText: String): String {
    if (plainText.isEmpty()) return ""

    val cipher = Cipher.getInstance(TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    val iv = cipher.iv
    val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

    val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
    val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

    return "$ivString:$encryptedString"
  }

  fun decrypt(encryptedData: String): String {
    if (encryptedData.isEmpty()) return ""

    val parts = encryptedData.split(":")
    if (parts.size != 2) {
      throw IllegalArgumentException("Invalid encrypted data format")
    }

    try {
      val iv = Base64.decode(parts[0], Base64.NO_WRAP)
      val encryptedBytes = Base64.decode(parts[1], Base64.NO_WRAP)

      val cipher = Cipher.getInstance(TRANSFORMATION)
      val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

      val decodedBytes = cipher.doFinal(encryptedBytes)
      return String(decodedBytes, StandardCharsets.UTF_8)
    } catch (e: Exception) {
      throw IllegalStateException("Decryption failed", e)
    }
  }

  private fun getOrCreateSecretKey(): SecretKey {
    if (keyStore.containsAlias(KEY_ALIAS)) {
      val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
      if (entry != null) {
        return entry.secretKey
      }
    }

    return generateKey()
  }

  private fun generateKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)

    val spec = KeyGenParameterSpec.Builder(
      KEY_ALIAS,
      KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
      .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
      .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
      .setKeySize(256)
      .setRandomizedEncryptionRequired(true)
      .build()

    keyGenerator.init(spec)
    return keyGenerator.generateKey()
  }
}
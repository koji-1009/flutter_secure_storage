package com.it_nomads.fluttersecurestorage.ciphers

import android.content.Context
import android.util.Base64
import java.security.Key
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

internal class StorageCipherGCMImplementation(
  context: Context,
  keyCipher: KeyCipher,
) : StorageCipher {
  private val cipher: Cipher = getCipher()
  private val secureRandom: SecureRandom = SecureRandom()
  private val secretKey: Key

  init {
    val preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    val aesPreferencesKey = getAESPreferencesKey()
    val aesKey = preferences.getString(aesPreferencesKey, null)

    if (aesKey != null) {
      val encrypted = Base64.decode(aesKey, Base64.DEFAULT)
      secretKey = keyCipher.unwrap(encrypted, KEY_ALGORITHM)
    } else {
      val key = ByteArray(KEY_SIZE)
      secureRandom.nextBytes(key)
      secretKey = SecretKeySpec(key, KEY_ALGORITHM)

      val encryptedKey = keyCipher.wrap(secretKey)
      val editor = preferences.edit()
      editor.putString(aesPreferencesKey, Base64.encodeToString(encryptedKey, Base64.DEFAULT))
      editor.apply()
    }
  }

  override fun encrypt(input: ByteArray): ByteArray {
    val iv = ByteArray(getIvSize())
    secureRandom.nextBytes(iv)

    val ivParameterSpec = getParameterSpec(iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

    val payload: ByteArray = cipher.doFinal(input)
    val combined = ByteArray(iv.size + payload.size)

    System.arraycopy(iv, 0, combined, 0, iv.size)
    System.arraycopy(payload, 0, combined, iv.size, payload.size)

    return combined
  }

  override fun decrypt(input: ByteArray): ByteArray {
    val iv = ByteArray(getIvSize())
    System.arraycopy(input, 0, iv, 0, iv.size)
    val ivParameterSpec = getParameterSpec(iv)

    val payloadSize = input.size - getIvSize()
    val payload = ByteArray(payloadSize)

    System.arraycopy(input, iv.size, payload, 0, payloadSize)

    cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
    return cipher.doFinal(payload)
  }

  private fun getAESPreferencesKey(): String {
    return "VGhpcyBpcyB0aGUga2V5IGZvcihBIHNlY3XyZZBzdG9yYWdlIEFFUyBLZXkK"
  }

  private fun getCipher(): Cipher {
    return Cipher.getInstance("AES/GCM/NoPadding")
  }

  private fun getIvSize(): Int {
    return 12
  }

  private fun getParameterSpec(iv: ByteArray): AlgorithmParameterSpec {
    return GCMParameterSpec(AUTHENTICATION_TAG_SIZE, iv)
  }

  companion object {
    private const val KEY_SIZE = 16
    private const val AUTHENTICATION_TAG_SIZE = 128
    private const val KEY_ALGORITHM = "AES"
    private const val SHARED_PREFERENCES_NAME = "FlutterSecureKeyStorage"
  }
}

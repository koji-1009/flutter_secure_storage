package com.koji_1009.app.fluttersecurestorage

import android.content.Context
import com.koji_1009.app.fluttersecurestorage.ciphers.KeyStoreCipher
import com.koji_1009.app.fluttersecurestorage.storage.DataStoreStorage

class FlutterSecureStorage(
  private val applicationContext: Context,
) {
  private val keyStoreCipher: KeyStoreCipher by lazy {
    KeyStoreCipher()
  }

  private var resetOnError = false

  fun setOptions(options: Map<String, String>) {
    resetOnError = options[OPTION_RESET_ON_ERROR] == OPTION_VALUE_TRUE
  }

  private val dataStoreStorage: DataStoreStorage by lazy {
    DataStoreStorage(applicationContext)
  }

  fun getResetOnError(): Boolean {
    return resetOnError
  }

  suspend fun containsKey(key: String): Boolean {
    return dataStoreStorage.containsKey(key)
  }

  suspend fun read(key: String): String? {
    val value = dataStoreStorage.read(key) ?: return null
    return decodeRawValue(value)
  }

  suspend fun readAll(): Map<String, String> {
    val values = dataStoreStorage.readAll()
    return values.mapValues { (_, value) -> decodeRawValue(value) }
  }

  suspend fun write(key: String, value: String) {
    val encodeValue = encodeRawValue(value)
    dataStoreStorage.write(key, encodeValue)
  }

  suspend fun delete(key: String) {
    dataStoreStorage.delete(key)
  }

  suspend fun deleteAll() {
    dataStoreStorage.deleteAll()
  }

  private fun encodeRawValue(value: String): String {
    return keyStoreCipher.encrypt(value)
  }

  private fun decodeRawValue(value: String): String {
    return keyStoreCipher.decrypt(value)
  }

  companion object {
    private const val OPTION_RESET_ON_ERROR = "resetOnError"

    private const val OPTION_VALUE_TRUE = "true"
  }
}

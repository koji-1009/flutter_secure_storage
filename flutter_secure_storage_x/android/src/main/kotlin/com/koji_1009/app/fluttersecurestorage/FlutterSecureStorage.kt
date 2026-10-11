package com.koji_1009.app.fluttersecurestorage

import android.content.Context
import com.koji_1009.app.fluttersecurestorage.ciphers.KeyStoreCipher
import com.koji_1009.app.fluttersecurestorage.storage.DataStoreStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class FlutterSecureStorage(
  private val applicationContext: Context,
) {
  private val keyStoreCipher: KeyStoreCipher by lazy {
    KeyStoreCipher()
  }

  private var resetOnError = false

  fun setOptions(options: Map<String, String>) {
    val prefix = options[OPTION_PREFERENCES_KEY_PREFIX]
    if (!prefix.isNullOrEmpty()) {
      elementPreferencesKeyPrefix = prefix
    }

    resetOnError = options[OPTION_RESET_ON_ERROR] == OPTION_VALUE_TRUE
  }

  private val dataStoreStorage: DataStoreStorage by lazy {
    DataStoreStorage(applicationContext)
  }

  private var elementPreferencesKeyPrefix: String = DEFAULT_ELEMENT_PREFERENCES_KEY_PREFIX
  private var migrated = false
  private val migrationMutex = Mutex()

  fun getResetOnError(): Boolean {
    return resetOnError
  }

  private suspend fun migrateLegacyKeys() {
    if (migrated) return
    migrationMutex.withLock {
      if (migrated) return

      val legacyPrefix = elementPreferencesKeyPrefix + KEY_SEPARATOR
      val values = dataStoreStorage.readAll()
      for ((key, value) in values) {
        if (key.startsWith(legacyPrefix)) {
          val cleanKey = key.removePrefix(legacyPrefix)
          if (!dataStoreStorage.containsKey(cleanKey)) {
            dataStoreStorage.write(cleanKey, value)
          }
          dataStoreStorage.delete(key)
        }
      }

      migrated = true
    }
  }

  suspend fun containsKey(key: String): Boolean {
    migrateLegacyKeys()
    return dataStoreStorage.containsKey(key)
  }

  suspend fun read(key: String): String? {
    migrateLegacyKeys()
    val value = dataStoreStorage.read(key) ?: return null
    return withContext(Dispatchers.IO) {
      decodeRawValue(value)
    }
  }

  suspend fun readAll(): Map<String, String> {
    migrateLegacyKeys()
    val values = dataStoreStorage.readAll()
    return withContext(Dispatchers.IO) {
      values.mapValues { (_, value) -> decodeRawValue(value) }
    }
  }

  suspend fun write(key: String, value: String) {
    migrateLegacyKeys()
    val encodeValue = withContext(Dispatchers.IO) {
      encodeRawValue(value)
    }
    dataStoreStorage.write(key, encodeValue)
  }

  suspend fun delete(key: String) {
    migrateLegacyKeys()
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
    private const val OPTION_PREFERENCES_KEY_PREFIX = "preferencesKeyPrefix"
    private const val OPTION_RESET_ON_ERROR = "resetOnError"

    private const val OPTION_VALUE_TRUE = "true"

    private const val DEFAULT_ELEMENT_PREFERENCES_KEY_PREFIX =
      "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIHNlY3VyZSBzdG9yYWdlCg"
    private const val KEY_SEPARATOR = "_"
  }
}

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

  fun getResetOnError(): Boolean {
    return resetOnError
  }

  fun addPrefixToKey(key: String): String {
    return elementPreferencesKeyPrefix + KEY_SEPARATOR + key
  }

  private fun removePrefixFromKey(keyWithPrefix: String): String {
    val prefixWithSeparator = elementPreferencesKeyPrefix + KEY_SEPARATOR
    return keyWithPrefix.replaceFirst(prefixWithSeparator.toRegex(), "")
  }

  suspend fun containsKey(key: String): Boolean {
    val keyWithPrefix = addPrefixToKey(key)
    return dataStoreStorage.containsKey(keyWithPrefix)
  }

  suspend fun read(key: String): String? {
    val keyWithPrefix = addPrefixToKey(key)
    val value = dataStoreStorage.read(keyWithPrefix)
    if (value.isNullOrEmpty()) {
      return null
    }

    return decodeRawValue(value)
  }

  suspend fun readAll(): Map<String, String> {
    val values = dataStoreStorage.readAll()
    val result = mutableMapOf<String, String>()
    for ((keyWithPrefix, value) in values) {
      if (keyWithPrefix.startsWith(elementPreferencesKeyPrefix + KEY_SEPARATOR)) {
        val cleanKey = removePrefixFromKey(keyWithPrefix)
        result[cleanKey] = decodeRawValue(value)
      }
    }

    return result
  }

  suspend fun write(key: String, value: String) {
    val keyWithPrefix = addPrefixToKey(key)
    val encodeValue = encodeRawValue(value)
    dataStoreStorage.write(keyWithPrefix, encodeValue)
  }

  suspend fun delete(key: String) {
    val keyWithPrefix = addPrefixToKey(key)
    dataStoreStorage.delete(keyWithPrefix)
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

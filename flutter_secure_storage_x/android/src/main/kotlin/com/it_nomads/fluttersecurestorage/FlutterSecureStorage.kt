package com.it_nomads.fluttersecurestorage

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.core.content.edit
import com.it_nomads.fluttersecurestorage.ciphers.KeyStoreCipher
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipherFactory
import com.it_nomads.fluttersecurestorage.storage.DataStoreStorage
import com.it_nomads.fluttersecurestorage.storage.StoreType
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class FlutterSecureStorage(
  private val applicationContext: Context,
) {
  private var preferences: SharedPreferences? = null
  private val storageCipherFactory: StorageCipherFactory by lazy {
    StorageCipherFactory()
  }
  private val storageCipher: StorageCipher by lazy {
    storageCipherFactory.getCurrentStorageCipher(applicationContext)
  }
  private val keyStoreCipher: KeyStoreCipher by lazy {
    KeyStoreCipher()
  }

  private var resetOnError = false
  private var useDataStore = false

  fun setOptions(options: Map<String, String>) {
    val name = options[OPTION_SHARED_PREFERENCES_NAME]
    if (!name.isNullOrEmpty()) {
      sharedPreferencesName = name
    }

    val prefix = options[OPTION_PREFERENCES_KEY_PREFIX]
    if (!prefix.isNullOrEmpty()) {
      elementPreferencesKeyPrefix = prefix
    }

    resetOnError = options[OPTION_RESET_ON_ERROR] == OPTION_VALUE_TRUE
    useDataStore = options[OPTION_DATA_STORE] == OPTION_VALUE_TRUE
  }

  private val dataStoreStorageOld: DataStoreStorage by lazy {
    DataStoreStorage(applicationContext, StoreType.PREVIOUS)
  }

  private val dataStoreStorage: DataStoreStorage by lazy {
    DataStoreStorage(applicationContext, StoreType.KEY_STORE)
  }

  private var elementPreferencesKeyPrefix: String = DEFAULT_ELEMENT_PREFERENCES_KEY_PREFIX
  private var sharedPreferencesName: String = DEFAULT_SHARED_PREFERENCES_NAME

  fun getResetOnError(): Boolean {
    return resetOnError
  }

  private fun getUseDataStore(): Boolean {
    return useDataStore
  }

  fun addPrefixToKey(key: String): String {
    return elementPreferencesKeyPrefix + KEY_SEPARATOR + key
  }

  private fun removePrefixFromKey(keyWithPrefix: String): String {
    val prefixWithSeparator = elementPreferencesKeyPrefix + KEY_SEPARATOR
    return keyWithPrefix.replaceFirst(prefixWithSeparator.toRegex(), "")
  }

  suspend fun containsKey(key: String): Boolean {
    ensureInitialized()
    val keyWithPrefix = addPrefixToKey(key)
    if (getUseDataStore()) {
      return dataStoreStorage.containsKey(keyWithPrefix)
    }

    return preferences!!.contains(keyWithPrefix)
  }

  suspend fun read(key: String): String? {
    ensureInitialized()
    val keyWithPrefix = addPrefixToKey(key)
    if (getUseDataStore()) {
      val value = dataStoreStorage.read(keyWithPrefix)
      if (value.isNullOrEmpty()) {
        return null
      }

      return decodeRawValue(value)
    }

    val rawValue = preferences!!.getString(keyWithPrefix, null) ?: return null

    return decodeRawValue(rawValue)
  }

  suspend fun readAll(): Map<String, String> {
    ensureInitialized()
    if (getUseDataStore()) {
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

    val raw = preferences!!.all
    val all = mutableMapOf<String, String>()
    for (entry in raw.entries) {
      val keyWithPrefix = entry.key
      if (keyWithPrefix.startsWith(elementPreferencesKeyPrefix + KEY_SEPARATOR)) {
        val key = removePrefixFromKey(keyWithPrefix)
        val value = entry.value
        if (value !is String) {
          continue
        }

        val decodeValue = decodeRawValue(value)
        all[key] = decodeValue
      }
    }
    return all
  }

  suspend fun write(key: String, value: String) {
    ensureInitialized()
    val keyWithPrefix = addPrefixToKey(key)
    if (getUseDataStore()) {
      val encodeValue = encodeRawValue(value)
      dataStoreStorage.write(keyWithPrefix, encodeValue)
      return
    }

    preferences!!.edit {
      val encodeValue = encodeRawValue(value)
      putString(keyWithPrefix, encodeValue)
    }
  }

  suspend fun delete(key: String) {
    ensureInitialized()
    val keyWithPrefix = addPrefixToKey(key)
    if (getUseDataStore()) {
      dataStoreStorage.delete(keyWithPrefix)
      return
    }

    preferences!!.edit {
      remove(keyWithPrefix)
    }
  }

  suspend fun deleteAll() {
    ensureInitialized()
    if (getUseDataStore()) {
      dataStoreStorage.deleteAll()
      return
    }

    preferences!!.edit { clear() }
  }

  private suspend fun ensureInitialized() {
    // SharedPreferences name can be set via options, so we initialize it here.
    val newPreferences = applicationContext.getSharedPreferences(
      sharedPreferencesName,
      Context.MODE_PRIVATE,
    )

    preferences = newPreferences
    if (getUseDataStore()) {
      val oldDataStoreValues = dataStoreStorageOld.readAll()
      if (oldDataStoreValues.isNotEmpty()) {
        val newValues = oldDataStoreValues.map { (key, value) ->
          key to encodeRawValue(decodeRawValueOld(value))
        }.toMap()

        dataStoreStorage.writeAll(newValues)
        dataStoreStorageOld.deleteAll()
        return
      }

      val entries = preferences!!.all
      if (entries.isNullOrEmpty()) {
        // No migration required
        return
      }
      val values = mutableMapOf<String, String>()
      for (entry in entries.entries) {
        val key = entry.key
        if (!key.contains(elementPreferencesKeyPrefix)) {
          continue
        }

        val value = entry.value
        if (value !is String) {
          continue
        }

        val newValue = encodeRawValue(decodeRawValueOld(value))
        values[key] = newValue
      }

      dataStoreStorage.writeAll(values)
      preferences!!.edit { clear() }
      return
    }

    val isKeyStoreMigrationFinished = storageCipherFactory.isFinishKeyStoreMigration(preferences!!)
    if (isKeyStoreMigrationFinished) {
      return
    }

    val entries = preferences!!.all
    val values = mutableMapOf<String, String>()
    for (entry in entries.entries) {
      val key = entry.key
      if (!key.contains(elementPreferencesKeyPrefix)) {
        continue
      }

      val value = entry.value
      if (value !is String) {
        continue
      }

      try {
        decodeRawValue(value)
      } catch (_: Exception) {
        val newValue = encodeRawValue(decodeRawValueOld(value))
        values[key] = newValue
      }
    }


    preferences!!.edit {
      for (entry in values) {
        putString(entry.key, entry.value)
      }
      storageCipherFactory.setFinishKeyStoreMigration(this)
    }
  }

  private fun encodeRawValue(value: String): String {
    return keyStoreCipher.encrypt(value)
  }

  private fun decodeRawValue(value: String): String {
    return keyStoreCipher.decrypt(value)
  }

  private fun encodeRawValueOld(value: String): String {
    val data = value.toByteArray(charset)
    val result = storageCipher.encrypt(data)

    return Base64.encodeToString(result, 0)
  }

  private fun decodeRawValueOld(value: String): String {
    val data = Base64.decode(value, 0)
    val result = storageCipher.decrypt(data)

    return String(result, charset)
  }

  companion object {
    private const val OPTION_SHARED_PREFERENCES_NAME = "sharedPreferencesName"
    private const val OPTION_PREFERENCES_KEY_PREFIX = "preferencesKeyPrefix"
    private const val OPTION_RESET_ON_ERROR = "resetOnError"
    private const val OPTION_DATA_STORE = "dataStore"

    private const val OPTION_VALUE_TRUE = "true"

    private const val DEFAULT_SHARED_PREFERENCES_NAME = "FlutterSecureStorage"
    private const val DEFAULT_ELEMENT_PREFERENCES_KEY_PREFIX =
      "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIHNlY3VyZSBzdG9yYWdlCg"
    private const val KEY_SEPARATOR = "_"

    private val charset: Charset = StandardCharsets.UTF_8
  }
}

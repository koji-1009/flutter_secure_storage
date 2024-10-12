package com.it_nomads.fluttersecurestorage

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipherFactory
import com.it_nomads.fluttersecurestorage.storage.DataStoreStorage
import java.lang.Exception
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class FlutterSecureStorage(
  private val applicationContext: Context,
) {
  private var preferences: SharedPreferences? = null
  private var storageCipher: StorageCipher? = null
  private var storageCipherFactory: StorageCipherFactory? = null

  private var resetOnError = false
  private var useEncryptedSharedPreferences = false
  private var useDataStore = false

  fun setOptions(options: Map<String, String>) {
    val name = options["sharedPreferencesName"]
    if (!name.isNullOrEmpty()) {
      sharedPreferencesName = name
    }

    val prefix = options["preferencesKeyPrefix"]
    if (!prefix.isNullOrEmpty()) {
      elementPreferencesKeyPrefix = prefix
    }

    resetOnError = options["resetOnError"] == "true"
    useEncryptedSharedPreferences = options["encryptedSharedPreferences"] == "true"
    useDataStore = options["dataStore"] == "true"
  }

  private val dataStoreStorage: DataStoreStorage by lazy {
    DataStoreStorage(applicationContext)
  }

  private var elementPreferencesKeyPrefix: String =
    "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIHNlY3VyZSBzdG9yYWdlCg"
  private var sharedPreferencesName: String = "FlutterSecureStorage"
  private var failedToUseEncryptedSharedPreferences = false

  fun getResetOnError(): Boolean {
    return resetOnError
  }

  private fun getUseEncryptedSharedPreferences(): Boolean {
    if (failedToUseEncryptedSharedPreferences) {
      return false
    }
    return useEncryptedSharedPreferences
  }

  private fun getUseDataStore(): Boolean {
    return useDataStore
  }

  fun addPrefixToKey(key: String): String {
    return elementPreferencesKeyPrefix + "_" + key
  }

  fun containsKey(key: String): Boolean {
    ensureInitialized()
    if (getUseDataStore()) {
      return dataStoreStorage.containsKey(key)
    }

    return preferences!!.contains(key)
  }

  fun read(key: String): String? {
    ensureInitialized()
    if (getUseDataStore()) {
      val value = dataStoreStorage.read(key)
      if (value.isNullOrEmpty()) {
        return null
      }

      return decodeRawValue(value)
    }

    val rawValue = preferences!!.getString(key, null)
    if (rawValue == null) {
      return null
    }

    if (getUseEncryptedSharedPreferences()) {
      return rawValue
    }
    return decodeRawValue(rawValue)
  }

  fun readAll(): Map<String, String> {
    ensureInitialized()
    if (getUseDataStore()) {
      val values = dataStoreStorage.readAll()
      val result = mutableMapOf<String, String>()
      for ((key, value) in values) {
        if (key.contains(elementPreferencesKeyPrefix)) {
          val key = key.replaceFirst((elementPreferencesKeyPrefix + '_').toRegex(), "")
          result[key] = decodeRawValue(value)
        }
      }

      return result
    }

    val raw = preferences!!.all
    val all = mutableMapOf<String, String>()
    for (entry in raw.entries) {
      val keyWithPrefix = entry.key
      if (keyWithPrefix.contains(elementPreferencesKeyPrefix)) {
        val key = entry.key.replaceFirst((elementPreferencesKeyPrefix + '_').toRegex(), "")
        val value = entry.value
        if (value !is String) {
          continue
        }

        if (getUseEncryptedSharedPreferences()) {
          all.put(key, value)
        } else {
          val decodeValue = decodeRawValue(value)
          all.put(key, decodeValue)
        }
      }
    }
    return all
  }

  fun write(key: String, value: String) {
    ensureInitialized()
    if (getUseDataStore()) {
      val encodeValue = encodeRawValue(value)
      dataStoreStorage.write(key, encodeValue)
      return
    }

    val editor = preferences!!.edit()

    if (getUseEncryptedSharedPreferences()) {
      editor.putString(key, value)
    } else {
      val encodeValue = encodeRawValue(value)
      editor.putString(key, encodeValue)
    }
    editor.apply()
  }

  fun delete(key: String) {
    ensureInitialized()
    if (getUseDataStore()) {
      dataStoreStorage.delete(key)
      return
    }

    val editor = preferences!!.edit()
    editor.remove(key)
    editor.apply()
  }

  fun deleteAll() {
    ensureInitialized()
    if (getUseDataStore()) {
      dataStoreStorage.deleteAll()
      return
    }

    val editor = preferences!!.edit()
    editor.clear()
    if (!getUseEncryptedSharedPreferences()) {
      storageCipherFactory!!.storeCurrentAlgorithms(editor)
    }
    editor.apply()
  }

  private fun ensureInitialized() {
    // Check if already initialized.
    // TODO: Disable for now because this will break mixed usage of secureSharedPreference
//        if (preferences != null) return;

    val nonEncryptedPreferences = applicationContext.getSharedPreferences(
      sharedPreferencesName,
      Context.MODE_PRIVATE,
    )
    if (storageCipher == null) {
      try {
        initStorageCipher(nonEncryptedPreferences)
      } catch (e: Exception) {
        Log.e(TAG, "StorageCipher initialization failed", e)
      }
    }

    if (getUseEncryptedSharedPreferences()) {
      try {
        preferences = initializeEncryptedSharedPreferencesManager(applicationContext)
        checkAndMigrateToEncrypted(nonEncryptedPreferences, preferences!!)
      } catch (e: Exception) {
        Log.e(TAG, "EncryptedSharedPreferences initialization failed", e)
        preferences = nonEncryptedPreferences
        failedToUseEncryptedSharedPreferences = true
      }
    } else {
      preferences = nonEncryptedPreferences
    }

    if (getUseDataStore()) {
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

        if (getUseEncryptedSharedPreferences()) {
          val encodeValue = encodeRawValue(value)
          dataStoreStorage.write(key, encodeValue)
        } else {
          values[key] = value
        }
      }

      dataStoreStorage.writeAll(values)
      preferences!!.edit().clear().apply()
      return
    }
  }

  private fun initStorageCipher(source: SharedPreferences) {
    storageCipherFactory = StorageCipherFactory(source)
    if (getUseDataStore()) {
      storageCipher = storageCipherFactory!!.getCurrentStorageCipher(applicationContext)
    } else if (getUseEncryptedSharedPreferences()) {
      storageCipher = storageCipherFactory!!.getSavedStorageCipher(applicationContext)
    } else if (storageCipherFactory!!.requiresReEncryption()) {
      reEncryptPreferences(storageCipherFactory!!, source)
    } else {
      storageCipher = storageCipherFactory!!.getCurrentStorageCipher(applicationContext)
    }
  }

  private fun reEncryptPreferences(
    storageCipherFactory: StorageCipherFactory,
    source: SharedPreferences,
  ) {
    try {
      storageCipher = storageCipherFactory.getSavedStorageCipher(applicationContext)
      val cache = mutableMapOf<String, String>()
      for (entry in source.all.entries) {
        val v = entry.value
        val key = entry.key
        if (v is String && key.contains(elementPreferencesKeyPrefix)) {
          val decodeValue = decodeRawValue(v)
          cache.put(key, decodeValue)
        }
      }
      storageCipher = storageCipherFactory.getCurrentStorageCipher(applicationContext)
      val editor = source.edit()
      for (entry in cache.entries) {
        val decodeValue = decodeRawValue(entry.value)
        editor.putString(entry.key, decodeValue)
      }
      storageCipherFactory.storeCurrentAlgorithms(editor)
      editor.apply()
    } catch (e: Exception) {
      Log.e(TAG, "re-encryption failed", e)
      storageCipher = storageCipherFactory.getSavedStorageCipher(applicationContext)
    }
  }

  private fun checkAndMigrateToEncrypted(source: SharedPreferences, target: SharedPreferences) {
    try {
      for (entry in source.all.entries) {
        val v = entry.value
        val key = entry.key
        if (v is String && key.contains(elementPreferencesKeyPrefix)) {
          val decodeValue = decodeRawValue(v)
          target.edit().putString(key, (decodeValue)).apply()
          source.edit().remove(key).apply()
        }
      }
      val sourceEditor = source.edit()
      storageCipherFactory!!.removeCurrentAlgorithms(sourceEditor)
      sourceEditor.apply()
    } catch (e: Exception) {
      Log.e(TAG, "Data migration failed", e)
    }
  }

  private fun initializeEncryptedSharedPreferencesManager(context: Context): SharedPreferences {
    val key = MasterKey.Builder(context)
      .setKeyGenParameterSpec(
        KeyGenParameterSpec.Builder(
          MasterKey.DEFAULT_MASTER_KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setKeySize(256).build()
      )
      .build()
    return EncryptedSharedPreferences.create(
      context,
      sharedPreferencesName,
      key,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }

  private fun encodeRawValue(value: String): String {
    val data = value.toByteArray(charset)
    val result = storageCipher!!.encrypt(data)

    return Base64.encodeToString(result, 0)
  }

  private fun decodeRawValue(value: String): String {
    val data = Base64.decode(value, 0)
    val result = storageCipher!!.decrypt(data)

    return String(result, charset)
  }

  companion object {
    private const val TAG = "SecureStorageAndroid"

    private val charset: Charset = StandardCharsets.UTF_8
  }
}

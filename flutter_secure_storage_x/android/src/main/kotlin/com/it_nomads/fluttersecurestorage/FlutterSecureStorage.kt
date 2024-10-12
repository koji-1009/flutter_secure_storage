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
import java.lang.Exception
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

class FlutterSecureStorage(private val applicationContext: Context) {
  private var options = mutableMapOf<String, String>()

  private var preferences: SharedPreferences? = null
  private var storageCipher: StorageCipher? = null
  private var storageCipherFactory: StorageCipherFactory? = null

  private var elementPreferencesKeyPrefix: String =
    "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIHNlY3VyZSBzdG9yYWdlCg"
  private var sharedPreferencesName: String = "FlutterSecureStorage"
  private var failedToUseEncryptedSharedPreferences = false

  fun getResetOnError(): Boolean {
    return options["resetOnError"] == "true"
  }

  private fun getUseEncryptedSharedPreferences(): Boolean {
    if (failedToUseEncryptedSharedPreferences) {
      return false
    }
    return options["encryptedSharedPreferences"] == "true"
  }

  fun containsKey(key: String): Boolean {
    ensureInitialized()
    return preferences!!.contains(key)
  }

  fun addPrefixToKey(key: String): String {
    return elementPreferencesKeyPrefix + "_" + key
  }

  fun read(key: String): String? {
    ensureInitialized()

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

    val raw = preferences!!.all as Map<String, String>

    val all = mutableMapOf<String, String>()
    for (entry in raw.entries) {
      val keyWithPrefix = entry.key
      if (keyWithPrefix.contains(elementPreferencesKeyPrefix)) {
        val key = entry.key.replaceFirst((elementPreferencesKeyPrefix + '_').toRegex(), "")
        if (getUseEncryptedSharedPreferences()) {
          all.put(key, entry.value)
        } else {
          val rawValue = entry.value
          val value = decodeRawValue(rawValue)

          all.put(key, value)
        }
      }
    }
    return all
  }

  fun write(key: String, value: String) {
    ensureInitialized()

    val editor = preferences!!.edit()

    if (getUseEncryptedSharedPreferences()) {
      editor.putString(key, value)
    } else {
      val result = storageCipher!!.encrypt(value.toByteArray(charset))
      editor.putString(key, Base64.encodeToString(result, 0))
    }
    editor.apply()
  }

  fun delete(key: String) {
    ensureInitialized()

    val editor = preferences!!.edit()
    editor.remove(key)
    editor.apply()
  }

  fun deleteAll() {
    ensureInitialized()

    val editor = preferences!!.edit()
    editor.clear()
    if (!getUseEncryptedSharedPreferences()) {
      storageCipherFactory!!.storeCurrentAlgorithms(editor)
    }
    editor.apply()
  }

  fun setOptions(options: Map<String, String>) {
    this.options = options.toMutableMap()
  }

  fun ensureOptions() {
    val name = options["sharedPreferencesName"]
    if (!name.isNullOrEmpty()) {
      sharedPreferencesName = name
    }

    val prefix = options["preferencesKeyPrefix"]
    if (!prefix.isNullOrEmpty()) {
      elementPreferencesKeyPrefix = prefix
    }
  }

  private fun ensureInitialized() {
    // Check if already initialized.
    // TODO: Disable for now because this will break mixed usage of secureSharedPreference
//        if (preferences != null) return;

    ensureOptions()

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
  }

  private fun initStorageCipher(source: SharedPreferences) {
    storageCipherFactory = StorageCipherFactory(source)
    if (getUseEncryptedSharedPreferences()) {
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
          val decodedValue = decodeRawValue(v)
          cache.put(key, decodedValue)
        }
      }
      storageCipher = storageCipherFactory.getCurrentStorageCipher(applicationContext)
      val editor = source.edit()
      for (entry in cache.entries) {
        val result = storageCipher!!.encrypt(entry.value.toByteArray(charset))
        editor.putString(entry.key, Base64.encodeToString(result, 0))
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
        val v: Any? = entry.value
        val key = entry.key
        if (v is String && key.contains(elementPreferencesKeyPrefix)) {
          val decodedValue = decodeRawValue(v)
          target.edit().putString(key, (decodedValue)).apply()
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

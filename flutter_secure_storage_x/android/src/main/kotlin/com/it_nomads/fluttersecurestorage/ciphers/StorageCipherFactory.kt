package com.it_nomads.fluttersecurestorage.ciphers

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.it_nomads.fluttersecurestorage.ciphers.deprecated.RSACipher18Implementation
import com.it_nomads.fluttersecurestorage.ciphers.deprecated.StorageCipher18Implementation

private enum class KeyCipherAlgorithm(
  val value: String,
  val minVersionCode: Int,
) {
  /**
   * RSA_ECB_OAEPwithSHA_256andMGF1Padding
   */
  TargetMinSDK23(
    value = "RSA_ECB_OAEPwithSHA_256andMGF1Padding",
    minVersionCode = Build.VERSION_CODES.M,
  ),

  /**
   * RSA_ECB_PKCS1Padding
   */
  TargetMinSDK1(
    value = "RSA_ECB_PKCS1Padding",
    minVersionCode = 1,
  ),
  ;

  fun getCipher(context: Context): KeyCipher = when (this) {
    TargetMinSDK23 -> RSACipherOAEPImplementation(context)
    TargetMinSDK1 -> RSACipher18Implementation()

  }
}

private enum class StorageCipherAlgorithm(
  val value: String,
  val minVersionCode: Int,
) {
  /**
   * AES_GCM_NoPadding
   */
  TargetMinSDK23(
    value = "AES_GCM_NoPadding",
    minVersionCode = Build.VERSION_CODES.M,
  ),

  /**
   * AES_CBC_PKCS7Padding
   */
  TargetMinSDK1(
    value = "AES_CBC_PKCS7Padding",
    minVersionCode = 1,
  ),
  ;

  fun getStorageCipher(context: Context, keyCipher: KeyCipher): StorageCipher = when (this) {
    TargetMinSDK23 -> StorageCipherGCMImplementation(context, keyCipher)
    TargetMinSDK1 -> StorageCipher18Implementation()
  }
}

internal class StorageCipherFactory(source: SharedPreferences, options: Map<String, String>) {
  private val savedKeyAlgorithm: KeyCipherAlgorithm by lazy {
    val savedKeyAlgorithmValue = source.getString(
      ELEMENT_PREFERENCES_ALGORITHM_KEY,
      DEFAULT_KEY_ALGORITHM.value,
    )!!
    KeyCipherAlgorithm.values().first {
      it.value == savedKeyAlgorithmValue
    }
  }
  private val savedStorageAlgorithm: StorageCipherAlgorithm by lazy {
    val savedStorageAlgorithmValue = source.getString(
      ELEMENT_PREFERENCES_ALGORITHM_STORAGE,
      DEFAULT_STORAGE_ALGORITHM.value,
    )!!
    StorageCipherAlgorithm.values().first {
      it.value == savedStorageAlgorithmValue
    }
  }

  private val currentKeyAlgorithm: KeyCipherAlgorithm by lazy {
    val currentKeyAlgorithmTmpValue = options["keyCipherAlgorithm"] ?: DEFAULT_KEY_ALGORITHM.value
    val currentKeyAlgorithmTmp = KeyCipherAlgorithm.values().first {
      it.value == currentKeyAlgorithmTmpValue
    }

    // TODO: remove this check
    if (currentKeyAlgorithmTmp.minVersionCode <= Build.VERSION.SDK_INT) {
      currentKeyAlgorithmTmp
    } else {
      DEFAULT_KEY_ALGORITHM
    }
  }
  private val currentStorageAlgorithm: StorageCipherAlgorithm by lazy {
    val currentStorageAlgorithmTmpValue =
      options["storageCipherAlgorithm"] ?: DEFAULT_STORAGE_ALGORITHM.value
    val currentStorageAlgorithmTmp = StorageCipherAlgorithm.values().first {
      it.value == currentStorageAlgorithmTmpValue
    }

    // TODO: remove this check
    if (currentStorageAlgorithmTmp.minVersionCode <= Build.VERSION.SDK_INT) {
      currentStorageAlgorithmTmp
    } else {
      DEFAULT_STORAGE_ALGORITHM
    }
  }

  // TODO: remove this method
  fun requiresReEncryption(): Boolean {
    return savedKeyAlgorithm != currentKeyAlgorithm || savedStorageAlgorithm != currentStorageAlgorithm
  }

  fun getSavedStorageCipher(context: Context): StorageCipher {
    val keyCipher = savedKeyAlgorithm.getCipher(context)
    return savedStorageAlgorithm.getStorageCipher(context, keyCipher)
  }

  fun getCurrentStorageCipher(context: Context): StorageCipher {
    val keyCipher = currentKeyAlgorithm.getCipher(context)
    return currentStorageAlgorithm.getStorageCipher(context, keyCipher)
  }

  fun storeCurrentAlgorithms(editor: SharedPreferences.Editor) {
    editor.putString(ELEMENT_PREFERENCES_ALGORITHM_KEY, currentKeyAlgorithm.value)
    editor.putString(ELEMENT_PREFERENCES_ALGORITHM_STORAGE, currentStorageAlgorithm.value)
  }

  fun removeCurrentAlgorithms(editor: SharedPreferences.Editor) {
    editor.remove(ELEMENT_PREFERENCES_ALGORITHM_KEY)
    editor.remove(ELEMENT_PREFERENCES_ALGORITHM_STORAGE)
  }

  companion object {
    private const val ELEMENT_PREFERENCES_ALGORITHM_PREFIX = "FlutterSecureSAlgorithm"
    private const val ELEMENT_PREFERENCES_ALGORITHM_KEY =
      ELEMENT_PREFERENCES_ALGORITHM_PREFIX + "Key"
    private const val ELEMENT_PREFERENCES_ALGORITHM_STORAGE =
      ELEMENT_PREFERENCES_ALGORITHM_PREFIX + "Storage"
    private val DEFAULT_KEY_ALGORITHM = KeyCipherAlgorithm.TargetMinSDK23
    private val DEFAULT_STORAGE_ALGORITHM = StorageCipherAlgorithm.TargetMinSDK23
  }
}

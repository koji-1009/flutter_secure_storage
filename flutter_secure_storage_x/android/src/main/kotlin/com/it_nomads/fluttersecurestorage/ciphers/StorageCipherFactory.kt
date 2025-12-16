package com.it_nomads.fluttersecurestorage.ciphers

import android.content.Context
import android.content.SharedPreferences

private enum class KeyCipherAlgorithm(
  val value: String,
) {
  /**
   * RSA_ECB_OAEPwithSHA_256andMGF1Padding
   */
  TargetMinSDK23(
    value = "RSA_ECB_OAEPwithSHA_256andMGF1Padding",
  ),
  ;

  fun getCipher(context: Context): KeyCipher = when (this) {
    TargetMinSDK23 -> RSACipherOAEPImplementation(context)
  }
}

private enum class StorageCipherAlgorithm(
  val value: String,
) {
  /**
   * AES_GCM_NoPadding
   */
  TargetMinSDK23(
    value = "AES_GCM_NoPadding",
  ),
  ;

  fun getStorageCipher(context: Context, keyCipher: KeyCipher): StorageCipher = when (this) {
    TargetMinSDK23 -> StorageCipherGCMImplementation(context, keyCipher)
  }
}

internal class StorageCipherFactory() {
  private val currentKeyAlgorithm: KeyCipherAlgorithm by lazy {
    KeyCipherAlgorithm.TargetMinSDK23
  }

  private val currentStorageAlgorithm: StorageCipherAlgorithm by lazy {
    StorageCipherAlgorithm.TargetMinSDK23
  }

  fun getCurrentStorageCipher(context: Context): StorageCipher {
    val keyCipher = currentKeyAlgorithm.getCipher(context)
    return currentStorageAlgorithm.getStorageCipher(context, keyCipher)
  }

  fun isFinishKeyStoreMigration(prefs: SharedPreferences): Boolean {
    return prefs.getBoolean(KEY_FINISH_KEY_STORE_MIGRATION, false)
  }

  fun setFinishKeyStoreMigration(editor: SharedPreferences.Editor) {
    editor.putBoolean(KEY_FINISH_KEY_STORE_MIGRATION, true)
  }

  companion object {
    private const val KEY_FINISH_KEY_STORE_MIGRATION = "KeyFinishKeyStoreMigration"

  }
}

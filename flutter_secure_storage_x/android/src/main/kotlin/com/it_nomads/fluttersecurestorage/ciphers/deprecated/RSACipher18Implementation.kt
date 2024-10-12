package com.it_nomads.fluttersecurestorage.ciphers.deprecated

import com.it_nomads.fluttersecurestorage.ciphers.KeyCipher
import java.security.Key

internal class RSACipher18Implementation : KeyCipher {
  override fun wrap(key: Key): Nothing {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun unwrap(wrappedKey: ByteArray, algorithm: String): Nothing {
    throw UnsupportedOperationException("Not implemented")
  }
}

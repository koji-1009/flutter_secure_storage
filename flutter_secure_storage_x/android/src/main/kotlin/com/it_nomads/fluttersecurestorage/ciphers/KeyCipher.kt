package com.it_nomads.fluttersecurestorage.ciphers

import java.security.Key

internal interface KeyCipher {
  fun wrap(key: Key): ByteArray

  fun unwrap(wrappedKey: ByteArray, algorithm: String): Key
}

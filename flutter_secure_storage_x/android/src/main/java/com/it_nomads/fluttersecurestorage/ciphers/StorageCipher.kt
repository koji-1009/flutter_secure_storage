package com.it_nomads.fluttersecurestorage.ciphers

internal interface StorageCipher {
  fun encrypt(input: ByteArray): ByteArray

  fun decrypt(input: ByteArray): ByteArray
}

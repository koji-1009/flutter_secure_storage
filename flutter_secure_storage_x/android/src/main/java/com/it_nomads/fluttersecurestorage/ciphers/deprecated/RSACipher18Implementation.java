package com.it_nomads.fluttersecurestorage.ciphers.deprecated;

import com.it_nomads.fluttersecurestorage.ciphers.KeyCipher;

import java.security.Key;

public class RSACipher18Implementation implements KeyCipher {
    public RSACipher18Implementation() {
    }

    @Override
    public byte[] wrap(Key key) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Key unwrap(byte[] wrappedKey, String algorithm) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

package com.it_nomads.fluttersecurestorage.ciphers.deprecated;

import android.content.Context;

import com.it_nomads.fluttersecurestorage.ciphers.KeyCipher;

import java.security.Key;

@Deprecated
public class RSACipher18Implementation implements KeyCipher {
    public RSACipher18Implementation(Context context) {
    }

    @Override
    public byte[] wrap(Key key) throws Exception {
        return null;
    }

    @Override
    public Key unwrap(byte[] wrappedKey, String algorithm) throws Exception {
        return null;
    }
}

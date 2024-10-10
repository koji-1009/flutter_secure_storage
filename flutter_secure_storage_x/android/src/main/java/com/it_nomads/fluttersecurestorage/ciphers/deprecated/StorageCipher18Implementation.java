package com.it_nomads.fluttersecurestorage.ciphers.deprecated;

import android.content.Context;

import com.it_nomads.fluttersecurestorage.ciphers.KeyCipher;
import com.it_nomads.fluttersecurestorage.ciphers.StorageCipher;

@Deprecated
public class StorageCipher18Implementation implements StorageCipher {
    public StorageCipher18Implementation(Context context, KeyCipher rsaCipher) {
    }

    @Override
    public byte[] encrypt(byte[] input) throws Exception {
        return null;
    }

    @Override
    public byte[] decrypt(byte[] input) throws Exception {
        return null;
    }
}

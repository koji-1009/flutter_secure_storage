package com.it_nomads.fluttersecurestorage.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
  name = "FlutterSecureStorage",
)

internal class DataStoreStorage(
  private val context: Context,
) {
  suspend fun containsKey(key: String): Boolean {
    return context.dataStore.data.map { pref ->
      pref.contains(stringPreferencesKey(key))
    }.firstOrNull() ?: false
  }

  suspend fun read(key: String): String? {
    return context.dataStore.data.map { pref ->
      pref[stringPreferencesKey(key)]
    }.firstOrNull()
  }

  suspend fun readAll(): Map<String, String> {
    val entries = context.dataStore.data.map { pref ->
      pref.asMap()
    }.firstOrNull()

    val result = mutableMapOf<String, String>()
    entries?.forEach { (key, value) ->
      result[key.name] = value as String
    }

    return result.toMap()
  }

  suspend fun write(key: String, value: String) {
    context.dataStore.edit { pref ->
      pref[stringPreferencesKey(key)] = value
    }
  }

  suspend fun writeAll(data: Map<String, String>) {
    context.dataStore.edit { pref ->
      data.forEach { (key, value) ->
        pref[stringPreferencesKey(key)] = value
      }
    }
  }

  suspend fun delete(key: String) {
    context.dataStore.edit { pref ->
      pref.remove(stringPreferencesKey(key))
    }
  }

  suspend fun deleteAll() {
    context.dataStore.edit { pref ->
      pref.clear()
    }
  }
}

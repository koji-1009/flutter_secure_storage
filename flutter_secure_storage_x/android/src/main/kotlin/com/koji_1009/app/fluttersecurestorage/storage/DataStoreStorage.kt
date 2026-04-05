package com.koji_1009.app.fluttersecurestorage.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
  name = "FlutterSecureStorageX",
)

internal class DataStoreStorage(context: Context) {
  private val dataStore: DataStore<Preferences> by lazy {
    context.dataStore
  }

  suspend fun containsKey(key: String): Boolean {
    return dataStore.data.map { pref ->
      pref.contains(stringPreferencesKey(key))
    }.firstOrNull() ?: false
  }

  suspend fun read(key: String): String? {
    return dataStore.data.map { pref ->
      pref[stringPreferencesKey(key)]
    }.firstOrNull()
  }

  suspend fun readAll(): Map<String, String> {
    val entries = dataStore.data.map { pref ->
      pref.asMap()
    }.firstOrNull()

    val result = mutableMapOf<String, String>()
    entries?.forEach { (key, value) ->
      result[key.name] = value as String
    }

    return result.toMap()
  }

  suspend fun write(key: String, value: String) {
    dataStore.edit { pref ->
      pref[stringPreferencesKey(key)] = value
    }
  }

  suspend fun writeAll(data: Map<String, String>) {
    dataStore.edit { pref ->
      data.forEach { (key, value) ->
        pref[stringPreferencesKey(key)] = value
      }
    }
  }

  suspend fun delete(key: String) {
    dataStore.edit { pref ->
      pref.remove(stringPreferencesKey(key))
    }
  }

  suspend fun deleteAll() {
    dataStore.edit { pref ->
      pref.clear()
    }
  }
}

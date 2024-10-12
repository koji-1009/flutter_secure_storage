package com.it_nomads.fluttersecurestorage.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
  name = "FlutterSecureStorage",
)

internal class DataStoreStorage(
  private val context: Context,
) {
  fun containsKey(key: String): Boolean = runBlocking {
    val result = context.dataStore.data.map { pref ->
      pref.contains(stringPreferencesKey(key))
    }.firstOrNull()

    result == true
  }

  fun read(key: String): String? = runBlocking {
    context.dataStore.data.map { pref ->
      pref[stringPreferencesKey(key)]
    }.firstOrNull()
  }

  fun readAll(): Map<String, String> = runBlocking {
    val entries = context.dataStore.data.map { pref ->
      pref.asMap()
    }.firstOrNull()

    val result = mutableMapOf<String, String>()
    entries?.forEach { (key, value) ->
      result[key.name] = value as String
    }

    result.toMap()
  }

  fun write(key: String, value: String): Unit = runBlocking {
    context.dataStore.edit { pref ->
      pref[stringPreferencesKey(key)] = value
    }
  }

  fun writeAll(data: Map<String, String>): Unit = runBlocking {
    context.dataStore.edit { pref ->
      data.forEach { (key, value) ->
        pref[stringPreferencesKey(key)] = value
      }
    }
  }

  fun delete(key: String): Unit = runBlocking {
    context.dataStore.edit { pref ->
      pref.remove(stringPreferencesKey(key))
    }
  }

  fun deleteAll(): Unit = runBlocking {
     context.dataStore.edit { pref ->
       pref.clear()
     }
   }
}

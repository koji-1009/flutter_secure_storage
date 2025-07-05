package com.it_nomads.fluttersecurestorage

import android.content.Context
import android.util.Log
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.FileNotFoundException

class FlutterSecureStoragePlugin : MethodCallHandler, FlutterPlugin {
  private var channel: MethodChannel? = null
  private var secureStorage: FlutterSecureStorage? = null
  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  fun initInstance(messenger: BinaryMessenger, context: Context) {
    try {
      secureStorage = FlutterSecureStorage(context)

      channel = MethodChannel(messenger, "plugins.it_nomads.com/flutter_secure_storage")
      channel!!.setMethodCallHandler(this)
    } catch (e: Exception) {
      Log.e(TAG, "Registration failed", e)
    }
  }

  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    initInstance(binding.binaryMessenger, binding.applicationContext)
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    coroutineScope.cancel()
    if (channel != null) {
      channel!!.setMethodCallHandler(null)
      channel = null
    }
    secureStorage = null
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    coroutineScope.launch {
      handle(call = call, result = result)
    }
  }

  private fun getKeyFromCall(call: MethodCall): String {
    val arguments = call.arguments as Map<String, *>
    return secureStorage!!.addPrefixToKey(arguments["key"] as String)
  }

  private fun getValueFromCall(call: MethodCall): String {
    val arguments = call.arguments as Map<String, *>
    return arguments["value"] as String
  }

  private suspend fun handle(call: MethodCall, result: MethodChannel.Result) {
    var resetOnError = false
    try {
      secureStorage!!.setOptions((call.arguments as Map<String, *>)["options"] as Map<String, String>)
      resetOnError = secureStorage!!.getResetOnError()
      when (call.method) {
        "write" -> {
          val key = getKeyFromCall(call)
          val value = getValueFromCall(call)
          secureStorage!!.write(key, value)
          result.success(null)
        }

        "read" -> {
          val key = getKeyFromCall(call)
          val value = secureStorage!!.read(key)
          result.success(value)
        }

        "readAll" -> {
          val value = secureStorage!!.readAll()
          result.success(value)
        }

        "containsKey" -> {
          val key = getKeyFromCall(call)
          val value = secureStorage!!.containsKey(key)
          result.success(value)
        }

        "delete" -> {
          val key = getKeyFromCall(call)
          secureStorage!!.delete(key)
          result.success(null)
        }

        "deleteAll" -> {
          secureStorage!!.deleteAll()
          result.success(null)
        }

        else -> result.notImplemented()
      }
    } catch (e: FileNotFoundException) {
      Log.i("Creating sharedPrefs", e.message!!)
    } catch (e: Exception) {
      if (resetOnError) {
        try {
          secureStorage!!.deleteAll()
          result.success("Data has been reset")
        } catch (ex: Exception) {
          result.error(
            "Exception encountered",
            "${call.method}: ${ex.message}",
            ex.stackTraceToString()
          )
        }
      } else {
        result.error(
          "Exception encountered",
          "${call.method}: ${e.message}",
          e.stackTraceToString()
        )
      }
    }
  }

  companion object {
    private const val TAG = "FlutterSecureStorage"
  }
}

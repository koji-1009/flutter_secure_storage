package com.it_nomads.fluttersecurestorage

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.it_nomads.fluttersecurestorage.FlutterSecureStoragePlugin.MethodResultWrapper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import java.io.FileNotFoundException
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception

class FlutterSecureStoragePlugin : MethodCallHandler, FlutterPlugin {
  private var channel: MethodChannel? = null
  private var secureStorage: FlutterSecureStorage? = null
  // TODO: Replace Kotlin Coroutines, when support for EncryptedSharedPreferences is dropped
  private var workerThread: HandlerThread? = null
  private var workerThreadHandler: Handler? = null

  fun initInstance(messenger: BinaryMessenger, context: Context) {
    try {
      secureStorage = FlutterSecureStorage(context)

      workerThread = HandlerThread("com.it_nomads.fluttersecurestorage.worker")
      workerThread!!.start()
      workerThreadHandler = Handler(workerThread!!.getLooper())

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
    if (channel != null) {
      workerThread!!.quitSafely()
      workerThread = null

      channel!!.setMethodCallHandler(null)
      channel = null
    }
    secureStorage = null
  }

  override fun onMethodCall(call: MethodCall, rawResult: MethodChannel.Result) {
    val result = MethodResultWrapper(rawResult)
    // Run all method calls inside the worker thread instead of the platform thread.
    workerThreadHandler!!.post(MethodRunner(call, result))
  }

  private fun getKeyFromCall(call: MethodCall): String {
    val arguments = call.arguments as Map<String, *>
    return secureStorage!!.addPrefixToKey(arguments["key"] as String)
  }

  private fun getValueFromCall(call: MethodCall): String {
    val arguments = call.arguments as Map<String, *>
    return arguments["value"] as String
  }

  /**
   * MethodChannel.Result wrapper that responds on the platform thread.
   */
  private class MethodResultWrapper(
    private val methodResult: MethodChannel.Result,
  ) : MethodChannel.Result {
    private val handler = Handler(Looper.getMainLooper())

    override fun success(result: Any?) {
      handler.post(Runnable { methodResult.success(result) })
    }

    override fun error(errorCode: String, errorMessage: String?, errorDetails: Any?) {
      handler.post(Runnable { methodResult.error(errorCode, errorMessage, errorDetails) })
    }

    override fun notImplemented() {
      handler.post(Runnable { methodResult.notImplemented() })
    }
  }

  /**
   * Wraps the functionality of onMethodCall() in a Runnable for execution in the worker thread.
   */
  private inner class MethodRunner(
    private val call: MethodCall,
    private val result: MethodChannel.Result,
  ) : Runnable {

    override fun run() {
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

            if (secureStorage!!.containsKey(key)) {
              val value = secureStorage!!.read(key)
              result.success(value)
            } else {
              result.success(null)
            }
          }

          "readAll" -> {
            result.success(secureStorage!!.readAll())
          }

          "containsKey" -> {
            val key = getKeyFromCall(call)

            val containsKey = secureStorage!!.containsKey(key)
            result.success(containsKey)
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
            handleException(ex)
          }
        } else {
          handleException(e)
        }
      }
    }

    private fun handleException(e: Exception) {
      val stringWriter = StringWriter()
      e.printStackTrace(PrintWriter(stringWriter))
      result.error("Exception encountered", call.method, stringWriter.toString())
    }
  }

  companion object {
    private const val TAG = "FlutterSecureStoragePl"
  }
}

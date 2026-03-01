package com.koji_1009.app.fluttersecurestorage

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.FlutterPlugin.FlutterPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class FlutterSecureStoragePlugin : MethodCallHandler, FlutterPlugin {
  private var channel: MethodChannel? = null
  private var secureStorage: FlutterSecureStorage? = null
  private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  override fun onAttachedToEngine(binding: FlutterPluginBinding) {
    channel = MethodChannel(binding.binaryMessenger, METHOD_CHANNEL_NAME).apply {
      setMethodCallHandler(this@FlutterSecureStoragePlugin)
    }
    secureStorage = FlutterSecureStorage(
      applicationContext = binding.applicationContext,
    )
  }

  override fun onDetachedFromEngine(binding: FlutterPluginBinding) {
    coroutineScope.cancel()
    channel?.setMethodCallHandler(null)
    channel = null
    secureStorage = null
  }

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    coroutineScope.launch {
      handle(call, result)
    }
  }

  private suspend fun handle(call: MethodCall, result: MethodChannel.Result) {
    val secureStorage = secureStorage
    if (secureStorage == null) {
      result.error(
        ERROR_CODE_SECURE_STORAGE_NOT_INITIALIZED,
        ERROR_MESSAGE_SECURE_STORAGE_NOT_INITIALIZED,
        null,
      )
      return
    }

    val method = Method.fromString(call.method)
    if (method == null) {
      result.notImplemented()
      return
    }

    val options = call.argument<Map<String, String>>(PARAM_OPTIONS)
    if (options == null) {
      result.error(
        ERROR_CODE_MISSING_PARAMETER,
        ERROR_MESSAGE_OPTIONS_REQUIRED,
        null,
      )
      return
    }
    secureStorage.setOptions(options)

    runCatching {
      execute(
        call = call,
        method = method,
        secureStorage = secureStorage,
      )
    }.onSuccess {
      result.success(
        it.value,
      )
    }.onFailure { exception ->
      when (exception) {
        is IllegalArgumentException -> {
          result.error(
            ERROR_CODE_MISSING_PARAMETER,
            exception.message ?: ERROR_MESSAGE_INVALID_PARAMETER,
            null,
          )
        }

        is UnsupportedOperationException -> {
          result.notImplemented()
        }

        is NoSuchAlgorithmException,
        is NoSuchPaddingException,
          -> {
          // Cipher algorithm or padding not supported on this device
          result.error(
            ERROR_CODE_CIPHER_ERROR,
            exception.message ?: ERROR_MESSAGE_CIPHER_UNAVAILABLE,
            null,
          )
        }

        is InvalidKeyException,
        is InvalidAlgorithmParameterException,
        is IllegalBlockSizeException,
        is BadPaddingException,
          -> {
          // Cipher encryption/decryption operation failed
          result.error(
            ERROR_CODE_CIPHER_ERROR,
            exception.message ?: ERROR_MESSAGE_CIPHER_OPERATION_FAILED,
            null,
          )
        }

        else -> {
          val resetOnError = secureStorage.getResetOnError()

          if (resetOnError) {
            runCatching {
              secureStorage.deleteAll()
            }.onSuccess {
              result.success(
                when (method) {
                  Method.READ -> null
                  Method.WRITE -> null
                  Method.CONTAINS_KEY -> false
                  Method.DELETE -> null
                  Method.READ_ALL -> emptyMap<String, String>()
                  Method.DELETE_ALL -> null
                }
              )
            }.onFailure { resetException ->
              result.error(
                ERROR_CODE_RESET_ON_ERROR_FAILED,
                "${method.methodName}: ${resetException.message}",
                resetException.stackTraceToString()
              )
            }
          } else {
            result.error(
              ERROR_CODE_ERROR,
              "${method.methodName}: ${exception.message}",
              exception.stackTraceToString()
            )
          }
        }
      }
    }
  }

  private enum class Method(val methodName: String) {
    WRITE("write"),
    READ("read"),
    CONTAINS_KEY("containsKey"),
    DELETE("delete"),
    READ_ALL("readAll"),
    DELETE_ALL("deleteAll");

    companion object {
      fun fromString(methodName: String): Method? {
        return entries.find { it.methodName == methodName }
      }
    }
  }

  private suspend fun execute(
    call: MethodCall,
    method: Method,
    secureStorage: FlutterSecureStorage,
  ): Result = when (method) {
    Method.WRITE -> {
      val key = call.requiredString(PARAM_KEY, "write operation requires key parameter")
      val value = call.requiredString(PARAM_VALUE, "write operation requires value parameter")
      secureStorage.write(key, value)
      Result.Write
    }

    Method.READ -> {
      val key = call.requiredString(PARAM_KEY, "read operation requires key parameter")
      val result = secureStorage.read(key)
      Result.Read(result)
    }

    Method.CONTAINS_KEY -> {
      val key = call.requiredString(PARAM_KEY, "containsKey operation requires key parameter")
      val result = secureStorage.containsKey(key)
      Result.ContainsKey(result)
    }

    Method.DELETE -> {
      val key = call.requiredString(PARAM_KEY, "delete operation requires key parameter")
      secureStorage.delete(key)
      Result.Delete
    }

    Method.READ_ALL -> {
      val result = secureStorage.readAll()
      Result.ReadAll(result)
    }

    Method.DELETE_ALL -> {
      secureStorage.deleteAll()
      Result.DeleteAll
    }
  }

  private fun MethodCall.requiredString(key: String, errorMessage: String): String {
    val value = argument<String>(key)
    if (value == null) {
      throw IllegalArgumentException(errorMessage)
    }
    return value
  }

  private sealed class Result(open val value: Any? = null) {
    object Write : Result()
    data class Read(override val value: String?) : Result(value)
    data class ContainsKey(override val value: Boolean) : Result(value)
    object Delete : Result()
    data class ReadAll(override val value: Map<String, String>) : Result(value)
    object DeleteAll : Result()
  }

  companion object {
    private const val METHOD_CHANNEL_NAME = "plugins.koji-1009.com/flutter_secure_storage"

    private const val PARAM_KEY = "key"
    private const val PARAM_VALUE = "value"
    private const val PARAM_OPTIONS = "options"

    private const val ERROR_CODE_SECURE_STORAGE_NOT_INITIALIZED = "Plugin Not Initialized"
    private const val ERROR_CODE_MISSING_PARAMETER = "Missing Parameter"
    private const val ERROR_CODE_CIPHER_ERROR = "Cipher Error"
    private const val ERROR_CODE_RESET_ON_ERROR_FAILED = "Reset Failed"
    private const val ERROR_CODE_ERROR = "Exception Encountered"

    private const val ERROR_MESSAGE_SECURE_STORAGE_NOT_INITIALIZED =
      "FlutterSecureStorage is not properly initialized"
    private const val ERROR_MESSAGE_OPTIONS_REQUIRED = "Options must be provided"
    private const val ERROR_MESSAGE_INVALID_PARAMETER = "Invalid parameter"
    private const val ERROR_MESSAGE_CIPHER_UNAVAILABLE =
      "Cipher algorithm or padding not supported on this device"
    private const val ERROR_MESSAGE_CIPHER_OPERATION_FAILED =
      "Cipher encryption/decryption operation failed"
  }
}

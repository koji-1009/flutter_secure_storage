part of '../flutter_secure_storage_x_platform_interface.dart';

const MethodChannel _channel =
    MethodChannel('plugins.it_nomads.com/flutter_secure_storage');

const EventChannel _eventChannel =
    EventChannel('plugins.it_nomads.com/flutter_secure_storage/events');

class MethodChannelFlutterSecureStorage extends FlutterSecureStoragePlatform {
  Stream<bool> get onCupertinoProtectedDataAvailabilityChanged => _eventChannel
      .receiveBroadcastStream()
      .where((event) => event is bool)
      .map((event) => event as bool);

  Future<bool?> isCupertinoProtectedDataAvailable() async {
    if (kIsWeb) {
      return null;
    }
    const platform = LocalPlatform();
    if (!(platform.isIOS || platform.isMacOS)) {
      return null;
    }

    final result = await _channel.invokeMethod<bool>(
      'isProtectedDataAvailable',
    );
    return result ?? false;
  }

  @override
  Future<bool> containsKey({
    required String key,
    required Map<String, String> options,
  }) async =>
      (await _channel.invokeMethod<bool>(
        'containsKey',
        {
          'key': key,
          'options': options,
        },
      ))!;

  @override
  Future<void> delete({
    required String key,
    required Map<String, String> options,
  }) =>
      _channel.invokeMethod<void>(
        'delete',
        {
          'key': key,
          'options': options,
        },
      );

  @override
  Future<void> deleteAll({
    required Map<String, String> options,
  }) =>
      _channel.invokeMethod<void>(
        'deleteAll',
        {
          'options': options,
        },
      );

  @override
  Future<String?> read({
    required String key,
    required Map<String, String> options,
  }) =>
      _channel.invokeMethod<String?>(
        'read',
        {
          'key': key,
          'options': options,
        },
      );

  @override
  Future<Map<String, String>> readAll({
    required Map<String, String> options,
  }) async {
    final results = await _channel.invokeMethod<Map>(
      'readAll',
      {
        'options': options,
      },
    );

    return results?.cast<String, String>() ?? <String, String>{};
  }

  @override
  Future<void> write({
    required String key,
    required String value,
    required Map<String, String> options,
  }) =>
      _channel.invokeMethod<void>('write', {
        'key': key,
        'value': value,
        'options': options,
      });
}

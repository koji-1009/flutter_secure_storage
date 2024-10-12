// ignore_for_file: constant_identifier_names

part of '../flutter_secure_storage_x.dart';

class AndroidOptions extends Options {
  const AndroidOptions({
    @Deprecated('Use dataStore instead')
    this.encryptedSharedPreferences = false,
    this.dataStore = false,
    this.resetOnError = false,
    this.sharedPreferencesName,
    this.preferencesKeyPrefix,
  });

  /// Deprecated: Use [dataStore] instead.
  /// EncryptedSharedPreferences is only available on API 23 and greater
  final bool encryptedSharedPreferences;

  /// Use DataStore instead of SharedPreferences. This feature is default in future releases.
  ///
  /// At the first run, data migration from the previous SharedPreferences to DataStore will be done.
  /// If you were using EncryptedSharedPreferences, set [encryptedSharedPreferences] to true.
  final bool dataStore;

  /// When an error is detected, automatically reset all data. This will prevent
  /// fatal errors regarding an unknown key however keep in mind that it will
  /// PERMANENTLY erase the data when an error occurs.
  ///
  /// Defaults to false.
  final bool resetOnError;

  /// The name of the sharedPreference database to use.
  /// You can select your own name if you want. A default name will
  /// be used if nothing is provided here.
  ///
  /// WARNING: If you change this you can't retrieve already saved preferences.
  /// WARNING: If you use DataStore, this will be ignored.
  final String? sharedPreferencesName;

  /// The prefix for a shared preference key. The prefix is used to make sure
  /// the key is unique to your application. If not provided, a default prefix
  /// will be used.
  ///
  /// WARNING: If you change this you can't retrieve already saved preferences.
  final String? preferencesKeyPrefix;

  static const AndroidOptions defaultOptions = AndroidOptions();

  @override
  Map<String, String> toMap() => <String, String>{
        'encryptedSharedPreferences': '$encryptedSharedPreferences',
        'dataStore': '$dataStore',
        'resetOnError': '$resetOnError',
        'sharedPreferencesName': sharedPreferencesName ?? '',
        'preferencesKeyPrefix': preferencesKeyPrefix ?? '',
      };

  AndroidOptions copyWith({
    bool? encryptedSharedPreferences,
    bool? dataStore,
    bool? resetOnError,
    String? preferencesKeyPrefix,
    String? sharedPreferencesName,
  }) =>
      AndroidOptions(
        encryptedSharedPreferences:
            encryptedSharedPreferences ?? this.encryptedSharedPreferences,
        dataStore: dataStore ?? this.dataStore,
        resetOnError: resetOnError ?? this.resetOnError,
        sharedPreferencesName:
            sharedPreferencesName ?? this.sharedPreferencesName,
        preferencesKeyPrefix: preferencesKeyPrefix ?? this.preferencesKeyPrefix,
      );
}

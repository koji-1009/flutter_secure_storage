// ignore_for_file: constant_identifier_names, deprecated_member_use_from_same_package

part of '../flutter_secure_storage_x.dart';

class AndroidOptions extends Options {
  const AndroidOptions({
    this.resetOnError = false,
    this.preferencesKeyPrefix,
  });

  /// When an error is detected, automatically reset all data. This will prevent
  /// fatal errors regarding an unknown key however keep in mind that it will
  /// PERMANENTLY erase the data when an error occurs.
  ///
  /// Defaults to false.
  final bool resetOnError;

  /// The prefix for a shared preference key. The prefix is used to make sure
  /// the key is unique to your application. If not provided, a default prefix
  /// will be used.
  ///
  /// WARNING: If you change this you can't retrieve already saved preferences.
  final String? preferencesKeyPrefix;

  static const AndroidOptions defaultOptions = AndroidOptions();

  @override
  Map<String, String> toMap() => <String, String>{
    'resetOnError': '$resetOnError',
    'preferencesKeyPrefix': preferencesKeyPrefix ?? '',
  };

  AndroidOptions copyWith({
    bool? resetOnError,
    String? preferencesKeyPrefix,
  }) => AndroidOptions(
    resetOnError: resetOnError ?? this.resetOnError,
    preferencesKeyPrefix: preferencesKeyPrefix ?? this.preferencesKeyPrefix,
  );
}
